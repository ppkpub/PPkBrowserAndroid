package org.ppkpub.ppkbrowser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Odin {
  static String[] LetterEscapeNumSet={"O","ILA","BCZ","DEF","GH","JKS","MN","PQR","TUV","WXY"};
  
  //public static HashMap<String , String> teamMap = null;
  
  public static JSONObject getRootOdinInfo(String root_odin)  {
	String interest="{\"ver\":1,\"hop_limit\":6,\"interest\":{\"uri\":\""+Config.PPK_URI_PREFIX+root_odin+Config.PPK_URI_RESOURCE_MARK+"\"}}";
    String str_ap_resp_json=APoverHTTP.fetchInterest(Config.PPK_ROOT_ODIN_PARSE_API_URL,  interest);
	
    try {
    	JSONObject  vd_set=null;
    	
    	if(Config.PPK_ROOT_ODIN_PARSE_API_SIGN_PUBKEY.length()>0){
    		vd_set=new JSONObject();
    		vd_set.put(Config.JSON_KEY_PPK_ALGO, Config.PPK_ROOT_ODIN_PARSE_API_SIGN_ALGO);
    		vd_set.put(Config.JSON_KEY_PPK_PUBKEY, Config.PPK_ROOT_ODIN_PARSE_API_SIGN_PUBKEY);
    	}
    	JSONObject tmp_resp = PPkURI.parseRespOfPTTP(Config.PPK_ROOT_ODIN_PARSE_API_URL,str_ap_resp_json,vd_set);   
		
		if(tmp_resp==null || tmp_resp.optInt(Config.JSON_KEY_PPK_VALIDATION,Config.PPK_VALIDATION_ERROR) == Config.PPK_VALIDATION_ERROR ){
			return null;
        }else{
        	return tmp_resp;
        }
	} catch (JSONException e) {
		Log.d("Odin-Exception",e.toString());
	}
    
    return null;
     
  }
    
  
  //将根标识中的英文字母按ODIN标识规范转换成对应数字
  public static String convertLetterToNumberInRootODIN(String  original_odin){  
     String converted_odin="";
     original_odin=original_odin.toUpperCase();
     for(int kk=0;kk<original_odin.length();kk++){  
        int chr=original_odin.charAt(kk);  
        switch(chr){
            case 'O':
              chr='0';
              break;
            case 'I':
            case 'L':
            case 'A':
              chr='1';
              break;
            case 'B':
            case 'C':
            case 'Z':
              chr='2';
              break;
            case 'D':
            case 'E':
            case 'F':
              chr='3';
              break;
            case 'G':
            case 'H':
              chr='4';
              break;
            case 'J':
            case 'K':
            case 'S':
              chr='5';
              break;
            case 'M':
            case 'N':
              chr='6';
              break;
            case 'P':
            case 'Q':
            case 'R':
              chr='7';
              break;
            case 'T':
            case 'U':
            case 'V':
              chr='8';
              break;
            case 'W':
            case 'X':
            case 'Y':
              chr='9';
              break;
            default:
              break;
        }
        converted_odin=converted_odin+(char)chr;
     }  
     return Util.isNumeric(converted_odin)?converted_odin:null;  
  }   
  
  //获得指定数字短标识的对应字母转义名称组合
  public static List getEscapedListOfShortODIN(Integer  short_odin){ 
    List<String> listEscaped = new ArrayList<String>();
    
    
    String strTmp=short_odin.toString();
    listEscaped=getEscapedLettersOfShortODIN(listEscaped,strTmp,0,"");
    
    System.out.println("listEscaped:"+listEscaped.toString());
    
    return listEscaped;
  }
  
  public static List getEscapedLettersOfShortODIN(List listEscaped,String  original,int posn,String pref){ 
    int tmpNum=Integer.parseInt(String.valueOf(original.charAt(posn)));
    System.out.println("original["+posn+"]:"+tmpNum);
    
    String tmpLetters=LetterEscapeNumSet[tmpNum];
    for(int tt=0;tt<tmpLetters.length();tt++){
      String new_str=pref+String.valueOf(tmpLetters.charAt(tt));
      
      if(posn<original.length()-1){
        listEscaped=getEscapedLettersOfShortODIN(listEscaped,original,posn+1,new_str);
      }else{
        listEscaped.add(new_str);
      }
    }
    
    return listEscaped;
  }
}
