package org.ppkpub.ppkbrowser;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.json.JSONObject;

import com.squareup.okhttp.Response;

import java.io.InputStream;

import android.util.Log;



public class APoverHTTP {
	/*
  public static String fetchInterest(String ap_url, String interest) {
    String str_ap_resp_json=null;

    try{
      //检查输入参数是否有效
      String req_uri = interest;
      if(req_uri.startsWith("{")){ //兼容处理
          JSONObject tmp_obj = new JSONObject(interest);
          req_uri = tmp_obj.optString(Config.PTTP_KEY_URI,"").trim();
      }
      
      if(req_uri.length()==0){
         Log.d("APoverHTTP","fetchInterest() req_uri is empty");
         return null;  
      }

      String ap_fetch_url=ap_url+"?"+Config.PTTP_INTEREST+"="+URLEncoder.encode(interest, "UTF-8");
      Log.d("APoverHTTP","fetchInterest("+ap_url+") ap_fetch_url="+ap_fetch_url);

      Response response = CommonHttpUtil.getInstance().getFullResponseFromUrl(ap_fetch_url);
      if(response==null){
    	  Log.d("APoverHTTP","fetchInterest() response is null");
          return null;
      }
      
      int http_status_code = Integer.parseInt(response.header("status"));
      Log.d("APoverHTTP","fetchInterest() http_status_code="+http_status_code);
      
      if(!response.isSuccessful()) {
          return null;
      }
      
      if( http_status_code == 200 ) 
      {
        String content = response.body().string();
        String content_type = response.header("content_type");
        Log.d("APoverHTTP","fetchInterest() content_type="+content_type);
        if( content_type !=null 
            && content_type.startsWith("text") 
            && content.startsWith("{") )
        { //是JSON格式的内容
          content_type="text/json";
          str_ap_resp_json=content;
        }
        else
        {
          
          
          str_ap_resp_json = genRespJSON(
        		req_uri,
                Config.PTTP_STATUS_CODE_OK,
                "http status_code ok",
                content_type,
                content
          );
        }
      }else{
        String status_detail="Invalid AP status_code : "+http_status_code;
        str_ap_resp_json = genRespJSON(
        		req_uri,
                Config.PTTP_STATUS_CODE_LOCAL_ERROR,
                status_detail,
                "text/html",
                status_detail
          );
      }

      
    }catch(Exception e){
    	Log.d("APoverHTTP-ERROR","fetchInterest("+ap_url+") error: "+e.toString());
    }
    
    //System.out.println("fetchInterest() str_ap_resp_json:"+str_ap_resp_json);
    return str_ap_resp_json;
  }
  
  //本地模拟生成AP应答数据块
  protected static String genRespJSON(
        String uri,
        int status_code,
        String status_detail,
        String content_type,
        String content
  ){
    try{
        JSONObject obj_chunk_metainfo=new JSONObject();
        obj_chunk_metainfo.put(Config.PTTP_KEY_CACHE_AS_LATEST,Config.DEFAULT_CACHE_AS_LATEST);
        obj_chunk_metainfo.put(Config.PTTP_KEY_STATUS_CODE,status_code);
        obj_chunk_metainfo.put(Config.PTTP_KEY_STATUS_DETAIL,status_detail);
        obj_chunk_metainfo.put(Config.PTTP_KEY_CONTENT_TYPE, content_type );
        obj_chunk_metainfo.put(Config.PTTP_KEY_CONTENT_LENGTH, content.length()  );
        //obj_chunk_metainfo.put("chunk_index", 0 );
        //obj_chunk_metainfo.put("chunk_count", 1 );

        JSONObject new_ap_resp=new JSONObject();
        new_ap_resp.put(Config.PTTP_KEY_VER,Config.PTTP_PROTOCOL_VER);
        new_ap_resp.put(Config.PTTP_KEY_SPEC,Config.PTTP_KEY_SPEC_NONE);
        new_ap_resp.put(Config.PTTP_KEY_URI,uri);
        new_ap_resp.put(Config.PTTP_KEY_METAINFO,obj_chunk_metainfo.toString());
        new_ap_resp.put(Config.PTTP_KEY_CONTENT,content);
        new_ap_resp.put(Config.PTTP_KEY_SIGNATURE,"");

        return  new_ap_resp.toString();
     }catch(Exception e){
    	Log.d("APoverHTTP-ERROR","APoverHTTP.genRespJSON() error: "+e.toString());
        return null;
     }
  }
  
  */
	
  public static String fetchInterest(String ap_url, String interest) {
    String str_ap_resp_json=null;

    
    try{
      String ap_fetch_url=ap_url+"?"+Config.PTTP_INTEREST+"="+URLEncoder.encode(interest, "UTF-8");
      Log.d("APoverHTTP","fetchInterest("+ap_url+") ap_fetch_url="+ap_fetch_url);
        
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
      
      
      int http_status_code = hc.getResponseCode();
      Log.d("APoverHTTP","fetchInterest() http_status_code="+http_status_code);
      if (http_status_code == HttpURLConnection.HTTP_OK) {
        //通过输入流获取二进制数据
        InputStream inStream = hc.getInputStream();
        //得到二进制数据，以二进制封装得到数据，具有通用性
        byte[] data = Util.readInputStream(inStream);
        
        if(data!=null){
          str_ap_resp_json=new String(data);
        }
      }

      
    }catch(Exception e){
    	Log.d("APoverHTTP-ERROR","fetchInterest("+ap_url+") error: "+e.toString());
    }
    
    //System.out.println("fetchInterest() str_ap_resp_json:"+str_ap_resp_json);
    return str_ap_resp_json;
  }
  
  
}

