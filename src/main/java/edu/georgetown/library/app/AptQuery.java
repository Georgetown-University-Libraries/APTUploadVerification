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
        System.out.println(String.format("%s [-apiprop <prop file>] (-listAll|-listIngested|-bag <bagname>)", CMD));
        System.out.println(String.format("%s -h", CMD));
    }
    
    public static CommandLine parseAipCommandLine(String main, String[] args) {
        DefaultParser clParse = new DefaultParser();
        Options opts = new Options();
        OptionGroup optGrp = new OptionGroup();
        optGrp.addOption(new Option("listAll","Returns a tab-separated list of All Inventory Items (including failures)"));
        optGrp.addOption(new Option("listIngested","Returns a tab-separated list of All Successfully Ingested Items"));
        optGrp.addOption(new Option("bag", true, "Returns the Ingest Stats for a Bag Name.  Bag packaging/ETAG info will be returned if successful."));
        optGrp.setRequired(true);
        opts.addOptionGroup(optGrp);
        opts.addOption("h", false, "Help Info");
        opts.addOption("apiprop", true, "API Config File (default: api.prop)");
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

    public static final int queryCommand(CommandLine cmdLine) throws Exception {
        String apiprop = cmdLine.getOptionValue("apiprop", "api.prop");
        try {
            AptApiSession apt = new AptApiSession(apiprop);
            apt.setDebug(cmdLine.hasOption("debug"));
            
            if (cmdLine.hasOption("listAll")) {
                AptItemEndpoint itemEndpoint = AptItemEndpoint.createInventoryListing(apt);
                itemEndpoint.iterateQuery();
                itemEndpoint.refineResults();                
            } else if (cmdLine.hasOption("listIngested")) {
                AptItemEndpoint itemEndpoint = AptItemEndpoint.createSuccessfulInventoryListing(apt);
                itemEndpoint.iterateQuery();
                itemEndpoint.refineResults();                                
            } else if (cmdLine.hasOption("bag")) {
                String bag = cmdLine.getOptionValue("bag");
                AptItemEndpoint itemEndpoint = AptItemEndpoint.createBagValidator(apt, bag);
                itemEndpoint.iterateQuery();
                AptItem item = itemEndpoint.get();
                if (item == null) {
                    fail(String.format("Item (%s) not found", bag));
                } else if (!item.isSuccessfullyIngested()) {
                    fail(String.format("Item (%s) is not ingested: %s", bag, item.toString()));
                } else {
                    System.out.println(String.format("Bag:%s; ETAG:%s; Created: %s; Updated: %s;", item.getName(), item.getEtag(), item.getCreatedStr(), item.getUpdatedStr()));
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
