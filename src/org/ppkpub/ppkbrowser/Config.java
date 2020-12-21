package org.ppkpub.ppkbrowser;

import java.io.File;

import org.json.JSONObject;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class Config {
  //name
  public static String appName = "PPk������ڲ��";
  //public static String defaultLang = "EN";
  
  public static String PPK_API_URL  = "http://47.114.169.156/ppkapi2/";  //��������ʶ�ķ���API
  public static String PPK_API_PUBKEY_PEM = "-----BEGIN PUBLIC KEY-----\r\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn+a+pl1qjw34cuoj+vDDioG5rsTXLK+h\r\nDLQY1nqXkrGt+50lz3T92IiPM1DWzrRz7ycrPkaesHsdCoB4zPRQy0OJHIQzGfpXdrqZbchJZfTM\r\nIFYDoPoPnlNH8xb4hQ8LoERtYxxwddGiHfvYhTEYFQBh4cj+AxEsex/duWim9O5q1I9PHK6anwlS\r\nNqmhyzxLAnFUKSqr2crUy7ZfCTi9zN63JXOsJtWi/dSZuK1RISej6zqQgkiQceIoqFXBjRVpYJQe\r\niQ3mw3uoZqth80e8UqT1ZXqyD82Obsb3ofKRFqbmEhuLi6+GJakgZsYs/BM/SpfIF0Wny5PWTemZ\r\nCWajXwIDAQAB\r\n-----END PUBLIC KEY-----\r\n"; //�Ա�ʶ�����������֤��Կ
  /*
  public static String ROOT_ODIN_PARSE_API_URL  = "http://test.ppkpub.org:8088/"; //���õĸ���ʶ��������API
  public static String ROOT_ODIN_PARSE_API_SIGN_PUBKEY_PEM ="";
  */
  public static String PPK_URI_PREFIX = "ppk:";
  public static String DIDPPK_URI_PREFIX = "did:"+PPK_URI_PREFIX;
  public static String PPK_URI_RESOURCE_MARK="*";
  public static String PAYTOPPK_URI_PREFIX = "paytoppk:";
  
  public static String ppkPayToolURI  = "https://ppk001.sinaapp.com/demo/pay/";

  public static String ppkSettingPage        = "about:settings";
  public static String ppkSettingPageFileURI = "file:///android_asset/settings.html";
  public static String ppkPayPage        = "about:pay";
  public static String ppkPayPageFileURI = "file:///android_asset/paycode.html";
  
  public static String ppkHotURI  = "ppk:joy/";
  public static String ppkDefaultHomepage  = ppkPayPage;
  
  public static int debugKey     = 0;  //��0ֵ��DEBUG������Ϣ
  
  public static String  jdbcURL      = null;
  public static String  proxyURL     = "http://tool.ppkpub.org/odin/proxy.php";
  
  public static String cacheDirPrefix = null;
  
  //version
  public static String stableVersion = "x.x.x" ;
  public static String versionUpdateURL = "http://ppkpub.org/PPkBrowserAndroid/bin/version.json";
  
  public static String developeVersion  = "" ; //���ַ���ʱ��ʾΪ��ʽ�汾�������汾ȡֵע�����:�ַ�����:01
  public static String developeVersionUpdateURL   = "http://47.114.169.156/autoupdate/ppkbrowser/version_test.json";
  //public static String developeVersionUpdateURL   = "http://192.168.62.99/autoupdate/ppkbrowser/version_local.json";

  public static String version = stableVersion + developeVersion;
  
  //public static int majorVersionDB = 1;

  //public static String defaultSqliteFile = null;  
  
  //bitcoin
  public static boolean useDustTX = true;
  public static Integer dustSize = 1000;
  //public static Integer minOrderMatchBTC = 100000;
  //public static Integer minFee = 10000;
  public static Integer maxUseUTXO = 20;  //�������ʹ�õ�UTXO��Ŀ��
  public static Integer maxFee = 99999;  //Avoid sending too much btc than normal fee
  public static Integer dataValue = 0;
  public static Integer btc_unit = 100000000;
  
  //PPk
  public static Integer ppkStandardDataFee = 1000;
  
  public static final long ppkToolCreationTime = 1400561240-1;  //UTC 2014-5-20 04:47:20
  public static Integer firstBlock = 0;  
  
  public static final int ODIN_PROTOCOL_VER=2; 
  public static final int PTTP_PROTOCOL_VER=2; 
  
  public static final Integer TESTNET_FIRST_BLOCK = 547660;  //Testnet
  public static final String PPK_ODIN_MARK_PUBKEY_HEX_TESTNET="02d173743cd0d94f64d241d82a42c6ca92327c443e489f3842464a4df118d4920a";//1PPkT1hoRbnvSRExCeNoP4s1zr61H12bbg : For testnet
  
  public static final Integer MAINNET_FIRST_BLOCK = 426896;  //Mainnet
  public static final String PPK_ODIN_MARK_PUBKEY_HEX_MAINNET="0320a0de360cc2ae8672db7d557086a4e7c8eca062c0a5a4ba9922dee0aacf3e12";//1PPkPubRnK2ry9PPVW7HJiukqbSnWzXkbi : For Mainnet
  
  public static String PPK_ODIN_MARK_PUBKEY_HEX=null;
  
  public static final byte PPK_PUBKEY_TYPE_FLAG=(byte)3;  //ODINЭ�������Ϣ����ʹ�õĹ�Կ����ǰ׺ȡֵ
  public static final byte PPK_PUBKEY_LENGTH=33;  //ODINЭ�������Ϣ����ʹ�õĵ�����Կ����
  public static final byte PPK_PUBKEY_EMBED_DATA_MAX_LENGTH=31;  //ODINЭ���ڵ�����Կ�����Ƕ�����Ϣ���ݳ���
  
  public static final int MAX_MULTISIG_TX_NUM = 2; //һ����������֧�ֵ������������ǩ�������Ŀ��������Ϊ2�����������ܻᱻ���ر�����ܾ�
  public static final int MAX_N = 3;   //����ǩ��1-OF-N�еĲ���N���������������Ϊ3�����������ܻᱻ���ر�����ܾ�
  public static final int MAX_OP_RETURN_LENGTH = 75; //OP_RETURN�ܴ�����ݵ�����ֽ���
  //public static final int MAX_ODIN_DATA_LENGTH=(MAX_N-2)*PPK_PUBKEY_EMBED_DATA_MAX_LENGTH+(MAX_N-1)*PPK_PUBKEY_EMBED_DATA_MAX_LENGTH*(MAX_MULTISIG_TX_NUM-1)+MAX_OP_RETURN_LENGTH;  //֧��Ƕ���ODIN��������ֽ���
  
  
  public static final Byte FUNC_ID_ODIN_REGIST='R'; 
  public static final Byte FUNC_ID_ODIN_UPDATE='U'; 

  public static final Byte DATA_TEXT_UTF8= 'T'; //normal text in UTF-8
  public static final Byte DATA_BIN_GZIP = 'G'; //Compressed by gzip
  public static final Byte DATA_BIN_DEFLATE = 'D'; //Compressed by deflate
  
  public static final String ODIN_CMD_UPDATE_BASE_INFO ="BI";
  public static final String ODIN_CMD_UPDATE_AP_SET ="AP";
  public static final String ODIN_CMD_UPDATE_VD_SET ="VD";
  public static final String ODIN_CMD_CONFIRM_UPDATE ="CU";
  public static final String ODIN_CMD_TRANS_REGISTER ="TR";  
  
  public static final String ODIN_STATUS_PENDING = "pending";       //"�ȴ���������¼"
  public static final String ODIN_STATUS_VALID = "valid";         //"����"
  public static final String ODIN_STATUS_INVALID = "invalid";       //"�д���"
  
  public static final String ODIN_UPDATE_STATUS_RECEIPTING = "receipting";    //"������";
  public static final String ODIN_UPDATE_STATUS_AWAITING = "awaiting";      //"��ͬ�����"
  
  public static final String ODIN_BASE_SET_REGISTER="register";
  public static final String ODIN_BASE_SET_ADMIN="admin";
  public static final String ODIN_BASE_SET_AUTH="auth";
  public static final String ODIN_BASE_SET_PNS_URL="pns_url";
  public static final String ODIN_BASE_SET_PNS_PARSER="pns_parser";
  
  public static final String ODIN_SET_VD_TYPE="type";
  public static final String ODIN_SET_VD_PUBKEY="pubkey";
  public static final String ODIN_SET_VD_CERT_URI="cert_uri"; //�����ֶΣ����ڲ���
  public static final String ODIN_SET_VD_ENCODE_TYPE_PEM = "PEM";   
  public static final String ODIN_SET_VD_ENCODE_TYPE_BASE64 = "BASE64";  
  
  public static final Byte DATA_CATALOG_UNKNOWN= 0; //Unkown Data,reserved
  
  public static final String ODIN_EXT_KEY_DID_DOC = "x_did";
  

  public static String PTTP_INTEREST="pttp";
  

  public static final String PTTP_KEY_VER="ver";
  public static final String PTTP_KEY_URI="uri";
  
  public static final String PTTP_KEY_OPTION="option";

  public static final String PTTP_KEY_SPEC="spec";
  public static final String PTTP_KEY_SPEC_NONE="none"; //��ǩ��
  public static final String PTTP_KEY_SPEC_PAST="past."; //����ǩ��������PASTPAST�淶
  public static final String PTTP_KEY_SPEC_PAST_HEADER_V1_PUBLIC="v1.public."; //����PASTPAST�淶v1�汾�Ͳ��ù�Կ����ǩ��
  
  public static final String PTTP_KEY_METAINFO="metainfo";
  public static final String PTTP_KEY_IAT="iat";
  public static final String PTTP_KEY_STATUS_CODE="status_code";
  public static final String PTTP_KEY_STATUS_DETAIL="status_detail";
  public static final String PTTP_KEY_CONTENT_ENCODING="content_encoding";
  public static final String PTTP_KEY_CONTENT_TYPE="content_type";
  public static final String PTTP_KEY_CONTENT_LENGTH="content_length";
  public static final String PTTP_KEY_CACHE_AS_LATEST="cache_as_latest";
  
  public static final String PTTP_KEY_CONTENT="content";
  public static final String PTTP_KEY_SIGNATURE="signature";
  
  public static final String PTTP_SIGN_MARK_INTEREST  =  "INTEREST";
  public static final String PTTP_SIGN_MARK_DATA  =  "DATA";
  
  public static final int PTTP_STATUS_CODE_OK = 200;
  public static final int PTTP_STATUS_CODE_LOCAL_ERROR = 775;  //PPk����ı����쳣����

  public static final String JSON_KEY_PPK_URI="ppk-uri";
  public static final String JSON_KEY_PPK_VALIDATION="validation";
  public static final String JSON_KEY_CHUNK_BYTES="chunk";
  public static final String JSON_KEY_CHUNK_TYPE="chunk-type";
  public static final String JSON_KEY_CHUNK_LENGTH="chunk-length";
  public static final String JSON_KEY_CHUNK_URL="chunk-url";
  public static final String JSON_KEY_EXP_UTC="exp-utc";
  public static final String JSON_KEY_FROM_CACHE="from-cache";

  public static final String JSON_KEY_ORIGINAL_RESP="original-resp";
  
  public static final String DEFAULT_CACHE_AS_LATEST="public,max-age=600"; //Ĭ����Ϊ���°汾�Ļ���ʱ����10����
  public static final String IGNORE_CACHE_AS_LATEST="no-store"; //����Ϊ���°汾����
  
  public static final long CACHE_NO_EXP_UTC=0; //���ڻ��������ʱ�����û�о���ʧЧʱ�䣬�����ϵͳ�洢���������������ܻ��泤��ʱ��
  
  public static final int PTTP_VALIDATION_OK        = 0;
  public static final int PTTP_VALIDATION_IGNORED   = 1;
  public static final int PTTP_VALIDATION_ERROR     = 2;
  
  //��ʱ�����ľ��ֶ�
  //public static String JSON_KEY_PPK_ALGO="algo";
  //public static String JSON_KEY_PPK_SIGN_BASE64="sign_base64";
  
  //Dat
  public static String[] DAT_DOWNLOAD_URL_LIST={"http://tool.ppkpub.org/dat/?uri=dat://","https://datbase.org/download/"}; 
  
  //IPFS
  //public static String IPFS_API_ADDRESS="/ip4/tool.ppkpub.org/tcp/5001"; //"https://ipfs.infura.io:5001"
  public static String IPFS_DOWNLOAD_URL="http://tool.ppkpub.org:8080/ipfs/";//"https://ipfs.infura.io/ipfs/";
  
  //Bytom File System
  public static String BTMFS_PROXY_URL="http://btmdemo.ppkpub.org/btmfs/"; //Test service
  
  //P2P
  public static boolean enableP2P = false;
  public static String  p2pSetJSON = null;   
  
  //Charset
  public static String PPK_TEXT_CHARSET="UTF-8";  //�����ı�����
  public static String BINARY_DATA_CHARSET="ISO-8859-1";  //����ԭʼ�������������ַ������ͼ��ת��

  //Extension
  final public static String EXT_PEER_WEB="PeerWeb";  //PeerWeb��չ�ӿڶ���
  
  //Local Settings
  final public static String LOCAL_SET_PASSWORD_SHA256_HEX="password_sha256_hex";
  
  private static PPkActivity mMainActivity=null;
  private static String mDefaultConfigFileName = "default_config";

  
  public static void init(PPkActivity main_activity) {
	mMainActivity=main_activity;
    
    try {
        PackageManager packageManager = main_activity.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
        		main_activity.getPackageName(), 0);
        stableVersion = packageInfo.versionName;
        version = stableVersion + developeVersion;
        if(developeVersion.length()>0) {
        	versionUpdateURL=developeVersionUpdateURL;
        }

    } catch (NameNotFoundException e) {
        e.printStackTrace();
    }
    
    File cacheDir =  main_activity.getCacheDir();
    cacheDirPrefix = cacheDir.getAbsolutePath()+"/" ;
    
    reloadUserDefinedSet();
  }
  
  public static void reloadUserDefinedSet() {
    String strTemp;
    
    try {
      strTemp=mMainActivity.getPrivateData(mDefaultConfigFileName);
      
      JSONObject obj_config=new JSONObject(strTemp);

      strTemp = obj_config.optString("Homepage") ;
      if(strTemp!=null && strTemp.length()>0 )
    	  ppkDefaultHomepage =  strTemp ;
      
      strTemp = obj_config.optString("StandardFeeSatoshi") ;
      if(strTemp!=null && strTemp.length()>0 )
    	  ppkStandardDataFee = Integer.parseInt( strTemp );
      
      strTemp = obj_config.optString("Debug") ;
      if(strTemp!=null && strTemp.length()>0 )
    	  debugKey = Integer.parseInt( strTemp );

      strTemp = obj_config.optString("ClearNetCacheWhenStart") ;
      if(strTemp!=null && strTemp.length()>0 )
    	  NetCache.mClearNetCacheWhenStart = Integer.parseInt( strTemp )!=0;
      /*
      String str_notif="StandardFeeSatoshi: "+ppkStandardDataFee;
      if(NetCache.mClearNetCacheWhenStart) {
    	  str_notif += "\n ClearNetCacheWhenStart: true";
      }
      if(debugKey!=0) {
    	  str_notif += "\n Debug mode enabled";
      }
      
      Toast.makeText( mMainActivity.getWindow().getContext(),str_notif, Toast.LENGTH_SHORT).show();
      */
    } catch (Exception e) {
      //System.out.println("Config.loadUserDefined() error:"+ e.toString() );
      Toast.makeText( mMainActivity.getWindow().getContext(),"Config.loadUserDefined() error:"+ e.toString(), Toast.LENGTH_SHORT).show();
	    }    
	  }
 
  public static String getUserDefinedSet(String set_name) {
    String strTemp=null;
    try {
      strTemp=mMainActivity.getPrivateData(mDefaultConfigFileName);
      JSONObject obj_config=new JSONObject(strTemp);

      strTemp=obj_config.optString(set_name) ;
    } catch (Exception e) {
      strTemp = null;
    }    
    
    if(strTemp==null || strTemp.length()==0 ) {
  	  if("StandardFeeSatoshi".equalsIgnoreCase(set_name)) {
  		  strTemp=ppkStandardDataFee.toString();
  	  }else if("Homepage".equalsIgnoreCase(set_name)) {
  		  strTemp=ppkDefaultHomepage;
  	  }else if("Debug".equalsIgnoreCase(set_name)) {
  		  strTemp=""+debugKey;
  	  }
  	  
    }
    
    return strTemp;
  }
  
  public static boolean saveUserDefinedSet(String set_name,String set_value) {
    String strTemp;
    try {
      strTemp=mMainActivity.getPrivateData(mDefaultConfigFileName);
      JSONObject obj_config= ( strTemp!=null && strTemp.length()>0 ) ? new JSONObject(strTemp): new JSONObject() ;

      obj_config.put(set_name ,set_value) ;
      //Toast.makeText( mMainActivity.getWindow().getContext(),"Save "+set_name+":"+set_value, Toast.LENGTH_SHORT).show();
      
      if(mMainActivity.putPrivateData(mDefaultConfigFileName,obj_config.toString())) {
          reloadUserDefinedSet();
          return true;
      }else {
    	  return false;
      }
    } catch (Exception e) {
      //System.out.println("Config.loadUserDefined() error:"+ e.toString() );
      Toast.makeText( mMainActivity.getWindow().getContext(),"Config.loadUserDefined() error:"+ e.toString(), Toast.LENGTH_SHORT).show();
      return false;
    }    
  }
  
  
  public static boolean setWalletProtectPassword( byte[] password ) {
	  if(password==null || password.length==0) {
		  return false;
	  }
	  try {
		byte[] password_sha256 =  Coder.encryptSHA256(password);
		
		return saveUserDefinedSet(LOCAL_SET_PASSWORD_SHA256_HEX, Util.bytesToHexString(password_sha256));

	  } catch (Exception e) {
		// TODO Auto-generated catch block
		return false;
	  }
  }
  
  public static boolean isWalletPasswordProtected(  ) {
	  String tmpstr = getUserDefinedSet( LOCAL_SET_PASSWORD_SHA256_HEX );
	  return tmpstr!=null && tmpstr.length()>0 ;
  }
  
  //��֤���ر�������
  public static boolean verifyWalletProtectPassword( byte[] password ) {
	  if(password==null || password.length==0) {
		  return false;
	  }
	  
	  try {
		  byte[] password_sha256 =  Coder.encryptSHA256(password);
		  
		  String existed_password_sha256_hex=getUserDefinedSet( LOCAL_SET_PASSWORD_SHA256_HEX );
		  
		  if(existed_password_sha256_hex!=null 
		     && existed_password_sha256_hex.equalsIgnoreCase(Util.bytesToHexString(password_sha256)) ) {
			  return true;
		  }
	  }catch (Exception e) {
		  // TODO Auto-generated catch block
	  }
	  
	  return false;
  }
  
  //���ݱ�Ӧ�õ���˽����
  public static String exportLocalProtectedData( byte[] password ) {
	  if(password==null || password.length==0) {
		  return null;
	  }
	  
	  try {
		  byte[] password_sha256 =  Coder.encryptSHA256(password);
		  
		  String existed_password_sha256_hex=getUserDefinedSet( LOCAL_SET_PASSWORD_SHA256_HEX );
		  
		  if(existed_password_sha256_hex!=null 
		     && existed_password_sha256_hex.equalsIgnoreCase(Util.bytesToHexString(password_sha256)) ) {
			  JSONObject tmpObjLocalPrivateData=new JSONObject();
			  tmpObjLocalPrivateData.put( BitcoinWallet.mWalletName , BitcoinWallet.getBackupData()) ;
			  tmpObjLocalPrivateData.put( ODIN.mSetFileName , ODIN.getBackupData()) ;
			  tmpObjLocalPrivateData.put( ResourceKey.mResKeyFileName , ResourceKey.getBackupData()) ;
			  //tmpObjLocalPrivateData.put( AssetWallet.mResKeyFileName , AssetWallet.getBackupData()) ;
			  
			  return tmpObjLocalPrivateData.toString();
		  }
	  }catch (Exception e) {
		  // TODO Auto-generated catch block
	  }
	  
	  return null;
  }

  //�ӱ������ݻָ�
  public static boolean restoreLocalProtectedData( byte[] password,String strLocalPrivateData ) {
	  if(password==null || password.length==0) {
		  return false;
	  }
	  
	  try {
		  byte[] password_sha256 =  Coder.encryptSHA256(password);
		  
		  String existed_password_sha256_hex=getUserDefinedSet( LOCAL_SET_PASSWORD_SHA256_HEX );
		  
		  if(existed_password_sha256_hex!=null 
		     && existed_password_sha256_hex.equalsIgnoreCase(Util.bytesToHexString(password_sha256)) ) {
			  JSONObject tmpObjLocalPrivateData=new JSONObject(strLocalPrivateData);
			  if(tmpObjLocalPrivateData.has( BitcoinWallet.mWalletName)) {
				  BitcoinWallet.restoreBackupData(tmpObjLocalPrivateData.optString( BitcoinWallet.mWalletName)) ;
			  }
			  if(tmpObjLocalPrivateData.has( ODIN.mSetFileName)) {
				  ODIN.restoreBackupData(tmpObjLocalPrivateData.optString( ODIN.mSetFileName)) ;
			  }
			  if(tmpObjLocalPrivateData.has( ResourceKey.mResKeyFileName)) {
				  ResourceKey.restoreBackupData(tmpObjLocalPrivateData.optString( ResourceKey.mResKeyFileName)) ;
			  }
			  
			  //if(tmpObjLocalPrivateData.has( AssetWallet.mResKeyFileName)) {
			  //  AssetWallet.restoreBackupData(tmpObjLocalPrivateData.optString( AssetWallet.mResKeyFileName)) ;
			  //}
			  
			  return true;
		  }
	  }catch (Exception e) {
		  // TODO Auto-generated catch block
	  }
	  
	  return false;
  }
}
