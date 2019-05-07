package org.ppkpub.ppkbrowser;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;

public class DownloadTool {
  //ProgressDialog progressDialog;

  private Context mContext;
  private DownloadManager mDownloadManager=null;
  
  public DownloadTool(Context context) {
      this.mContext = context;
  }
  
  public long downloadPublicStorage(String url,String local_filename,BroadcastReceiver receiver) {
     final String packageName = "com.android.providers.downloads";
     int state = mContext.getPackageManager().getApplicationEnabledSetting(packageName);
     long downloadId=-1;
     
     //������ع������Ƿ񱻽���
     if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
         || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
         || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
         AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setTitle("��ܰ��ʾ").setMessage
             ("ϵͳ���ع���������ֹ�����ֶ���").setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
                 try {
                     Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                     intent.setData(Uri.parse("package:" + packageName));
                     mContext.startActivity(intent);
                 } catch (ActivityNotFoundException e) {
                     Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                     mContext.startActivity(intent);
                 }
             }
         }).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
         builder.create().show();
     } else {
         //������������
         DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
         request.setAllowedOverRoaming(false);
 
         //֪ͨ����ʾ
         request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
         request.setTitle(Config.appName);
         request.setDescription("����������...");
         request.setVisibleInDownloadsUi(true);
 
         //�������ص�·��
         request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, local_filename);
 
         //��ȡDownloadManager
         mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
         downloadId = mDownloadManager.enqueue(request);
 
         mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
     }
     System.out.println("downloadId="+downloadId);
     return downloadId;
  }
    
  //��ȡ�����洢������״̬
  public int getPublicStorageStatus(long downloadId) {
	  int status = -1;
	  if(mDownloadManager!=null) {//ʹ��DownloadManager��д�ⲿ�洢
	     DownloadManager.Query query = new DownloadManager.Query();
	     query.setFilterById(downloadId);
	     Cursor cursor = mDownloadManager.query(query);
	     if (cursor!=null) {
	    	 if(cursor.moveToFirst()){
		         status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
		         /*
		            int[] bytesAndStatus = new int[]{0, 0, 0};
		            //�Ѿ����ص��ֽ���
	                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
	                //�������ص��ֽ���
	                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
	                //״̬���ڵ�������
	                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
	                
	                System.out.prntln( "���ؽ��ȣ�" + bytesAndStatus[0] + "/" + bytesAndStatus[1] + "");
		          */
	    	 }
	    	 cursor.close();
	     }else {//��Ҫ�ֹ����������״̬��������
	    	 
	     }
	  }
	  
	  return status;
 }
  
  //��ȡ˽�д洢����״̬
  public int getPrivateStorageStatus() {
	  return mPrivateStatus;
  }
	 
  private int mPrivateStatus=-1;
  
  public void downloadPrivateCache(final String url,final String local_filename,
		                           final Handler handler, final ProgressDialog progressDialog) {
	  mPrivateStatus=DownloadManager.STATUS_PENDING;
	  handler.sendEmptyMessage(mPrivateStatus);
      new Thread() {
          public void run() {
        	  mPrivateStatus=DownloadManager.STATUS_RUNNING;
        	  handler.sendEmptyMessage(mPrivateStatus);
              HttpClient client = new DefaultHttpClient();
              HttpGet get = new HttpGet(url);
              HttpResponse response;
              try {
                  response = client.execute(get);
                  HttpEntity entity = response.getEntity();
                  int length = (int) entity.getContentLength();  
                  if(progressDialog!=null)
                	  progressDialog.setMax(length);
                  InputStream is = entity.getContent();
                  FileOutputStream fileOutputStream = null;
                  if (is != null) {
                      File file = new File(
                    		  mContext.getCacheDir(),
                    		  local_filename);
                      fileOutputStream = new FileOutputStream(file);
                      byte[] buf = new byte[100];
                      int ch = -1;
                      int process = 0;
                      while ((ch = is.read(buf)) != -1) {
                          fileOutputStream.write(buf, 0, ch);
                          process += ch;

                          if(progressDialog!=null)
                        	  progressDialog.setProgress(process);
                      }

                  }
                  fileOutputStream.flush();
                  if (fileOutputStream != null) {
                      fileOutputStream.close();
                  }
                  
                  //Finish download
                  progressDialog.cancel();
                  mPrivateStatus=DownloadManager.STATUS_SUCCESSFUL;
                  handler.sendEmptyMessage(mPrivateStatus);
              } catch (Exception e) {
                  e.printStackTrace();
                  mPrivateStatus=DownloadManager.STATUS_FAILED;
                  handler.sendEmptyMessage(mPrivateStatus);
              }
          }

      }.start();
  }
	 
}

