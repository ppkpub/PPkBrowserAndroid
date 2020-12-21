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


public class P2PAsyncTask extends AsyncTask<String, Void, JSONObject>{
	
	public final static String TASK_START="startP2P";
	
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
	
	public P2PAsyncTask(WebView parent_webview,String call_task_name){
		this.mParentWebview=parent_webview;
		this.mTaskName=call_task_name;
	}	
	
	public static void init(PPkActivity main_activity ) {
	    mMainActivity=main_activity;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("P2PAsyncTask","PreExecute ");
        if(Config.debugKey!=0){
	        String str_info="Prepare P2P "+mTaskName;
	        Toast.makeText(mParentWebview.getContext(), str_info, Toast.LENGTH_SHORT).show();
        }
        //progressBar.setVisibility(View.VISIBLE);//显示进度条提示框
    }

    @Override
    protected JSONObject doInBackground(String... params) {
    	try{
    		P2P.start( Config.cacheDirPrefix + "jkademlia" );
	    	return genRespOK(null);
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
        
        Log.d("P2PAsyncTask","onPostExecute result='"+status+"',"+ tmp_obj_json);
        if(!status.equalsIgnoreCase(STATUS_OK) || Config.debugKey!=0){
	        String str_info="P2P."+mTaskName+" resp : '"+status+"',"+ tmp_obj_json;
	        Toast.makeText(mParentWebview.getContext(), str_info, Toast.LENGTH_SHORT).show();
        }
        

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