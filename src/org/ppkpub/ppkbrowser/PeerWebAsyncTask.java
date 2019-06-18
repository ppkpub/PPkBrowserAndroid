package org.ppkpub.ppkbrowser;

import java.math.BigInteger;

import org.bitcoinj.core.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;


public class PeerWebAsyncTask extends AsyncTask<String, Void, JSONObject>{
	public final static String TASK_NAME_GET_DEFAULT_ADDRESS="getDefaultAddress";
	public final static String TASK_NAME_GET_ADDRESS_SUMMARY="getAddressSummary";
	//public final static String TASK_NAME_GET_ADDRESS_LIST="getAddressList";
	public final static String TASK_NAME_CHANGE_ANOTHER_ADDRESS="changeAnotherAddress";
	public final static String TASK_NAME_GENERATE_NEW_ADDRESS="generateNewAddress"; 
	public final static String TASK_NAME_IMPORT_PRIVATE_KEY="importPrivateKey";
	public final static String TASK_NAME_GET_SIGNED_TX="getSignedTX";
	public final static String TASK_NAME_SEND_SIGNED_TX="sendSignedTX";
	public final static String TASK_NAME_GET_SIGNED_ODIN_BTC_TX="getSignedOdinBitcoinTX";
	public final static String TASK_NAME_GET_DEFAULT_ODIN="getDefaultODIN";
	public final static String TASK_NAME_CHANGE_ANOTHER_ODIN="changeAnotherODIN";
	//public final static String TASK_NAME_AUTH_AS_ODIN_OWNER="authAsOdinOwner";  //待实现
	public final static String TASK_NAME_GET_PPK_RESOURCE_PUBKEY="getPPkResourcePubkey";  
	public final static String TASK_NAME_SET_PPK_RESOURCE_KEY="setPPkResourceKey";  
	public final static String TASK_NAME_SIGN_WITH_PPK_RESOURCE_PRVKEY="signWithPPkResourcePrvKey"; 
	public final static String TASK_NAME_VERIFY_SIGN="verifySign"; 
	public final static String TASK_NAME_GET_PPK_RESOURCE="getPPkResource";
	public final static String TASK_NAME_GET_DEFAULT_SETTING="getDefaultSetting";  
	public final static String TASK_NAME_SET_DEFAULT_SETTING="setDefaultSetting";  
	public final static String TASK_NAME_BACKCUP_DATA="backupPrivateData";  

	public final static String STATUS_OK="OK";
	public final static String STATUS_CANCELED="CANCELED";
	public final static String STATUS_INVALID_FUNCTION="INVALID_FUNCTION"; //无效方法
	public final static String STATUS_INVALID_ARGU="INVALID_ARGU";     //无效参数
	public final static String STATUS_ADDRESS_NOT_EXIST="ADDRESS_NOT_EXIST";     //地址不存在
	public final static String STATUS_PPK_RESOURCE_NOT_EXIST="PPK_RESOURCE_NOT_EXIST";     //PPk资源不存在
	public final static String STATUS_UNKOWN_EXCEPTION="UNKOWN_EXCEPTION"; //未知异常
	
	private static PPkActivity mMainActivity=null;
	private WebView 	mParentWebview   = null;
	private String 		mTaskName = null;
	
	String js_callback_function=null;
	
	public PeerWebAsyncTask(WebView parent_webview,String call_task_name){
		this.mParentWebview=parent_webview;
		this.mTaskName=call_task_name;
	}	
	
	public static void init(PPkActivity main_activity ) {
	    mMainActivity=main_activity;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("PeerWebAsyncTask","PreExecute ");
        if(Config.debugKey){
	        String str_info="Calling PeerWeb."+mTaskName;
	        Toast.makeText(mParentWebview.getContext(), str_info, Toast.LENGTH_SHORT).show();
        }
        //progressBar.setVisibility(View.VISIBLE);//显示进度条提示框
    }

