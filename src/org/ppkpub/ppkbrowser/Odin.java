package org.ppkpub.ppkbrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ODIN {
  public static String mSetFileName = "resources_odin_set"; //��Ϊ��ݱ�ʶ�����ODIN������
  public static String  statusMessage = "";
    
  private static String[] LetterEscapeNumSet={"O","ILA","BCZ","DEF","GH","JKS","MN","PQR","TUV","WXY"};
  
  private static PPkActivity mMainActivity=null;
  private static JSONObject mObjOdinSet;
  
  
  //��ʽ������URI������ʹ֮����ODIN��ʶ����淶����Ч����null
  //����prior_add_resource_markȡֵtrueʱ ����׷����Դ��ʶ������Ҫ����IDʹ��ʱ���� ������ݳ�����ַ�����Զ��ж����ȱ�ٵ�"/"�ַ�����Դ��־
  public static String formatPPkURI(String ppk_uri){
  	  return formatPPkURI(ppk_uri,false);
  }
  public static String formatPPkURI(String ppk_uri,boolean prior_add_resource_mark_for_id){
    if(ppk_uri==null)
        return null;
    
    if( ppk_uri.indexOf("//") >0 ){
        //����������/�ַ�
        return null;
    }
    
    int scheme_posn=ppk_uri.indexOf(":");
    String main_part=null;
    
    if( scheme_posn<0){ //�����ַû���κ�����ǰ׺ʱ
        main_part = ppk_uri.trim();
    }else{//���ǰ׺�Ƿ���ppk:��ʼ
        String prefix = ppk_uri.substring(0,scheme_posn+1);
        
        if( !prefix.equalsIgnoreCase(Config.PPK_URI_PREFIX) )
            return null;
        
        main_part = ppk_uri.substring(scheme_posn+1).trim();
    }
    
    if(main_part.length()==0){ //û��ָ��ʵ����ַʱ��ʹ��Ĭ����ҳ
        ppk_uri = Config.ppkDefaultHomepage;
    }else{
        ppk_uri = Config.PPK_URI_PREFIX+main_part;
    }
    
    int old_resoure_mark_posn=ppk_uri.lastIndexOf("#");
    if(old_resoure_mark_posn==ppk_uri.length()-1) {//�Զ��滻�ɰ�URI�еĺ�׺��־��#
    	ppk_uri = ppk_uri.substring(0, old_resoure_mark_posn)+Config.PPK_URI_RESOURCE_MARK;;
    }
    
    int resoure_mark_posn=ppk_uri.lastIndexOf(Config.PPK_URI_RESOURCE_MARK);
    
    if( resoure_mark_posn<0){
    	if(!prior_add_resource_mark_for_id) {
    		//�Զ��ж������ȱ�ٵ�"/"�ַ�
	        int fisrt_slash_posn=ppk_uri.indexOf("/");
	        if(fisrt_slash_posn<0){ //�Ǹ���ʶ
	            ppk_uri += "/";
	        }else{ //����չ��ʶ
	            //�ж�β����������Դ���Ƿ����ļ���չ�����߷�����־��
	            int last_slash_posn=ppk_uri.lastIndexOf("/");
	            
	            if( last_slash_posn!=ppk_uri.length()-1){ //������"/"�ַ���β
	                int last_point_posn=ppk_uri.lastIndexOf(".");
	                int function_mark_posn=ppk_uri.lastIndexOf(")");
	                if(last_point_posn<last_slash_posn && function_mark_posn<0){
	                    //û���ļ���չ�������Ƿ�����ʶ��Ĭ��ΪĿ¼����Ҫ����"/"
	                    ppk_uri += "/";
	                }
	            }
	        }
    	}
    	
    	ppk_uri += Config.PPK_URI_RESOURCE_MARK;
    }

    return ppk_uri;
  }
  
  //�⹹PPK��Դ��ַ
  public static PPkUriParts splitPPkURI(String in_uri)
  {
    try{
      //���URI��ʽ����Ҫ��
      String format_ppk_uri = formatPPkURI(in_uri);
      
      if( format_ppk_uri==null ){
        Log.d("Odin","splitPPkURI() meet invalid ppk-uri:"+in_uri);
        return null;
      }
      
      int resoure_mark_posn=format_ppk_uri.indexOf(Config.PPK_URI_RESOURCE_MARK);
      
      PPkUriParts obj_uri_parts = new PPkUriParts();
      obj_uri_parts.resource_versoin = format_ppk_uri.substring(resoure_mark_posn+1,format_ppk_uri.length());
      String str_path_segment = format_ppk_uri.substring(Config.PPK_URI_PREFIX.length(),resoure_mark_posn);

      //System.out.println("str_path_segment="+str_path_segment);
      if(str_path_segment.endsWith("/")){ //����"ppk:123/"��"ppk:123/abc/"ָ��Ĭ��������ҳ 
        obj_uri_parts.resource_id="";
        obj_uri_parts.parent_odin_path=str_path_segment.substring(0,str_path_segment.length()-1);
      }else{
        int tmp_posn=str_path_segment.lastIndexOf('/');
        if(tmp_posn>0){
          obj_uri_parts.resource_id=str_path_segment.substring(tmp_posn+1,str_path_segment.length());
          obj_uri_parts.parent_odin_path=str_path_segment.substring(0,tmp_posn);
        }else{
          obj_uri_parts.parent_odin_path="";
          obj_uri_parts.resource_id=str_path_segment;
        }
      }

      obj_uri_parts.format_uri = format_ppk_uri;
          
      //System.out.println("ODIN.splitPPkURI()\n format_uri="+obj_uri_parts.format_uri+" , parent_odin_path="+obj_uri_parts.parent_odin_path+", resource_id="+obj_uri_parts.resource_id+", resource_versoin="+obj_uri_parts.resource_versoin+"\n");
      
      return obj_uri_parts;
    }catch(Exception e){
      Log.d("Odin","splitPPkURI() meet invalid ppk-uri:"+in_uri + " ,"+e.toString() );
      return null;
    }
  }
  
  //���PPk URI��Ӧ��Դ�汾�ţ�����β���ơ�*1.0�����������������û���򷵻ؿ��ַ���
  public static String getPPkResourceVer(String ppk_uri){
    if(ppk_uri==null)
        return null;

    int resoure_mark_posn=ppk_uri.indexOf(Config.PPK_URI_RESOURCE_MARK);

    return resoure_mark_posn>0 ? ppk_uri.substring(resoure_mark_posn+1).trim():"";

  }
  
  //��ñ�ʾ���°汾��URI��ȥ�����ܵİ汾�ţ�
  public static String getLastestPPkURI(String ppk_uri){
    if(ppk_uri==null)
        return null;

    int resoure_mark_posn=ppk_uri.indexOf(Config.PPK_URI_RESOURCE_MARK);
    if( resoure_mark_posn<0){ 
        ppk_uri += Config.PPK_URI_RESOURCE_MARK;
    }else{
        ppk_uri = ppk_uri.substring(0,resoure_mark_posn+1);
    }

    return ppk_uri;
  }
  
//������ʶ�е�Ӣ����ĸ���¶��Ź淶ת���ɶ�Ӧ����
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
  
  //���ָ�����ֶ̱�ʶ�Ķ�Ӧ��ĸת���������
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
  
  //������dir:ppk:��ʼ��ַת���õ�ʵ�ʵ�PPK��Դ��ַ����������򷵻�null
  public static String  getRealPPkURI(String uri){
      if( uri.toLowerCase().startsWith(Config.DIDPPK_URI_PREFIX)) 
	      	uri = Config.PPK_URI_PREFIX + uri.substring(Config.DIDPPK_URI_PREFIX.length());

      return formatPPkURI( uri )  ;
  }

  public static JSONObject getRootOdinSet(String root_odin)  {
		String str_req_uri = Config.PPK_URI_PREFIX+root_odin+Config.PPK_URI_RESOURCE_MARK;
		//String interest="{\"ver\":1,\"hop_limit\":6,\"interest\":{\"uri\":\""+str_req_uri+"\"}}";
		
		String str_ap_resp_json=APoverHTTP.fetchInterest(Config.PPK_API_URL,  str_req_uri);
		
	    try {
	    	JSONObject  parent_vd_set=null;
	    	
	    	if(Config.PPK_API_PUBKEY_PEM.length()>0){
	    		parent_vd_set=new JSONObject();
	    		parent_vd_set.put(Config.ODIN_SET_VD_TYPE, Config.ODIN_SET_VD_ENCODE_TYPE_PEM);
	    		parent_vd_set.put(Config.ODIN_SET_VD_PUBKEY, Config.PPK_API_PUBKEY_PEM);
	    	}
	    	JSONObject tmp_resp = PTTP.parseRespOfPTTP(Config.PPK_API_URL,str_ap_resp_json,str_req_uri,parent_vd_set);   
			
			if(tmp_resp==null || tmp_resp.optInt(Config.JSON_KEY_PPK_VALIDATION,Config.PTTP_VALIDATION_ERROR) == Config.PTTP_VALIDATION_ERROR ){
				return null;
	        }else{
	        	byte[] api_content_bytes= (byte[])tmp_resp.opt(Config.JSON_KEY_CHUNK_BYTES) ;
	        	String str_resp_setting = new String( api_content_bytes );
	              
	            return new JSONObject( str_resp_setting  );
	        }
		} catch (JSONException e) {
			Log.d("Odin-Exception",e.toString());
		}
	    
	    return null;
   }

  public static void init(PPkActivity main_activity ) {
	  mMainActivity=main_activity;

      try {
    	mObjOdinSet=new JSONObject();
    	
    	String  odin_json=mMainActivity.getPrivateData(mSetFileName);
    	Log.d("ODIN","odin_json="+odin_json);
        if (odin_json!=null && odin_json.length()>0 ) {
          statusMessage = Language.getLangLabel("Found odin setting data"); 
          Log.d("Odin",statusMessage);
          
          try{
        	  mObjOdinSet=new JSONObject(odin_json); 
          }catch(Exception ex){
        	  mObjOdinSet=new JSONObject();
        	  Log.d("Odin","Load odin setting data failed!"+ex.toString());
          }
        } else {
          statusMessage = Language.getLangLabel("Creating new odin setting file"); 
          Log.d("Odin",statusMessage);
          
          mObjOdinSet.put("encrypted",false);
        }
            
      } catch (Exception e) {
        Log.d("Odin","Error during init: "+e.toString());
        //e.printStackTrace();
        //System.exit(-1);
      }
    
  }
  
  //��õ�ǰʹ�õİ¶���
  public static String getDefaultOdinURI() {
    String exist_odin_uri = mObjOdinSet.optString("default", null);
    
    if(exist_odin_uri!=null ) {
    	int old_resoure_mark_posn=exist_odin_uri.lastIndexOf("#");
        if(old_resoure_mark_posn==exist_odin_uri.length()-1) {//�Զ������ɰ�URI�еĺ�׺��־��#
        	exist_odin_uri=ODIN.formatPPkURI(exist_odin_uri, true);
        	
        	setDefaultOdinURI(exist_odin_uri);
        }
        
        
    }
    return exist_odin_uri;
  }
  
  //���õ�ǰʹ�õ�BTC��ַ
  public static boolean setDefaultOdinURI(String odin_uri) {
	try {
		mObjOdinSet.put("default", odin_uri);
		saveOdinSet();
		return true;
	} catch (JSONException e) {
		return false;
	}
  }
  
  public static void saveOdinSet() {
	  String odin_json=mObjOdinSet.toString();
	  Log.d("Odin","odin_json="+odin_json);
	  mMainActivity.putPrivateData(mSetFileName,odin_json);
  }
  
  public static String getBackupData(  ) {
	  return mMainActivity.getPrivateData(mSetFileName);
  }
  
  public static boolean restoreBackupData( String data  ) {
	  try{
		  JSONObject tmpObjSet=new JSONObject(data); 
		  mObjOdinSet=tmpObjSet;
		  saveOdinSet();
    	  return true;
	  } catch (Exception e) {
        Log.d("Odin","RestoreBackupData failed: "+e.toString());
        //e.printStackTrace();
      }
	  
	  return false;
  }
}
