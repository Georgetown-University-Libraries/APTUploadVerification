package edu.georgetown.library.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import sun.misc.IOUtils;

public class AptQuery {
    
    
    
    public static void main(String[] args) {
        System.out.println(AptItemAction.getAction("Ingest"));
        System.out.println(AptItemAction.getAction("Fixity Check"));
        
        
        String prop = args.length > 0 ? args[0] : "api.prop";
        try {
            AptQuery apt = new AptQuery(prop);
            apt.doQueryItems();
            apt.doQueryObjects();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String root;
    String user;
    String apiKey;
    
    public AptQuery(String apiKeyProp) throws Exception {
        File f = new File(apiKeyProp);
        if (!f.exists()) {
            throw new Exception("Property File Not Found");
        }
        Properties prop = new Properties();
        prop.load(new FileInputStream(f));
        user = prop.getProperty("user","");
        if (user.isEmpty()) {
            throw new Exception("API User is undefined");            
        }
        root = prop.getProperty("url","");
        if (root.isEmpty()) {
            throw new Exception("API URL is undefined");            
        }
        apiKey = prop.getProperty("key","");
        if (apiKey.isEmpty()) {
            throw new Exception("API Key is undefined");            
        }

    }
    
    public JSONArray doQuery(URIBuilder uribuild) throws IOException, URISyntaxException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
         try {
             HttpGet httpget = new HttpGet(uribuild.build());
             
             httpget.addHeader("Content-Type", "application/json");
             httpget.addHeader("Accept", "application/json");
             httpget.addHeader("X-Fluctus-API-User", user);
             httpget.addHeader("X-Fluctus-API-Key", apiKey);

             System.out.println("Executing request " + httpget.getRequestLine());

             // Create a custom response handler
             ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                 @Override
                 public String handleResponse(
                         final HttpResponse response) throws ClientProtocolException, IOException {
                     int status = response.getStatusLine().getStatusCode();
                     if (status >= 200 && status < 300) {
                         HttpEntity entity = response.getEntity();
                         return entity != null ? EntityUtils.toString(entity) : null;
                     } else {
                         throw new ClientProtocolException("Unexpected response status: " + status);
                     }
                 }

             };
             String responseBody = httpclient.execute(httpget, responseHandler);
             
             JSONObject jobj = new JSONObject(responseBody);
             
             System.out.println("----------------------------------------");
             System.out.println(jobj.get("count"));
             JSONArray jarr = jobj.getJSONArray("results");
             return jarr;
         } finally {
             httpclient.close();
         }
     }

    
    
