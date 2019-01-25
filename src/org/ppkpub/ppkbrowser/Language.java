package org.ppkpub.ppkbrowser;

import org.json.JSONObject;

public class Language {
  private static JSONObject objLang = null;
  private static String currentLang = "";


  public static void setLang(String lang) {
	  currentLang=lang;
  }
    
  public static String getCurrentLang(){
       return currentLang;
  }
    
  public static String getLangLabel(String enStr) {
    try{
        if(objLang !=null && objLang.has(enStr))
            return objLang.getString(enStr);
    }catch(Exception ex){
            //logger.error("Failed to get language string :"+ex.toString());
    }
    
    return enStr;
  }
}
