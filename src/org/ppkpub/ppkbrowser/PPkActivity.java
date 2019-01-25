package org.ppkpub.ppkbrowser;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceResponse;
//import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PPkActivity extends Activity
{

    private EditText    weburl;
    private WebView     webshow;
    private ImageButton buttonGo;
    private ImageButton buttonBack;
    private ImageButton buttonForward;
    private ImageButton buttonStop;
    private ImageButton buttonRefresh;
    private ImageButton buttonHome;
    private ImageButton buttonSetting;
    private ProgressBar progressBar;
    private boolean     bLoadingHttpPage;
    
    private TextView    textStatus;
    
    //private OdinTransctionData    	objOdinTransctionData; //暂存ODIN标识相关交易数据，用于JS与Android间的交互
    
    // 建立一个链表来保存历史记录
    //private BrowserPageList historylist = new BrowserPageList();
    // 建立一个链表来保存书签记录
    //BrowserPageList bookmarklist = new BrowserPageList();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //初始化内置的Bitcoin钱包
        BitcoinWallet.init(this);
        
        setContentView(R.layout.activity_ppk);

        setTitle(Config.appName+" V"+Config.version);
        
        progressBar = (ProgressBar) findViewById(R.id.progressBarLoadPage);
        progressBar.setVisibility(View.GONE);
        textStatus = (TextView) findViewById(R.id.textStatus);
        
        weburl = (EditText) findViewById(R.id.weburl);
        webshow = (WebView) findViewById(R.id.webshow);
        webshow.getSettings().setJavaScriptEnabled(true);
        webshow.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webshow.getSettings().setSupportMultipleWindows(true);
        webshow.getSettings().setSupportZoom(true);
        webshow.getSettings().setBuiltInZoomControls(true);
        webshow.getSettings().setDisplayZoomControls(false);
        
        webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);

        
        webshow.setWebViewClient(new PPkWebViewClient());
        
        webshow.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if( bLoadingHttpPage ){
                    if (newProgress == 100) {
                        progressBar.setVisibility(View.GONE);
                    } else{
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    super.onProgressChanged(view, newProgress);
                }
            }
        });
        
        webshow.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                Log.d("browser","motionEvent:"+motionEvent.toString());
                return false;
            }
        });
        
        /*
        //禁止Android WebView 文本框获取焦点后自动放大
        webshow.setOnFocusChangeListener(new View.OnFocusChangeListener() {  
            @Override   
            public void onFocusChange(View v, boolean hasFocus) {   
                if(hasFocus)   
                {   
                    try {   
                        Field defaultScale = WebView.class.getDeclaredField("mDefaultScale");   
                        defaultScale.setAccessible(true);   
                        //WebViewSettingUtil.getInitScaleValue(VideoNavigationActivity.this, false )/100.0f 是另一个方法，可以用float 的scale替代   
                        defaultScale.setFloat(webshow, 1);   
                    } catch (SecurityException e) {   
                        e.printStackTrace();   
                    } catch (IllegalArgumentException e) {   
                        e.printStackTrace();   
                    } catch (IllegalAccessException e) {   
                        e.printStackTrace();   
                    } catch (NoSuchFieldException e) {   
                        e.printStackTrace();   
                    }    
                }   
            }   
        });  
        */
        gotoURI( Config.ppkDefaultHomepage );
        
        buttonGo = (ImageButton) findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String url = weburl.getText().toString();
                Log.d("browser", "url:" + url);
                gotoURI( url );
            }
        });
        
        buttonBack = (ImageButton) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(webshow.canGoBack()){
                    webshow.goBack();
                    textStatus.setText("Back to "+webshow.getUrl());
                    weburl.setText(webshow.getUrl());
                    weburl.setTextColor(Color.BLACK);
                }
            }
        });
        
        buttonForward = (ImageButton) findViewById(R.id.buttonForward);
        buttonForward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(webshow.canGoForward()){
                    webshow.goForward();
                    textStatus.setText("Forward to "+webshow.getUrl());
                    weburl.setText(webshow.getUrl());
                    weburl.setTextColor(Color.BLACK);
                }
            }
        });
        
        buttonStop = (ImageButton) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                textStatus.setText("Stop loading "+webshow.getUrl());
                webshow.stopLoading() ;
                progressBar.setVisibility(View.GONE);
            }
        });

        buttonRefresh = (ImageButton) findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                textStatus.setText("Reloading "+webshow.getUrl());
                webshow.clearCache(true);
                webshow.reload() ;
            }
        });
        
        buttonHome = (ImageButton) findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                
                gotoURI(Config.ppkDefaultHomepage) ;
            }
        });
        
        buttonSetting = (ImageButton) findViewById(R.id.buttonSetting);
        buttonSetting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //gotoURI("https://blockchain.info/unspent?active="+BitcoinWallet.getDefaultAddress()) ;
            	//gotoURI("http://192.168.62.99:12345/odin?address="+BitcoinWallet.getDefaultAddress()) ;
            	//gotoURI("http://btmdemo.ppkpub.org/odin/test/?address="+BitcoinWallet.getDefaultAddress()) ;
            	gotoURI(Config.ppkSettingPage);
            }
        });
    }
    
    @JavascriptInterface
    public void getSignedOdinBitcoinTX(final String odin_data_json_hex,final String callback_function){
    	Log.d("browser", "getSignedOdinBitcoinTX " + odin_data_json_hex+","+callback_function);

    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	AlertDialog dialog;
    	
    	OdinTransctionData objOdinTransctionData=new OdinTransctionData( new String( Util.hexStringToBytes(odin_data_json_hex) ) );
    	
    	if(objOdinTransctionData.source==null || objOdinTransctionData.source.length()==0 
    	  || objOdinTransctionData.data_hex==null || objOdinTransctionData.data_hex.length()==0 	
    			){
    		dialog = new AlertDialog.Builder(this)
			    .setTitle("提示")
				.setNegativeButton("关闭", cancelButtonClickListener)
				.setMessage("无效的ODIN标识消息内容！")
				.create();
    	}else{
    		BigDecimal fee_btc = new BigDecimal(objOdinTransctionData.fee_satoshi.longValue()).divide(new BigDecimal(Config.btc_unit));
			dialog = new AlertDialog.Builder(this)
			    .setTitle("确认发送下述比特币交易吗?")
				.setNegativeButton("取消", cancelButtonClickListener)
				.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//处理确认按钮的点击事件
						new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_SIGNED_ODIN_BTC_TX  ).execute(odin_data_json_hex,callback_function);
					}
				})
				.setMessage("确认发送ODIN标识对应交易吗?"
				             +"\n发送地址：\n"+objOdinTransctionData.source
    						 +"\n矿工费用："+fee_btc+" BTC"
    						 +"\n请注意该比特币交易一旦发出，将无法撤销！")
				.create();
    	}
		dialog.show();
    	
    	/*
    	objOdinTransctionData=new OdinTransctionData( new String( Util.hexStringToBytes(message) ) );
    	
    	webshow.post(new Runnable() {
    		@Override
    		public void run() {
    			String signed_tx_hex= objOdinTransctionData.genSignedTransctionHex();
    			textStatus.setText("sendOdinTX: signed_tx_hex ok");
    			Log.d("browser", "sendOdinTX signed_tx_hex=" + signed_tx_hex);
    			webshow.loadUrl("javascript:callback_confirmSendTX('" + signed_tx_hex + "')");
    		}
    	});
		*/
    }
    
    @JavascriptInterface
    public void getSignedTX(final String coin_name,final String tx_argus_json_hex,final String callback_function){
    	Log.d("browser", "getSignedTX " +coin_name+","+tx_argus_json_hex+","+callback_function);
    	
    	if("BITCOIN".equalsIgnoreCase(coin_name)){
    		DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
        	
        	AlertDialog dialog;
        	
        	JSONObject obj_tx_argus;
        	String source=null;
        	String destination=null;
        	BigDecimal amount_btc=null;
        	BigDecimal fee_btc=null;
        	try {
				obj_tx_argus=new JSONObject( new String( Util.hexStringToBytes(tx_argus_json_hex) ) );
				source=obj_tx_argus.getString("source");
				destination=obj_tx_argus.getString("destination");
				amount_btc=new BigDecimal(obj_tx_argus.getLong("amount_satoshi")).divide(new BigDecimal(Config.btc_unit));
				fee_btc=new BigDecimal(obj_tx_argus.getLong("fee_satoshi")).divide(new BigDecimal(Config.btc_unit));
			} catch (Exception e) {
				obj_tx_argus=null;
			}        	
        	
        	if(obj_tx_argus==null){
        		dialog = new AlertDialog.Builder(this)
    			    .setTitle("提示")
    				.setNegativeButton("关闭", cancelButtonClickListener)
    				.setMessage("无效的交易内容！")
    				.create();
        	}else{
    			dialog = new AlertDialog.Builder(this)
    			    .setTitle("确认发送比特币交易吗?")
    				.setNegativeButton("取消", cancelButtonClickListener)
    				.setPositiveButton("确定", new OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						//处理确认按钮的点击事件
    						new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_SIGNED_TX  ).execute(coin_name,tx_argus_json_hex,callback_function);
    					}
    				})
    				.setMessage("确认发送下述比特币交易吗?"
    				         +"\n发送地址：\n"+source
    						 +"\n目标地址：\n"+destination
    						 +"\n交易金额："+amount_btc+" BTC"
    						 +"\n矿工费用："+fee_btc+" BTC"
    						 +"\n请注意该比特币交易一旦发出，将无法撤销！")
    				.create();
        	}
    		dialog.show();
    	}else{
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported coin:"+coin_name
    			);
    	}
    }
    
    @JavascriptInterface
    public void sendSignedTX(String coin_name,String source_address,String odin_data_json_hex,String callback_function){
    	Log.d("browser", "sendSignedTX " + source_address+","+ odin_data_json_hex+","+callback_function);
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_SEND_SIGNED_TX  ).execute(coin_name,source_address,odin_data_json_hex,callback_function);
    }
    
    @JavascriptInterface
    public void getDefaultAddress(String coin_name,String callback_function){
    	Log.d("browser", "getDefaultAddress " + coin_name+","+callback_function);
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_DEFAULT_ADDRESS ).execute(coin_name,callback_function);
    }
    
    @JavascriptInterface
    public void getAddressSummary(String coin_name,String address,String callback_function){
    	Log.d("browser", "getAddressSummary " + coin_name+"," + address+","+callback_function);
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_ADDRESS_SUMMARY ).execute(coin_name,address,callback_function);
    }
    
    @JavascriptInterface
    public void generateNewAddress(String coin_name,String callback_function){
    	Log.d("browser", "generateNewAddress " + coin_name+","+callback_function);
    	
    	if("BITCOIN".equalsIgnoreCase(coin_name)){
    		ECKey key = new ECKey();
    		String prv_key=key.getPrivateKeyAsWiF(MainNetParams.get());
    		importPrivateKey(coin_name,prv_key,callback_function);
    	}else{
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported coin:"+coin_name
    			);
    	}
    }
    
    @JavascriptInterface
    public void importPrivateKey(final String coin_name,final String prv_key,final String callback_function){
    	Log.d("browser", "importPrivateKey " + coin_name+","+prv_key+","+callback_function);
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
    	String address =  BitcoinWallet.getAddressOfPrviteKey(prv_key);
    	if(address==null || !address.startsWith("1") ){
    		dialog = new AlertDialog.Builder(this)
			    .setTitle("提示")
				.setNegativeButton("关闭", cancelButtonClickListener)
				.setMessage("无效的比特币地址:"+address+"\n请使用以5,L或K起始的私钥字符串！")
				.create();
    		
    		dialog.show();
    	}else{
    		LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_import_prvkey , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("确认使用新地址吗?")
			    .setMessage("地址：\n"+address)
			    .setView(DialogView)//设置自定义对话框的样式
				.setNegativeButton("取消", cancelButtonClickListener)
				.setPositiveButton("确定", null)
				.create();

			EditText txtImportPrvkey = (EditText)DialogView.findViewById(R.id.import_prvkey ); 
			txtImportPrvkey.setText(prv_key);
			
			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View v){
		            	//处理确认按钮的点击事件
						CheckBox chkBackupedPrvkey = (CheckBox)DialogView.findViewById(R.id.import_checkBoxBackupedPrvkey );
					    if(chkBackupedPrvkey.isChecked() ){
					    	dialog.dismiss();
					    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_IMPORT_PRIVATE_KEY ).execute(coin_name,prv_key,callback_function);
					    }else{
					    	TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.import_warningMessage ); 
					    	txtWarningMessage.setText("请复制备份好上述私钥，然后勾选确认！") ;
					    	txtWarningMessage.setTextColor(Color.RED);
					    }
		            }
		        });
			
			( (Button)DialogView.findViewById(R.id.import_btnCopyPrvkey ) ).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	// Copy the Text to the clipboard
	                ClipboardManager manager = 
	                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	                manager.setText( prv_key );
	                
	                // Show a message:
	                Toast.makeText(v.getContext(), "已复制到剪贴板",Toast.LENGTH_SHORT)
	                     .show();
	            }
	        });
				
    	}
    }
  
    public String getPrivateData(String  data_name){
    	Log.d("browser", "getPrivateData " + data_name);
    	SharedPreferences tmpSP = getSharedPreferences("ppkbrowser_private",MODE_PRIVATE) ;
    	return tmpSP.getString(data_name,null) ;
    }
    
    public boolean putPrivateData(String  data_name,String data_val){
    	SharedPreferences tmpSP = getSharedPreferences("ppkbrowser_private",MODE_PRIVATE) ;
    	tmpSP.edit()
	        .putString(data_name,data_val)
	        .apply();
    	
    	return true;
    }
    
    public void gotoURI(String destURI){
        weburl.setText(destURI);
        weburl.setTextColor(Color.BLACK);
        textStatus.setText("Go to "+destURI);
        Log.d("browser", "Go to " + destURI);
        webshow.getSettings().setJavaScriptEnabled(true);
        if (destURI != null){
            if(destURI.toLowerCase().startsWith( Config.PPK_URI_PREFIX )) {
                new ShowPPkUriAsyncTask().execute(destURI);
            }else {
            	if(destURI.toLowerCase().startsWith( Config.ppkSettingPage )) {
            		destURI="file:///android_asset/settings.html";
            	}
                webshow.loadUrl(destURI);
                webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
            }
        }
    }

    public WebResourceResponse getPPkResource(String ppk_uri){
        Log.d("browser","getPPkResource: " + ppk_uri);
        WebResourceResponse res = null;
        JSONObject obj_ap_resp=PPkURI.fetchPPkURI(ppk_uri);
        byte[] result_bytes=(byte[])obj_ap_resp.opt(Config.JSON_KEY_PPK_CHUNK);
        String mimeType=obj_ap_resp.optString(Config.JSON_KEY_PPK_CHUNK_TYPE);
        String encoding=Config.PPK_TEXT_CHARSET;
        
        ByteArrayInputStream bis = new ByteArrayInputStream(result_bytes);

        res = new WebResourceResponse(mimeType, encoding, bis);
        
        return res;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_ENTER)
        {
            String url = weburl.getText().toString();
            Log.d("browser", "onKeyDown(SEARCH/ENTER)  url:" + url);
            gotoURI( url );
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webshow.canGoBack())
        {
            Log.d("browser", "onKeyDown(BACK) " );
              webshow.goBack();
              weburl.setText(webshow.getUrl());
            
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    //缺省的取消或关闭按钮的点击事件处理
    private class DefaultCancelButtonClickListener implements OnClickListener{
    	public String js_callback_function=null;
    	
    	public DefaultCancelButtonClickListener(String callback_function){
    		this.js_callback_function=callback_function;
    	}
    	
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			webshow.post(new Runnable() {
	    		@Override
	    		public void run() {
	    			Log.d("browser", "DefaultCancelButtonClickListener callback_function=" + js_callback_function);
	    			PeerWebAsyncTask.callbackBeforeExceute(
	        				webshow,
	        				js_callback_function,
	        				PeerWebAsyncTask.STATUS_CANCELED,
	        				null
	        			);
	    		}
	    	});
		}
		
    }
    
    // 监听页面中资源加载和点击链接
    private class PPkWebViewClient extends WebViewClient {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("browser","shouldOverrideUrlLoading 。。 url: "+ url);
        weburl.setText(url);
        weburl.setTextColor(Color.BLACK);
        textStatus.setText("Opening "+url);
        if (url != null && url.toLowerCase().startsWith( Config.PPK_URI_PREFIX )) {
            PPkActivity.this.bLoadingHttpPage=false;
            
            view.stopLoading();
            
            new ShowPPkUriAsyncTask().execute(url);
            
            return true;
        }
        PPkActivity.this.bLoadingHttpPage=true;
        return false;
      }
      
      /*
      @Override
      public void onLoadResource(WebView view, String url) {
        System.out.println("onLoadResource  "+url);    
        super.onLoadResource(view, url);
      }
      */
    
      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Log.d("browser","loadRes: " + url);
        if (url != null && url.toLowerCase().startsWith( Config.PPK_URI_PREFIX )) {
            return getPPkResource(url);
        }else{
            return super.shouldInterceptRequest(view,url);
        }
      }


      @Override
      public void onPageFinished(WebView view, String url) {
        //textStatus.setText("Finished");
        view.getSettings().setJavaScriptEnabled(true);
        view.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
        super.onPageFinished(view, url);
        //PPkActivity.this.setTitle(view.getTitle()); 
        printBackForwardList();
      }
      
      @Override
      public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
          Log.d("browser", "doUpdateVisitedHistory  isReload=" + isReload + ", url= " + url );
          if(!isReload)
              super.doUpdateVisitedHistory(view, url, isReload);
      }
      
      @Override
      public void onReceivedSslError(WebView view,
              SslErrorHandler handler, SslError error) {
          // TODO Auto-generated method stub
          // handler.cancel();// Android默认的处理方式
          handler.proceed();// 接受所有网站的证书
          // handleMessage(Message msg);// 进行其他处理
      }
    }

    
    class ShowPPkUriAsyncTask extends AsyncTask<String, Void, JSONObject>{
    	String ppkURI=null;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weburl.setTextColor(Color.BLACK);
            textStatus.setText("PreExecute PPk resource ");
            //String str_info="<h3>Loading PPk resource ...<br>正在读取PPk资源信息...</h3>";
            //webshow.loadDataWithBaseURL(null, str_info, "text/html", "utf-8", null);
            progressBar.setVisibility(View.VISIBLE);//显示进度条提示框
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            this.ppkURI=params[0];
            String result_content=null;
            
            JSONObject obj_ap_resp=PPkURI.fetchPPkURI(this.ppkURI);
            return obj_ap_resp;

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            String result_uri=null;
            String from_ap_url=null;
            String result_content="<h3>ERROR while loading PPk URI. <br>访问PPk网址时出错了<br>URI:  "+ppkURI+"</h3>";;
            String mimeType="text/html";
            String encoding="utf-8";
            
            super.onPostExecute(result);
            if(result!=null){
                try {
                	
                	int validcode=result.optInt(Config.JSON_KEY_PPK_VALIDATION,Config.PPK_VALIDATION_ERROR);
                    if( validcode == Config.PPK_VALIDATION_IGNORED 
                      || validcode == Config.PPK_VALIDATION_OK ){
	                    result_uri=result.optString(Config.JSON_KEY_PPK_URI);
	                    from_ap_url=result.optString(Config.JSON_KEY_PPK_CHUNK_URL);
	                    mimeType=result.optString(Config.JSON_KEY_PPK_CHUNK_TYPE,mimeType);
	                    if( mimeType.toLowerCase().startsWith("text") ){
	                    	result_content=new String((byte[])result.opt(Config.JSON_KEY_PPK_CHUNK),Config.PPK_TEXT_CHARSET);
	                    }else if( mimeType.toLowerCase().startsWith("image") ){
	                    	String image64 = Base64.encodeToString((byte[])result.opt(Config.JSON_KEY_PPK_CHUNK), Base64.DEFAULT);
	                   	    result_content = "<img src=\"data:"+mimeType+";base64," + image64 + "\" />";
	                   	    mimeType = "text/html";
	                    }else{
	                    	result_content=new String((byte[])result.opt(Config.JSON_KEY_PPK_CHUNK),Config.BINARY_DATA_CHARSET);
	                    }
	                    
	                    if(validcode == Config.PPK_VALIDATION_OK)
	                    	weburl.setTextColor( Color.rgb(34,139,34) );                   	
                    }else{
                    	result_content += "<font color='#F00'>Valiade failed!</font>";
                    }
                    
                    //setTitle(result_uri); //在标题栏临时显示当前实际访问的URI地址
                    
                    //String page_title=Util.getPageTitle(result_uri,result_content);
                    //historylist.addURL(page_title,result_uri);
                } catch (UnsupportedEncodingException e) {
                    Log.d("browser","UnsupportedEncodingException:"+e.toString());
                }
                
            }
            textStatus.setText("Loaded PPkURI: "+result_uri + "\nAP: "+from_ap_url);
            webshow.loadDataWithBaseURL(result_uri, result_content, mimeType, encoding, this.ppkURI);

            progressBar.setVisibility(View.GONE);
            
            printBackForwardList();
        }

   }
    
   public void printBackForwardList() {
        WebBackForwardList currentList = webshow.copyBackForwardList();
        int currentSize = currentList.getSize();
        for(int i = 0; i < currentSize; ++i)
        {
            WebHistoryItem item = currentList.getItemAtIndex(i);
            String url = item.getUrl();
            Log.d("browser", "The URL at index: " + Integer.toString(i) + " is " + url );
        }
   }
}