    public void doQueryItems() throws IOException, URISyntaxException {
       CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String path = root+"/items/";
            
            URIBuilder uribuild = new URIBuilder(path);
            //uribuild.addParameter("stage", "resolve");
            //uribuild.addParameter("stage", "cleanup");
            uribuild.addParameter("action", "Ingest");
            uribuild.addParameter("per_page", "200");
            //uribuild.addParameter("name_exact", "srcorg.10822_761808.tar");
            
            JSONArray jarr = doQuery(uribuild);
            for(int i=0; i<jarr.length(); i++) {
                JSONObject item = jarr.getJSONObject(i);
                try {
                    AptItem aptItem = new AptItem(item);
                    addItem(aptItem);
                    aptItem.print();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }               
            }
            System.out.println("----------------------------------------");
            for(AptItem aptItem: aptItems.values()) {
                if (aptItem.action == AptItemAction.Ingest) {
                    aptItem.print();                    
                }
            }
        } finally {
            httpclient.close();
        }
    }

    public void doQueryObjects() throws IOException, URISyntaxException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String path = root+"/objects/";
            
            URIBuilder uribuild = new URIBuilder(path);
            //uribuild.addParameter("stage", "resolve");
            //uribuild.addParameter("stage", "cleanup");
            //uribuild.addParameter("action", "ingest");
            //uribuild.addParameter("name_exact", "srcorg.10822_761721.tar");
            
            JSONArray jarr = doQuery(uribuild);
            System.out.println(jarr.length());
            for(int i=0; i<jarr.length(); i++) {
                JSONObject item = jarr.getJSONObject(i);
                try {
                    AptObject aptObj = new AptObject(item);
                    if (aptObj.state == AptObjectState.A) {
                        aptObj.print();                        
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            httpclient.close();
        }
    }
    
    public enum AptObjectState {A, D;}
    public enum AptObjectAccess {consortia, restricted, institution;}
    
    public class AptObject {
        public String bagName;
        public String etag = "NotYetProvided";
        public AptObjectState state;
        public AptObjectAccess access;
        public AptObject(JSONObject obj) throws Exception{
            this.bagName = obj.getString("bag_name");
            try {
                this.access = AptObjectAccess.valueOf(obj.getString("access"));
                this.state = AptObjectState.valueOf(obj.getString("state"));
            } catch (Exception e) {
                throw new Exception("Cannot read return object " + obj.toString());
            }
        }
        public void print() {
            System.out.println(String.format("%40s %10s %10s %10s", 
                this.bagName, 
                this.state,
                this.access,
                this.etag
            ));            
        }
    }
    
    public enum AptItemAction {Ingest, FixityCheck, Restore, Delete, DPN;
        
        public static AptItemAction getAction(String s) {
            if (s.equals("Fixity Check")) {
                return AptItemAction.FixityCheck;
            }
            return AptItemAction.valueOf(s);
        }
    } 
    
    public enum AptItemStage {Requested,Receive,Fetch,Unpack,Validate,Store,Record,Cleanup,Resolve;}
    public enum AptItemStatus {Pending,Started,Success,Failed,Cancelled;}
    public static SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static SimpleDateFormat printSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public class AptItem {
        String identifier;
        Date created;
        Date updated;
        AptItemStage stage;
        AptItemAction action;
        AptItemStatus status;
        String etag;
        String name;
        public AptItem(JSONObject obj) throws Exception {
            this.identifier = obj.getString("object_identifier");
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
        
        public void print() {
            System.out.println(String.format("%60s %10s %10s %10s %25s %15s %15s", 
                this.name,
                this.stage,
                this.action,
                this.status,
                this.etag,
                printSDF.format(this.created),
                printSDF.format(this.updated)
            ));
        }
    }
    
    public HashMap<String,AptItem> aptItems = new HashMap<>(); 
    
    public void addItem(AptItem item) {
        if (item.action == AptItemAction.Delete || item.action == AptItemAction.Ingest) {
            AptItem curItem = aptItems.get(item.name);
            if (curItem == null) {
                aptItems.put(item.name, item);
            } else if (item.created.compareTo(curItem.created) > 0) {
                aptItems.put(item.name, item);
            }
        }
    }
}

/*
{
    "date":"2016-03-03T17:43:47.637Z",
    "note":"Deleted generic file 'georgetown.edu/terry.item1/data/10822-549388/Checksum Tests/Thumbs.db' from 'https://s3.amazonaws.com/aptrust.test.preservation/2d6db443-8f32-4e9d-b976-c73646c7cec4' at 2016-03-03T17:50:46Z at the request of twb27@georgetown.edu",
    "object_identifier":"georgetown.edu/terry.item1",
    "created_at":"2016-03-03T17:43:47.640Z",
    "bucket":"aptrust.receiving.test.georgetown.edu",
    "institution":"georgetown.edu",
    "updated_at":"2016-03-11T14:44:38.432Z",
    "stage":"Resolve",
    "name":"terry.item1.tar",
    "action":"Delete",
    "bag_date":"2016-02-20T00:08:06.000Z",
    "reviewed":true,
    "needs_admin_review":false,
    "etag":"f153d02f118bc08d0f64dbdc5e7fe5d0",
    "id":136352,
    "user":"system@aptrust.org",
    "generic_file_identifier":"georgetown.edu/terry.item1/data/10822-549388/Checksum Tests/Thumbs.db",
    "outcome":"Not started",
    "retry":true,
    "status":"Success"
}
*/