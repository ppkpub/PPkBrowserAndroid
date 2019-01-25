package org.ppkpub.ppkbrowser;

import java.math.BigInteger;

import org.bitcoinj.core.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;


public class PeerWebAsyncTask extends AsyncTask<String, Void, JSONObject>{
	public final static String TASK_NAME_GET_DEFAULT_ADDRESS="getDefaultAddress";
	public final static String TASK_NAME_GET_ADDRESS_SUMMARY="getAddressSummary";
	public final static String TASK_NAME_AUTH_AS_ADDRESS_OWNER="authAsAddressOwner";  //待实现
	public final static String TASK_NAME_GENERATE_NEW_ADDRESS="generateNewAddress"; 
	public final static String TASK_NAME_IMPORT_PRIVATE_KEY="importPrivateKey";
	public final static String TASK_NAME_GET_SIGNED_TX="getSignedTX";
	public final static String TASK_NAME_SEND_SIGNED_TX="sendSignedTX";
	public final static String TASK_NAME_GET_SIGNED_ODIN_BTC_TX="getSignedOdinBitcoinTX";
	public final static String TASK_NAME_GET_PPK_RESOURCE="getPPkResource";
	
	
	public final static String STATUS_OK="OK";
	public final static String STATUS_CANCELED="CANCELED";
	public final static String STATUS_INVALID_FUNCTION="INVALID_FUNCTION"; //无效方法
	public final static String STATUS_INVALID_ARGU="INVALID_ARGU";     //无效参数
	public final static String STATUS_ADDRESS_NOT_EXIST="ADDRESS_NOT_EXIST";     //地址不存在
	public final static String STATUS_UNKOWN_EXCEPTION="UNKOWN_EXCEPTION"; //未知异常
	
	WebView 	webshow   = null;
	String 		task_name = null;
	
	String js_callback_function=null;
	
