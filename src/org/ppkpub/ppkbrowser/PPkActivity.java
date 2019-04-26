package org.ppkpub.ppkbrowser;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceResponse;
//import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import net.i2p.crypto.eddsa.math.GroupElement;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
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
    private ImageButton buttonBack;
    private ImageButton buttonForward;
    private ImageButton buttonStop;
    private ImageButton buttonRefresh;
    private ImageButton buttonHome;
    private ImageButton buttonSetting;
    private ProgressBar progressBar;
    private boolean     bLoadingHttpPage;
    
    private ImageButton buttonHongBao; //�����ر��
    
    private TextView    textStatus;

    
    //private OdinTransctionData    	objOdinTransctionData; //�ݴ�¶�����ؽ������ݣ�����JS��Android��Ľ���
    
    // ����һ��������������ʷ��¼
    //private BrowserPageList historylist = new BrowserPageList();
    // ����һ��������������ǩ��¼
    //BrowserPageList bookmarklist = new BrowserPageList();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //��ʼ��
        Config.init(this);
        BitcoinWallet.init(this);
        Odin.init(this);
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
                Log.d("browser","�Ѿ�����ض�");
                //Toast.makeText(webshow.getContext(), "�Ѿ�����׶�" , 0).show();
            }

            @Override
            public void onPageTop(int l, int t, int oldl, int oldt) {
                Log.d("browser","�Ѿ����ﶥ��");
                textStatus.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
            	textStatus.setVisibility(View.GONE);
            	/*
            	int dy=t-oldt;
            	if(dy>10) {//���¹���һ������
            		
            	}else if(dy<-10) { //���Ϲ���һ������
            		
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
        
        //gotoURI( Config.ppkDefaultHomepage );
        gotoURI("ppk:joy/"); //�������ر��
        
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
        
        buttonHongBao = (ImageButton) findViewById(R.id.buttonHongBao);
        buttonHongBao.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            	gotoURI("ppk:joy");
            }
        });
        
        //��鱾�����ݱ�����������״̬
        if(!Config.isWalletPasswordProtected()) {
        	setLocalDataProtectedPassword();
        }
    }
    
    public WebView getCurrentWebview() {
    	return webshow;
    }
    
    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebChromeClient.onActivityResult(requestCode, resultCode,intent);
    }
    
    //���� WebView ���ڴ�й¶����
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
			    .setTitle("��ʾ")
				.setNegativeButton("�ر�", cancelButtonClickListener)
				.setMessage("��Ч�İ¶�����Ϣ���ݣ�")
				.create();
    	}else{
    		BigDecimal fee_btc = new BigDecimal(objOdinTransctionData.fee_satoshi.longValue()).divide(new BigDecimal(Config.btc_unit));
			dialog = new AlertDialog.Builder(this)
			    .setTitle("ȷ�Ϸ����������رҽ�����?")
				.setNegativeButton("ȡ��", cancelButtonClickListener)
				.setPositiveButton("ȷ��", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//����ȷ�ϰ�ť�ĵ���¼�
						new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_SIGNED_ODIN_BTC_TX  ).execute(odin_data_json_hex,callback_function);
					}
				})
				.setMessage("ȷ�Ϸ��Ͱ¶��Ŷ�Ӧ������?"
				             +"\n���͵�ַ��\n"+objOdinTransctionData.source
    						 +"\n�󹤷��ã�"+fee_btc+" BTC"
    						 +"\n��ע��ñ��رҽ���һ�����������޷�������")
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
    			    .setTitle("��ʾ")
    				.setNegativeButton("�ر�", cancelButtonClickListener)
    				.setMessage("��Ч�Ľ������ݣ�")
    				.create();
        	}else{
    			dialog = new AlertDialog.Builder(this)
    			    .setTitle("ȷ�Ϸ��ͱ��رҽ�����?")
    				.setNegativeButton("ȡ��", cancelButtonClickListener)
    				.setPositiveButton("ȷ��", new OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						//����ȷ�ϰ�ť�ĵ���¼�
    						new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_SIGNED_TX  ).execute(coin_name,tx_argus_json_hex,callback_function);
    					}
    				})
    				.setMessage("ȷ�Ϸ����������رҽ�����?"
    				         +"\n���͵�ַ��\n"+source
    						 +"\nĿ���ַ��\n"+destination
    						 +"\n���׽�"+amount_btc+" BTC"
    						 +"\n�󹤷��ã�"+fee_btc+" BTC"
    						 +"\n��ע��ñ��رҽ���һ�����������޷�������")
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
				    .setTitle("��ʾ")
					.setNegativeButton("�ر�", cancelButtonClickListener)
					.setMessage("��Ч��������:"+set_name)
					.create();
	    		
			dialog.show();
		}else {
			LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_input_item , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("����ȱʡ����")
			    .setView(DialogView)//�����Զ���Ի������ʽ
				.setNegativeButton("ȡ��", cancelButtonClickListener)
				.setPositiveButton("ȷ��", null)
				.create();

			final EditText txtSetName = (EditText)DialogView.findViewById(R.id.input_item_name ); 
			final EditText txtSetValue = (EditText)DialogView.findViewById(R.id.input_item_value ); 
			txtSetName.setText( set_name );
			txtSetValue.setText( set_value );

			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//����ȷ�ϰ�ť�ĵ���¼�
	            	String result_set_name = txtSetName.getText().toString() ;
	            	String result_set_value = txtSetValue.getText().toString() ;
	            	if(Config.saveUserDefinedSet( result_set_name, result_set_value)) {
		            	dialog.dismiss();
		            	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_DEFAULT_SETTING ).execute(result_set_name,callback_function);
	            	}else {
	            		Toast.makeText(v.getContext(), "�������ó�����������ԣ�",Toast.LENGTH_SHORT)
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
		                Toast.makeText(v.getContext(), "�Ѹ��Ƶ�������",Toast.LENGTH_SHORT)
		                     .show();
	            	} catch (Exception e) {
	            		Toast.makeText(v.getContext(), "����ʧ��",Toast.LENGTH_SHORT)
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
			                Toast.makeText(v.getContext(), "��ճ���ɹ�",Toast.LENGTH_SHORT)
			                     .show();
		                }catch(Exception e) {
		                	Toast.makeText(v.getContext(), "��Ч��ճ������",Toast.LENGTH_SHORT)
		                     .show();
		                }
		             }
	            }
	        });
		}
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
    	
    	if(!"BITCOIN".equalsIgnoreCase(coin_name)){
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
		    .setTitle("��ѡ��Ҫʹ�õĵ�ַ")
		    .setView(DialogView)//�����Զ���Ի������ʽ
			.setNegativeButton("ȡ��", cancelButtonClickListener)
			.setPositiveButton("ȷ��", null)
			.create();
		
		final EditText txtSelectedAddress = (EditText)DialogView.findViewById(R.id.dsa_txtSelectedAddress ); 

		final ListView listviewAddress = (ListView)DialogView.findViewById(R.id.dsa_listViewAddress ); 
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			    this,   // Context������                 
			    android.R.layout.simple_list_item_1,  // �����id
			    address_label_list);
		
		listviewAddress.setAdapter(adapter);
		listviewAddress.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				//Toast.makeText(webshow.getContext(), "����˵�" + position + "������:"+address_list.get(position) , 0).show();
				txtSelectedAddress.setText(address_list.get(position));
			}
        });
		if (address_list.size() > 5){
            ViewGroup.LayoutParams layoutParams = listviewAddress.getLayoutParams();
            layoutParams.height = 5 * 80;    //�����б���ʾ�߶�
            listviewAddress.setLayoutParams(layoutParams);
        }
		
		dialog.show();
		
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//����ȷ�ϰ�ť�ĵ���¼�
	            	String selected_address=txtSelectedAddress.getText().toString()  ;
	            	if(selected_address.length()==0){
	            		TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.dsa_warningMessage ); 
				    	txtWarningMessage.setText("����б��е��ѡ��Ҫʹ�õĵ�ַ��") ;
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
                Toast.makeText(v.getContext(), "�Ѹ��Ƶ�������",Toast.LENGTH_SHORT)
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
    	}else if("BITCOIN".equalsIgnoreCase(coin_name)){
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
    	
    	//��鱾�����ݱ�����������״̬
        if(!Config.isWalletPasswordProtected()) {
        	setLocalDataProtectedPassword();
        	return;
        }	
        
    	final AlertDialog dialog;

		LayoutInflater factory = LayoutInflater.from(this);
    	final View DialogView = factory.inflate(R.layout.dialog_backup_data , null);
    	
		dialog = new AlertDialog.Builder(this)
		    .setTitle("���ݻ�ָ����ݣ������������")
		    .setView(DialogView)//�����Զ���Ի������ʽ
			.setNegativeButton("�ر�", null)
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
            		txtWarningMessage.setText("��������ȷ�����룬Ȼ�����ԣ�") ;
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
            		Toast.makeText(v.getContext(), "�ѻָ����",Toast.LENGTH_SHORT)
            			.show();
            		dialog.dismiss();
            		PeerWebAsyncTask.callbackBeforeExceute(
            				webshow,
            				callback_function,
            				PeerWebAsyncTask.STATUS_OK,
            				""
            			);
            	}else {
            		txtWarningMessage.setText("��������ȷ�����룬Ȼ�����ԣ�") ;
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
	                Toast.makeText(v.getContext(), "�Ѹ��Ƶ�������",Toast.LENGTH_SHORT)
	                     .show();
            	} catch (Exception e) {
            		Toast.makeText(v.getContext(), "����ʧ��",Toast.LENGTH_SHORT)
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
		                Toast.makeText(v.getContext(), "��ճ���ɹ�",Toast.LENGTH_SHORT)
		                     .show();
	                }catch(Exception e) {
	                	Toast.makeText(v.getContext(), "��Ч��ճ������",Toast.LENGTH_SHORT)
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
			    .setTitle("��ʾ")
				.setNegativeButton("�ر�", cancelButtonClickListener)
				.setMessage("���ݱ������������ã������޸ģ�")
				.create();
    		
    		dialog.show();
    	}else{
    		LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_set_local_password  , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("�°汾������ʾ")
			    .setView(DialogView)//�����Զ���Ի������ʽ
				.setPositiveButton("ȷ��", null)
				.create();

			final EditText txtNewLocalPassword = (EditText)DialogView.findViewById(R.id.set_local_password_new ); 
			final TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.set_local_password_warningMessage ); 
			txtWarningMessage.setTextColor(Color.RED);
			
			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View v){
		            	//����ȷ�ϰ�ť�ĵ���¼�
		            	String strNewPassword=txtNewLocalPassword.getText().toString();
		            	
		            	if(strNewPassword.trim().length()==0) {
		            		txtWarningMessage.setText("��������Ч�����루����ֻʹ��Ӣ����ĸ�����֣���Ȼ��ȷ�ϱ��棡") ;
		            	}else {
							CheckBox chkRemeber = (CheckBox)DialogView.findViewById(R.id.set_local_password_checkRemeber );
						    if(chkRemeber.isChecked() ){
						    	if(Config.setWalletProtectPassword( strNewPassword.getBytes() )) {
						    		dialog.dismiss();
						    	}else {
						    		txtWarningMessage.setText("�����������\n��ȷ��������Ч�����루����ֻʹ��Ӣ����ĸ�����֣���Ȼ�����ԣ�") ;
						    	}
						    }else{
						    	txtWarningMessage.setText("��ȷ��������������󲢼Ǻã�Ȼ��ѡȷ�ϣ�") ;
						    }
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
			    .setTitle("��ʾ")
				.setNegativeButton("�ر�", cancelButtonClickListener)
				.setMessage("��Ч�ı��رҵ�ַ:"+address+"\n��ʹ����5,L��K��ʼ��˽Կ�ַ�����")
				.create();
    		
    		dialog.show();
    	}else{
    		LayoutInflater factory = LayoutInflater.from(this);
        	final View DialogView = factory.inflate(R.layout.dialog_import_prvkey , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("ȷ��ʹ���µ�ַ��?")
			    .setMessage("��ַ��\n"+address)
			    .setView(DialogView)//�����Զ���Ի������ʽ
				.setNegativeButton("ȡ��", cancelButtonClickListener)
				.setPositiveButton("ȷ��", null)
				.create();

			EditText txtImportPrvkey = (EditText)DialogView.findViewById(R.id.import_prvkey ); 
			txtImportPrvkey.setText(prv_key);
			
			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View v){
		            	//����ȷ�ϰ�ť�ĵ���¼�
						CheckBox chkBackupedPrvkey = (CheckBox)DialogView.findViewById(R.id.import_checkBoxBackupedPrvkey );
					    if(chkBackupedPrvkey.isChecked() ){
					    	dialog.dismiss();
					    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_IMPORT_PRIVATE_KEY ).execute(coin_name,prv_key,callback_function);
					    }else{
					    	TextView txtWarningMessage = (TextView)DialogView.findViewById(R.id.import_warningMessage ); 
					    	txtWarningMessage.setText("�븴�Ʊ��ݺ�����˽Կ��Ȼ��ѡȷ�ϣ�") ;
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
	                Toast.makeText(v.getContext(), "�Ѹ��Ƶ�������",Toast.LENGTH_SHORT)
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
    public void changeAnotherODIN(final String new_odin_uri,final String callback_function){
    	Log.d("browser", "changeAnotherODIN " + new_odin_uri+","+callback_function);
    	
    	if( ! PPkURI.isValidPPkURI(new_odin_uri) ){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported new_odin_uri:"+new_odin_uri
    			);
    		return;
    	}
    	
    	/*�����ƣ����ָ��odin��ӵ��Ȩ
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
		    .setTitle("��ȷ���л�ʹ���µİ¶���")
		    .setMessage("��ȷ��ʹ������İ¶�����Ϊ�û����\n  "+new_odin_uri)
			.setNegativeButton("ȡ��", cancelButtonClickListener)
			.setPositiveButton("ȷ��", null)
			.create();
		
		dialog.show();
		
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//����ȷ�ϰ�ť�ĵ���¼�
	            	Odin.setDefaultOdinURI(new_odin_uri);
				    dialog.dismiss();
				    Toast.makeText(v.getContext(), "OK,�û���ݱ�ʶ����Ϊ "+new_odin_uri, Toast.LENGTH_SHORT).show();
				    new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_CHANGE_ANOTHER_ODIN ).execute(new_odin_uri,callback_function);
				    
	            }
	        });

    }
    
    @JavascriptInterface
    public void getPPkResourcePubkey(final String ppk_uri,final String callback_function){
    	Log.d("browser", "getPPkResourcePubkey " + ppk_uri+","+callback_function);
    	
    	if( ! PPkURI.isValidPPkURI(ppk_uri)  ){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported uri: "+ppk_uri
    			);
    		return;
    	}
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_PPK_RESOURCE_PUBKEY ).execute(ppk_uri,callback_function);
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
    public void setPPkResourceKey(final String ppk_uri,final String callback_function){
    	Log.d("browser", "setPPkResourceKey " + ppk_uri +","+callback_function);
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
    	if( !PPkURI.isValidPPkURI(ppk_uri) ){
    		dialog = new AlertDialog.Builder(this)
			    .setTitle("��ʾ")
				.setNegativeButton("�ر�", cancelButtonClickListener)
				.setMessage("��Ч����Դ��ַ:"+ppk_uri+"\n���� "+Config.PPK_URI_PREFIX+" �� "+Config.DIDPPK_URI_PREFIX+"  ��ʼ�� ")
				.create(); 
    		
    		dialog.show();
    	}else{
    		JSONObject obj_key = ResourceKey.getKey(ppk_uri,true);
    		if(obj_key==null) {
    			dialog = new AlertDialog.Builder(this)
    				    .setTitle("��ʾ")
    					.setNegativeButton("�ر�", cancelButtonClickListener)
    					.setMessage("�޷�������Ч��Կ��\n��Ӧ��ַ:"+ppk_uri)
    					.create();
    	    		
    			dialog.show();
    			
    		}else {
	    		LayoutInflater factory = LayoutInflater.from(this);
	        	final View DialogView = factory.inflate(R.layout.dialog_setkey , null);
	        	
				dialog = new AlertDialog.Builder(this)
				    .setTitle("�鿴/���ñ�ʶ��֤��Կ")
				    .setMessage("��ʶ��"+ppk_uri)
				    .setView(DialogView)//�����Զ���Ի������ʽ
					.setNegativeButton("ȡ��", cancelButtonClickListener)
					.setPositiveButton("ȷ��", null)
					.create();
	
				final EditText txtSetPubkey = (EditText)DialogView.findViewById(R.id.setkey_pubkey ); 
				final EditText txtSetPrvkey = (EditText)DialogView.findViewById(R.id.setkey_prvkey ); 

				txtSetPubkey.setText( obj_key.optString(ResourceKey.PUBLIC_KEY , "") );
				txtSetPrvkey.setText( obj_key.optString(ResourceKey.PRIVATE_KEY , "") );
				
				dialog.show();
				
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View v){
		            	//����ȷ�ϰ�ť�ĵ���¼�
		            	//������Կ
		            	String pub_key = txtSetPubkey.getText().toString() ;
	    				String prv_key = txtSetPrvkey.getText().toString() ;

		            	ResourceKey.saveKey(ppk_uri, prv_key,pub_key,ResourceKey.DEFAULT_ALGO_TYPE_RSA);
		            	
		            	dialog.dismiss();
				    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_SET_PPK_RESOURCE_KEY ).execute(ppk_uri,pub_key,callback_function);
		            }
		        });
				
				( (Button)DialogView.findViewById(R.id.setkey_btnCopyPrvkey ) ).setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View v){
		            	try {
		    				String pub_key = txtSetPubkey.getText().toString() ;
		    				String prv_key = txtSetPrvkey.getText().toString() ;
		    				
		            		JSONObject new_key=new JSONObject();
		            		new_key.put("ppk_uri",ppk_uri);
		            		new_key.put("RSAPublicKey",pub_key);
							new_key.put("RSAPrivateKey",prv_key);
		            	
			            	// Copy the Text to the clipboard
			                ClipboardManager manager = 
			                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			                manager.setText( new_key.toString() );
			                
			                // Show a message:
			                Toast.makeText(v.getContext(), "�Ѹ��Ƶ�������",Toast.LENGTH_SHORT)
			                     .show();
		            	} catch (JSONException e) {
		            		Toast.makeText(v.getContext(), "����ʧ��",Toast.LENGTH_SHORT)
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
				                Toast.makeText(v.getContext(), "��ճ���ɹ�",Toast.LENGTH_SHORT)
				                     .show();
			                }catch(Exception e) {
			                	txtSetPrvkey.setText( "" );
				                txtSetPubkey.setText( "" );
			                	Toast.makeText(v.getContext(), "���Ϸ�����Կ",Toast.LENGTH_SHORT)
			                     .show();
			                }
			             }
		            }
		        });
    		}
    	}
    }
    
    @JavascriptInterface
    public void signWithPPkResourcePrvKey(final String ppk_uri,final String requester_uri,final String data_hex,final String callback_function){
    	//Log.d("browser", "signWithPPkResourcePrvKey " + ppk_uri +"," + requester_uri +"," +callback_function);
    	
    	final AlertDialog dialog;
    	DefaultCancelButtonClickListener cancelButtonClickListener=new DefaultCancelButtonClickListener(callback_function);
    	
    	JSONObject obj_key = ResourceKey.getKey(ppk_uri,false);
		if(obj_key==null) {
			dialog = new AlertDialog.Builder(this)
				    .setTitle("��ʾ")
					.setNegativeButton("�ر�", cancelButtonClickListener)
					.setMessage("��δ������Ч��Կ��\n��Ӧ��ַ:"+ppk_uri)
					.create();
	    		
			dialog.show();
		}else {
			final String tmp_private_key= obj_key.optString(ResourceKey.PRIVATE_KEY , "");
			final String tmp_pub_key= obj_key.optString(ResourceKey.PUBLIC_KEY , "");
			final String tmp_sign_algo=RSACoder.DEFAULT_SIGNATURE_ALGORITHM ;
			String tmp_sign;
			try {
				tmp_sign=RSACoder.sign(
						Util.hexStringToBytes(data_hex) , tmp_private_key,tmp_sign_algo
				     );

				//Log.d("browser", "signPPkResource.verifySign " + data_hex+","+tmp_pub_key +"," + tmp_sign  +"," + tmp_sign_algo  +","  +callback_function);
				if( !RSACoder.verify(Util.hexStringToBytes(data_hex)  , tmp_pub_key, tmp_sign,tmp_sign_algo) ) {
					throw new Exception("Self-check sign failed!");
				}
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
        	final View DialogView = factory.inflate(R.layout.dialog_setkey , null);
        	
			dialog = new AlertDialog.Builder(this)
			    .setTitle("����Դ��ʶ����ǩ��")
			    .setMessage("��Դ��ַ��\n"+ppk_uri)
			    .setView(DialogView)//�����Զ���Ի������ʽ
				.setNegativeButton("ȡ��", cancelButtonClickListener)
				.setPositiveButton("ȷ��", null)
				.create();

			final TextView lblAlgoType = (TextView)DialogView.findViewById(R.id.setkey_algo_type ); 
			TextView lblPubKey = (TextView)DialogView.findViewById(R.id.setkey_pubkey_label ); 
			TextView lblPrvKey = (TextView)DialogView.findViewById(R.id.setkey_prvkey_label ); 
			lblAlgoType.setText("ǩ���㷨��"+tmp_sign_algo) ;
			lblPubKey.setText("ԭ�����ݣ�") ;
			lblPrvKey.setText("���ɵ�ǩ����") ;

			final EditText txtSetPubkey = (EditText)DialogView.findViewById(R.id.setkey_pubkey ); 
			final EditText txtSetPrvkey = (EditText)DialogView.findViewById(R.id.setkey_prvkey ); 
			txtSetPubkey.setText( new String(Util.hexStringToBytes(data_hex)) );
			txtSetPrvkey.setText( tmp_sign );

			dialog.show();
			
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v){
	            	//����ȷ�ϰ�ť�ĵ���¼�
	            	String result_sign = txtSetPrvkey.getText().toString() ;
	            	if(result_sign.length()>0) {
		            	dialog.dismiss();
				    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_SIGN_WITH_PPK_RESOURCE_PRVKEY ).execute(ppk_uri,result_sign,tmp_sign_algo,callback_function);
	            	}else {
	            		Toast.makeText(v.getContext(), "ǩ����Ч����������ԣ�",Toast.LENGTH_SHORT)
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
		                Toast.makeText(v.getContext(), "�Ѹ��Ƶ�������",Toast.LENGTH_SHORT)
		                     .show();
	            	} catch (JSONException e) {
	            		Toast.makeText(v.getContext(), "����ʧ��",Toast.LENGTH_SHORT)
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
			                Toast.makeText(v.getContext(), "��ճ���ɹ�",Toast.LENGTH_SHORT)
			                     .show();
		                }catch(Exception e) {
		                	txtSetPrvkey.setText( "" );
			                txtSetPubkey.setText( "" );
		                	Toast.makeText(v.getContext(), "���Ϸ���ǩ��",Toast.LENGTH_SHORT)
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
		    .setTitle("����ǩ��")
		    .setView(DialogView)//�����Զ���Ի������ʽ
			.setNegativeButton("ȡ��", cancelButtonClickListener)
			.setPositiveButton("ȷ��", null)
			.create();

		TextView lblAlgoType = (TextView)DialogView.findViewById(R.id.setkey_algo_type ); 
		TextView lblPubKey = (TextView)DialogView.findViewById(R.id.setkey_pubkey_label ); 
		TextView lblPrvKey = (TextView)DialogView.findViewById(R.id.setkey_prvkey_label ); 
		lblAlgoType.setText("ǩ����֤�㷨��"+sign_algo) ;
		lblPubKey.setText("ԭ�����ݣ�") ;
		lblPrvKey.setText("����֤ǩ����") ;
		
		EditText txtSetPubkey = (EditText)DialogView.findViewById(R.id.setkey_pubkey ); 
		EditText txtSetPrvkey = (EditText)DialogView.findViewById(R.id.setkey_prvkey ); 

		txtSetPubkey.setText( new String(Util.hexStringToBytes(data_hex)) );
		//txtSetPrvkey.setText("Verifying\n" + data_hex+"\n"+pub_key +"\n" + sign  +"\n" + sign_algo  +"\n"  +callback_function);
		txtSetPrvkey.setText( sign );
		
		dialog.show();
		
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
            	//����ȷ�ϰ�ť�ĵ���¼�
            	dialog.dismiss();
            	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_VERIFY_SIGN ).execute(data_hex,pub_key,sign,sign_algo,callback_function);
            }
        });
		
    	*/
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_VERIFY_SIGN ).execute(data_hex,pub_key,sign,sign_algo,callback_function);
    }
    
    @JavascriptInterface
    public void getPPkResource(final String ppk_uri,final String resp_type,final String callback_function){
    	Log.d("browser", "getPPkResource " + ppk_uri+","+callback_function);
    	
    	if( ! PPkURI.isValidPPkURI(ppk_uri)  ){
    		PeerWebAsyncTask.callbackBeforeExceute(
    				this.webshow,
    				callback_function,
    				PeerWebAsyncTask.STATUS_INVALID_ARGU,
    				"Not supported uri: "+ppk_uri
    			);
    		return;
    	}
    	
    	new PeerWebAsyncTask( webshow,PeerWebAsyncTask.TASK_NAME_GET_PPK_RESOURCE ).execute(ppk_uri,resp_type,callback_function);
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
    	destURI=destURI.trim();
        weburl.setText(destURI);
        weburl.setTextColor(Color.BLACK);
        textStatus.setText("Go to "+destURI);
        Log.d("browser", "Go to " + destURI);
        
        if (destURI != null){
            if( PPkURI.isValidPPkURI(destURI) ) {
                new ShowPPkUriAsyncTask().execute(destURI);
            }else if(destURI.equalsIgnoreCase(Config.ppkSettingPage )) { 
        		destURI=Config.ppkSettingPageFileURI;
        		webshow.getSettings().setJavaScriptEnabled(true);
        		webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
        		webshow.loadUrl(destURI);
            }else {
            	if(destURI.toLowerCase().startsWith( "file:" )) {
	            	if(destURI.equalsIgnoreCase( Config.ppkSettingPageFileURI )) { //��ȫ��������ض���file:��ʼ��ַ������JS
	            		webshow.getSettings().setJavaScriptEnabled(true);
	            		webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
	            	}else {
	            		webshow.getSettings().setJavaScriptEnabled(false);
	            	}
            	}else {
            		webshow.getSettings().setJavaScriptEnabled(true);
            		webshow.addJavascriptInterface(PPkActivity.this, Config.EXT_PEER_WEB);
            	}
            	
            	webshow.loadUrl(destURI);
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
    
    public WebResourceResponse getPPkResource(String ppk_uri){
        Log.d("browser","getPPkResource: " + ppk_uri);
        WebResourceResponse res = null;
        JSONObject obj_ap_resp=PPkURI.fetchPPkURI(ppk_uri);
        
        byte[] result_bytes=null;
        String mimeType=null;
        String encoding=Config.PPK_TEXT_CHARSET;
        
        if(obj_ap_resp==null) {
        	result_bytes="ERROR.Please reload.��ˢ������".getBytes();
        	mimeType="text/html";
        }else {
	        result_bytes=(byte[])obj_ap_resp.opt(Config.JSON_KEY_PPK_CHUNK);
	        mimeType=obj_ap_resp.optString(Config.JSON_KEY_PPK_CHUNK_TYPE);
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
    
    //ȱʡ��ȡ����رհ�ť�ĵ���¼�����
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
    
    // ����ҳ������Դ���غ͵������
    private class PPkWebViewClient extends WebViewClient {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("browser","shouldOverrideUrlLoading ���� url: "+ url);
        weburl.setText(url);
        weburl.setTextColor(Color.BLACK);
        textStatus.setText("Opening "+url);
        if (url != null) { 
        	if( PPkURI.isValidPPkURI(url) ) {
	            PPkActivity.this.bLoadingHttpPage=false;
	            
	            view.stopLoading();
	            
	            new ShowPPkUriAsyncTask().execute(url);
	            
	            return true;
        	}else if(url.toLowerCase().startsWith( "file:" )) {
            	if(!url.equalsIgnoreCase( Config.ppkSettingPageFileURI )) { //��ȫ������Բ����ض���file:��ʼ��ַ���жϷ��ʣ�ֻ��ʾ�հ�ҳ��
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
        if (PPkURI.isValidPPkURI(url)) {
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
          // handler.cancel();// AndroidĬ�ϵĴ���ʽ
          handler.proceed();// ����������վ��֤��
          // handleMessage(Message msg);// ������������
      }
    }

    
    class ShowPPkUriAsyncTask extends AsyncTask<String, Void, JSONObject>{
    	String ppkURI=null;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weburl.setTextColor(Color.BLACK);
            textStatus.setText("PreExecute PPk resource ");
            //String str_info="<h3>Loading PPk resource ...<br>���ڶ�ȡPPk��Դ��Ϣ...</h3>";
            //webshow.loadDataWithBaseURL(null, str_info, "text/html", "utf-8", null);
            progressBar.setVisibility(View.VISIBLE);//��ʾ��������ʾ��
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
            String result_content="<h3>ERROR while loading PPk URI. <br>����PPk��ַʱ������<br>URI:  "+ppkURI+"</h3>";;
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
                    
                    //setTitle(result_uri); //�ڱ�������ʱ��ʾ��ǰʵ�ʷ��ʵ�URI��ַ
                    
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