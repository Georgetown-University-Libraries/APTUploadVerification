package edu.georgetown.library.app;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.json.JSONObject;

public class AptObjectEndpoint extends AptEndpoint {

    public AptObjectEndpoint(AptApiSession session) throws URISyntaxException {
        super(session);
    }
    private HashMap<String,AptObject> aptObjects = new HashMap<>(); 
    
    public void addObject(AptObject obj) {
        aptObjects.put(obj.getName(), obj);
    }
    
    public AptObject get(String name) {
        return aptObjects.get(name);
    }

    public AptObject get() {
        if (aptObjects.size() == 1) {
            return aptObjects.values().iterator().next();
        }
        return null;
    }

    @Override
    public String getEndpointName() {
        return "objects";
    }

    @Override
    public void processResult(JSONObject item) throws Exception {
        AptObject aptObject = new AptObject(item);
        if (session.isDebug()) aptObject.print();                        
        if (aptObject.isIngested()) {
            addObject(aptObject);
        }
    }

    public static AptObjectEndpoint createInventoryListing(AptApiSession session) throws URISyntaxException {
        AptObjectEndpoint objEndpoint = new AptObjectEndpoint(session);
        objEndpoint.uriBuilder.addParameter("per_page", IPP);
        return objEndpoint;
    }

    public static AptObjectEndpoint createBagValidator(AptApiSession session, String bagName) throws URISyntaxException {
        AptObjectEndpoint objEndpoint = new AptObjectEndpoint(session);
        objEndpoint.uriBuilder.addParameter("name_exact", bagName);
        return objEndpoint;
    }
    
    public void refineResults() {
        if (session.isDebug()) {
            System.out.println("----------------------------------------");            
        }
        for(AptObject aptObject: aptObjects.values()) {
            aptObject.print();
        }
    }
}
