package edu.georgetown.library.app;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;

public class AptItemEndpoint extends AptEndpoint {

    public AptItemEndpoint(AptApiSession session) throws URISyntaxException {
        super(session);
    }
    private HashMap<String,AptItem> aptItems = new HashMap<>(); 
    
    public void addItem(AptItem item) {
        if (item.isValidAction()) {
            AptItem curItem = aptItems.get(item.getName());
            if (curItem == null) {
                aptItems.put(item.getName(), item);
            } else if (item.compareTo(curItem) > 0) {
                aptItems.put(item.getName(), item);
            }
        }
    }

    public AptItem get(String name) {
        return aptItems.get(name);
    }

    public AptItem get() {
        if (aptItems.size() == 1) {
            return aptItems.values().iterator().next();
        }
        return null;
    }

    
    @Override
    public String getEndpointName() {
        return "items";
    }

    @Override
    public void processResult(JSONObject item) throws Exception {
        AptItem aptItem = new AptItem(item);
        addItem(aptItem);
        if (session.isDebug()) aptItem.print();
    }

    public static AptItemEndpoint createInventoryListing(AptApiSession session) throws URISyntaxException {
        AptItemEndpoint itemEndpoint = new AptItemEndpoint(session);
        itemEndpoint.uriBuilder.addParameter("action", AptItem.AptItemAction.Ingest.name().toLowerCase());
        itemEndpoint.uriBuilder.addParameter("per_page", IPP);
        return itemEndpoint;
    }

    public static AptItemEndpoint createSuccessfulInventoryListing(AptApiSession session) throws URISyntaxException {
        AptItemEndpoint itemEndpoint = new AptItemEndpoint(session);
        itemEndpoint.uriBuilder.addParameter("action", AptItem.AptItemAction.Ingest.name().toLowerCase());
        itemEndpoint.uriBuilder.addParameter("status", AptItem.AptItemStatus.Success.name().toLowerCase());
        itemEndpoint.uriBuilder.addParameter("per_page", IPP);
        return itemEndpoint;
    }

    public static AptItemEndpoint createBagValidator(AptApiSession session, String bagName) throws URISyntaxException {
        AptItemEndpoint itemEndpoint = new AptItemEndpoint(session);
        //itemEndpoint.uriBuilder.addParameter("action", AptItem.AptItemAction.Ingest.name().toLowerCase());
        itemEndpoint.uriBuilder.addParameter("name_exact", bagName);
        return itemEndpoint;
    }

    public void setSince(String since) throws ParseException {
        Date date = AptItem.paramSDF.parse(since);
        this.uriBuilder.addParameter("updated_since", AptItem.parserSDF.format(date));
    }
    
    public void refineResults() {
        if (session.isDebug()) {
            System.out.println("----------------------------------------");            
        }
        for(AptItem aptItem: aptItems.values()) {
            if (aptItem.isIngestAction()) {
                aptItem.print();                    
            }
        }
    }
}
