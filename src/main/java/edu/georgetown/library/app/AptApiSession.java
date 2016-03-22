package edu.georgetown.library.app;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.http.client.methods.HttpGet;

public class AptApiSession {
    String root;
    String user;
    String apiKey;
    boolean debug;

    public AptApiSession(String apiKeyProp) throws Exception {
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

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public boolean isDebug() {
        return this.debug;
    }
    
    public void addHeaders(HttpGet httpget) {
        httpget.addHeader("Content-Type", "application/json");
        httpget.addHeader("Accept", "application/json");
        httpget.addHeader("X-Fluctus-API-User", user);
        httpget.addHeader("X-Fluctus-API-Key", apiKey);        
    }
    
    public String getEndpointUrl(String endpoint) {
        return String.format("%s/%s", root, endpoint);
    }
    
}