	public PeerWebAsyncTask(WebView parent_webview,String call_task_name){
		this.webshow=parent_webview;
		this.task_name=call_task_name;
	}
	
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("PeerWebAsyncTask","PreExecute ");
        if(Config.debugKey){
	        String str_info="Calling PeerWeb."+task_name;
	        Toast.makeText(webshow.getContext(), str_info, Toast.LENGTH_SHORT).show();
        }
        //progressBar.setVisibility(View.VISIBLE);//显示进度条提示框
    }

    @Override
    protected JSONObject doInBackground(String... params) {
    	try{
	    	if(task_name==TASK_NAME_GET_DEFAULT_ADDRESS){
	    		String coin_name=params[0];
		    	js_callback_function=params[1];
		    	if( "BITCOIN".equalsIgnoreCase(coin_name) ){
		    		String address=BitcoinWallet.getDefaultAddress();
		    		return address==null? 
		    				genRespError(STATUS_ADDRESS_NOT_EXIST,"No default address") :  genRespOK("address",address);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}
	    	}else if(task_name==TASK_NAME_GET_ADDRESS_SUMMARY){
	    		String coin_name=params[0];
	    		String address=params[1];
		    	js_callback_function=params[2];
		    	if( "BITCOIN".equalsIgnoreCase(coin_name) ){
		    		JSONObject tmp_obj=BitcoinWallet.getAddressSummary(address);
		    		return tmp_obj==null? 
		    				genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error") :  genRespOK(tmp_obj);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}
	    	/*}else if(task_name==TASK_NAME_GENERATE_NEW_ADDRESS){
	    		String coin_name=params[0];
		    	js_callback_function=params[1];
		    	if( "BITCOIN".equalsIgnoreCase(coin_name) ){
		    		String address=BitcoinWallet.generateNewAddress();
		    		return address==null? 
		    				genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error") :  genRespOK("address",address);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}*/
	    	}else if(task_name==TASK_NAME_IMPORT_PRIVATE_KEY){
	    		String coin_name=params[0];
	    		String prv_key=params[1];
		    	js_callback_function=params[2];
		    	if( "BITCOIN".equalsIgnoreCase(coin_name) ){
		    		String address=BitcoinWallet.importPrivateKey(prv_key);
		    		return address==null? 
		    				genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error") :  genRespOK("address",address);
		    	}else{
		    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
		    	}
	    	}else if(task_name==TASK_NAME_GET_SIGNED_ODIN_BTC_TX){
		    	String odin_data_json_hex=params[0];
		    	js_callback_function=params[1];
		    	Log.d("PeerWebAsyncTask", "odin_data_json_hex=" + odin_data_json_hex+"\njs_callback_function="+js_callback_function);
		    	OdinTransctionData objOdinTransctionData=new OdinTransctionData( new String( Util.hexStringToBytes(odin_data_json_hex) ) );
		    	
		    	try {
					String signed_tx_hex= objOdinTransctionData.genSignedTransctionHex();
					Log.d("PeerWebAsyncTask", "signed_tx_hex=" + signed_tx_hex);
					return genRespOK("signed_tx_hex",signed_tx_hex);
		    	} catch (Exception e) {
	        		return genRespError(STATUS_UNKOWN_EXCEPTION,e.toString());
				}  
	    	}else if(task_name==TASK_NAME_GET_SIGNED_TX){
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
		        	
					obj_tx_argus=new JSONObject( new String( Util.hexStringToBytes(tx_argus_json_hex) ) );
					source=obj_tx_argus.getString("source");
					destination=obj_tx_argus.getString("destination");
					amount_satoshi=BigInteger.valueOf(obj_tx_argus.getLong("amount_satoshi"));
					fee_satoshi=BigInteger.valueOf(obj_tx_argus.getLong("fee_satoshi"));

			    	Transaction tx=BitcoinWallet.transaction(
					    	        source,
					    	        destination,
					    	        amount_satoshi,
					    	        fee_satoshi,
					    	        "",
					    	        ""
					    	      );
			        signed_tx_hex=Util.bytesToHexString( tx.bitcoinSerialize() );
	        	} catch (Exception e) {
	        		return	genRespError(STATUS_UNKOWN_EXCEPTION,e.toString());
				}  
	        	Log.d("PeerWebAsyncTask", "signed_tx_hex=" + signed_tx_hex);
	    		return genRespOK("signed_tx_hex",signed_tx_hex);
	    	}else if(task_name==TASK_NAME_SEND_SIGNED_TX){
	    		String coin_name=params[0];
	    		String source=params[1];
		    	String signed_tx_hex=params[2];
		    	js_callback_function=params[3];
		    	Log.d("PeerWebAsyncTask",  "coin_name=" + coin_name+",source=" + source+",signed_tx_hex=" + signed_tx_hex+"\njs_callback_function="+js_callback_function);

				try{
					boolean send_result=false;
					
					if( "BITCOIN".equalsIgnoreCase(coin_name) ){
						send_result=BitcoinWallet.sendTransaction(source, signed_tx_hex);
					}else{
			    		return genRespError(STATUS_INVALID_ARGU,"Not supported coin:"+coin_name); 
			    	}
					Log.d("PeerWebAsyncTask", "send_result=" + send_result);
					return send_result ? 
			        		 genRespOK(null) : genRespError(STATUS_UNKOWN_EXCEPTION,"Unkown error");
				}catch(Exception e){
		    		e.printStackTrace();
		    		Log.d("PeerWebAsyncTask", "send TX exception:" + e.toString());
		    		return genRespError(STATUS_UNKOWN_EXCEPTION,e.toString()); 
		    	}
	    	}else{
	    		return genRespError(STATUS_INVALID_FUNCTION,"Not supported function:"+task_name); 
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    		return genRespError(STATUS_UNKOWN_EXCEPTION,e.toString()); 
    	}
    	
    }

    @Override
    protected void onPostExecute(JSONObject result) {
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
	        String str_info="PeerWeb."+task_name+" resp : '"+status+"',"+ tmp_obj_json;
	        Toast.makeText(webshow.getContext(), str_info, Toast.LENGTH_SHORT).show();
        }
        
        webshow.loadUrl("javascript:"+js_callback_function+"('"+status+"',"+ tmp_obj_json +");" );

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

    public static void callbackBeforeExceute(final WebView webshow,final String js_callback_function,final String status,final String errdesc ){
    	webshow.post(new Runnable() {
    		@Override
    		public void run() {
    			JSONObject tmp_obj=genJSONObject("errdesc", errdesc);
    			String tmp_obj_json = (tmp_obj==null) ? "null" : tmp_obj.toString();
    			
    			webshow.loadUrl("javascript:"+js_callback_function+"('"+status+"',"+ tmp_obj_json +");" );
    			//webshow.e
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