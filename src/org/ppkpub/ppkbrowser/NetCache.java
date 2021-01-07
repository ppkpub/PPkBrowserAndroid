package org.ppkpub.ppkbrowser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.util.Log;
import android.widget.Toast;

//网络内容本地缓存管理
public class NetCache {
  public static boolean mClearNetCacheWhenStart = false;
  
  private static String mNetCacheDirPrefix=null; 
  private static PPkActivity mMainActivity=null;
  

  public static void init(PPkActivity main_activity ) {
	  mMainActivity=main_activity;
	  
	  mNetCacheDirPrefix=Config.cacheDirPrefix+"netcache/";
	  
	  if(mClearNetCacheWhenStart) {
		  Toast.makeText(mMainActivity.getWindow().getContext(), "正在自动清除网络缓存..." , 0).show(); 
		  clearNetCache();
	  }
  }
  
  //获取缓存路径
  public static String  getNetCachePath(   ) {
  	return mNetCacheDirPrefix;
  }
  
  //写入缓存，待完善
  //改用org.apache.commons.io.FileUtils
  public static boolean saveNetCache( String uri, String data  )
  {
    String cache_filename = getNetCacheFilename(uri);
    
    if( cache_filename == null ){ 
        return false;
    }
    
    Log.d("NetCache","Try to save cache_file :"+cache_filename);
    
    exportTextToFile(data,cache_filename);
    
    return true;
  }
  
  //读取缓存
  public static String readNetCache( String uri )
  {
    String cache_filename = getNetCacheFilename(uri);
    Log.d("NetCache","Util.readNetCache() cache_filename = "+cache_filename);
    String str_cached_data = readTextFile(cache_filename);
    
    /*
    if(str_cached_data==null){   
        System.out.println("Util.readNetCache() failed to read cache for "+uri);
    }else{
        System.out.println("Util.readNetCache() matched cache for "+uri);
    }
    */
    
    return str_cached_data;
  }
  
  //删除缓存
  public static void deleteNetCache( String uri )
  {
    String cache_filename = getNetCacheFilename(uri);
    System.out.println("Util.deleteNetCache() cache_filename = "+cache_filename);

    Util.deleteDir(cache_filename,true);
  }
  
  public static String getNetCacheFilename( String uri )
  {
    if(uri==null)
        return null;

    String format_safe_filename = java.net.URLEncoder.encode(uri.replaceFirst(":","/"))
                                 .replace("%2F","/").replace("*","#").replace("..","__");
    return  mNetCacheDirPrefix + format_safe_filename;
  }
  
  /*
  public static boolean exportTextToFile(String text, String fileName) {
    try {
      String file_path = fileName.substring(0,fileName.lastIndexOf('/'));
      File fp = new File(file_path);    
      // 创建目录    
      if (!fp.exists()) {    
          fp.mkdirs();// 目录不存在的情况下，创建目录。    
      } 
      
      FileWriter fw = new FileWriter(fileName);  
      fw.write(text,0,text.length());  
      fw.flush();  
      return true;
    } catch (Exception e) {
      System.out.println(e.toString());
      return false;
    }
  }
  */
  
  public static boolean exportTextToFile(String text, String fileName) {
    try {
      String file_path = fileName.substring(0,fileName.lastIndexOf('/'));
      File fp = new File(file_path);    
      // 创建目录    
      if (!fp.exists()) {    
          fp.mkdirs();// 目录不存在的情况下，创建目录。    
      } 
      File file1 = new File(fileName);
	  Writer writer = new BufferedWriter(
			new OutputStreamWriter(
					new FileOutputStream(file1), Config.PPK_TEXT_CHARSET));
	  writer.write(text);
	  writer.flush();
	  writer.close();

      return true;
    } catch (Exception e) {
      System.out.println(e.toString());
      return false;
    }
  }
  
  public static String readTextFile(String fileName){  
    return readTextFile(fileName,Config.PPK_TEXT_CHARSET);
  } 
    
  public static String readTextFile(String fileName,String encode){  
    try {
        InputStreamReader read = new InputStreamReader (new FileInputStream(fileName),encode);
        BufferedReader reader=new BufferedReader(read);
        String str="";
        String line;
        while ((line = reader.readLine()) != null) {
            str+=line;
        }
        reader.close();
        read.close();
        return str;
    }catch(Exception e){
      //logger.error( "readTextFile() "+e.toString());
      return null;
    }
  }
  
  //清除缓存
  public static boolean clearNetCache(  )
  {
	  Util.deleteDir(mNetCacheDirPrefix,false);
	  return true;
  }
  
}