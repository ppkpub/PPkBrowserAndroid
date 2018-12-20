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
import android.util.Log;

public class Util {
	public static String getPage(String urlString) {
	    return getPage(urlString, 1);
	}

	public static String getPage(String urlString, int retries) {
	    try {
	      return CommonHttpUtil.getSourceFromUrl(urlString);
	    } catch (Exception e) {
	      Log.d("Util","Fetch URL error: "+e.toString());
	    }
	    return null;
	}  

    //��ȡ��ҳ����
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
       * ��ͼƬ�ֽ��������Base64���봦������Data URL
	   * @param  img_type ��ͼƬ�����ͣ��� image/jpeg 
	   * @param  img_bytes �� ͼƬ���ֽ�����
	   * @return Data URL�ַ���
	   */
  public static String imageToBase64DataURL(String img_type, byte[] img_bytes) {
    return "data:"+img_type+";base64,"+Base64.encodeToString( img_bytes, Base64.DEFAULT );
  }
	  
  public static byte[] readInputStream(InputStream inStream) throws Exception{
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    //����һ��Buffer�ַ���
    byte[] buffer = new byte[1024];
    //ÿ�ζ�ȡ���ַ������ȣ����Ϊ-1������ȫ����ȡ���
    int len = 0;
    //ʹ��һ����������buffer������ݶ�ȡ����
    while( (len=inStream.read(buffer)) != -1 ){
        //���������buffer��д�����ݣ��м����������ĸ�λ�ÿ�ʼ����len�����ȡ�ĳ���
        outStream.write(buffer, 0, len);
    }
    //�ر�������
    inStream.close();
    //��outStream�������д���ڴ�
    return outStream.toByteArray();
  }

  /**
   * ƥ���Ƿ�Ϊ����
   * @param str ����Ϊ���ģ�Ҳ������-19162431.1254����ʹ��BigDecimal�Ļ������-1.91624311254E7
   * @return
   * @author yutao
   * @date 2016��11��14������7:41:22
   */
  public static boolean isNumeric(String str) {
    String bigStr;
    try {
        bigStr = new BigDecimal(str).toString();
    } catch (Exception e) {
        return false;//�쳣 ˵�����������֡�
    }
    return true;
  }

  /*
  * Convert byte[] to hex string.��   
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
  
  
  public static String  fetchURI(String uri){
    try{
      String[] uri_chunks=uri.split(":");
      if(uri_chunks.length<2){
    	Log.d("Util","fetchURI() meet invalid uri:"+uri);
        return null;
      }
      
      if(uri_chunks[0].equalsIgnoreCase("ipfs")){
        return getIpfsData(uri_chunks[1]);
      }else if(uri_chunks[0].equalsIgnoreCase("dat")){
        return getDatData(uri);
      }else if(uri_chunks[0].equalsIgnoreCase("btmfs")){
        return getBtmfsData(uri);
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
       Log.d("Util","fetchURI("+uri+") error:"+e.toString());
    }
    return null;
  }
  
  public static String getBtmfsData(String btmfs_uri){
      String tmp_url=Config.BTMFS_PROXY_URL+"?uri=" + java.net.URLEncoder.encode(btmfs_uri);
      Log.d("Util","Using BTMFS Proxy to fetch:"+ tmp_url);
      
      return getPage(tmp_url);
  }
  
  public static String getIpfsData(String ipfs_hash_address){
	  String tmp_url=Config.IPFS_DOWNLOAD_URL+ipfs_hash_address;
	  Log.d("Util","Using IPFS Proxy to fetch:"+ tmp_url);
      
      return getPage(tmp_url);
    /*try{
      IPFS ipfs = new IPFS(Config.IPFS_API_ADDRESS);
      Multihash filePointer = Multihash.fromBase58(ipfs_hash_address);
      byte[] fileContents = ipfs.cat(filePointer);
      return new String(fileContents);
    }catch(Exception e){
      System.out.println("Util.getIpfsData() error:"+e.toString());
      
      String tmp_url=Config.IPFS_DOWNLOAD_URL+ipfs_hash_address;
      System.out.println("Using IPFS Proxy to fetch:"+ tmp_url);
      
      return getPage(tmp_url);
    }
    */
  }
  
  public static String getDatData(String dat_uri){
      String dat_hash=dat_uri.substring("dat://".length());
      
      String tmp_url=null;
      String tmp_page_result=null;
      for(int kk=0;kk<Config.DAT_DOWNLOAD_URL_LIST.length;kk++){
          tmp_url=Config.DAT_DOWNLOAD_URL_LIST[kk]+dat_hash;
          Log.d("Util","Using Dat Proxy to fetch:"+ tmp_url);
          
          tmp_page_result=getPage(tmp_url);
          if(tmp_page_result!=null && tmp_page_result.length()>0){
              return tmp_page_result;
          }
      }
      return null;
  }

  
}
