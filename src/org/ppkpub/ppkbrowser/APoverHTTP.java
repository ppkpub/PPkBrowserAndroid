package org.ppkpub.ppkbrowser;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.InputStream;

import android.util.Log;



public class APoverHTTP {
  public static String fetchInterest(String ap_url, String interest) {
    String str_ap_resp_json=null;

    
    try{
      String ap_fetch_url=ap_url+"?pttp_interest="+URLEncoder.encode(interest, "UTF-8");
      Log.d("APoverHTTP","APoverHTTP.fetchInterest("+ap_url+") ap_fetch_url="+ap_fetch_url);
        
      URL url = new URL(ap_fetch_url);
      HttpURLConnection.setFollowRedirects(true);  
      HttpURLConnection hc = (HttpURLConnection) url.openConnection();  
      hc.setRequestMethod("GET");  
      hc.addRequestProperty("User-Agent", Config.appName+" "+Config.version); 
      hc.setRequestProperty("Connection", "keep-alive");  
      hc.setRequestProperty("Cache-Control", "no-cache");  
      hc.setDoInput(true); //允许输入流，即允许下载
      hc.setDoOutput(true); //允许输出流，即允许上传
      hc.setReadTimeout(5*1000);
      hc.connect();
      
      
      int httpStatusCode = hc.getResponseCode();
      Log.d("APoverHTTP","APoverHTTP.fetchInterest() httpStatusCode="+httpStatusCode);
      if (httpStatusCode == HttpURLConnection.HTTP_OK) {
        //通过输入流获取二进制数据
        InputStream inStream = hc.getInputStream();
        //得到二进制数据，以二进制封装得到数据，具有通用性
        byte[] data = Util.readInputStream(inStream);
        
        if(data!=null){
          str_ap_resp_json=new String(data);
        }
      }

      
    }catch(Exception e){
    Log.d("APoverHTTP-ERROR","APoverHTTP.fetchInterest("+ap_url+") error: "+e.toString());
    }
    
    System.out.println("APoverHTTP.fetchInterest() str_ap_resp_json:"+str_ap_resp_json);
    return str_ap_resp_json;
  }
  
  
}

