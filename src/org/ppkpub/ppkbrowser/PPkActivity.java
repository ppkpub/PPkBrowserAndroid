package org.ppkpub.ppkbrowser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceResponse;
//import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private ProgressBar progressBar;
    private boolean     bLoadingHttpPage;
    
    private TextView    textStatus;
    
    // 建立一个链表来保存历史记录
    //private BrowserPageList historylist = new BrowserPageList();
    // 建立一个链表来保存书签记录
    //BrowserPageList bookmarklist = new BrowserPageList();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppk);

        setTitle(Config.appName+" V"+Config.version);
        
        progressBar = (ProgressBar) findViewById(R.id.progressBarLoadPage);
        textStatus = (TextView) findViewById(R.id.textStatus);
        
        weburl = (EditText) findViewById(R.id.weburl);
        webshow = (WebView) findViewById(R.id.webshow);
        webshow.getSettings().setJavaScriptEnabled(true);
        webshow.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webshow.getSettings().setSupportMultipleWindows(true);
        webshow.getSettings().setBuiltInZoomControls(true);
        
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
            }else{
                webshow.loadUrl(destURI);
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