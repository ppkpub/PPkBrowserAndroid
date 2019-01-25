package org.ppkpub.ppkbrowser;

import java.io.IOException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

//import okhttp3.OkHttpClient; 
//import okhttp3.Request;
//import org.apache.http.util.CharsetUtils;

public final class CommonHttpUtilOkHttp {
	private OkHttpClient client = new OkHttpClient();
	
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

    public String getContentFromUrl(String url) throws IOException {
    	Request request = new Request.Builder().url(url).build();
        return  client.newCall(request).execute().body().string();
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