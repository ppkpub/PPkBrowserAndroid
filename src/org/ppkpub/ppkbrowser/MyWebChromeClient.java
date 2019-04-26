package org.ppkpub.ppkbrowser;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;
import android.annotation.TargetApi;
import android.app.Activity;


public class MyWebChromeClient extends WebChromeClient {
	private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessage5;
    public static final int FILECHOOSER_RESULTCODE = 5173;
    public static final int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 5174;

    private PPkActivity mActivity;
    private MyWebView mWebView;

    public MyWebChromeClient(PPkActivity activity,MyWebView webview) {
        this.mWebView = webview;
        this.mActivity = activity;
    }
    /*
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
	*/
    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        this.openFileChooser(uploadMsg, "*/*");
    }

    // For Android >= 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
            String acceptType) {
        this.openFileChooser(uploadMsg, acceptType, null);
    }

    // For Android >= 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
            String acceptType, String capture) {
    	Toast.makeText(mWebView.getContext(), ">=4.1 openFileChooser() called" , 0).show();
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        mActivity.startActivityForResult(Intent.createChooser(i, "File Browser"),
                FILECHOOSER_RESULTCODE);
    }

    // For Lollipop 5.0+ Devices
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView mWebView,
            ValueCallback<Uri[]> filePathCallback,
            WebChromeClient.FileChooserParams fileChooserParams) {
    	Toast.makeText(mWebView.getContext(), "5.0+ onShowFileChooser() called" , 0).show();
        if (mUploadMessage5 != null) {
            mUploadMessage5.onReceiveValue(null);
            mUploadMessage5 = null;
        }
        mUploadMessage5 = filePathCallback;
        Intent intent = fileChooserParams.createIntent();
        try {
        	mActivity.startActivityForResult(intent,
                    FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
        } catch (ActivityNotFoundException e) {
        	Toast.makeText(mWebView.getContext(), "onShowFileChooser() ActivityNotFoundException:"+e.toString() , 0).show();
            mUploadMessage5 = null;
            return false;
        }
        return true;
    }
    
    public void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            if (null == mUploadMessage5) {
                return;
            }
            mUploadMessage5.onReceiveValue(WebChromeClient.FileChooserParams
                    .parseResult(resultCode, intent));
            mUploadMessage5 = null;
        }
    }
}