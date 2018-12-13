package org.ppkpub.ppkbrowser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.lang.Math;

public class Config {
  //name
  public static String appName = "PPkBrowserForAndroid";
  public static String defaultLang = "EN";
  
  public static String PPK_ROOT_ODIN_PARSE_API_URL  = "http://45.32.19.146/odin/";
  public static String PPK_URI_PREFIX = "ppk:";
  public static String PPK_URI_RESOURCE_MARK="#";
  public static String ppkDefaultHomepage      = "ppk:0/";
  
  
  public static boolean debugKey = false;
  
  public static String  jdbcUrl      = "";
  
  //version
  public static Integer majorVersion = 0;
  public static Integer minorVersion = 202;
  public static String version = Integer.toString(majorVersion)+"."+Integer.toString(minorVersion);
  public static Integer majorVersionDB = 1;
  
  public static String defaultSqliteFile = null;  
  
 
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
  public static int MAX_ODIN_DATA_LENGTH=(MAX_N-2)*PPK_PUBKEY_EMBED_DATA_MAX_LENGTH+(MAX_N-1)*PPK_PUBKEY_EMBED_DATA_MAX_LENGTH*(MAX_MULTISIG_TX_NUM-1)+MAX_OP_RETURN_LENGTH;  //支持嵌入的ODIN数据最大字节数
  
  
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
  
  public static int PPK_VALIDATION_OK        = 0;
  public static int PPK_VALIDATION_IGNORED   = 1;
  public static int PPK_VALIDATION_ERROR     = 2;
  
  //IPFS
  public static String IPFS_API_ADDRESS="/ip4/127.0.0.1/tcp/5001"; //"https://ipfs.infura.io:5001"
  public static String IPFS_PROXY_URL="https://ipfs.infura.io/ipfs/";
  
  //Bytom File System
  public static String BTMFS_PROXY_URL="http://45.32.19.146/btmfs/"; //Test service
  
  //Charset
  public static String PPK_TEXT_CHARSET="UTF-8";  //适用文本内容
  public static String BINARY_DATA_CHARSET="ISO-8859-1";  //适用原始二进制数据与字符串类型间的转换

}
