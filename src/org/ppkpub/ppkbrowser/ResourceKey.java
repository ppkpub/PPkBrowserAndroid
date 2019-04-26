package org.ppkpub.ppkbrowser;

import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

public class ResourceKey  {
  final  public  static String mResKeyFileName = "resources_key";

  private static PPkActivity mMainActivity=null;
  
  public static final String PUBLIC_KEY  = "pub";  
  public static final String PRIVATE_KEY = "prv";  
  public static final String ALGO_TYPE   = "type";  
  
  public static final String DEFAULT_ALGO_TYPE_RSA = "RSA";  
  
  private static JSONObject mObjResKeySet;
  
  public static void init(PPkActivity main_activity ) {
      //Locale.setDefault(new Locale("en", "US"));
	  mMainActivity=main_activity;
	  
	  try {
	    mObjResKeySet=new JSONObject();
	    String str_json=mMainActivity.getPrivateData(mResKeyFileName);
	    mObjResKeySet=new JSONObject(str_json); 
	  } catch (Exception e) {
        Log.d("ResourceKey","Init failed: "+e.toString());
        //e.printStackTrace();
      }	
  }
  
  public static String getBackupData(  ) {
	  return mMainActivity.getPrivateData(mResKeyFileName);
  }
  
  public static boolean restoreBackupData( String data  ) {
	  try{
		  JSONObject tmpObjSet=new JSONObject(data); 
		  mObjResKeySet=tmpObjSet;
    	  saveSet();
    	  return true;
	  } catch (Exception e) {
        Log.d("ResourceKey","RestoreBackupData failed: "+e.toString());
        //e.printStackTrace();
      }
	  
	  return false;
  }
  
  public static boolean saveSet() {
	  try{
		String wallet_json=mObjResKeySet.toString();
		mMainActivity.putPrivateData(mResKeyFileName,wallet_json);
		return true;
	  } catch (Exception e) {
        Log.d("ResourceKey","Save failed: "+e.toString());
        Toast.makeText( mMainActivity.getWindow().getContext(),"ResourceKey save failed:"+e.toString(), Toast.LENGTH_SHORT).show();
        //e.printStackTrace();
        return false;
      }
	  
  }
  
  public static boolean saveKey(String res_uri,String prv_key,String pub_key,String algo_type) {
	  try {
		  JSONObject tmp_obj=new JSONObject();
		  tmp_obj.put(PRIVATE_KEY , prv_key);
		  tmp_obj.put(PUBLIC_KEY, pub_key);
		  tmp_obj.put(ALGO_TYPE, algo_type);
		  
		  
		  return saveKey(res_uri,tmp_obj);
	  } catch (Exception e) {
		  return false;
	  }
  }
  
  public static boolean saveKey(String res_uri,JSONObject obj_key) {
	  try {
		  if(PPkURI.isValidPPkURI(res_uri))
				res_uri=PPkURI.getRealPPkURI(res_uri);
		  
		  mObjResKeySet.put(res_uri,obj_key);
	  
		  return saveSet();
	  } catch (Exception e) {
		  return false;
	  }

  }
  
  public static JSONObject getKey(String res_uri,boolean auto_gene_new) {
	  try {
		if(PPkURI.isValidPPkURI(res_uri))
			res_uri=PPkURI.getRealPPkURI(res_uri);
		  
		JSONObject tmp_obj=mObjResKeySet.optJSONObject(res_uri);
		
		if( auto_gene_new && tmp_obj==null ) {
			//Toast.makeText( mMainActivity.getWindow().getContext(),"new key for :"+res_uri, Toast.LENGTH_SHORT).show();
			
			
			//系统为用户自动产生一对RSA公私钥供选用
			JSONObject keyMap = RSACoder.initKey();  

	        String publicKey = RSACoder.getPublicKey(keyMap);  
	        String privateKey = RSACoder.getPrivateKey(keyMap); 
	        
	        tmp_obj=new JSONObject();
			tmp_obj.put(PRIVATE_KEY , privateKey);
			tmp_obj.put(PUBLIC_KEY, publicKey);
			tmp_obj.put(ALGO_TYPE, DEFAULT_ALGO_TYPE_RSA);
			  
	        //if(!saveKey(res_uri,tmp_obj)) { //待对话框里确认后再保存
	        //	return null;
	        //}
	        
		}
		
		return tmp_obj;
	  } catch (Exception e) {
		Toast.makeText( mMainActivity.getWindow().getContext(), "ResourceKey getkey error:"+e.toString(), Toast.LENGTH_SHORT).show();
		return null;
	  }

  }
  
}