    @Override
    protected JSONObject doInBackground(String... params) {
    	try{
	    	if(mTaskName==TASK_NAME_GET_DEFAULT_ADDRESS){
	    		String coin_name=params[0];
		    	js_callback_function=params[1];
		    	if( CoinDefine.COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name)
		    	    ||  CoinDefine.COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name)){
		    		String address=BitcoinWallet.getDefaultAddress();
		    		return address==null? 
		    				genRespError(STATUS_ADDRESS_NOT_EXIST,"No default address") :  genRespOK("address",address);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}
	    	}else if(mTaskName==TASK_NAME_GET_DEFAULT_SETTING){
	    		String set_name=params[0];
		    	js_callback_function=params[1];
		    	
		    	String set_value=Config.getUserDefinedSet( set_name);

	    		return genRespOK(set_name,set_value);
	    	}else if(mTaskName==TASK_NAME_GET_DEFAULT_ODIN){
		    	js_callback_function=params[0];
	    		String odin_uri=Odin.getDefaultOdinURI();
	    		return odin_uri==null? 
	    				genRespError(STATUS_PPK_RESOURCE_NOT_EXIST,"No default odin") :  genRespOK("odin_uri",odin_uri);
	    	}else if(mTaskName==TASK_NAME_GENERATE_NEW_ADDRESS){
	    		String coin_name=params[0];
		    	js_callback_function=params[1];
	    		String tmp_coin_api_uri=coin_name+"create_new_key()#";
	    		
	    		JSONObject obj_ap_resp = PPkURI.fetchPPkURI(tmp_coin_api_uri);
	    		if(obj_ap_resp==null)
		    		return genRespError(STATUS_UNKOWN_EXCEPTION,"Failed to get the ppk resource "+tmp_coin_api_uri);
	    		
	    		String api_result =new String( (byte[])obj_ap_resp.get(Config.JSON_KEY_PPK_CHUNK));
	    		Log.d("API",api_result);
	    		JSONObject tmp_obj_result=new  JSONObject(api_result);
	    		
	    		String address=tmp_obj_result.optString("address",null);
	    		return address==null? 
	    				genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error") :  genRespOK("address",address);
	    	}else if(mTaskName==TASK_NAME_GET_PPK_RESOURCE){
	    		String ppk_uri=params[0];
	    		String resp_type=params[1];
		    	js_callback_function=params[2];
		    	
		    	JSONObject obj_ap_resp = PPkURI.fetchPPkURI(ppk_uri);
		    	if(obj_ap_resp==null)
		    		return genRespError(STATUS_UNKOWN_EXCEPTION,"Failed to get the ppk resource "+ppk_uri);
		    	
	    		if("full".equalsIgnoreCase(resp_type) ) { //返回PTTP协议原始应答数据包
	    			return genRespOK("full_resp_json",
	    							 obj_ap_resp.getString(Config.JSON_KEY_ORIGINAL_RESP )
	    							);
	    		}else {//只返回PTTP协议应答的content正文信息
	    			JSONObject objContent=new JSONObject();
	    			objContent.put("type" ,obj_ap_resp.getString(Config.JSON_KEY_PPK_CHUNK_TYPE));
	    			objContent.put("length" ,obj_ap_resp.getLong(Config.JSON_KEY_PPK_CHUNK_LENGTH));
	    			objContent.put("url" ,obj_ap_resp.getString(Config.JSON_KEY_PPK_CHUNK_URL));
	    			objContent.put("content_base64" ,Base64.encodeToString( (byte[])obj_ap_resp.get(Config.JSON_KEY_PPK_CHUNK), Base64.DEFAULT ));
	    			
	    			return genRespOK(objContent);
	    		}
	    	}else if(mTaskName==TASK_NAME_GET_PPK_RESOURCE_PUBKEY){
	    		String ppk_uri=params[0];
		    	js_callback_function=params[1];
		    	
		    	JSONObject obj_ap_resp = PPkURI.fetchPPkURI(ppk_uri);
		    	if(obj_ap_resp==null)
		    		return genRespError(STATUS_UNKOWN_EXCEPTION,"Failed to get the ppk resource "+ppk_uri);

		    	String vd_set_pubkey="";
	    		
	    		byte[] result_bytes=(byte[])obj_ap_resp.opt(Config.JSON_KEY_PPK_CHUNK);
	    		JSONObject obj_res= new JSONObject(new String(result_bytes,Config.PPK_TEXT_CHARSET)) ;
	    		JSONObject exist_vd_set = obj_res.optJSONObject("vd_set");
	    	    if(exist_vd_set!=null){
	    	        vd_set_pubkey=exist_vd_set.optString(Config.JSON_KEY_PPK_PUBKEY,"");
	    	    }else {
	    	    	//尝试DID
	    	    	JSONArray tmpDidAuths = obj_res.optJSONArray("authentication");
	    	    	if(tmpDidAuths!=null) {
	    	    		exist_vd_set=tmpDidAuths.getJSONObject(0);
	    	    		if(exist_vd_set!=null) {
	    	    			vd_set_pubkey=exist_vd_set.optString("publicKeyPem","");
	    	    		}
	    	    	}
	    	    }
	    	    
	    	    if(vd_set_pubkey!=null && vd_set_pubkey.length()>0)
	    	    	vd_set_pubkey=RSACoder.parseValidPubKey(RSACoder.KEY_ALGORITHM,vd_set_pubkey);
	    	    
	    	    String local_pub_key="";
	    	    JSONObject obj_local_key = ResourceKey.getKey(ppk_uri,false);
	    		if(obj_local_key!=null) {
	    			local_pub_key=obj_local_key.optString(ResourceKey.PUBLIC_KEY , "");
	    		}
    			
	    	    JSONObject objContent=new JSONObject();
    			objContent.put("res_uri" ,ppk_uri);
    			objContent.put("online_pubkey",vd_set_pubkey);
    			objContent.put("local_pubkey",local_pub_key);
    			
		    	return genRespOK(objContent);
	        }else if(mTaskName==TASK_NAME_CHANGE_ANOTHER_ODIN){ 	
	    		String new_odin_uri=params[0];
		    	js_callback_function=params[1];
		    	return genRespOK("odin_uri",new_odin_uri);
	        }else if( mTaskName==TASK_NAME_SET_PPK_RESOURCE_KEY){ 	
	    		String res_uri=params[0];
	    		String pub_key=params[1];
		    	js_callback_function=params[2];
		    	JSONObject objContent=new JSONObject();
    			objContent.put("res_uri" ,res_uri);
    			objContent.put("local_pubkey",pub_key);
    			
		    	return genRespOK(objContent);
	        }else if(mTaskName==TASK_NAME_SIGN_WITH_PPK_RESOURCE_PRVKEY){ 	
	    		String res_uri=params[0];
	    		String sign=params[1];
	    		String sign_algo=params[2];
		    	js_callback_function=params[3];
		    	JSONObject objContent=new JSONObject();
    			objContent.put("res_uri" ,res_uri);
    			objContent.put("sign",sign);
    			objContent.put("algo",sign_algo);
    			
		    	return genRespOK(objContent);	
	        }else if(mTaskName==TASK_NAME_VERIFY_SIGN){ 	
	        	String data_hex=params[0];
	        	String pub_key=params[1];
	    		String sign=params[2];
	    		String sign_algo=params[3];
		    	js_callback_function=params[4];
		    	
		    	if( RSACoder.verify(Util.hexStringToBytes(data_hex)  , pub_key, sign,sign_algo) ) {
		    		return genRespOK(null);		
		    	}else {
		    		return genRespError(STATUS_INVALID_ARGU,"Failed to verify the sign!");
		    	}

	    	}else if(mTaskName==TASK_NAME_GET_ADDRESS_SUMMARY){
	    		String coin_name=params[0];
	    		String address=params[1];
		    	js_callback_function=params[2];
		    	if( CoinDefine.COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name) ){
		    		JSONObject tmp_obj=BitcoinWallet.getAddressSummary(address);
		    		return tmp_obj==null? 
		    				genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error") :  genRespOK(tmp_obj);
		    	}else if( CoinDefine.COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name) ){
		    		JSONObject tmp_obj=BitcoinWallet.getBchAddressSummary(address);
		    		return tmp_obj==null? 
		    				genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error") :  genRespOK(tmp_obj);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}
	    	}else if(mTaskName==TASK_NAME_CHANGE_ANOTHER_ADDRESS){ 	
	    		String coin_name=params[0];
	    		String address=params[1];
		    	js_callback_function=params[2];
		    	return genRespOK("address",address);
		    /*
	    	}else if(mTaskName==TASK_NAME_GET_ADDRESS_LIST){
	    		String coin_name=params[0];
		    	js_callback_function=params[1];
		    	if( COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name) ){
		    		List<String> address_list=BitcoinWallet.getAddresses();
		    		if(address_list==null) 
		    			return genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error");
		    		
		    		JSONObject tmp_obj=new JSONObject();
		    		tmp_obj.put("addresses", new JSONArray(address_list));
		    		return genRespOK(tmp_obj);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}
		    */
	    	}else if(mTaskName==TASK_NAME_IMPORT_PRIVATE_KEY){
	    		String coin_name=params[0];
	    		String prv_key=params[1];
		    	js_callback_function=params[2];
		    	if( CoinDefine.COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name) ){
		    		String address=BitcoinWallet.importPrivateKey(prv_key);
		    		return address==null? 
		    				genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error") :  genRespOK("address",address);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}
	    	}else if(mTaskName==TASK_NAME_GET_SIGNED_ODIN_BTC_TX){
		    	String odin_data_json_hex=params[0];
		    	js_callback_function=params[1];
		    	Log.d("PeerWebAsyncTask", "odin_data_json_hex=" + odin_data_json_hex+"\njs_callback_function="+js_callback_function);
		    	OdinTransctionData objOdinTransctionData=new OdinTransctionData( new String( Util.hexStringToBytes(odin_data_json_hex) ) );
		    	
		    	try {
					String signed_tx_hex= objOdinTransctionData.genSignedTransctionHex();
					Log.d("PeerWebAsyncTask", "signed_tx_hex=" + signed_tx_hex);
					return genRespOK("signed_tx_hex",signed_tx_hex);
		    	} catch (Exception e) {
		    		e.printStackTrace();
	        		return genRespError(STATUS_UNKOWN_EXCEPTION,e.toString());
				}  
	    	}else if(mTaskName==TASK_NAME_GET_SIGNED_TX){
	    		String coin_name=params[0];
		    	String tx_argus_json_hex=params[1];
		    	js_callback_function=params[2];
		    	Log.d("PeerWebAsyncTask", "coin_name=" + coin_name+"\ntx_argus_json_hex=" + tx_argus_json_hex+"\njs_callback_function="+js_callback_function);
		    
		    	String signed_tx_hex=null;
	        	try {
	        		JSONObject obj_tx_argus;
		        	String source=null;
		        	String destination=null;
		        	BigInteger amount_satoshi;
		        	BigInteger fee_satoshi;
		        	byte[] data;
		        	
					obj_tx_argus=new JSONObject( new String( Util.hexStringToBytes(tx_argus_json_hex) ) );
					source=obj_tx_argus.getString("source");
					destination=obj_tx_argus.getString("destination");
					amount_satoshi=BigInteger.valueOf(obj_tx_argus.getLong("amount_satoshi"));
					fee_satoshi=BigInteger.valueOf(obj_tx_argus.getLong("fee_satoshi"));
					data=Util.hexStringToBytes(obj_tx_argus.optString("data_hex",""));
					
			    	Transaction tx=BitcoinWallet.transaction(
					    	        source,
					    	        destination,
					    	        amount_satoshi,
					    	        fee_satoshi,
					    	        "",
					    	        data,
					    	        CoinDefine.COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name)
					    	      );
			        signed_tx_hex=Util.bytesToHexString( tx.bitcoinSerialize() );
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        		return	genRespError(STATUS_UNKOWN_EXCEPTION,e.toString());
				}  
	        	Log.d("PeerWebAsyncTask", "signed_tx_hex=" + signed_tx_hex);
	    		return genRespOK("signed_tx_hex",signed_tx_hex);
	    	}else if(mTaskName==TASK_NAME_SEND_SIGNED_TX){
	    		String coin_name=params[0];
	    		String source=params[1];
		    	String signed_tx_hex=params[2];
		    	js_callback_function=params[3];
		    	Log.d("PeerWebAsyncTask",  "coin_name=" + coin_name+",source=" + source+",signed_tx_hex=" + signed_tx_hex+"\njs_callback_function="+js_callback_function);

				try{
					String send_result_txid=null;
					
					if( CoinDefine.COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name) ){
						send_result_txid=BitcoinWallet.sendTransaction(source, signed_tx_hex);
					}else if( CoinDefine.COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name) ){
						send_result_txid=BitcoinWallet.sendBchTransaction(source, signed_tx_hex);
					}else{
			    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
			    	}
					Log.d("PeerWebAsyncTask", "send_result_txid=" + send_result_txid);
					return send_result_txid!=null ? 
			        		 genRespOK("txid",send_result_txid) : genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error");
				}catch(Exception e){
		    		e.printStackTrace();
		    		Log.d("PeerWebAsyncTask", "send TX exception:" + e.toString());
		    		return genRespError(STATUS_UNKOWN_EXCEPTION,e.toString()); 
		    	}
	    	}else{
	    		return genRespError(STATUS_INVALID_FUNCTION,"Not supported function:"+mTaskName); 
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    		return genRespError(STATUS_UNKOWN_EXCEPTION,e.toString()); 
    	}
    	
    }

    @Override
    protected void onPostExecute(JSONObject result) {
    	
    	if(js_callback_function==null) //未指定有效回调方法则直接返回
    		return;
    	
    	String status=null;
    	JSONObject obj_data=null;
    	
        super.onPostExecute(result);
        if(result==null){
        	status=STATUS_UNKOWN_EXCEPTION;
        }else{
        	status=result.optString("status", STATUS_UNKOWN_EXCEPTION);
        	obj_data=result.optJSONObject("data");
        }
        
        String tmp_obj_json = (obj_data==null) ? "null" : obj_data.toString();
        
        Log.d("PeerWebAsyncTask","onPostExecute result='"+status+"',"+ tmp_obj_json);
        if(!status.equalsIgnoreCase(STATUS_OK) || Config.debugKey){
	        String str_info="PeerWeb."+mTaskName+" resp : '"+status+"',"+ tmp_obj_json;
	        Toast.makeText(mParentWebview.getContext(), str_info, Toast.LENGTH_SHORT).show();
        }
        
        mParentWebview.loadUrl("javascript:"+js_callback_function+"('"+status+"',"+ tmp_obj_json +");" );

        //progressBar.setVisibility(View.GONE);
    }
    
    public static JSONObject genRespOK(JSONObject obj_data){
    	return genResp(STATUS_OK,obj_data);
    } 
    
    public static JSONObject genRespOK(String k,String v){
    	try {
    		JSONObject objData=new JSONObject();
    		objData.put(k, v);
			
			return genResp(STATUS_OK,objData);
		} catch (JSONException e) {
			return null;
		}
    }
    
    public static JSONObject genRespError(String errcode){
    	return genResp(errcode,null);
    }
    
    public static JSONObject genRespError(String errcode,String errdesc){
    	return genResp(errcode,genJSONObject("errdesc",errdesc));
    }
    
    public static JSONObject genResp(String status,JSONObject obj_data){
    	try {
    		JSONObject objResp=new JSONObject();
			objResp.put("status", status);
			
			if(obj_data!=null)
				objResp.put("data", obj_data);
			
			return objResp;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

    public static void callbackBeforeExceute(final WebView mParentWebview,final String js_callback_function,final String status,final String errdesc ){
    	mParentWebview.post(new Runnable() {
    		@Override
    		public void run() {
    			JSONObject tmp_obj=genJSONObject("errdesc", errdesc);
    			String tmp_obj_json = (tmp_obj==null) ? "null" : tmp_obj.toString();
    			
    			mParentWebview.loadUrl("javascript:"+js_callback_function+"('"+status+"',"+ tmp_obj_json +");" );
    		}
    	});
    }
    
    public static JSONObject genJSONObject(String k,String v){
    	try {
    		JSONObject objData=new JSONObject();
    		objData.put(k, v);
			
			return objData;
		} catch (JSONException e) {
			return null;
		}
    }
}