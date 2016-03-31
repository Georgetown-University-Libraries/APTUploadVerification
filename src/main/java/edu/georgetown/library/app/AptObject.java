package edu.georgetown.library.app;

import org.json.JSONObject;

public class AptObject {
    public enum AptObjectState {A, D;}
    public enum AptObjectAccess {consortia, restricted, institution;}

    private String bagName;
    private String identifier;
    private String etag;
    private AptObjectState state;
    private AptObjectAccess access;
    public AptObject(JSONObject obj) throws Exception{
        this.identifier = obj.getString("identifier");
        this.bagName = obj.getString("bag_name");
        this.etag = obj.getString("etag");
        try {
            this.access = AptObjectAccess.valueOf(obj.getString("access"));
            this.state = AptObjectState.valueOf(obj.getString("state"));
        } catch (Exception e) {
            throw new Exception("Cannot read return object " + obj.toString());
        }
    }

    public String getName() {
        return bagName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEtag() {
        return etag;
    }

    
    public boolean isIngested() {
        return state == AptObjectState.A;
    }
    
    public String toString() {
        return String.format("%s\t%s\t%s\t%s\t%s", 
            this.identifier, 
            this.bagName, 
            this.state,
            this.access,
            this.etag
        );            
    }

    public void print() {
        System.out.println(this.toString());            
    }
}
