package org.ppkpub.ppkbrowser;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

//import okhttp3.OkHttpClient; 
//import okhttp3.Request;
//import org.apache.http.util.CharsetUtils;

public final class CommonHttpUtil {
	private DefaultHttpClient httpclient = new DefaultHttpClient();
	
    //协议类型
    private final static String HTTP = "http:";
    private final static String HTTPS = "https:";
    
    //共用的静态对象，在强调安全性时最好自行new一个对象使用
    private static CommonHttpUtil instance = null;

    public static CommonHttpUtil getInstance() {
	    if(instance == null) {
	      instance = new CommonHttpUtil();
	    } 
	    return instance;
	}
    
    public String getContentFromUrl(String url,String proxy_url) throws IOException {
        return proxy_url==null ?
        		getContentFromUrl(url) : getContentFromUrl( proxy_url +"?url="+ java.net.URLEncoder.encode(url) );
    }

    public String getContentFromUrl(String url) throws IOException {
        HttpGet httpget = new HttpGet(url);

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
        System.out.println("----------------------------------------");
        return responseBody;
    }

    /*
    private String sendPostForm(String url,final Map<String,String> params) throws Exception {
        FormBody.Builder builder = new FormBody.Builder(CharsetUtils.get("UTF-8"));
        if (params != null) {
            for (Map.Entry<String, String> entry: params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return client.newCall(request).execute().body().string();
 
    }
 	*/
}