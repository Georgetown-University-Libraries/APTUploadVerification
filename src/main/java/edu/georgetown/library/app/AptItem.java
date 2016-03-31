package edu.georgetown.library.app;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class AptItem implements Comparable<AptItem> {
    public enum AptItemStage {Requested,Receive,Fetch,Unpack,Validate,Store,Record,Cleanup,Resolve;}
    public enum AptItemStatus {Pending,Started,Success,Failed,Cancelled;}
    public static SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static SimpleDateFormat printSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static SimpleDateFormat paramSDF=new SimpleDateFormat("yyyy-MM-dd");
    public enum AptItemAction {Ingest, FixityCheck, Restore, Delete, DPN;
        
        public static AptItemAction getAction(String s) {
            if (s.equals("Fixity Check")) {
                return AptItemAction.FixityCheck;
            }
            return AptItemAction.valueOf(s);
        }
    } 

    @SuppressWarnings("unused")
    private String identifier;
    private Date created;
    private Date updated;
    private AptItemStage stage;
    private AptItemAction action;
    private AptItemStatus status;
    private String etag;
    private String name;
    
    public AptItem(JSONObject obj) throws Exception {
        this.identifier = obj.optString("object_identifier","TBD");
        this.name = obj.getString("name");
        this.etag = obj.getString("etag");

        try {
            this.action = AptItemAction.getAction(obj.getString("action"));
            this.stage = AptItemStage.valueOf(obj.getString("stage"));
            this.status = AptItemStatus.valueOf(obj.getString("status"));
            this.created = parserSDF.parse(obj.getString("created_at"));
            this.updated = parserSDF.parse(obj.getString("updated_at"));
        } catch (Exception e) {
            throw new Exception("Cannot read return object " + e.getMessage() + ": " + obj.toString());
        }            
    }
    
    public String toString() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s", 
            this.name,
            this.stage,
            this.action,
            this.status,
            this.etag,
            printSDF.format(this.created),
            printSDF.format(this.updated)
        ); 
    }
    
    public void print() {
        System.out.println(this.toString());
    }
    
    public boolean isIngestAction() {
        return this.action.equals(AptItemAction.Ingest);
    }
    public boolean isSuccessfullyIngested() {
        return this.status.equals(AptItemStatus.Success) && this.action.equals(AptItemAction.Ingest);
    }
    
    public String getEtag() {
        return this.etag;
    }

    public String getName() {
        return this.name;
    }
    
    public Date getCreated() {
        return this.created;
    }

    public Date getUpdated() {
        return this.updated;
    }
    public String getCreatedStr() {
        return printSDF.format(this.created);
    }

    public String getUpdatedStr() {
        return printSDF.format(this.updated);
    }

    public boolean isValidAction() {
        return (this.action == AptItemAction.Delete || this.action == AptItemAction.Ingest);
    }

    @Override
    public int compareTo(AptItem arg0) {
        if (this.name.equals(arg0.name)) {
            return this.created.compareTo(arg0.created);
        } else {
            return this.name.compareTo(arg0.name);
        }
    }
}
