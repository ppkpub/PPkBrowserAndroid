package org.ppkpub.ppkbrowser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;

import android.util.Base64;

public class Util {
	static TestLogger logger = new TestLogger(Util.class);

	public static String getPage(String urlString) {
	    return getPage(urlString, 1);
    }

    public static String getPage(String urlString, int retries) {
	    try {
	      logger.info("Getting URL: "+urlString);
	      doTrustCertificates();
	      URL url = new URL(urlString);
	      HttpURLConnection connection = null;
	      connection = (HttpURLConnection)url.openConnection();
	      connection.setUseCaches(false);
	      connection.addRequestProperty("User-Agent", Config.appName+" "+Config.version); 
	      connection.setRequestMethod("GET");
	      connection.setDoOutput(true);
	      connection.setReadTimeout(10000);
	      connection.connect();

	      BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	      StringBuilder sb = new StringBuilder();
	      String line;
	      
	      while ((line = rd.readLine()) != null)
	      {
	        sb.append(line + '\n');
	      }
	      //System.out.println (sb.toString());

	      return sb.toString();
	    } catch (Exception e) {
	      logger.error("Fetch URL error: "+e.toString());
	    }
	    return "";
    }  

    public static void doTrustCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
              public java.security.cert.X509Certificate[] getAcceptedIssuers()
              {
                return null;
              }
              public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
              {
              }
              public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
              {
              }
            }
        };
        try 
        {
          SSLContext sc = SSLContext.getInstance("SSL");
          sc.init(null, trustAllCerts, new java.security.SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } 
        catch (Exception e) 
        {
          System.out.println(e);
        }
    }  
    
    //获取网页标题
  	public static String getPageTitle(String urladdress,String htmlcontent)
  	{
      String temp="";
      try{
        String regex="<[Tt][Ii][Tt][Ll][Ee]>([^</[Tt][Ii][Tt][Ll][Ee]>]*)";
        Pattern pat = Pattern.compile(regex);
        Matcher match = pat.matcher(htmlcontent);
        while (match.find()) {
          int start = match.start();
          int end = match.end();
          temp = htmlcontent.substring(start+7, end);
        }
        
      }catch(Exception ex){
      }
      return temp.length()>0 ? temp:urladdress;
  	}
  	
	public static String  fetchURI(String uri){
	    try{
	      String[] uri_chunks=uri.split(":");
	      if(uri_chunks.length<2){
	        logger.error("Util.fetchURI() meet invalid uri:"+uri);
	        return null;
	      }
	      
	      if(uri_chunks[0].equalsIgnoreCase("ipfs")){
	        //return getIpfsData(uri_chunks[1]);
	    	  return "ipfs not supported";
	      }else if(uri_chunks[0].equalsIgnoreCase("ppk")){
	        JSONObject obj_ap_resp=PPkURI.fetchPPkURI(uri);
	        if(obj_ap_resp==null)
	          return null;
	        
	        return obj_ap_resp.optString(Config.JSON_KEY_ORIGINAL_RESP,"ERROR:Invalid PTTP data!");
	      }else if(uri_chunks[0].equalsIgnoreCase("data")){
	        int from=uri_chunks[1].indexOf(",");
	        if(from>=0){
	          return uri_chunks[1].substring(from+1,uri_chunks[1].length());
	        } else
	          return uri_chunks[1];
	      }else{
	        return getPage(uri);
	      }
	    }catch(Exception e){
	      logger.error("Util.fetchURI("+uri+") error:"+e.toString());
	    }
	    return null;
    }
	

  public static byte[] toByteArray(List<Byte> in) {
    final int n = in.size();
    byte ret[] = new byte[n];
    for (int i = 0; i < n; i++) {
      ret[i] = in.get(i);
    }
    return ret;
  }
  
  public static List<Byte> toByteArrayList(byte[] in) {
    List<Byte> arrayList = new ArrayList<Byte>();

    for (byte b : in) {
      arrayList.add(b);
    }
    return arrayList;
  }  
	  
  /**
       * 对图片字节数组进行Base64编码处理生成Data URL
	   * @param  img_type ：图片的类型，如 image/jpeg 
	   * @param  img_bytes ： 图片的字节数组
	   * @return Data URL字符串
	   */
  public static String imageToBase64DataURL(String img_type, byte[] img_bytes) {
    return "data:"+img_type+";base64,"+Base64.encodeToString( img_bytes, Base64.DEFAULT );
  }
	  
  public static byte[] readInputStream(InputStream inStream) throws Exception{
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    //创建一个Buffer字符串
    byte[] buffer = new byte[1024];
    //每次读取的字符串长度，如果为-1，代表全部读取完毕
    int len = 0;
    //使用一个输入流从buffer里把数据读取出来
    while( (len=inStream.read(buffer)) != -1 ){
        //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
        outStream.write(buffer, 0, len);
    }
    //关闭输入流
    inStream.close();
    //把outStream里的数据写入内存
    return outStream.toByteArray();
  }

  /**
   * 匹配是否为数字
   * @param str 可能为中文，也可能是-19162431.1254，不使用BigDecimal的话，变成-1.91624311254E7
   * @return
   * @author yutao
   * @date 2016年11月14日下午7:41:22
   */
  public static boolean isNumeric(String str) {
    String bigStr;
    try {
        bigStr = new BigDecimal(str).toString();
    } catch (Exception e) {
        return false;//异常 说明包含非数字。
    }
    return true;
  }

  /*
  * Convert byte[] to hex string.。   
  * @param src byte[] data   
  * @return hex string   
  */      
 public static String bytesToHexString(byte[] src){   
     StringBuilder stringBuilder = new StringBuilder("");   
     if (src == null || src.length <= 0) {   
         return null;   
     }   
     for (int i = 0; i < src.length; i++) {   
         int v = src[i] & 0xFF;   
         String hv = Integer.toHexString(v);   
         if (hv.length() < 2) {   
             stringBuilder.append(0);   
         }   
         stringBuilder.append(hv);   
     }   
     return stringBuilder.toString();   
 }   
 
  /**  
   * Convert hex string to byte[]  
   * @param hexString the hex string  
   * @return byte[]  
   */  
  public static byte[] hexStringToBytes(String hexString) {   
      if (hexString == null || hexString.equals("")) {   
          return null;   
      }   
      hexString = hexString.toUpperCase();   
      int length = hexString.length() / 2;   
      char[] hexChars = hexString.toCharArray();   
      byte[] d = new byte[length];   
      for (int i = 0; i < length; i++) {   
          int pos = i * 2;   
          d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
      }   
      return d;   
  }   

  /**  
   * Convert char to byte  
   * @param c char  
   * @return byte  
   */  
  private static byte charToByte(char c) {   
      return (byte) "0123456789ABCDEF".indexOf(c);   
  }  
}
