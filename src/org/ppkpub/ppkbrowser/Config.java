package org.ppkpub.ppkbrowser;

import org.json.JSONObject;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class Config {
  //name
  public static String appName = "PPk浏览器内测版";
  //public static String defaultLang = "EN";
  
  public static String PPK_ROOT_ODIN_PARSE_API_URL  = "http://tool.ppkpub.org/odin/";  //解析根标识的服务API
  public static String PPK_ROOT_ODIN_PARSE_API_SIGN_ALGO ="SHA256withRSA" ;//解析根标识的签名算法
  public static String PPK_ROOT_ODIN_PARSE_API_SIGN_PUBKEY ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDTPD2Kkey5UIgOtlbu/wM8JGiTxNKF6fF4YQPU\r\niSR0tIoJWjqdMwL3AY36nJ2zp1VOzIbZGMrgKJTVu8YrNO2sLLTaaIsjaVk3mYRfCXq1tNbE1tyb\r\nyOORvNrcxlPIYHHT428C/aWm8wZ1MY/Ybru4zEPlMBj/ZHZ9yBYZ3vy4wQIDAQAB\r\n"; //对标识解析结果的验证公钥
  /*
  public static String PPK_ROOT_ODIN_PARSE_API_URL  = "http://test.ppkpub.org:8088/"; //备用的根标识解析服务API
  public static String PPK_ROOT_ODIN_PARSE_API_SIGN_ALGO ="" ;
  public static String PPK_ROOT_ODIN_PARSE_API_SIGN_PUBKEY ="";
  */
  public static String PPK_URI_PREFIX = "ppk:";
  public static String DIDPPK_URI_PREFIX = "did:"+PPK_URI_PREFIX;
  public static String PPK_URI_RESOURCE_MARK="#";
  public static String ppkDefaultHomepage  = "ppk:0/";
  public static String ppkSettingPage      = "about:settings";
  public static String ppkSettingPageFileURI="file:///android_asset/settings.html";
  
  public static boolean debugKey = false;
  
  public static String  jdbcURL      = null;
  public static String  proxyURL     = "http://tool.ppkpub.org/odin/proxy.php";
  
  //version
  public static String stableVersion = "x.x.x" ;
  public static String versionUpdateURL = "https://github.com/ppkpub/PPkBrowserAndroid/raw/master/bin/version.json";
  
  public static String developeVersion  = "" ; //:01
  //public static String developeVersionUpdateURL   = "http://tool.ppkpub.org/autoupdate/ppkbrowser/version_test.json";
  public static String developeVersionUpdateURL   = "http://192.168.62.99:8081/autoupdate/ppkbrowser/version_local.json";

  public static String version = stableVersion + developeVersion;
  
  //public static int majorVersionDB = 1;

  //public static String defaultSqliteFile = null;  
  
  //bitcoin
  public static boolean useDustTX = true;
  public static Integer dustSize = 1000;
  //public static Integer minOrderMatchBTC = 100000;
  //public static Integer minFee = 10000;
  public static Integer maxFee = 99999;  //Avoid sending too much btc than normal fee
  public static Integer dataValue = 0;
  public static Integer btc_unit = 100000000;
  
  public static Integer ppkStandardDataFee = 1000;
 
  //PPk
  public static long ppkToolCreationTime = 1400561240-1;  //UTC 2014-5-20 04:47:20
  public static Integer firstBlock = 0;  
  
  public static int ODIN_PROTOCOL_VER=1; 
  
  public static Integer TESTNET_FIRST_BLOCK = 547660;  //Testnet
  public static String PPK_ODIN_MARK_PUBKEY_HEX_TESTNET="02d173743cd0d94f64d241d82a42c6ca92327c443e489f3842464a4df118d4920a";//1PPkT1hoRbnvSRExCeNoP4s1zr61H12bbg : For testnet
  
  public static Integer MAINNET_FIRST_BLOCK = 426896;  //Mainnet
  public static String PPK_ODIN_MARK_PUBKEY_HEX_MAINNET="0320a0de360cc2ae8672db7d557086a4e7c8eca062c0a5a4ba9922dee0aacf3e12";//1PPkPubRnK2ry9PPVW7HJiukqbSnWzXkbi : For Mainnet
  
  public static String PPK_ODIN_MARK_PUBKEY_HEX=null;
  
  public static byte PPK_PUBKEY_TYPE_FLAG=(byte)3;  //ODIN协议承载消息内容使用的公钥类型前缀取值
  public static byte PPK_PUBKEY_LENGTH=33;  //ODIN协议承载消息内容使用的单条公钥长度
  public static byte PPK_PUBKEY_EMBED_DATA_MAX_LENGTH=31;  //ODIN协议在单条公钥中最多嵌入的消息数据长度
  
  public static int MAX_MULTISIG_TX_NUM = 2; //一条交易里能支持的最大数量多重签名输出条目，建议设为2，如果过大可能会被比特币网络拒绝
  public static int MAX_N = 3;   //多重签名1-OF-N中的参数N最大数量，建议设为3，如果过大可能会被比特币网络拒绝
  public static int MAX_OP_RETURN_LENGTH = 75; //OP_RETURN能存放数据的最大字节数
  //public static int MAX_ODIN_DATA_LENGTH=(MAX_N-2)*PPK_PUBKEY_EMBED_DATA_MAX_LENGTH+(MAX_N-1)*PPK_PUBKEY_EMBED_DATA_MAX_LENGTH*(MAX_MULTISIG_TX_NUM-1)+MAX_OP_RETURN_LENGTH;  //支持嵌入的ODIN数据最大字节数
  
  
  public static Byte FUNC_ID_ODIN_REGIST='R'; 
  public static Byte FUNC_ID_ODIN_UPDATE='U'; 

  public static Byte DATA_TEXT_UTF8= 'T'; //normal text in UTF-8
  public static Byte DATA_BIN_GZIP = 'G'; //Compressed by gzip
  
  public static String ODIN_CMD_UPDATE_BASE_INFO ="BI";
  public static String ODIN_CMD_UPDATE_AP_SET ="AP";
  public static String ODIN_CMD_UPDATE_VD_SET ="VD";
  public static String ODIN_CMD_CONFIRM_UPDATE ="CU";
  public static String ODIN_CMD_TRANS_REGISTER ="TR";  
  
  public static Byte DATA_CATALOG_UNKNOWN= 0; //Unkown Data,reserved
  

  public static String PTTP_INTEREST="pttp_interest";
  
  public static String JSON_KEY_PPK_DATA="data";
  public static String JSON_KEY_PPK_SIGN="sign";
  public static String JSON_KEY_PPK_URI="ppk-uri";
  public static String JSON_KEY_PPK_ALGO="algo";
  public static String JSON_KEY_PPK_SIGN_BASE64="sign_base64";
  public static String JSON_KEY_PPK_PUBKEY="pubkey";
  public static String JSON_KEY_PPK_CERT_URI="cert_uri";
  public static String JSON_KEY_PPK_VALIDATION="validation";
  public static String JSON_KEY_PPK_CHUNK="chunk";
  public static String JSON_KEY_PPK_CHUNK_TYPE="chunk-type";
  public static String JSON_KEY_PPK_CHUNK_LENGTH="chunk-length";
  public static String JSON_KEY_PPK_CHUNK_URL="chunk-url";
  public static String JSON_KEY_PPK_REGISTER="register";
  public static String JSON_KEY_PPK_ADMIN="admin";
  public static String JSON_KEY_ORIGINAL_RESP="original_resp";
  public static String JSON_KEY_PPK_STATUS_CODE="status_code";
  
  
  public static int PPK_VALIDATION_OK        = 0;
  public static int PPK_VALIDATION_IGNORED   = 1;
  public static int PPK_VALIDATION_ERROR     = 2;
  
  //Dat
  public static String[] DAT_DOWNLOAD_URL_LIST={"http://tool.ppkpub.org/dat/?uri=dat://","https://datbase.org/download/"}; 
  
  //IPFS
  //public static String IPFS_API_ADDRESS="/ip4/tool.ppkpub.org/tcp/5001"; //"https://ipfs.infura.io:5001"
  public static String IPFS_DOWNLOAD_URL="http://tool.ppkpub.org:8080/ipfs/";//"https://ipfs.infura.io/ipfs/";
  
  //Bytom File System
  public static String BTMFS_PROXY_URL="http://btmdemo.ppkpub.org/btmfs/"; //Test service
  
  //Charset
  public static String PPK_TEXT_CHARSET="UTF-8";  //适用文本内容
  public static String BINARY_DATA_CHARSET="ISO-8859-1";  //适用原始二进制数据与字符串类型间的转换
  
  //Extension
  final public static String EXT_PEER_WEB="PeerWeb";  //PeerWeb扩展接口对象
  
  //Local Settings
  final public static String LOCAL_SET_PASSWORD_SHA256_HEX="password_sha256_hex";
  
  private static PPkActivity mMainActivity=null;
  private static String mDefaultConfigFileName = "default_config";
  
  
  
  public static void init(PPkActivity main_activity) {
	mMainActivity=main_activity;
    String strTemp;
    
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
    
    try {
      strTemp=mMainActivity.getPrivateData(mDefaultConfigFileName);
      JSONObject obj_config=new JSONObject(strTemp);

      strTemp = obj_config.optString("StandardFeeSatoshi") ;
      if(strTemp!=null && strTemp.length()>0 )
    	  ppkStandardDataFee = Integer.parseInt( strTemp );
      
      System.out.println("StandardFeeSatoshi:"+ppkStandardDataFee);
      Toast.makeText( mMainActivity.getWindow().getContext(),"StandardFeeSatoshi:"+ppkStandardDataFee, Toast.LENGTH_SHORT).show();
      
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
      Toast.makeText( mMainActivity.getWindow().getContext(),"Save "+set_name+":"+set_value, Toast.LENGTH_SHORT).show();
      
      return mMainActivity.putPrivateData(mDefaultConfigFileName,obj_config.toString());
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
  
  //验证本地保护密码
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
  
  //备份本应用的隐私数据
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
			  tmpObjLocalPrivateData.put( Odin.mSetFileName , Odin.getBackupData()) ;
			  tmpObjLocalPrivateData.put( ResourceKey.mResKeyFileName , ResourceKey.getBackupData()) ;
			  //tmpObjLocalPrivateData.put( AssetWallet.mResKeyFileName , AssetWallet.getBackupData()) ;
			  
			  return tmpObjLocalPrivateData.toString();
		  }
	  }catch (Exception e) {
		  // TODO Auto-generated catch block
	  }
	  
	  return null;
  }

  //从备份数据恢复
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
			  if(tmpObjLocalPrivateData.has( Odin.mSetFileName)) {
				  Odin.restoreBackupData(tmpObjLocalPrivateData.optString( Odin.mSetFileName)) ;
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
