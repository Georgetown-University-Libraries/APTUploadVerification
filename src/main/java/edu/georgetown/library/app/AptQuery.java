package edu.georgetown.library.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class AptQuery {

    public static final String CMD = "AptQuery";
    public static final void main(String[] args) {
        CommandLine cmdLine = parseAipCommandLine(CMD, args);
        try {
            queryCommand(cmdLine);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void usage() {
        System.out.println("Use Object API (recommended)");
        System.out.println(String.format("  %s -obj [-apiprop <prop file>] (-listAll|-bag <bagname>)", CMD));
        System.out.println("Use Item API");
        System.out.println(String.format("  %s -item [-apiprop <prop file>] [-since YYYY-MM-DD] (-listAll|-listIngested|-bag <bagname>)", CMD));
        System.out.println("Get usage Info");
        System.out.println(String.format("  %s -h", CMD));
    }

    public static CommandLine parseAipCommandLine(String main, String[] args) {
        DefaultParser clParse = new DefaultParser();
        Options opts = new Options();
        OptionGroup optGrp = new OptionGroup();
        optGrp.addOption(new Option("obj","Use Objects Endpoint"));
        optGrp.addOption(new Option("item","Use Items Enpoint"));
        optGrp.setRequired(true);
        opts.addOptionGroup(optGrp);
        optGrp = new OptionGroup();
        optGrp.addOption(new Option("listAll","Returns a tab-separated list of All Inventory Items (including failures)"));
        optGrp.addOption(new Option("listIngested","Returns a tab-separated list of All Successfully Ingested Items"));
        optGrp.addOption(new Option("bag", true, "Returns the Ingest Stats for a Bag Name.  Bag packaging/ETAG info will be returned if successful."));
        optGrp.setRequired(true);
        opts.addOptionGroup(optGrp);
        opts.addOption("h", false, "Help Info");
        opts.addOption("apiprop", true, "API Config File (default: api.prop)");
        opts.addOption("since", true, "Updated since (YYYY-MM-DD)");
        opts.addOption("debug", "Output debug messages");

        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmdLine = clParse.parse(opts, args);
            if (cmdLine.hasOption("h")) {
                usage();
                formatter.printHelp(CMD, opts);
                System.exit(0);
            }
            return cmdLine;
        } catch (ParseException e) {
            usage();
            formatter.printHelp(CMD, opts);
            fail("Invalid Options");
        }
        return null;
    }

    public static void refineOptions(AptItemEndpoint endpoint, CommandLine cmdLine) throws java.text.ParseException {
        if (cmdLine.hasOption("since")) {
            endpoint.setSince(cmdLine.getOptionValue("since"));
        }
    }

    public static final int queryCommand(CommandLine cmdLine) throws Exception {
        String apiprop = cmdLine.getOptionValue("apiprop", "api.prop");
        try {
            AptApiSession apt = new AptApiSession(apiprop);
            apt.setDebug(cmdLine.hasOption("debug"));

            if (cmdLine.hasOption("obj")){
                //APT Partner Tools have replaced the Upload Verification process.
                //Georgetown only uses this code to perform an inventory listing: -listAll
                if (cmdLine.hasOption("listAll")) {
                    AptObjectEndpoint objEndpoint = AptObjectEndpoint.createInventoryListing(apt);
                    objEndpoint.iterateQuery();
                    objEndpoint.refineResults();
                } else if (cmdLine.hasOption("bag")) {
                    String bag = cmdLine.getOptionValue("bag");
                    AptObjectEndpoint objEndpoint = AptObjectEndpoint.createBagValidator(apt, bag);
                    objEndpoint.iterateQuery();
                    AptObject obj = objEndpoint.get();
                    if (obj == null) {
                        fail(String.format("Item (%s) not found", bag));
                    } else if (!obj.isIngested()) {
                        fail(String.format("Item (%s) is not ingested: %s", bag, obj.toString()));
                    } else {
                        System.out.println(obj.getEtag());
                    }
                }
            } else if(cmdLine.hasOption("item")){
              //APT Partner Tools have replaced the Upload Verification process.
              //Georgetown only uses this code to perform an inventory listing: -listAll
                if (cmdLine.hasOption("listAll")) {
                    AptItemEndpoint itemEndpoint = AptItemEndpoint.createInventoryListing(apt);
                    itemEndpoint.iterateQuery();
                    itemEndpoint.refineInventoryResults();
                } else if (cmdLine.hasOption("listIngested")) {
                    AptItemEndpoint itemEndpoint = AptItemEndpoint.createSuccessfulInventoryListing(apt);
                    itemEndpoint.iterateQuery();
                    itemEndpoint.refineInventoryResults();
                } else if (cmdLine.hasOption("bag")) {
                    String bag = cmdLine.getOptionValue("bag");
                    AptItemEndpoint itemEndpoint = AptItemEndpoint.createBagValidator(apt, bag);
                    refineOptions(itemEndpoint, cmdLine);
                    itemEndpoint.iterateQuery();
                    AptItem item = itemEndpoint.get();
                    if (item == null) {
                        fail(String.format("Item (%s) not found", bag));
                    } else if (item.isSuccessfullyIngested()) {
                        System.out.println(item.getEtag());
                    } else if (item.isFailedIngest()) {
                        System.out.println(String.format("INGEST-FAIL:%s",item.getEtag()));
                    } else {
                        fail(String.format("Item (%s) is not ingested: %s", bag, item.toString()));
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static final int FAIL = 100;
    public static final void fail(String message) {
        System.err.println(message);
        System.exit(FAIL);
    }

}
