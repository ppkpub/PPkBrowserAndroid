package org.ppkpub.ppkbrowser;

import org.json.JSONObject;

public class UpdateInfoService {
  //Handler handler;
  //Context context;
  JSONObject updateInfo=null;
  
  private static UpdateInfoService mObjInstance=null;
  

  public UpdateInfoService() {
	  mObjInstance=this;
  }
  
  public static  UpdateInfoService getInstance() {
	  if(mObjInstance==null) {
		  new UpdateInfoService();
	  }
		  
	  return mObjInstance;
  }

  public boolean refreshUpdateInfo()  {
	  try {
		  String path = Config.versionUpdateURL;
		  System.out.println("update info url="+path);
	      String info = Util.getPage(path);
	      System.out.println("page info="+info);
	      this.updateInfo = new JSONObject(info);
	      
	      return true;
	  }catch(Exception e) {
		  System.out.println("refreshUpdateInfo error:"+e.toString());
		  return false;
	  }
  }
  
  public JSONObject getUpdateInfo()  {
      return this.updateInfo;
  }
  
  public boolean isNeedUpdate() {
	  if(updateInfo==null)
		  return false;
	  
      String new_version = updateInfo.optString("version","0");
      String now_version = Config.version;
      /*
      try {
          PackageManager packageManager = context.getPackageManager();
          PackageInfo packageInfo = packageManager.getPackageInfo(
                  context.getPackageName(), 0);
          now_version = packageInfo.versionName;
      } catch (NameNotFoundException e) {
          e.printStackTrace();
      }*/
      System.out.println("new_version:"+new_version+",now_version:"+now_version);
      if (new_version.compareToIgnoreCase(now_version)>0) {
          return true;
      } else {
          return false;
      }
  }
  
  
}

