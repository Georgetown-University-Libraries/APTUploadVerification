package edu.georgetown.library.app;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AptEndpoint {
    AptApiSession session;
    URIBuilder uriBuilder;
    
    public static final String IPP = "100";
    public AptEndpoint(AptApiSession session) throws URISyntaxException {
        this.session = session;
        uriBuilder = getEndpointUriBuilder();
    }

    public abstract String getEndpointName();
    public abstract void processResult(JSONObject item) throws Exception;
    
    public URIBuilder getEndpointUriBuilder() throws URISyntaxException {
        return new URIBuilder(session.getEndpointUrl(getEndpointName()));
    }
    
    public JSONObject runQuery() throws IOException, URISyntaxException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(uriBuilder.build());
            session.addHeaders(httpget);

            if (session.isDebug()) System.out.println("Executing request " + httpget.getRequestLine());

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
             
            if (session.isDebug()) {
                System.out.println("----------------------------------------");
                System.out.println(responseBody);
                System.out.println("----------------------------------------");
                System.out.println(jobj.get("count"));
            }
            return jobj;
        } finally {
            httpclient.close();
        }
    }

    public void iterateQuery() throws IOException, URISyntaxException {
        JSONObject jobj = runQuery();
        JSONArray jarr = jobj.getJSONArray("results");
        for(int i=0; i<jarr.length(); i++) {
            JSONObject item = jarr.getJSONObject(i);
            try {
                processResult(item);
            } catch (Exception e) {
                e.printStackTrace();
            }               
        }                
        if (!jobj.isNull("next")) {
            uriBuilder = new URIBuilder(jobj.getString("next"));
            iterateQuery();
        }
    }
}