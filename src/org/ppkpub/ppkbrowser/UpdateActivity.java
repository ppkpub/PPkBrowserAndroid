package org.ppkpub.ppkbrowser;
//��Activity�е��ã�������°�ť���ᵯ���Ի�����ʾ�汾�ź����������������½�UpdateActivity.java

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
	
	private boolean mUsePublicStorage=false; //Ĭ�ϲ�ʹ���ⲿ�洢����Ҫʹ������Ҫ��Ӧ����Ȩ��
	
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
	    	textDesc.setText("û�л����Ч�ĸ�����Ϣ");
	    }
	    
	    if (updateInfoService.isNeedUpdate()) {
	    	textDesc.setText("��ǰ�汾��"+Config.version+"\n���°汾��"+info.optString("version","")+"\n����˵����\n"+info.optString("description",info.toString()));
	    }else {
	    	textDesc.setText("��ǰ�汾��"+Config.version+"\n�Ѿ������°汾������Ҫ����");
	    }	
	    
	    textStatus = (TextView) findViewById(R.id.update_view_status);
	    textStatus.setText("������Դ��"+Config.versionUpdateURL);
	}
	
	private void startUpdate() {
		if(mDownloadId>=0) {
			textStatus.setText("�Ѿ������������������У����Ժ�...");
			return;
		}
		
	    if(info==null) {
	    	textStatus.setText("û�л����Ч�ĸ�����Ϣ");
	    	return;
	    }
	    
	    if (!updateInfoService.isNeedUpdate()) {
	    	textStatus.setText("�Ѿ������°汾������Ҫ����");
	    	return;
	    }	
	    
        try {
        	String new_ver = info.getString("version");
        	/*
        	if(mUsePublicStorage)
        		mNewestApkFileName = "PPkBrowserV"+new_ver+".apk"; //ʹ���ⲿ�洢ʱ��ͬ�汾�洢�ļ����Ʋ�ͬ���������û��ļ����ļ���ͻ
        	else
        		mNewestApkFileName = "PPkBrowser.apk"; //ʹ���ڲ�����ʱ��ͬһ���ƣ�����ռ��
        	*/
        	File apkFile = getApkFile();
        	if(apkFile.exists()) { //ɾ���Ѵ��ڵľ��ļ�
        		apkFile.delete();
        	}
        	
        	textStatus.setText("��ʼ���µ��汾 "+new_ver+"\n"+info);
        	
        	JSONArray download_list=info.optJSONArray("download_list");
        	
        	String tmp_url=download_list.optString(0);
        	if(tmp_url!=null && tmp_url.trim().length()>0)
        		downFile(tmp_url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			textStatus.setText("�Զ����³�����ӹ��� ppkpub.org �����������°汾��װ��");
		}

	    
	}
	
	private void downFile(final String url) {
		if(mUsePublicStorage) {
			downFileToPublicStorage(url);
		}else {
			downFileToPrivateCache(url);
		}
	}
	
	//ʹ��ϵͳ�Դ������ع��������ص������洢
	private void downFileToPublicStorage(final String url) {
		textStatus.setText("��ʼ���ظ����ļ�:"+url);
	    
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
	

	//���д������ص�˽�л���Ŀ¼
	private void downFileToPrivateCache(final String url) {
		textStatus.setText("��ʼ���ظ����ļ�:"+url);
		
		mDownloadId=0;
	    
		ProgressDialog progressDialog = new ProgressDialog(UpdateActivity.this);
	    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    progressDialog.setTitle("�����°汾");
	    progressDialog.setMessage("������...");
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
	  * �������״̬
	  */
	 private void checkDownloadStatus() {
		 if(mDownloadId<0)
			 return;
		 
		 int status = mUsePublicStorage ? mDownloadTool.getPublicStorageStatus(mDownloadId)
				                        : mDownloadTool.getPrivateStorageStatus();
         switch (status) {
             //������ͣ
             case DownloadManager.STATUS_PAUSED:
                 break;
             //�����ӳ�
             case DownloadManager.STATUS_PENDING:
                 break;
             //��������
             case DownloadManager.STATUS_RUNNING:
                 break;
             //�������
             case DownloadManager.STATUS_SUCCESSFUL:
            	 textStatus.setText( "������ɣ����ڵ����İ�װ��ʾ�����������");
            	 mDownloadId=-1;
                 installAPK();
                 break;
             //����ʧ��
             case DownloadManager.STATUS_FAILED:
            	 textStatus.setText( "����ʧ��");
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
	  * 7.0����
	  */
	 private void installAPK() {
	     File apkFile = getApkFile();
	     
	     Intent intent = new Intent(Intent.ACTION_VIEW);
	     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	     
	     //����ֻ��汾�ţ������Android7.0������Ӧ�ù�����
	     if (Build.VERSION.SDK_INT >= 24) {
	         Uri apkUri = FileProvider.getUriForFile(UpdateActivity.this, "org.ppkpub.ppkbrowser.fileprovider", apkFile);
	         intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
	         intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
	     } else { //�����汾ֱ�ӵ���
	    	 if(!mUsePublicStorage ) {  //����7.0�汾����ʹ��˽�д洢����Ҫ�޸�APK�ļ���дȨ�޲��ܰ�װ
	    	     try {
	    	    	 String cmd = "chmod 777 " + apkFile.getPath() ;
	    	    	 //textStatus.setText( "cmd:"+cmd);
	    	    	 Runtime.getRuntime().exec(cmd);
	    	     } catch (Exception e) {
	    	    	 //e.printStackTrace();
	    	    	 textStatus.setText( "��Ȩ�޷��ʰ�װ�ļ�:"+apkFile.getPath()+"\n���������ذ�װ��");
	    	     }
	    	 }
	         intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
	     }
	     UpdateActivity.this.startActivity(intent);
	 }
}