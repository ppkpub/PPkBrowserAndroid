package org.ppkpub.ppkbrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class MyWebView extends WebView {
	private PPkActivity mMainActivity;
	private OnScrollChangeListener mOnScrollChangeListener;
 
    public MyWebView(final Context context) {
        super(context);
    }
 
    public MyWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }
 
    public MyWebView(final Context context, final AttributeSet attrs,
                             final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(PPkActivity activity) {
    	this.mMainActivity=activity;
    	
	    this.getSettings().setJavaScriptEnabled(true);
	    this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
	    this.getSettings().setSupportMultipleWindows(false);
	    this.getSettings().setSupportZoom(true);
	    this.getSettings().setBuiltInZoomControls(true);
	    this.getSettings().setDisplayZoomControls(false);
	 
	    this.addJavascriptInterface(this.mMainActivity, Config.EXT_PEER_WEB);
	
	 
	    this.setOnTouchListener(new View.OnTouchListener()
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
    }
  
    @Override 
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        
        if(this.mOnScrollChangeListener==null)
        	return;
        
        // webview的高度
        float webcontent = getContentHeight() * getScale();
        // 当前webview的高度
        float webnow = getHeight() + getScrollY();
        if (Math.abs(webcontent - webnow) < 1) {
            //处于底端 
            mOnScrollChangeListener.onPageEnd(l, t, oldl, oldt);
        } else if (getScrollY() == 0) {
            //处于顶端
            mOnScrollChangeListener.onPageTop(l, t, oldl, oldt);
        } else { 
            mOnScrollChangeListener.onScrollChanged(l, t, oldl, oldt); 
        } 
    }
    
    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        this.mOnScrollChangeListener = listener; 
    }
    
    public interface OnScrollChangeListener {
        
        public void onPageEnd(int l, int t, int oldl, int oldt);
        
        public void onPageTop(int l, int t, int oldl, int oldt);
        
        public void onScrollChanged(int l, int t, int oldl, int oldt); 
    
    }
    
}