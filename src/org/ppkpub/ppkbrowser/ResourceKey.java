package org.ppkpub.ppkbrowser;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

import org.bitcoinj.core.ECKey;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

public class ResourceKey  {
  final  public  static String mResKeyFileName = "resources_key";

  private static PPkActivity mMainActivity=null;
  
  //发给外部验证用的签名算法类型
  public static final String SIGN_ALGO_BITCOIN_SIGNMSG = "BitcoinSignMsg";  
  
  //本地使用的公钥类型定义
  public static final String KEY_ALGO   = "key_algo";  
  
  public static final String KEY_ALGO_ECC_SECP256K1 = "EcdsaSecp256k1";  
  public static final String KEY_ALGO_ED25519 		= "Ed25519";  
  
  private static JSONObject mObjResKeySet;
  
  public static void init(PPkActivity main_activity ) {
      //Locale.setDefault(new Locale("en", "US"));
	  mMainActivity=main_activity;
	  
	  try {
	    mObjResKeySet=new JSONObject();
	    String str_json=mMainActivity.getPrivateData(mResKeyFileName);
	    if(str_json!=null )
	    	mObjResKeySet=new JSONObject(str_json); 
	    else
	    	mObjResKeySet=new JSONObject();
	  } catch (Exception e) {
        Log.d("ResourceKey","Init failed: "+e.toString());
        //e.printStackTrace();
        mObjResKeySet=new JSONObject();
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
  
  public static boolean saveKey(String res_uri,String prv_key,String pub_key,String str_key_algo) {
	  try {
		  JSONObject tmp_obj=new JSONObject();
		  tmp_obj.put(RSACoder.PRIVATE_KEY , prv_key);
		  tmp_obj.put(RSACoder.PUBLIC_KEY, pub_key);
		  tmp_obj.put(KEY_ALGO, str_key_algo);
		  
		  
		  return saveKey(res_uri,tmp_obj);
	  } catch (Exception e) {
		  return false;
	  }
  }
  
  public static boolean saveKey(String res_uri,JSONObject obj_key) {
	  try {
		  res_uri=ODIN.getRealPPkURI(res_uri);
		  
		  if(res_uri==null)
			  return false;
		  
		  mObjResKeySet.put(res_uri,obj_key);
	  
		  return saveSet();
	  } catch (Exception e) {
		  return false;
	  }

  }
  
  public static JSONObject getKey(String res_uri,boolean auto_gene_new) {
	  try {
		res_uri=ODIN.getRealPPkURI(res_uri);
		if(res_uri==null)
			return null;
		  
		JSONObject tmp_obj=mObjResKeySet.optJSONObject(res_uri);
		
		if( auto_gene_new && tmp_obj==null ) {
			//Toast.makeText( mMainActivity.getWindow().getContext(),"new key for :"+res_uri, Toast.LENGTH_SHORT).show();
			
			
			//系统为用户自动产生一对RSA公私钥供选用
			JSONObject keyMap = RSACoder.initKey();  

	        String publicKey = RSACoder.getPublicKey(keyMap);  
	        String privateKey = RSACoder.getPrivateKey(keyMap); 
	        
	        tmp_obj=new JSONObject();
			tmp_obj.put(RSACoder.PRIVATE_KEY , privateKey);
			tmp_obj.put(RSACoder.PUBLIC_KEY, publicKey);
			tmp_obj.put(KEY_ALGO, RSACoder.KEY_ALGORITHM);
			  
	        //if(!saveKey(res_uri,tmp_obj)) { //待对话框里确认后再保存
	        //	return null;
	        //}
	        
		}
		
		if( tmp_obj==null) {
			//尝试用BTC地址私钥
			JSONObject obj_ap_resp = PTTP.getPPkResource(res_uri);
			if(obj_ap_resp!=null) {
		    	byte[] result_bytes=(byte[])obj_ap_resp.opt(Config.JSON_KEY_CHUNK_BYTES);
	    		JSONObject obj_res= new JSONObject(new String(result_bytes,Config.PPK_TEXT_CHARSET)) ;
	    		
	    		String  exist_register = obj_res.optString("register");
	    		
	    		if(exist_register!=null) {
		    		String vd_set_pubkey= BitcoinWallet.getPubkeyHex(exist_register);
		    		String vd_set_prvkey= BitcoinWallet.getPrvkeyHex(exist_register);
		    		if(vd_set_pubkey!=null && vd_set_prvkey!=null) {
			    		
						tmp_obj=new JSONObject();
						tmp_obj.put(RSACoder.PRIVATE_KEY , vd_set_prvkey);
						tmp_obj.put(RSACoder.PUBLIC_KEY, vd_set_pubkey);
						tmp_obj.put(KEY_ALGO, ResourceKey.KEY_ALGO_ECC_SECP256K1);	
		    		}
	    		}
			}
			
		}
		
		return tmp_obj;
	  } catch (Exception e) {
		Toast.makeText( mMainActivity.getWindow().getContext(), "ResourceKey getkey error:"+e.toString(), Toast.LENGTH_SHORT).show();
		return null;
	  }

  }
  
  public static boolean verify(byte[] data, String pubkey_hex, String sign_base64,String sign_algo) {  
  	boolean result=false;
  	try {
	  if(SIGN_ALGO_BITCOIN_SIGNMSG.equalsIgnoreCase( sign_algo ) ) {
		  ECKey tmp_key=ECKey.fromPublicOnly(Util.hexStringToBytes(pubkey_hex) );
		  tmp_key.verifyMessage(new String(data,Config.PPK_TEXT_CHARSET), sign_base64);
		  result=true;
	  }else {
		  result=RSACoder.verify(data  , pubkey_hex, sign_base64,sign_algo);
	  }
  	}catch(Exception e) {
  	  Log.d("Resourcekey","verify("+pubkey_hex+","+sign_algo+") exception:"+e.toString());
  	  result=false;
  	}
  	return result;
  }  
  
}
