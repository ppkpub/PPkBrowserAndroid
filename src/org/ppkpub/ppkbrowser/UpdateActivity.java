package org.ppkpub.ppkbrowser;
//在Activity中调用，点击更新按钮，会弹出对话框，显示版本号和升级描述。这里新建UpdateActivity.java

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateActivity extends Activity {
	private UpdateInfoService updateInfoService;
	private JSONObject info;

	private TextView textStatus;
	
	private DownloadTool mDownloadTool;
	private long mDownloadId=-1;
	
	private String mNewestApkFileName="PPkBrowser.apk";
	
	private boolean mUsePublicStorage=false; //默认不使用外部存储，如要使用则需要相应配置权限
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout .activity_update);
	    
	    
	    Button button_update = (Button) findViewById(R.id.update_btn_start);
	    button_update.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            startUpdate();
	        }
	    });
	    
	    Button btn_cancel = (Button) findViewById(R.id.update_btn_cancel);
	    btn_cancel.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	finish();
	        }
	    });
	    
	    ImageButton button_back = (ImageButton) findViewById(R.id.update_btn_back);
	    button_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	    TextView textDesc = (TextView) findViewById(R.id.update_view_desc);
		
		updateInfoService = UpdateInfoService.getInstance();
	    info=updateInfoService.getUpdateInfo();
	    
	    if(info==null) {
	    	textDesc.setText("没有获得有效的更新信息");
	    }
	    
	    if (updateInfoService.isNeedUpdate()) {
	    	textDesc.setText("当前版本："+Config.version+"\n最新版本："+info.optString("version","")+"\n升级说明：\n"+info.optString("description",info.toString()));
	    }else {
	    	textDesc.setText("当前版本："+Config.version+"\n已经是最新版本，不需要更新");
	    }	
	    
	    textStatus = (TextView) findViewById(R.id.update_view_status);
	    textStatus.setText("更新来源："+Config.versionUpdateURL);
	}
	
	private void startUpdate() {
		if(mDownloadId>=0) {
			textStatus.setText("已经有下载任务在运行中，请稍候...");
			return;
		}
		
	    if(info==null) {
	    	textStatus.setText("没有获得有效的更新信息");
	    	return;
	    }
	    
	    if (!updateInfoService.isNeedUpdate()) {
	    	textStatus.setText("已经是最新版本，不需要更新");
	    	return;
	    }	
	    
        try {
        	String new_ver = info.getString("version");
        	/*
        	if(mUsePublicStorage)
        		mNewestApkFileName = "PPkBrowserV"+new_ver+".apk"; //使用外部存储时不同版本存储文件名称不同，避免与用户的既有文件冲突
        	else
        		mNewestApkFileName = "PPkBrowser.apk"; //使用内部缓存时用同一名称，减少占用
        	*/
        	File apkFile = getApkFile();
        	if(apkFile.exists()) { //删除已存在的旧文件
        		apkFile.delete();
        	}
        	
        	textStatus.setText("开始更新到版本 "+new_ver+"\n"+info);
        	
        	JSONArray download_list=info.optJSONArray("download_list");
        	
        	String tmp_url=download_list.optString(0);
        	if(tmp_url!=null && tmp_url.trim().length()>0)
        		downFile(tmp_url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			textStatus.setText("自动更新出错，请从官网 ppkpub.org 自行下载最新版本安装。");
		}

	    
	}
	
	private void downFile(final String url) {
		if(mUsePublicStorage) {
			downFileToPublicStorage(url);
		}else {
			downFileToPrivateCache(url);
		}
	}
	
	//使用系统自带的下载管理器下载到公共存储
	private void downFileToPublicStorage(final String url) {
		textStatus.setText("开始下载更新文件:"+url);
	    
		mDownloadTool = new DownloadTool(UpdateActivity.this);
		mDownloadId=mDownloadTool.downloadPublicStorage(url, mNewestApkFileName,mReceiver);
		
		Toast.makeText(UpdateActivity.this, "UpdateActivity downFile() mDownloadId="+mDownloadId, Toast.LENGTH_SHORT).show();
		  
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
	     @Override
	     public void onReceive(Context context, Intent intent) {
	    	 checkDownloadStatus();
	     }
	}; 
	

	//自行处理下载到私有缓存目录
	private void downFileToPrivateCache(final String url) {
		textStatus.setText("开始下载更新文件:"+url);
		
		mDownloadId=0;
	    
		ProgressDialog progressDialog = new ProgressDialog(UpdateActivity.this);
	    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    progressDialog.setTitle("下载新版本");
	    progressDialog.setMessage("下载中...");
	    progressDialog.setProgress(0);
	    progressDialog.show();
	      
		mDownloadTool = new DownloadTool(UpdateActivity.this);
		mDownloadTool.downloadPrivateCache(url, mNewestApkFileName,handlerDownloadPrivateCache,progressDialog);
		
		//Toast.makeText(UpdateActivity.this, "UpdateActivity downFile() mDownloadId="+mDownloadId, Toast.LENGTH_SHORT).show();
		  
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handlerDownloadPrivateCache = new Handler() {
	      public void handleMessage(Message msg) {
	    	  checkDownloadStatus();
	      }
	};

	 /**
	  * 检查下载状态
	  */
	 private void checkDownloadStatus() {
		 if(mDownloadId<0)
			 return;
		 
		 int status = mUsePublicStorage ? mDownloadTool.getPublicStorageStatus(mDownloadId)
				                        : mDownloadTool.getPrivateStorageStatus();
         switch (status) {
             //下载暂停
             case DownloadManager.STATUS_PAUSED:
                 break;
             //下载延迟
             case DownloadManager.STATUS_PENDING:
                 break;
             //正在下载
             case DownloadManager.STATUS_RUNNING:
                 break;
             //下载完成
             case DownloadManager.STATUS_SUCCESSFUL:
            	 textStatus.setText( "下载完成，请在弹出的安装提示界面继续操作");
            	 mDownloadId=-1;
                 installAPK();
                 break;
             //下载失败
             case DownloadManager.STATUS_FAILED:
            	 textStatus.setText( "下载失败");
            	 mDownloadId=-1;
                 break;
         }
	     
	 }
	 
	 private File getApkFile() {
		 if(mUsePublicStorage)
			 return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mNewestApkFileName);
		 else
			 return new File(UpdateActivity.this.getCacheDir(), mNewestApkFileName);
	 }
	 
	 /**
	  * 7.0兼容
	  */
	 private void installAPK() {
	     File apkFile = getApkFile();
	     
	     Intent intent = new Intent(Intent.ACTION_VIEW);
	     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	     
	     //检查手机版本号，如果是Android7.0将采用应用共享方法
	     if (Build.VERSION.SDK_INT >= 24) {
	         Uri apkUri = FileProvider.getUriForFile(UpdateActivity.this, "org.ppkpub.ppkbrowser.fileprovider", apkFile);
	         intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
	         intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
	     } else { //其他版本直接调用
	    	 if(!mUsePublicStorage ) {  //对于7.0版本以下使用私有存储，需要修改APK文件读写权限才能安装
	    	     try {
	    	    	 String cmd = "chmod 777 " + apkFile.getPath() ;
	    	    	 //textStatus.setText( "cmd:"+cmd);
	    	    	 Runtime.getRuntime().exec(cmd);
	    	     } catch (Exception e) {
	    	    	 //e.printStackTrace();
	    	    	 textStatus.setText( "无权限访问安装文件:"+apkFile.getPath()+"\n请自行下载安装。");
	    	     }
	    	 }
	         intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
	     }
	     UpdateActivity.this.startActivity(intent);
	 }
}