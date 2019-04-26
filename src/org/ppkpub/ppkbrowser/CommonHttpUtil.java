package org.ppkpub.ppkbrowser;

import java.io.IOException;
import java.util.Map;

import com.squareup.okhttp.*; //okhttp2

public final class CommonHttpUtil {
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
    
    public String getContentFromUrl(String url,String proxy_url) throws IOException {
        return proxy_url==null ?
        		getContentFromUrl(url) : getContentFromUrl( proxy_url +"?url="+ java.net.URLEncoder.encode(url) );
    }

    public String getContentFromUrl(String url) {
        try{
            Request request = new Request.Builder().url(url).build();
            return  client.newCall(request).execute().body().string();
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private String sendPostForm(String url,final Map<String,String> params) throws Exception {
        FormEncodingBuilder builder =  new FormEncodingBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry: params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return client.newCall(request).execute().body().string();
 
    }

    public String sendPostJSON(String url,final String post_json)throws Exception {
        return sendPostContent(url, post_json, "application/json");
    }
    public String sendPostContent(String url,final String post_content,String content_type ) throws Exception {
        String result=null;
        MediaType media_type = MediaType.parse(content_type);

        RequestBody requestBody = RequestBody.create(media_type,post_content);
        Request request = new Request
                .Builder()
                .post(requestBody)//Post请求的参数传递
                .url(url)
                .build();
        try{
            Response response = client.newCall(request).execute();
            result = response.body().string();
            response.body().close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }
}