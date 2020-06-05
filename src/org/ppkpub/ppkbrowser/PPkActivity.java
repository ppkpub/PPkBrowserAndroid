package org.ppkpub.ppkbrowser;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.karics.library.zxing.android.CaptureActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceResponse;
//import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PPkActivity extends Activity
{

    private EditText    weburl;
    private MyWebView	webshow;
    private MyWebChromeClient mWebChromeClient;
    private ImageButton buttonGo;
    private ImageButton buttonScan;
    private ImageButton buttonBack;
    private ImageButton buttonForward;
    private ImageButton buttonStop;
    private ImageButton buttonRefresh;
    private ImageButton buttonHome;
    private ImageButton buttonSetting;
    private ProgressBar progressBar;
    private boolean     bLoadingHttpPage;
    
    private ImageButton buttonStar; //最新动态按钮
    
    private TextView    textStatus;
    
    private UpdateInfoService updateInfoService; //版本更新检查
    
    private static final int REQUEST_CODE_SCAN = 0x0000;

    
    //private OdinTransctionData    	objOdinTransctionData; //暂存奥丁号相关交易数据，用于JS与Android间的交互
    
    // 建立一个链表来保存历史记录
    //private BrowserPageList historylist = new BrowserPageList();
    // 建立一个链表来保存书签记录
    //BrowserPageList bookmarklist = new BrowserPageList();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //初始化
        Config.init(this);
        NetCache.init(this);
        BitcoinWallet.init(this);
        ODIN.init(this);
        ResourceKey.init(this);
        PeerWebAsyncTask.init(this);
        
        setContentView(R.layout.activity_ppk);

        setTitle(Config.appName+" V"+Config.version);

        progressBar = (ProgressBar) findViewById(R.id.progressBarLoadPage);
        progressBar.setVisibility(View.GONE);
        textStatus = (TextView) findViewById(R.id.textStatus);
        
        weburl = (EditText) findViewById(R.id.weburl);
        webshow = (MyWebView) findViewById(R.id.webshow);
        webshow.init(this);
        
        webshow.setOnScrollChangeListener(new MyWebView.OnScrollChangeListener() {
            @Override
            public void onPageEnd(int l, int t, int oldl, int oldt) {
                Log.d("browser","已经到达地端");
                //Toast.makeText(webshow.getContext(), "已经到达底端" , 0).show();
            }

            @Override
            public void onPageTop(int l, int t, int oldl, int oldt) {
                Log.d("browser","已经到达顶端");
                textStatus.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
            	textStatus.setVisibility(View.GONE);
            	/*
            	int dy=t-oldt;
            	if(dy>10) {//往下滚动一定距离
            		
            	}else if(dy<-10) { //往上滚动一定距离
            		
            	}
            	*/
            }
        });

        webshow.setWebViewClient(new PPkWebViewClient());
        
        mWebChromeClient=new MyWebChromeClient(this,webshow){
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
        };
        webshow.setWebChromeClient(mWebChromeClient);
        
        webshow.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                //调用系统浏览器处理下载事件
            	Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        
        gotoURI( Config.ppkDefaultHomepage );
        //gotoURI( Config.ppkHotURI ); //缺省显示“精彩推荐”
        
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
        
        buttonScan = (ImageButton) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            	Intent intent = new Intent(PPkActivity.this,
						CaptureActivity.class);
				startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });
        
        buttonBack = (ImageButton) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            	gotoBack();
            }
        });
        
        buttonForward = (ImageButton) findViewById(R.id.buttonForward);
        buttonForward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            	gotoForward();
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
            	String current_url =  webshow.getUrl();
                textStatus.setText("Reloading "+ current_url);
                NetCache.deleteNetCache(current_url);
                webshow.clearCache(true);
                //webshow.reload() ;
                gotoURI( current_url );
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
        
        buttonStar = (ImageButton) findViewById(R.id.buttonStar);
        buttonStar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            	gotoURI(Config.ppkHotURI);
            }
        });
        
        //检查本机数据保护密码设置状态
        if(!Config.isWalletPasswordProtected()) {
        	setLocalDataProtectedPassword();
        }
        
        //检查新版本
        checkUpdate();
    }
    
    
    private void checkUpdate() {
	    Toast.makeText(PPkActivity.this, "自动检查更新版本...", Toast.LENGTH_SHORT).show();
	    
	    new Thread() {
	        public void run() {
	            try {
	            	updateInfoService = new UpdateInfoService( );
	                updateInfoService.refreshUpdateInfo();
	                //Toast.makeText(PPkActivity.this, "最新版本:"+updateInfoService.getNewstVersion(), Toast.LENGTH_SHORT).show();
	                handlerUpdateService.sendEmptyMessage(0);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }.start();
	}
    
    @SuppressLint("HandlerLeak")
	private Handler handlerUpdateService = new Handler() {
	    public void handleMessage(Message msg) {
	    	Toast.makeText(PPkActivity.this, "最新版本:"+updateInfoService.getNewstVersion(), Toast.LENGTH_SHORT).show();
	    	
	        if (updateInfoService.isNeedUpdate()) {
            	Intent intent = new Intent(PPkActivity.this,
						UpdateActivity.class);
				startActivityForResult(intent, REQUEST_CODE_SCAN);
	        }
	    }
	};
    
    public WebView getCurrentWebview() {
    	return webshow;
    }
    
    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // 扫描二维码/条码回传
 		if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
 			if (intent != null) {

 				String content = intent.getStringExtra("codedContent");

 				//Toast.makeText(webshow.getContext(), "扫码结果： " + content , 0).show();
 				gotoURI(content);
 				
 				/*
 				//调用签名确认
 				String user_odin_uri=ODIN.getDefaultOdinURI();
 				String login_confirm_url=content;
 				signWithPPkResourcePrvKey(
 					user_odin_uri,
 					login_confirm_url,
 					Util.bytesToHexString(login_confirm_url.getBytes()),
 					null
 				  );*/
 			}
 		}else {
 			mWebChromeClient.onActivityResult(requestCode, resultCode,intent);
 		}
 	}
    
    //避免 WebView 的内存泄露问题
    @Override
    protected void onDestroy() {
        if (webshow != null) {
        	webshow.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        	webshow.clearHistory();
            ((ViewGroup) webshow.getParent()).removeView(webshow);
            webshow.destroy();
            webshow = null;
        }
        super.onDestroy();
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
				.setMessage("无效的奥丁号消息内容！")
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
				.setMessage("确认发送奥丁号对应交易吗?"
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
    	
    	if(CoinDefine.COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name)
    	|| CoinDefine.COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name)){
    		DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
        	
        	AlertDialog dialog;
        	
        	JSONObject obj_tx_argus;
        	String source=null;
        	String destination=null;
        	BigDecimal amount_btc=null;
        	BigDecimal fee_btc=null;
        	
        	Long amount_satoshi=0L;
        	try {
				obj_tx_argus=new JSONObject( new String( Util.hexStringToBytes(tx_argus_json_hex) ) );
				source=obj_tx_argus.getString("source");
				destination=obj_tx_argus.getString("destination");
				amount_satoshi=obj_tx_argus.getLong("amount_satoshi");
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
        		JSONObject obj_coin_def = CoinDefine.getCoinDefine(coin_name);
        		String coin_label_cn=obj_coin_def.optString("label_cn",coin_name);
        		String coin_symbol=obj_coin_def.optString("symbol",coin_name);
    			dialog = new AlertDialog.Builder(this)
    			    .setTitle("确认发送"+coin_label_cn+"交易吗?")
    				.setNegativeButton("取消", cancelButtonClickListener)
    				.setPositiveButton("确定", new OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						//处理确认按钮的点击事件
    						new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_SIGNED_TX  ).execute(coin_name,tx_argus_json_hex,callback_function);
    					}
    				})
    				.setMessage("确认发送下述"+coin_label_cn+"交易吗?"
    				         +"\n发送地址：\n"+source
    						 +"\n目标地址：\n"+destination
    						 +"\n交易金额："+amount_btc+" "+ coin_symbol 
    						 +"\n矿工费用："+fee_btc+" "+ coin_symbol
    						 +"\n请注意该交易一旦发出，将无法撤销！")
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
    public void getDefaultSetting(String set_name,String callback_function){
    	Log.d("browser", "getDefaultSetting " + set_name+" , "+callback_function);
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_DEFAULT_SETTING ).execute(set_name,callback_function);
    }
    
    @JavascriptInterface
    public void setDefaultSetting(String set_name,final String callback_function){
    	Log.d("browser", "setDefaultSetting " + set_name+","+callback_function);
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
    	String set_value = Config.getUserDefinedSet(set_name) ;
		if(set_value==null) {
			dialog = new AlertDialog.Builder(this)
				    .setTitle("提示")
					.setNegativeButton("关闭", cancelButtonClickListener)
					.setMessage("无效的配置项:"+set_name)
					.create();
	    		
			dialog.show();
		}else {
			LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_input_item , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("设置缺省参数")
			    .setView(DialogView)//设置自定义对话框的样式
				.setNegativeButton("取消", cancelButtonClickListener)
				.setPositiveButton("确定", null)
				.create();

			final EditText txtSetName = (EditText)DialogView.findViewById(R.id.input_item_name ); 
			final EditText txtSetValue = (EditText)DialogView.findViewById(R.id.input_item_value ); 
			txtSetName.setText( set_name );
			txtSetValue.setText( set_value );

			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//处理确认按钮的点击事件
	            	String result_set_name = txtSetName.getText().toString() ;
	            	String result_set_value = txtSetValue.getText().toString() ;
	            	if(Config.saveUserDefinedSet( result_set_name, result_set_value)) {
		            	dialog.dismiss();
		            	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_DEFAULT_SETTING ).execute(result_set_name,callback_function);
	            	}else {
	            		Toast.makeText(v.getContext(), "保存配置出错，请检查后重试！",Toast.LENGTH_SHORT)
	                     .show();
	            	}
	            }
	        });
			
			( (Button)DialogView.findViewById(R.id.input_item_btnCopyValue ) ).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	try {
		            	// Copy the Text to the clipboard
		                ClipboardManager manager = 
		                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		                manager.setText( txtSetValue.getText().toString() );
		                
		                // Show a message:
		                Toast.makeText(v.getContext(), "已复制到剪贴板",Toast.LENGTH_SHORT)
		                     .show();
	            	} catch (Exception e) {
	            		Toast.makeText(v.getContext(), "复制失败",Toast.LENGTH_SHORT)
	                     .show();
					}
	            }
	        });
			
			( (Button)DialogView.findViewById(R.id.input_item_btnPasteValue ) ).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	// get the Text from the clipboard
	                ClipboardManager manager = 
	                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	                CharSequence tmp_clip = manager.getText();
	                if(tmp_clip!=null) {
		                try {
		                	txtSetValue.setText( tmp_clip.toString() );
			                Toast.makeText(v.getContext(), "已粘贴成功",Toast.LENGTH_SHORT)
			                     .show();
		                }catch(Exception e) {
		                	Toast.makeText(v.getContext(), "无效的粘贴数据",Toast.LENGTH_SHORT)
		                     .show();
		                }
		             }
	            }
	        });
		}
    }
    
    @JavascriptInterface
    public void clearNetCache(final String clear_domain,final String callback_function){
    	Log.d("browser", "clearNetCache " + clear_domain+","+callback_function);

    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	AlertDialog dialog;
    	
    	dialog = new AlertDialog.Builder(this)
		    .setTitle("确认清除缓存吗?")
			.setNegativeButton("取消", cancelButtonClickListener)
			.setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//处理确认按钮的点击事件
					new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_CLEAR_NET_CACHE  ).execute(clear_domain,callback_function);
				}
			})
			.setMessage("确认清除全部网络缓存吗?")
			.create();
    	
		dialog.show();
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
    
    /*
    @JavascriptInterface
    public void getAddressList(String coin_name,String callback_function){
    	Log.d("browser", "getAddressList " + coin_name+","+callback_function);
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_ADDRESS_LIST ).execute(coin_name,callback_function);
    }
    */
    @JavascriptInterface
    public void changeAnotherAddress(final String coin_name,final String callback_function){
    	Log.d("browser", "changeAnotherAddress " + coin_name+","+callback_function);
    	
    	if(!CoinDefine.COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name)){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported coin:"+coin_name
    			);
    		return;
    	}
    	
    	final List<String> address_list=BitcoinWallet.getAddresses();
		if(address_list==null||address_list.size()==0 ) {
			PeerWebAsyncTask.callbackBeforeExceute(
				this.webshow,
				callback_function,
				PeerWebAsyncTask.STATUS_ADDRESS_NOT_EXIST,
				"No valid address for "+coin_name
			);
		}
		List<String> address_label_list=new ArrayList<String>();
		for(int kk=0;kk<address_list.size();kk++){
			address_label_list.add( Util.shortAddressView(address_list.get(kk)) );
		}
		
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
		LayoutInflater factory = LayoutInflater.from(this);
    	final View DialogView = factory.inflate(R.layout.dialog_select_address , null);
    	
		dialog = new AlertDialog.Builder(this)
		    .setTitle("请选择要使用的地址")
		    .setView(DialogView)//设置自定义对话框的样式
			.setNegativeButton("取消", cancelButtonClickListener)
			.setPositiveButton("确定", null)
			.create();
		
		final EditText txtSelectedAddress = (EditText)DialogView.findViewById(R.id.dsa_txtSelectedAddress ); 

		final ListView listviewAddress = (ListView)DialogView.findViewById(R.id.dsa_listViewAddress ); 
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			    this,   // Context上下文                 
			    android.R.layout.simple_list_item_1,  // 子项布局id
			    address_label_list);
		
		listviewAddress.setAdapter(adapter);
		listviewAddress.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				//Toast.makeText(webshow.getContext(), "点击了第" + position + "条数据:"+address_list.get(position) , 0).show();
				txtSelectedAddress.setText(address_list.get(position));
			}
        });
		if (address_list.size() > 5){
            ViewGroup.LayoutParams layoutParams = listviewAddress.getLayoutParams();
            layoutParams.height = 5 * 80;    //设置列表显示高度
            listviewAddress.setLayoutParams(layoutParams);
        }
		
		dialog.show();
		
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//处理确认按钮的点击事件
	            	String selected_address=txtSelectedAddress.getText().toString()  ;
	            	if(selected_address.length()==0){
	            		TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.dsa_warningMessage ); 
				    	txtWarningMessage.setText("请从列表中点击选择要使用的地址！") ;
				    	txtWarningMessage.setTextColor(Color.RED);
	            		return;
	            	}
					CheckBox chkSetAdDefault = (CheckBox)DialogView.findViewById(R.id.dsa_checkSetAsDefaultAddress );
				    if(chkSetAdDefault.isChecked() ){
				    	BitcoinWallet.setDefaultAddress(selected_address);
				    }
				    
				    dialog.dismiss();
				    new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_CHANGE_ANOTHER_ADDRESS ).execute(coin_name,selected_address,callback_function);
				    
	            }
	        });
		/*
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
        */
    }
    
    @JavascriptInterface
    public void generateNewAddress(String coin_name,String callback_function){
    	Log.d("browser", "generateNewAddress " + coin_name+","+callback_function);
    	
    	if(coin_name==null || coin_name.length()==0) {
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"coin_name is empty"
    			);
    	}else if(CoinDefine.COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name)){
    		ECKey key = new ECKey();
    		String prv_key=key.getPrivateKeyAsWiF(MainNetParams.get());
    		importPrivateKey(coin_name,prv_key,callback_function);
    	}else if( coin_name.toLowerCase().startsWith(Config.PPK_URI_PREFIX ) ){
    		new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GENERATE_NEW_ADDRESS ).execute(coin_name,callback_function);
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
    public void backupPrivateData(final String callback_function){
    	Log.d("browser", "backupPrivateData called"+callback_function);
    	
    	//检查本机数据保护密码设置状态
        if(!Config.isWalletPasswordProtected()) {
        	setLocalDataProtectedPassword();
        	return;
        }	
        
    	final AlertDialog dialog;

		LayoutInflater factory = LayoutInflater.from(this);
    	final View DialogView = factory.inflate(R.layout.dialog_backup_data , null);
    	
		dialog = new AlertDialog.Builder(this)
		    .setTitle("备份或恢复数据，请谨慎操作！")
		    .setView(DialogView)//设置自定义对话框的样式
			.setNegativeButton("关闭", null)
			.create();

		final EditText txtPassword = (EditText)DialogView.findViewById(R.id.backup_data_password ); 
		final EditText txtDataContent = (EditText)DialogView.findViewById(R.id.backup_data_content ); 
		final TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.backup_data_warningMessage ); 
		txtWarningMessage.setTextColor(Color.RED);
		
		dialog.show();
		
		( (Button)DialogView.findViewById(R.id.backup_data_btnExport ) ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
            	String strPassword=txtPassword.getText().toString();
            	
            	String  tmp_backup_data = Config.exportLocalProtectedData( strPassword.getBytes() );
            			
            	if(tmp_backup_data!=null) {
            		txtWarningMessage.setText("") ;
            		txtDataContent.setText(tmp_backup_data);
            	}else {
            		txtWarningMessage.setText("请输入正确的密码，然后重试！") ;
            	}
            }
        });
		
		( (Button)DialogView.findViewById(R.id.backup_data_btnRestore ) ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
            	String strPassword=txtPassword.getText().toString();
            	String strData=txtDataContent.getText().toString();

            	if( Config.restoreLocalProtectedData( strPassword.getBytes(),strData )) {
            		txtWarningMessage.setText("") ;
            		Toast.makeText(v.getContext(), "已恢复完成",Toast.LENGTH_SHORT)
            			.show();
            		dialog.dismiss();
            		PeerWebAsyncTask.callbackBeforeExceute(
            				webshow,
            				callback_function,
            				PeerWebAsyncTask.STATUS_OK,
            				""
            			);
            	}else {
            		txtWarningMessage.setText("请输入正确的密码，然后重试！") ;
            	}
            }
        });

		
		( (Button)DialogView.findViewById(R.id.backup_data_btnCopy ) ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
            	try {
	            	// Copy the Text to the clipboard
	                ClipboardManager manager = 
	                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	                manager.setText( txtDataContent.getText().toString() );
	                
	                // Show a message:
	                Toast.makeText(v.getContext(), "已复制到剪贴板",Toast.LENGTH_SHORT)
	                     .show();
            	} catch (Exception e) {
            		Toast.makeText(v.getContext(), "复制失败",Toast.LENGTH_SHORT)
                     .show();
				}
            }
        });
		
		( (Button)DialogView.findViewById(R.id.backup_data_btnPaste ) ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
            	// get the Text from the clipboard
                ClipboardManager manager = 
                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                CharSequence tmp_clip = manager.getText();
                if(tmp_clip!=null) {
	                try {
	                	txtDataContent.setText( tmp_clip.toString() );
		                Toast.makeText(v.getContext(), "已粘贴成功",Toast.LENGTH_SHORT)
		                     .show();
	                }catch(Exception e) {
	                	Toast.makeText(v.getContext(), "无效的粘贴数据",Toast.LENGTH_SHORT)
	                     .show();
	                }
	             }
            }
        });
    }

    public void setLocalDataProtectedPassword(){
    	Log.d("browser", "setLocalDataProtectedPassword " );
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(null);

    	if(Config.isWalletPasswordProtected() ){
    		dialog = new AlertDialog.Builder(this)
			    .setTitle("提示")
				.setNegativeButton("关闭", cancelButtonClickListener)
				.setMessage("数据保护密码已设置，不能修改！")
				.create();
    		
    		dialog.show();
    	}else{
    		LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_set_local_password  , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("提示")
			    .setView(DialogView)//设置自定义对话框的样式
				.setPositiveButton("确定", null)
				.create();

			final EditText txtNewLocalPassword = (EditText)DialogView.findViewById(R.id.set_local_password_new ); 
			final TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.set_local_password_warningMessage ); 
			txtWarningMessage.setTextColor(Color.RED);
			
			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View v){
		            	//处理确认按钮的点击事件
		            	String strNewPassword=txtNewLocalPassword.getText().toString();
		            	
		            	if(strNewPassword.trim().length()==0) {
		            		txtWarningMessage.setText("请输入有效的密码（建议只使用英文字母和数字），然后确认保存！") ;
		            	}else {
							CheckBox chkRemeber = (CheckBox)DialogView.findViewById(R.id.set_local_password_checkRemeber );
						    if(chkRemeber.isChecked() ){
						    	if(Config.setWalletProtectPassword( strNewPassword.getBytes() )) {
						    		dialog.dismiss();
						    	}else {
						    		txtWarningMessage.setText("保存密码出错！\n请确保输入有效的密码（建议只使用英文字母和数字），然后重试！") ;
						    	}
						    }else{
						    	txtWarningMessage.setText("请确保输入的密码无误并记好，然后勾选确认！") ;
						    }
			            }
		            }
		        });
    	 }
    }
    
    public void verifyLocalDataProtectedPassword(final View.OnClickListener passedListener){
    	Log.d("browser", "verifyLocalDataProtectedPassword " );
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(null);

    	if(!Config.isWalletPasswordProtected() ){
    		dialog = new AlertDialog.Builder(this)
			    .setTitle("提示")
				.setNegativeButton("忽略", null)
				.setPositiveButton("现在就设置", null)
				.setMessage("数据保护密码尚未设置，你的钱包私钥、身份密钥等重要数据会有泄漏风险！")
				.create();
    		
    		dialog.show();
    		
    		dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//处理忽略按钮的点击事件
	            	dialog.dismiss();
	            	passedListener.onClick(null);
	            }
	        });
    		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//处理确认按钮的点击事件
	            	dialog.dismiss();
	            	setLocalDataProtectedPassword();
	            }
	        });
    	}else{
    		LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_verify_data_pwd  , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("验证操作权限")
			    .setView(DialogView)//设置自定义对话框的样式
			    .setNegativeButton("取消", null)
				.setPositiveButton("确定", null)
				.create();

			final EditText txtLocalPassword = (EditText)DialogView.findViewById(R.id.verify_data_password ); 
			final TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.verify_data_warningMessage ); 
			txtWarningMessage.setTextColor(Color.RED);
			
			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//处理确认按钮的点击事件
	            	String strInputPassword=txtLocalPassword.getText().toString().trim();
	            	if( Config.verifyWalletProtectPassword( strInputPassword.getBytes() )) {
	            		dialog.dismiss();
	            		passedListener.onClick(null);
		            }else {
		            	txtWarningMessage.setText("密码不正确，请重新输入！") ;
		            }
	            }
	        });
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
    
    @JavascriptInterface
    public void getDefaultODIN(String callback_function){
    	Log.d("browser", "getDefaultODIN " + callback_function);
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_DEFAULT_ODIN ).execute(callback_function);
    }
    
    @JavascriptInterface
    public void changeAnotherODIN( String new_id_uri,final String callback_function){
    	Log.d("browser", "changeAnotherODIN " + new_id_uri+","+callback_function);
    	
    	final String format_id_uri=ODIN.formatPPkURI(new_id_uri,true);
    	if( format_id_uri ==null ){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported new_id_uri:"+new_id_uri
    			);
    		return;
    	}
    	
    	/*待完善，检查指定odin的拥有权
    	final List<String> address_list=BitcoinWallet.getAddresses();
		if(address_list==null||address_list.size()==0 ) {
			PeerWebAsyncTask.callbackBeforeExceute(
				this.webshow,
				callback_function,
				PeerWebAsyncTask.STATUS_ADDRESS_NOT_EXIST,
				"No valid address for "+new_odin_uri
			);
		}
    	*/
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
		LayoutInflater factory = LayoutInflater.from(this);
    	final View DialogView = factory.inflate(R.layout.dialog_select_address , null);
    	
		dialog = new AlertDialog.Builder(this)
		    .setTitle("请确定切换使用新的奥丁号")
		    .setMessage("请确定使用下面的奥丁号作为用户身份\n  "+format_id_uri)
			.setNegativeButton("取消", cancelButtonClickListener)
			.setPositiveButton("确定", null)
			.create();
		
		dialog.show();
		
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//处理确认按钮的点击事件
	            	ODIN.setDefaultOdinURI(format_id_uri);
				    dialog.dismiss();
				    Toast.makeText(v.getContext(), "OK,用户身份标识已设为 "+format_id_uri, Toast.LENGTH_SHORT).show();
				    new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_CHANGE_ANOTHER_ODIN ).execute(format_id_uri,callback_function);
				    
	            }
	        });

    }
    
    @JavascriptInterface
    public void getPPkResourcePubkey(String input_uri,final String callback_function){
    	Log.d("browser", "getPPkResourcePubkey " + input_uri+","+callback_function);
    	
    	final String format_id_uri=ODIN.formatPPkURI(input_uri,true);
    	if( format_id_uri ==null ){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported uri: "+input_uri
    			);
    		return;
    	}
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_PPK_RESOURCE_PUBKEY ).execute(format_id_uri,callback_function);
    }
    /*
    public void getPPkResourcePubkey(final String ppk_uri,final String callback_function){
    	Log.d("browser", "getPPkResourcePubkey " + ppk_uri +","+callback_function);
    	
    	if(ppk_uri==null || !ppk_uri.startsWith( Config.PPK_URI_PREFIX ) ){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Invalid pubkey for "+ppk_uri
    			);
    	}else{
    		JSONObject obj_key = ResourceKey.getKey(ppk_uri,false);
    		if(obj_key==null) {
    			PeerWebAsyncTask.callbackBeforeExceute(
        				this.webshow,
        				callback_function,
        				PeerWebAsyncTask.STATUS_INVALID_ARGU,
        				"Failed to create key for "+ppk_uri
        			);
    		}else {
				final String pub_key= obj_key.optString(ResourceKey.PUBLIC_KEY , "");
				
				new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_PPK_RESOURCE_PUBKEY ).execute(ppk_uri,pub_key,callback_function);
    		}
    	}
    }
    */
    
    @JavascriptInterface
    public void setPPkResourceKey(String input_uri,final String callback_function){
    	Log.d("browser", "setPPkResourceKey " + input_uri +","+callback_function);
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
    	final String format_id_uri=ODIN.formatPPkURI(input_uri,true);
    	if( format_id_uri ==null ){
    		dialog = new AlertDialog.Builder(this)
			    .setTitle("提示")
				.setNegativeButton("关闭", cancelButtonClickListener)
				.setMessage("无效的资源地址:"+input_uri+"\n需以 "+Config.PPK_URI_PREFIX+" 或 "+Config.DIDPPK_URI_PREFIX+"  起始。 ")
				.create(); 
    		
    		dialog.show();
    	}else{
    		final JSONObject obj_key = ResourceKey.getKey(format_id_uri,true);
    		if(obj_key==null) {
    			dialog = new AlertDialog.Builder(this)
    				    .setTitle("提示")
    					.setNegativeButton("关闭", cancelButtonClickListener)
    					.setMessage("无法生成有效密钥！\n对应地址:"+format_id_uri)
    					.create();
    	    		
    			dialog.show();
    			
    		}else {
    			LayoutInflater factory = LayoutInflater.from(this);
	        	final View DialogView = factory.inflate(R.layout.dialog_set_reskey , null);
	        	
				dialog = new AlertDialog.Builder(this)
				    .setTitle("查看/设置标识验证密钥")
				    .setMessage("标识："+format_id_uri)
				    .setView(DialogView)//设置自定义对话框的样式
					.setNegativeButton("取消", cancelButtonClickListener)
					.setPositiveButton("确定", null)
					.create();
				
	    		verifyLocalDataProtectedPassword(new View.OnClickListener() {
		            @Override
		            public void onClick(View v){
		            	//数据密码验证通过后的处理
		            	//设置资源密钥

	    				final EditText txtSetPubkey = (EditText)DialogView.findViewById(R.id.setkey_pubkey ); 
	    				final EditText txtSetPrvkey = (EditText)DialogView.findViewById(R.id.setkey_prvkey ); 
	
	    				txtSetPubkey.setText( obj_key.optString(RSACoder.PUBLIC_KEY , "") );
	    				txtSetPrvkey.setText( obj_key.optString(RSACoder.PRIVATE_KEY , "") );
	    				
	    				dialog.show();
	    				
	    				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	    		            @Override
	    		            public void onClick(View v){
	    		            	//处理确认按钮的点击事件
	    		            	//保存密钥
	    		            	String pub_key = txtSetPubkey.getText().toString() ;
	    	    				String prv_key = txtSetPrvkey.getText().toString() ;
	
	    		            	ResourceKey.saveKey(format_id_uri, prv_key,pub_key,RSACoder.KEY_ALGORITHM);
	    		            	
	    		            	dialog.dismiss();
	    				    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_SET_PPK_RESOURCE_KEY ).execute(format_id_uri,pub_key,callback_function);
	    		            }
	    		        });
	    				
	    				( (Button)DialogView.findViewById(R.id.setkey_btnCopyPrvkey ) ).setOnClickListener(new View.OnClickListener() {
	    		            @Override
	    		            public void onClick(View v){
	    		            	try {
	    		    				String pub_key = txtSetPubkey.getText().toString() ;
	    		    				String prv_key = txtSetPrvkey.getText().toString() ;
	    		    				
	    		            		JSONObject new_key=new JSONObject();
	    		            		new_key.put("ppk_uri",format_id_uri);
	    		            		new_key.put("RSAPublicKey",pub_key);
	    							new_key.put("RSAPrivateKey",prv_key);
	    		            	
	    			            	// Copy the Text to the clipboard
	    			                ClipboardManager manager = 
	    			                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	    			                manager.setText( new_key.toString() );
	    			                
	    			                // Show a message:
	    			                Toast.makeText(v.getContext(), "已复制到剪贴板",Toast.LENGTH_SHORT)
	    			                     .show();
	    		            	} catch (JSONException e) {
	    		            		Toast.makeText(v.getContext(), "复制失败",Toast.LENGTH_SHORT)
	    		                     .show();
	    						}
	    		            }
	    		        });
	    				
	    				( (Button)DialogView.findViewById(R.id.setkey_btnPastePrvkey ) ).setOnClickListener(new View.OnClickListener() {
	    		            @Override
	    		            public void onClick(View v){
	    		            	// get the Text from the clipboard
	    		                ClipboardManager manager = 
	    		                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	    		                CharSequence tmp_clip = manager.getText();
	    		                if(tmp_clip!=null) {
	    			                try {
	    			                	String new_key_json=tmp_clip.toString() ;
	    			                	JSONObject new_key=new JSONObject(new_key_json);
	    				                
	    				                txtSetPrvkey.setText( new_key.optString("RSAPrivateKey","") );
	    				                txtSetPubkey.setText( new_key.optString("RSAPublicKey","")  );
	    				                
	    				                // Show a message:
	    				                Toast.makeText(v.getContext(), "已粘贴成功",Toast.LENGTH_SHORT)
	    				                     .show();
	    			                }catch(Exception e) {
	    			                	txtSetPrvkey.setText( "" );
	    				                txtSetPubkey.setText( "" );
	    			                	Toast.makeText(v.getContext(), "不合法的密钥",Toast.LENGTH_SHORT)
	    			                     .show();
	    			                }
	    			             }
	    		            }
	    		        });
	        		
		            }
		        });
    		}
    		
    	}
    }
    
    @JavascriptInterface
    public void signWithPPkResourcePrvKey(String in_uri,final String requester_uri,final String data_hex,final String callback_function){
    	//Log.d("browser", "signWithPPkResourcePrvKey " + in_uri +"," + requester_uri +"," +callback_function);
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
    	final String ppk_uri=ODIN.formatPPkURI(in_uri);
    	
    	JSONObject obj_key = ResourceKey.getKey(ppk_uri,false);
		if(obj_key==null) {
			dialog = new AlertDialog.Builder(this)
				    .setTitle("提示")
					.setNegativeButton("关闭", cancelButtonClickListener)
					.setMessage("尚未配置有效密钥！\n对应地址:"+ppk_uri)
					.create();
	    		
			dialog.show();
		}else {
			final String tmp_private_key= obj_key.optString(RSACoder.PRIVATE_KEY , "");
			final String tmp_pub_key= obj_key.optString(RSACoder.PUBLIC_KEY , "");
			
			String tmp_key_algo= obj_key.optString(ResourceKey.KEY_ALGO , RSACoder.KEY_ALGORITHM); 
			final String tmp_sign_algo= tmp_key_algo.equalsIgnoreCase(RSACoder.KEY_ALGORITHM) 
									? RSACoder.DEFAULT_SIGNATURE_ALGORITHM : ResourceKey.SIGN_ALGO_BITCOIN_SIGNMSG; 

			String tmp_sign;
			try {
				if( ResourceKey.SIGN_ALGO_BITCOIN_SIGNMSG.equalsIgnoreCase(tmp_sign_algo)  ) {
					//ECKey tmp_key=new ECKey();
					//Log.d("browser", "signPPkResource.tmp_pubkey= " + tmp_key.getPublicKeyAsHex());
					ECKey tmp_key=ECKey.fromPrivate(Util.hexStringToBytes(tmp_private_key) );
					String tmp_msg=new String(Util.hexStringToBytes(data_hex),Config.PPK_TEXT_CHARSET);
					tmp_sign= tmp_key.signMessage( tmp_msg ) ;
				
					/*
					tmp_key=ECKey.signedMessageToKey(tmp_msg, tmp_sign);
					Log.d("browser", "signPPkResource.tmp_key= " + tmp_key.getPublicKeyAsHex());
					
					try {
						tmp_key=ECKey.fromPublicOnly(Util.hexStringToBytes(tmp_pub_key) );
						tmp_key.verifyMessage(tmp_msg, tmp_sign);
						Log.d("browser","verify ok");
				  	}catch(Exception e) {
				  		Log.d("browser","verify exception:"+e.toString());
				  	}
				  	*/
				}else {
					tmp_sign=RSACoder.sign(
							Util.hexStringToBytes(data_hex) , tmp_private_key,tmp_sign_algo
					     );
					
				}
				
				//Log.d("browser", "signPPkResource.verifySign " + data_hex+","+tmp_pub_key +"," + tmp_sign  +"," + tmp_sign_algo  +","  +callback_function);
				if( !ResourceKey.verify(Util.hexStringToBytes(data_hex)  , tmp_pub_key, tmp_sign,tmp_sign_algo) ) {
					throw new Exception("Self-check sign failed with "+tmp_sign_algo);
				}
				//Log.d("browser","Sign passed");
			} catch (Exception e) {
				//e.printStackTrace();
				PeerWebAsyncTask.callbackBeforeExceute(
	    				this.webshow,
	    				callback_function,
	    				PeerWebAsyncTask.STATUS_UNKOWN_EXCEPTION,
	    				"Sign failed for "+ppk_uri
	    			);
				return;
			}
			
			LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_set_reskey , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("按资源标识生成签名")
			    .setMessage("资源地址：\n"+ppk_uri)
			    .setView(DialogView)//设置自定义对话框的样式
				.setNegativeButton("取消", cancelButtonClickListener)
				.setPositiveButton("确定", null)
				.create();

			final TextView lblAlgoType = (TextView)DialogView.findViewById(R.id.setkey_algo_type ); 
			TextView lblPubKey = (TextView)DialogView.findViewById(R.id.setkey_pubkey_label ); 
			TextView lblPrvKey = (TextView)DialogView.findViewById(R.id.setkey_prvkey_label ); 
			lblAlgoType.setText("签名算法："+tmp_sign_algo) ;
			lblPubKey.setText("原文内容：") ;
			lblPrvKey.setText("生成的签名：") ;

			final EditText txtSetPubkey = (EditText)DialogView.findViewById(R.id.setkey_pubkey ); 
			final EditText txtSetPrvkey = (EditText)DialogView.findViewById(R.id.setkey_prvkey ); 
			txtSetPubkey.setText( new String(Util.hexStringToBytes(data_hex)) );
			txtSetPrvkey.setText( tmp_sign );

			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//处理确认按钮的点击事件
	            	String result_sign = txtSetPrvkey.getText().toString() ;
	            	if(result_sign.length()>0) {
		            	dialog.dismiss();
		    	        new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_SIGN_WITH_PPK_RESOURCE_PRVKEY ).execute(ppk_uri,result_sign,tmp_sign_algo,callback_function);
	            	}else {
	            		Toast.makeText(v.getContext(), "签名无效，请检查后重试！",Toast.LENGTH_SHORT)
	                     .show();
	            	}
	            }
	        });
			
			( (Button)DialogView.findViewById(R.id.setkey_btnCopyPrvkey ) ).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	try {
	    				String original = txtSetPubkey.getText().toString() ;
	    				String tmp_sign = txtSetPrvkey.getText().toString() ;
	    				
	            		JSONObject new_key=new JSONObject();
	            		new_key.put("original",original);
	            		new_key.put("sign",tmp_sign);
	            		new_key.put("algo",tmp_sign_algo);
	            		new_key.put("pub_key",tmp_pub_key);
						
	            	
		            	// Copy the Text to the clipboard
		                ClipboardManager manager = 
		                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		                manager.setText( new_key.toString() );
		                
		                // Show a message:
		                Toast.makeText(v.getContext(), "已复制到剪贴板",Toast.LENGTH_SHORT)
		                     .show();
	            	} catch (JSONException e) {
	            		Toast.makeText(v.getContext(), "复制失败",Toast.LENGTH_SHORT)
	                     .show();
					}
	            }
	        });
			
			( (Button)DialogView.findViewById(R.id.setkey_btnPastePrvkey ) ).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	// get the Text from the clipboard
	                ClipboardManager manager = 
	                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	                CharSequence tmp_clip = manager.getText();
	                if(tmp_clip!=null) {
		                try {
		                	txtSetPrvkey.setText( tmp_clip.toString() );
		                	//String new_key_json=tmp_clip.toString() ;
		                	//JSONObject new_key=new JSONObject(new_key_json);
			                
			                //txtSetPrvkey.setText( new_key.optString("sign","") );
			                //txtSetPubkey.setText( new_key.optString("original","")  );
			                
			                // Show a message:
			                Toast.makeText(v.getContext(), "已粘贴成功",Toast.LENGTH_SHORT)
			                     .show();
		                }catch(Exception e) {
		                	txtSetPrvkey.setText( "" );
			                txtSetPubkey.setText( "" );
		                	Toast.makeText(v.getContext(), "不合法的签名",Toast.LENGTH_SHORT)
		                     .show();
		                }
		             }
	            }
	        });
		}
    }
    
    @JavascriptInterface
    public void verifySign(final String data_hex,final String pub_key,final String sign,final String sign_algo,final String callback_function){
    	//Log.d("browser", "verifySign " + data_hex+","+pub_key +"," + sign  +"," + sign_algo  +","  +callback_function);
    	/*
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View DialogView = factory.inflate(R.layout.dialog_setkey , null);
    	
		dialog = new AlertDialog.Builder(this)
		    .setTitle("测试签名")
		    .setView(DialogView)//设置自定义对话框的样式
			.setNegativeButton("取消", cancelButtonClickListener)
			.setPositiveButton("确定", null)
			.create();

		TextView lblAlgoType = (TextView)DialogView.findViewById(R.id.setkey_algo_type ); 
		TextView lblPubKey = (TextView)DialogView.findViewById(R.id.setkey_pubkey_label ); 
		TextView lblPrvKey = (TextView)DialogView.findViewById(R.id.setkey_prvkey_label ); 
		lblAlgoType.setText("签名验证算法："+sign_algo) ;
		lblPubKey.setText("原文内容：") ;
		lblPrvKey.setText("待验证签名：") ;
		
		EditText txtSetPubkey = (EditText)DialogView.findViewById(R.id.setkey_pubkey ); 
		EditText txtSetPrvkey = (EditText)DialogView.findViewById(R.id.setkey_prvkey ); 

		txtSetPubkey.setText( new String(Util.hexStringToBytes(data_hex)) );
		//txtSetPrvkey.setText("Verifying\n" + data_hex+"\n"+pub_key +"\n" + sign  +"\n" + sign_algo  +"\n"  +callback_function);
		txtSetPrvkey.setText( sign );
		
		dialog.show();
		
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
            	//处理确认按钮的点击事件
            	dialog.dismiss();
            	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_VERIFY_SIGN ).execute(data_hex,pub_key,sign,sign_algo,callback_function);
            }
        });
		
    	*/
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_VERIFY_SIGN ).execute(data_hex,pub_key,sign,sign_algo,callback_function);
    }
    
    @JavascriptInterface
    public void getPPkResource(final String input_uri,final String resp_type,final String callback_function){
    	Log.d("browser", "getPPkResource " + input_uri+","+callback_function);
    	
    	final String format_ppk_uri=ODIN.formatPPkURI(input_uri);
    	if( format_ppk_uri ==null ){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported uri: "+input_uri
    			);
    		return;
    	}
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_PPK_RESOURCE ).execute(format_ppk_uri,resp_type,callback_function);
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
    	if(destURI==null)
    		return;
    	
    	destURI=destURI.trim();
    	
    	//处理完善destURI，自动补上缺少的 ppk:或http:前缀
    	if(!destURI.contains(":")) {
    		if(destURI.contains(".") && !Util.isNumeric(destURI) )
    			destURI = "http://"+destURI;
    	    else
    		    destURI = "ppk:"+destURI;
    	}
    	
        weburl.setText(destURI);
        weburl.setTextColor(Color.BLACK);
        textStatus.setText("Go to "+destURI);
        Log.d("browser", "Go to " + destURI);
        
        if (destURI != null){
        	final String format_ppk_uri=ODIN.formatPPkURI(destURI);
        	if( format_ppk_uri !=null ){
                new ShowPPkUriAsyncTask().execute(format_ppk_uri);
            }else if(destURI.equalsIgnoreCase(Config.ppkSettingPage )) { 
        		destURI=Config.ppkSettingPageFileURI;
        		webshow.getSettings().setJavaScriptEnabled(true);
        		webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
        		webshow.loadUrl(destURI);
            }else {
            	if(destURI.toLowerCase().startsWith( "http:" )
            			|| destURI.toLowerCase().startsWith( "https:" )) {
            		//普通网页允许执行JS
            		webshow.getSettings().setJavaScriptEnabled(true);  
            		webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
            		
            		webshow.loadUrl(destURI);
            	}else if(destURI.toLowerCase().startsWith( "file:" )) {
	            	if(destURI.equalsIgnoreCase( Config.ppkSettingPageFileURI )) { //安全起见，对特定的file:起始网址才允许JS
	            		webshow.getSettings().setJavaScriptEnabled(true);
	            		webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
	            	}else {
	            		webshow.getSettings().setJavaScriptEnabled(false);
	            	}
	            	
	            	webshow.loadUrl(destURI);
            	}else {
            		//安全起见，非指定网页类型不允许JS和访问
            		webshow.getSettings().setJavaScriptEnabled(false); 
            		String str_info =  destURI.replace("<", "&lt;").replace(">", "&gt;"); //过滤 html敏感字符后显示网址作为正文
                    webshow.loadDataWithBaseURL(null, str_info, "text/html", "utf-8", null);
            	}
            }
        }
    }
    
    public boolean gotoBack( ){
        if(!webshow.canGoBack()){
        	return false;
            
        }
        
        WebBackForwardList mWebBackForwardList = webshow.copyBackForwardList();
        WebHistoryItem item= mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()-1);
	    String historyUrl = item.getUrl();
	    
        webshow.goBack();
        textStatus.setText("Back to "+historyUrl);
        weburl.setText(historyUrl);
        weburl.setTextColor(Color.BLACK);

	    return true;
    }
    
    public boolean gotoForward( ){
        if(!webshow.canGoForward()){
        	return false;
        }
        
        WebBackForwardList mWebBackForwardList = webshow.copyBackForwardList();
        WebHistoryItem item= mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()+1);
	    String forwardUrl = item.getUrl();
	    
        webshow.goForward();
        textStatus.setText("Forward to "+forwardUrl);
        weburl.setText(forwardUrl);
        weburl.setTextColor(Color.BLACK);

	    return true;
    }
    
    public WebResourceResponse getPPkContent(String ppk_uri){
        Log.d("browser","getPPkContent: " + ppk_uri);
        WebResourceResponse res = null;
        JSONObject obj_ap_resp=PTTP.getPPkResource(ppk_uri);
        
        byte[] result_bytes=null;
        String mimeType=null;
        String encoding=Config.PPK_TEXT_CHARSET;
        
        if(obj_ap_resp==null) {
        	result_bytes="ERROR.Please reload.请刷新重试".getBytes();
        	mimeType="text/html";
        }else {
	        result_bytes=(byte[])obj_ap_resp.opt(Config.JSON_KEY_CHUNK_BYTES);
	        mimeType=obj_ap_resp.optString(Config.JSON_KEY_CHUNK_TYPE);
        }
        
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
            gotoBack();
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
			if(this.js_callback_function==null)
				return;
			
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
        if (url != null) { 
        	final String format_ppk_uri=ODIN.formatPPkURI(url);
        	if( format_ppk_uri !=null ){
	            PPkActivity.this.bLoadingHttpPage=false;
	            
	            view.stopLoading();
	            
	            new ShowPPkUriAsyncTask().execute(url);
	            
	            return true;
        	}else if(url.toLowerCase().startsWith( "file:" )) {
            	if(!url.equalsIgnoreCase( Config.ppkSettingPageFileURI )) { //安全起见，对不是特定的file:起始网址将中断访问，只显示空白页面
            		PPkActivity.this.bLoadingHttpPage=false;
    	            
    	            view.stopLoading();
    	            
    	            new ShowPPkUriAsyncTask().execute("about:blank");
            		return true;
            	}
            }
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
        final String format_ppk_uri=ODIN.formatPPkURI(url);
    	if( format_ppk_uri !=null ){
            return getPPkContent(url);
        }else{
            return super.shouldInterceptRequest(view,url);
        }
      }


      @Override
      public void onPageFinished(WebView view, String url) {
        //textStatus.setText("Finished");
        view.getSettings().setJavaScriptEnabled(true);
        view.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
        
        String show_url=view.getUrl();
        if(show_url.equalsIgnoreCase( Config.ppkSettingPageFileURI ) ) {
        	show_url=Config.ppkSettingPage;
        	weburl.setText(show_url);
            weburl.setTextColor(Color.BLACK);
    	}

        super.onPageFinished(view, url);
        //PPkActivity.this.setTitle(view.getTitle()); 
        //printBackForwardList();
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
            
            JSONObject obj_ap_resp=PTTP.getPPkResource(this.ppkURI);
            return obj_ap_resp;

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            String result_uri=this.ppkURI;
            String str_more_info="";
            String from_ap_url="";
            String result_content="<h3>ERROR while loading PPk URI. <br>访问PPk网址时出错了<br>URI:  "+ppkURI+"</h3>";;
            String mimeType="text/html";
            String encoding="utf-8";

            super.onPostExecute(result);
            
            if(result!=null){
	            try {
	            	int validcode=result.optInt(Config.JSON_KEY_PPK_VALIDATION,Config.PTTP_VALIDATION_ERROR);
	                if( validcode == Config.PTTP_VALIDATION_IGNORED 
	                  || validcode == Config.PTTP_VALIDATION_OK ){
	                    result_uri=result.optString(Config.JSON_KEY_PPK_URI,"");
	                    from_ap_url=result.optString(Config.JSON_KEY_CHUNK_URL,"");
	                    mimeType=result.optString(Config.JSON_KEY_CHUNK_TYPE,mimeType);
	                    int status_code = result.optInt(Config.PTTP_KEY_STATUS_CODE);
	                    if(status_code==Config.PTTP_STATUS_CODE_OK){
	                        if( mimeType.toLowerCase().startsWith("text") ){
	                            result_content=new String((byte[])result.opt(Config.JSON_KEY_CHUNK_BYTES),Config.PPK_TEXT_CHARSET);
	                        }else if( mimeType.toLowerCase().startsWith("image") ){
	                            String image64 = Base64.encodeToString((byte[])result.opt(Config.JSON_KEY_CHUNK_BYTES), Base64.DEFAULT);
	                            result_content = "<img src=\"data:"+mimeType+";base64," + image64 + "\" />";
	                            mimeType = "text/html";
	                        }else{
	                            //result_content=new String((byte[])result.opt(Config.JSON_KEY_CHUNK_BYTES),Config.BINARY_DATA_CHARSET);
	                            mimeType = "text/html";
	                            result_content =  "Not supported content_type: "+result.optString(Config.JSON_KEY_CHUNK_TYPE,"");
	                        }
	                    }else if(status_code==301 || status_code==302){
	                        String dest_url = new String( (byte[])result.opt(Config.JSON_KEY_CHUNK_BYTES) );
	                        mimeType = "text/html";
	                        result_content="<html><head><meta http-equiv='refresh' content='2;url="+dest_url+"'></head>Redirecting to "+dest_url+"<html>";
	                    }else{
	                        mimeType = "text/html";
	                        result_content = status_code+" "+(new String( (byte[])result.opt(Config.JSON_KEY_CHUNK_BYTES) ));
	                    }
	                    
	                    if(validcode == Config.PTTP_VALIDATION_OK)
	                    	weburl.setTextColor( Color.rgb(34,139,34) );                   	
	                }else{
	                    mimeType = "text/html";
	                	result_content += "<font color='#F00'>Valiade failed! 收到签名不一致的内容数据块!</font>";
	                }
	                
	                if(result.optBoolean(Config.JSON_KEY_FROM_CACHE,false)) {
	                	str_more_info = "CacheOf: "+ from_ap_url;
	                	str_more_info = "CacheFile: "+ result.optString("debug_cache_file_name");
	                }else {
	                	str_more_info = "AP: "+ from_ap_url;
	                }
	                
	                if(Config.debugKey!=0) {
	                	long exp_utc = result.optLong(Config.JSON_KEY_EXP_UTC);
	                	str_more_info += "\nDEBUG: VALIDCODE="+validcode
	                            +" EXP_UTC="+ exp_utc
	                            +" LEFT_SECONDS="+ ( exp_utc - Util.getNowTimestamp() );
	                }
	                
	                //setTitle(result_uri); //在标题栏临时显示当前实际访问的URI地址
	                
	                //String page_title=Util.getPageTitle(result_uri,result_content);
	                //historylist.addURL(page_title,result_uri);
	            } catch (Exception e) {
	                Log.d("browser"," exception:"+e.toString());
	                result_content = "Exception:"+e.toString();
	            }
            }
            
            textStatus.setText("Loaded PTTP: "+result_uri + "\n"+str_more_info);
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