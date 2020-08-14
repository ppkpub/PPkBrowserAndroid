package org.ppkpub.ppkbrowser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;

import android.util.Base64;
import android.util.Log;

public class Util {
	//�ж�һ���ַ�����URI
	public static boolean  isURI(String str){
		return str!=null && str.indexOf(':')>0;
	}
  
	public static String getPage(String urlString) {
	    return getPage(urlString, 1);
	}

	public static String getPage(String urlString, int retries) {
	    try {
	      return CommonHttpUtil.getInstance().getContentFromUrl(urlString);
	    } catch (Exception e) {
	      Log.d("Util","Fetch URL error: "+e.toString());
	    }
	    return null;
	}  

    //��ȡ��ҳ����
  	public static String getPageTitle(String urladdress,String htmlcontent)
  	{
      String temp="";
      try{
        String regex="<[Tt][Ii][Tt][Ll][Ee]>([^</[Tt][Ii][Tt][Ll][Ee]>]*)";
        Pattern pat = Pattern.compile(regex);
        Matcher match = pat.matcher(htmlcontent);
        while (match.find()) {
          int start = match.start();
          int end = match.end();
          temp = htmlcontent.substring(start+7, end);
        }
        
      }catch(Exception ex){
      }
      return temp.length()>0 ? temp:urladdress;
  	}
  	
	
	

  public static byte[] toByteArray(List<Byte> in) {
    final int n = in.size();
    byte ret[] = new byte[n];
    for (int i = 0; i < n; i++) {
      ret[i] = in.get(i);
    }
    return ret;
  }
  
  public static List<Byte> toByteArrayList(byte[] in) {
    List<Byte> arrayList = new ArrayList<Byte>();

    for (byte b : in) {
      arrayList.add(b);
    }
    return arrayList;
  }  
	  
  /**
       * ��ͼƬ�ֽ��������Base64���봦������Data URL
	   * @param  img_type ��ͼƬ�����ͣ��� image/jpeg 
	   * @param  img_bytes �� ͼƬ���ֽ�����
	   * @return Data URL�ַ���
	   */
  public static String imageToBase64DataURL(String img_type, byte[] img_bytes) {
    return "data:"+img_type+";base64,"+Base64.encodeToString( img_bytes, Base64.DEFAULT );
  }
	  
  public static byte[] readInputStream(InputStream inStream) throws Exception{
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    //����һ��Buffer�ַ���
    byte[] buffer = new byte[1024];
    //ÿ�ζ�ȡ���ַ������ȣ����Ϊ-1������ȫ����ȡ���
    int len = 0;
    //ʹ��һ����������buffer������ݶ�ȡ����
    while( (len=inStream.read(buffer)) != -1 ){
        //���������buffer��д�����ݣ��м����������ĸ�λ�ÿ�ʼ����len�����ȡ�ĳ���
        outStream.write(buffer, 0, len);
    }
    //�ر�������
    inStream.close();
    //��outStream�������д���ڴ�
    return outStream.toByteArray();
  }

  /**
   * ƥ���Ƿ�Ϊ����
   * @param str ����Ϊ���ģ�Ҳ������-19162431.1254����ʹ��BigDecimal�Ļ������-1.91624311254E7
   * @return
   * @author yutao
   * @date 2016��11��14������7:41:22
   */
  public static boolean isNumeric(String str) {
    String bigStr;
    try {
        bigStr = new BigDecimal(str).toString();
    } catch (Exception e) {
        return false;//�쳣 ˵�����������֡�
    }
    return true;
  }

  /*
  * Convert byte[] to hex string.��   
  * @param src byte[] data   
  * @return hex string   
  */      
 public static String bytesToHexString(byte[] src){   
     StringBuilder stringBuilder = new StringBuilder("");   
     if (src == null || src.length <= 0) {   
         return null;   
     }   
     for (int i = 0; i < src.length; i++) {   
         int v = src[i] & 0xFF;   
         String hv = Integer.toHexString(v);   
         if (hv.length() < 2) {   
             stringBuilder.append(0);   
         }   
         stringBuilder.append(hv);   
     }   
     return stringBuilder.toString();   
 }   
 
  /**  
   * Convert hex string to byte[]  
   * @param hexString the hex string  
   * @return byte[]  
   */  
  public static byte[] hexStringToBytes(String hexString) {   
	  if (hexString == null) {   
          return null;   
      }
      if (hexString.equals("")) {   
          return new byte[0];   
      }
      
      hexString = hexString.toUpperCase();   
      int length = hexString.length() / 2;   
      char[] hexChars = hexString.toCharArray();   
      byte[] d = new byte[length];   
      for (int i = 0; i < length; i++) {   
          int pos = i * 2;   
          d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
      }   
      return d;   
  }   

  /**  
   * Convert char to byte  
   * @param c char  
   * @return byte  
   */  
  private static byte charToByte(char c) {   
      return (byte) "0123456789ABCDEF".indexOf(c);   
  }  
  
  
//��ȡURI����������
  //���Զ�����301/302ת��õ���������
  public static String  fetchUriContent(String uri){
    try{
      String[] uri_chunks=uri.split(":");
      if(uri_chunks.length<2){
    	Log.d("Util","fetchURI() meet invalid uri:"+uri);
        return null;
      }
      
      if(uri_chunks[0].equalsIgnoreCase("ipfs")){
        return getIpfsData(uri_chunks[1]);
      }else if(uri_chunks[0].equalsIgnoreCase("dat")){
        return getDatData(uri);
      }else if(uri_chunks[0].equalsIgnoreCase("btmfs")){
        return getBtmfsData(uri);
      }else if(uri_chunks[0].equalsIgnoreCase("ppk")){
    	JSONObject obj_decoded_chunk=PTTP.getPPkResource(uri);
        if(obj_decoded_chunk==null)
            return null;
          
        return new String( (byte[])obj_decoded_chunk.opt(Config.JSON_KEY_CHUNK_BYTES) );
      }else if(uri_chunks[0].equalsIgnoreCase("data")){
        int from=uri_chunks[1].indexOf(",");
        if(from>=0){
          return uri_chunks[1].substring(from+1,uri_chunks[1].length());
        } else
          return uri_chunks[1];
      }else{
        return getPage(uri);
      }
    }catch(Exception e){
       Log.d("Util","fetchURI("+uri+") error:"+e.toString());
    }
    return null;
  }
  
  
  
  //ѭ��ɾ��Ŀ¼���ļ����� 
  public static void deleteDir(String file_path,boolean del_self) {
      File fp = new File(file_path);
      deleteDir(fp, del_self);
  }    
  
  public static void deleteDir(File file,boolean del_self) {
    if (file.isDirectory()) {
        for (File f : file.listFiles())
            deleteDir(f,true);
    }
    
    if(del_self)
        file.delete(); 
 }
  
  public static String getBtmfsData(String btmfs_uri){
      String tmp_url=Config.BTMFS_PROXY_URL+"?uri=" + java.net.URLEncoder.encode(btmfs_uri);
      Log.d("Util","Using BTMFS Proxy to fetch:"+ tmp_url);
      
      return getPage(tmp_url);
  }
  
  public static String getIpfsData(String ipfs_hash_address){
	  String tmp_url=Config.IPFS_DOWNLOAD_URL+ipfs_hash_address;
	  Log.d("Util","Using IPFS Proxy to fetch:"+ tmp_url);
      
      return getPage(tmp_url);
    /*try{
      IPFS ipfs = new IPFS(Config.IPFS_API_ADDRESS);
      Multihash filePointer = Multihash.fromBase58(ipfs_hash_address);
      byte[] fileContents = ipfs.cat(filePointer);
      return new String(fileContents);
    }catch(Exception e){
      System.out.println("Util.getIpfsData() error:"+e.toString());
      
      String tmp_url=Config.IPFS_DOWNLOAD_URL+ipfs_hash_address;
      System.out.println("Using IPFS Proxy to fetch:"+ tmp_url);
      
      return getPage(tmp_url);
    }
    */
  }
  
  public static String getDatData(String dat_uri){
      String dat_hash=dat_uri.substring("dat://".length());
      
      String tmp_url=null;
      String tmp_page_result=null;
      for(int kk=0;kk<Config.DAT_DOWNLOAD_URL_LIST.length;kk++){
          tmp_url=Config.DAT_DOWNLOAD_URL_LIST[kk]+dat_hash;
          Log.d("Util","Using Dat Proxy to fetch:"+ tmp_url);
          
          tmp_page_result=getPage(tmp_url);
          if(tmp_page_result!=null && tmp_page_result.length()>0){
              return tmp_page_result;
          }
      }
      return null;
  }
  
  

  //ȱʡ��ȡָ����ַδ���ѽ��׵ķ�������������ǩ�����͵����
  public static List<UnspentOutput> getUnspents(String address,boolean isOdinTransaction) {
    if(!Config.useDustTX){
      UnspentList ul = getUnspentListWithoutDustTX(address);
      return ul.unspents;
    }
    
    if(!isOdinTransaction){ //��ͨת�˽��ף�ֱ�ӷ����������������UTXO�б�
        try {
            UnspentList ul=getValidUnspents(address,false,true);
            return ul.unspents;
        } catch (Exception e) {
            //��API���쳣���Զ��л�����һ������API
            System.out.println("getUnspents() BTC.com API exception:"+e.toString());
            UnspentList ul = getUnspentListWithoutDustTX(address);
            return ul.unspents;
        }
    }
    
    //����ʹ�û������һ�η��ͽ������UTXO
    List<UnspentOutput> unspents = BitcoinWallet.getCachedLastUnspents(address);
    String lastTxHash=null;
    int txCounter=0;
    Double valueCounter=0.0;

    //System.out.println("Util.getUnspents() getCachedLastUnspents:"+unspents);
    if(unspents!=null){
        for (UnspentOutput unspent : unspents) {
            lastTxHash = unspent.txid;
            txCounter++;
            valueCounter += unspent.amt_satoshi.doubleValue();
        }
        System.out.println("Found cached utxo:"+ lastTxHash +" , txCounter="+txCounter+",valueCounter="+valueCounter);
        
        if(valueCounter >  Config.ppkStandardDataFee + (Config.MAX_MULTISIG_TX_NUM+1) * Config.dustSize){
            return unspents;
        }
        
        System.out.println("The cached utxos not enough for new transaction.");
    }
    
    //����API��������ȡ���õ�δ���ѽ����б�
    UnspentList ul = getValidUnspents(address,true, true);
    
    if( ul==null ){
        unspents = new ArrayList<UnspentOutput> () ;
        System.out.println("Util.getUnspents() unspents is empty");
    }else{
        unspents = ul.unspents;
        System.out.println("Util.getUnspents() unspents tx_num="+ul.tx_num+"  sum_satoshi="+ul.sum_satoshi);
    }

    return unspents;
  }
  
  public static UnspentList getValidUnspents(
        String address,
        boolean for_odin_transaction, 
        boolean need_script_detail
  ) {
    List<UnspentOutput> unspents=new ArrayList<UnspentOutput> () ;
    int txCounter=0;
    long valueCounter=0L;
    int total_count=0;
    boolean is_enough=false; 
    
    //�Ȼ�ȡ���õ���ͨδ���ѽ����б�
    UnspentList ulWithoutDustTX = getUnspentListWithoutDustTX(address);
    
    if(ulWithoutDustTX!=null ){
        unspents = ulWithoutDustTX.unspents;
        txCounter = ulWithoutDustTX.tx_num;
        valueCounter = ulWithoutDustTX.sum_satoshi.longValue();
        total_count = ulWithoutDustTX.tx_total_num;
    }
    
    System.out.println("Util.getValidUnspents() ulWithoutDustTX txCounter="+txCounter+"  valueCounter="+valueCounter+"  total_count="+total_count);

    //�ٸ������þ����Ƿ񲹳��ȡ���õ��������ǩ��δ���ѽ����б�
    if(Config.useDustTX){
        String api_url="";
        try {
            String result=null;
            JSONObject tempObject=null;

            api_url="https://chain.api.btc.com/v3/address/" + address + "/unspent"; 
            tempObject=fetchBtcAPI(api_url);
            
            total_count=tempObject.getJSONObject("data").getInt("total_count");
            int pagesize=tempObject.getJSONObject("data").getInt("page_size");
            JSONArray tempArray=tempObject.getJSONObject("data").getJSONArray("list");
            
            if( tempArray.length()<pagesize )
                total_count=tempArray.length(); //����API���ص��쳣����
            else if(total_count>pagesize) //���ڳ���1ҳ��utxo������Ҫ��������ȷ����������ȷ��
                total_count=getRealUnspentCount(address);

            int max_page=(int)(Math.round( Math.ceil((double)total_count/(double)pagesize) ));
            for(int pp=1;pp<=max_page;pp++){
                System.out.println("\nDEBUG20200328 getValidUnspents() page = "+pp+"\n");
                if(pp>1){ //��һҳ����Ҫ�ظ���ȡ��ֱ��ʹ�������ѻ�õ�����
                    api_url="https://chain.api.btc.com/v3/address/" + address + "/unspent?page=" + pp ;
                    tempObject=fetchBtcAPI(api_url);
                    tempArray=tempObject.getJSONObject("data").getJSONArray("list");
                }

                for(int tt=tempArray.length()-1;tt>=0;tt--){
                    JSONObject item_obj=(JSONObject)tempArray.get(tt);

                    UnspentOutput tempUnspentObj=new UnspentOutput();
                    
                    tempUnspentObj.amt_satoshi=BigInteger.valueOf(item_obj.getLong("value"));
                    tempUnspentObj.txid=item_obj.getString("tx_hash");
                    tempUnspentObj.vout=item_obj.getInt("tx_output_n");
                    tempUnspentObj.scriptPubKeyHex="";
                    
                    System.out.println("\nDEBUG20200328 check utxo: "+tempUnspentObj.toString()+" \n");

                    if( !existedUnspent(unspents,tempUnspentObj) ){
                        System.out.println("\nDEBUG20200328 found new utxo\n");
                        if(need_script_detail){
                            try {
                                JSONObject tempObjectTx=fetchBtcAPI("https://blockchain.info/zh-cn/rawtx/" + tempUnspentObj.txid);;
                                JSONArray tempArrayOutputs=tempObjectTx.getJSONArray("out");
                                JSONObject item_output=(JSONObject)tempArrayOutputs.get(tempUnspentObj.vout);

                                tempUnspentObj.scriptPubKeyHex=item_output.getString("script");
                            }catch (Exception e1) {
                                try {
                                  JSONObject tempObjectTx=fetchBtcAPI("https://chain.api.btc.com/v3/tx/" + tempUnspentObj.txid + "?verbose=3");
                                  JSONArray tempArrayOutputs=tempObjectTx.getJSONObject("data").getJSONArray("outputs");
                                  JSONObject item_output=(JSONObject)tempArrayOutputs.get(tempUnspentObj.vout);

                                  tempUnspentObj.scriptPubKeyHex=item_output.getString("script_hex");
                                }catch (Exception e2) {
                                  System.out.println(" getUnspents() api.btc.com: "+e2.toString());
                                }
                            }
                            //System.out.println(">>>>>>>>>>tempUnspentObj["+tt+"]:"+tempUnspentObj.txid+","+tempUnspentObj.amt_satoshi+","+tempUnspentObj.vout+","+tempUnspentObj.scriptPubKeyHex);
                        }
                        
                        if(!need_script_detail || tempUnspentObj.scriptPubKeyHex.length()>0){
                          unspents.add(tempUnspentObj);
                          valueCounter += item_obj.getLong("value");
                          txCounter ++ ;
                        }
                    }
                    
                    if(for_odin_transaction){ //����֯ODIN��Ϣ��Ҫ�ж��Ƿ������㹻��UTXO��
                        if( txCounter>Config.MAX_MULTISIG_TX_NUM+1 
                           && (valueCounter > Config.maxFee || valueCounter >  Config.ppkStandardDataFee + (Config.MAX_MULTISIG_TX_NUM+1) * Config.dustSize))  {  //if enough for max ODIN fee 
                           is_enough = true;
                        }
                    }else if( txCounter >= Config.maxUseUTXO )  {   //����֯��ͨת����Ϣ��Ҫ�ж��Ƿ��Ѵﵽ���UTXO��
                        is_enough = true;
                    }
                    
                    if(is_enough)
                        break;
                }
                
                if(is_enough)
                    break;
            }
        } catch (Exception e) {
          //����APIʱ���쳣��ǰ����
        	System.out.println("getValidUnspents() API("+api_url+" ) exception:"+e.toString());
        }
    }else{
        System.out.println("Util.getValidUnspents() ignored dust UTXO");
    }
    
    if(total_count<txCounter || txCounter==0) //�����쳣������
        total_count = txCounter;
        
    System.out.println("Util.getValidUnspents() result txCounter="+txCounter+"  valueCounter="+valueCounter+"  total_count="+total_count);
    
    return new UnspentList(unspents,txCounter,valueCounter,total_count);
  }
  
  public static boolean existedUnspent(List<UnspentOutput> unspents,UnspentOutput matchUnspentObj){
      for (UnspentOutput unspent : unspents) {
          if(unspent.txid.equalsIgnoreCase(matchUnspentObj.txid)
            && unspent.vout == matchUnspentObj.vout
          )
              return true;
      }
      
      return false;
  }
  
  public static int getRealUnspentCount(String address) {
     try {
        UnspentList ul = getUnspentListWithoutDustTX(address);
        int normal_tx_count = ul.tx_total_num;
        
        System.out.println("\nDEBUG20200328 normal_tx_count = "+normal_tx_count+"\n");
        
        //��btc.com��blockchain.info�ֱ�ȡ�������ֵ���ó���ȷ��utxo��
        BigInteger balanceWithDustTX = getBTCBalanceWithDustTX(address);
        BigInteger balanceWithoutDustTX = getBTCBalanceWithoutDustTX(address);
        
        if(balanceWithDustTX==null || balanceWithoutDustTX==null){
            return normal_tx_count;
        }
        
        int ppk_special_tx_count = ( balanceWithDustTX.subtract(balanceWithoutDustTX).intValue( ) )/1000 ;
        System.out.println("\nDEBUG20200328 ppk_special_tx_count = "+ppk_special_tx_count+"\n");
        
        return normal_tx_count+ppk_special_tx_count;
      } catch (Exception e) {
        //��API���쳣
    	System.out.println("getRealUnspentCount() BTC.com API exception:"+e.toString());
        return -1;
    }
  }
  
  public static JSONObject fetchBtcAPI(String api_url) throws Exception {
    String result=null;
    try {
        //System.out.println("\nCall fetchBtcAPI("+api_url+")\n");
        result = CommonHttpUtil.getInstance().getContentFromUrl( api_url );
        //System.out.println("\n result = "+result+"\n");
        return new JSONObject(result);
    }catch (Exception e) {
        //System.out.println("\n Retry by proxy\n");
        if(Config.proxyURL!=null && Config.proxyURL.length()>0)
            api_url=Config.proxyURL+"?url=" + java.net.URLEncoder.encode(api_url);
        result = CommonHttpUtil.getInstance().getContentFromUrl( api_url );
        return new JSONObject(result);
    }
  }
  
  //ȱʡ��ȡָ��BCH��ַδ���ѽ��׵ķ�������������ǩ�����͵����
  public static List<UnspentOutput> getBitcoinCashUnspents(String address) {
	  List<UnspentOutput> unspents;
	  
	  unspents=getBitcoinCashUnspents(address,null);
	  if(unspents==null){
		  unspents=getBitcoinCashUnspents(address,Config.proxyURL);
	  }
	  return unspents;
  }
  
  public static List<UnspentOutput> getBitcoinCashUnspents(String address,String proxy_url) {
    //����API��������ȡ���õ�δ���ѽ����б�
    List<UnspentOutput>  unspents=new ArrayList<UnspentOutput> ();
    int txCounter=0;
    Double valueCounter=0.0;

    try {
        String result=null;
        JSONObject tempObject=null;

        //API����������������ò�ѯδ���ѽ����б�
       	result = CommonHttpUtil.getInstance().getContentFromUrl( "https://bch-chain.api.btc.com/v3/address/" + address + "/unspent",proxy_url );
       	tempObject=new JSONObject(result);

       	Integer total_count=tempObject.getJSONObject("data").getInt("total_count");
        Integer pagesize=tempObject.getJSONObject("data").getInt("page_size");
        JSONArray utxoArray=tempObject.getJSONObject("data").getJSONArray("list");

        if(total_count>pagesize ){
          result = CommonHttpUtil.getInstance().getContentFromUrl( "https://bch-chain.api.btc.com/v3/address/" + address + "/unspent?page="+ 
                            Math.round( Math.ceil((double)total_count/(double)pagesize) ) );
          JSONArray lastArray=(new JSONObject(result)).getJSONObject("data").optJSONArray("list");
          if(lastArray!=null) {
	       	  for(int tt=lastArray.length()-1;tt>=0;tt--){
	       		utxoArray.put( lastArray.get(tt) ); //�ϲ���һҳ�����һҳ�Ľ����б�����
	    	  }
          }
        }
        
        txCounter=0;
        valueCounter=0.0;
        for(int tt=utxoArray.length()-1;tt>=0;tt--){
            JSONObject item_obj=(JSONObject)utxoArray.get(tt);
            
            UnspentOutput tempUnspentObj=new UnspentOutput();
            
            tempUnspentObj.amt_satoshi=BigInteger.valueOf(item_obj.getLong("value"));
            tempUnspentObj.txid=item_obj.getString("tx_hash");
            tempUnspentObj.vout=item_obj.getInt("tx_output_n");
            tempUnspentObj.scriptPubKeyHex="";
            
            System.out.println("  tempUnspentObj: "+tempUnspentObj.toString());
            

            try {
              result = CommonHttpUtil.getInstance().getContentFromUrl( "https://bch-chain.api.btc.com/v3/tx/" + tempUnspentObj.txid + "?verbose=3",proxy_url );
              //System.out.println("Get https://chain.api.btc.com/v3/tx/" + tempUnspentObj.txid + "?verbose=3\n  result: "+result);
              JSONObject tempObjectTx=new JSONObject(result);
              JSONArray tempArrayOutputs=tempObjectTx.getJSONObject("data").getJSONArray("outputs");
              JSONObject item_output=(JSONObject)tempArrayOutputs.get(tempUnspentObj.vout);

              tempUnspentObj.scriptPubKeyHex=item_output.getString("script_hex");
            }catch (Exception e2) {
              Log.d("Util"," getBitcoinCashUnspents() api.btc.com: "+e2.toString());
            }
              
            if(tempUnspentObj.scriptPubKeyHex.length()>0){
              unspents.add(tempUnspentObj);
              valueCounter += item_obj.getDouble("value");
              txCounter ++ ;
            }
            
            if( txCounter>Config.MAX_MULTISIG_TX_NUM+1 
               && (valueCounter > Config.maxFee || valueCounter >  Config.ppkStandardDataFee + (Config.MAX_MULTISIG_TX_NUM+1) * Config.dustSize))  {  //if enough for max ODIN fee 
              break;
            }
        }
    } catch (Exception e) {
      //��API���쳣���Զ��л�����һ������API
      Log.d("Util","getBitcoinCashUnspents() BTC.com API exception:"+e.toString());
      return null;
    }
    
    return unspents;
  }
/*
  //���õĻ�ȡָ����ַδ���ѽ��׵ķ���������������ǩ�����͵����
  public static UnspentList getUnspentListWithoutDustTX(String address) {
	  UnspentList ul;
	  
	  ul=getUnspentListWithoutDustTX(address,null);
	  if(ul==null){
		  ul=getUnspentListWithoutDustTX(address,Config.proxyURL);
	  }
	  return ul;
  }
*/  
  //���õĻ�ȡָ����ַδ���ѽ��׵ķ���������������ǩ�����͵����
  public static UnspentList getUnspentListWithoutDustTX(String address) {
    List<UnspentOutput> unspents = new ArrayList<UnspentOutput> ();
    int txCounter=0;
    long valueCounter=0L;
    try {
        JSONObject tempResultObject=fetchBtcAPI("https://blockchain.info/unspent?active="+address);
        JSONArray tempArray=tempResultObject.getJSONArray("unspent_outputs");
        ArrayList<HashMap<String, Object>> item_set_array = new ArrayList<HashMap<String, Object>>();
        for(int tt=0;tt<tempArray.length();tt++){
            JSONObject item_obj=(JSONObject)tempArray.get(tt);
            
            UnspentOutput tempUnspentObj=new UnspentOutput();
            tempUnspentObj.amt_satoshi=BigInteger.valueOf(item_obj.getLong("value"));
            tempUnspentObj.txid=item_obj.getString("tx_hash_big_endian");
            tempUnspentObj.vout=item_obj.getInt("tx_output_n");
            //tempUnspentObj.type=item_obj.getString("");
            //tempUnspentObj.confirmations=item_obj.getInt("confirmations");

            //tempUnspentObj.scriptPubKeyAsm="Invalid";
            tempUnspentObj.scriptPubKeyHex=item_obj.getString("script");

            unspents.add(tempUnspentObj);
            valueCounter += item_obj.getLong("value");
            txCounter ++ ;
        }
    } catch (Exception e) {
    	System.out.println(" getUnspentListWithoutDustTX() "+e.toString());
    }
    return new UnspentList(unspents,txCounter,valueCounter,txCounter);
  }
/*
  public static List<UnspentOutput> getUnspentListWithoutDustTX-old(String address,String proxy_url) {
    List<UnspentOutput> unspents = new ArrayList<UnspentOutput> ();
    try {
        String result = CommonHttpUtil.getInstance().getContentFromUrl( "https://blockchain.info/unspent?active="+address,proxy_url);
        //String result="{\"unspent_outputs\":[{\"tx_hash\":\"1b93a91401cc67eacbecdba413bc5ec7bb10bdf6f6f6e4a9e7117bee29f9cb02\",\"tx_hash_big_endian\":\"02cbf929ee7b11e7a9e4f6f6f6bd10bbc75ebc13a4dbeccbea67cc0114a9931b\",\"tx_index\":406599423,\"tx_output_n\":2,\"script\":\"76a914b4e2b540b202be227031921163c419f9fa2224ce88ac\",\"value\":10000,\"value_hex\":\"2710\",\"confirmations\":155}]}";
    	JSONObject tempResultObject=new JSONObject(result);
        JSONArray tempArray=tempResultObject.getJSONArray("unspent_outputs");
        ArrayList<HashMap<String, Object>> item_set_array = new ArrayList<HashMap<String, Object>>();
        for(int tt=0;tt<tempArray.length();tt++){
            JSONObject item_obj=(JSONObject)tempArray.get(tt);
            
            UnspentOutput tempUnspentObj=new UnspentOutput();
            tempUnspentObj.amt_satoshi=BigInteger.valueOf(item_obj.getLong("value"));
            tempUnspentObj.txid=item_obj.getString("tx_hash_big_endian");
            tempUnspentObj.vout=item_obj.getInt("tx_output_n");
            //tempUnspentObj.type=item_obj.getString("");
            //tempUnspentObj.confirmations=item_obj.getInt("confirmations");

            //tempUnspentObj.scriptPubKeyAsm="Invalid";
            tempUnspentObj.scriptPubKeyHex=item_obj.getString("script");

            unspents.add(tempUnspentObj);
        }
    } catch (Exception e) {
        Log.d("Util"," getUnspentListWithoutDustTX() "+e.toString());
    }
    return unspents;
  }
*/
  /*
  public static TransactionInfo getTransaction(String txHash) {
    
    try {
      String result = CommonHttpUtil.getInstance().getContentFromUrl(transactionAddress(txHash));
    	
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      TransactionInfo transactionInfo = objectMapper.readValue(result, new TypeReference<TransactionInfo>() {});
      return transactionInfo;
    } catch (Exception e) {
      Log.d("Util","getTransaction() "+e.toString());
      return null;
    }
  }
  */
  public static String transactionAddress(String txHash) {
    return "https://api.biteasy.com/blockchain/v1/transactions/"+txHash;
  }

  public static BigInteger getBTCBalance(String address) {
    if(!Config.useDustTX){
      return getBTCBalanceWithoutDustTX(address);
    }
    
    return getBTCBalanceWithDustTX(address);
  }
  
  public static BigInteger getBTCBalanceWithDustTX(String address) {
    try {
      JSONObject tempResultObject=fetchBtcAPI("https://chain.api.btc.com/v3/address/"+address);
      tempResultObject=tempResultObject.getJSONObject("data");

      return  BigInteger.valueOf(tempResultObject.getLong("balance"));
    } catch (Exception e) {
      //return getBTCBalanceWithoutDustTX(address);
      return null;
    }
  }

  public static BigInteger getBTCBalanceWithoutDustTX(String address) {
    try {
      JSONObject addressInfo=fetchBtcAPI("https://blockchain.info/zh-cn/address/"+address+"?format=json&limit=0");
      return BigInteger.valueOf(addressInfo.getLong("final_balance"));
    } catch (Exception e) {
      System.out.println("getBTCBalanceWithoutDustTX() "+e.toString());
      return BigInteger.ZERO;
    }
  }
  
  //������Ч�Ĺ�Կ���ݿ���Ƕ��ָ������������
  public static byte[] generateValidPubkey(String data_str){
    System.out.println("Util.generateValidPubkey() data_str="+data_str);
    byte[] data = null;
    
    try {
      data = data_str.getBytes(Config.BINARY_DATA_CHARSET);
      
      return generateValidPubkey(data);
    } catch (Exception e) {
      return null;
    }
  }
  
  public static byte[] generateValidPubkey(byte[] data){
    if(data.length>Config.PPK_PUBKEY_EMBED_DATA_MAX_LENGTH){
      System.out.println("The data segment length should be less than " + Config.PPK_PUBKEY_EMBED_DATA_MAX_LENGTH);
      return null;
    }
    
    List<Byte> dataArrayList = new ArrayList<Byte>();
    
    try {
      dataArrayList = Util.toByteArrayList(data);
      
      for(int kk=dataArrayList.size();kk<Config.PPK_PUBKEY_EMBED_DATA_MAX_LENGTH;kk++){
        dataArrayList.add((byte)0x20); //׷�ӿո�
      }
    } catch (Exception e) {
      return null;
    }

    dataArrayList.add(0,(byte) data.length ); 
    dataArrayList.add(0, Config.PPK_PUBKEY_TYPE_FLAG); 
    
    while(dataArrayList.size()<Config.PPK_PUBKEY_LENGTH)
      dataArrayList.add((byte)0x20);
    
    data = Util.toByteArray(dataArrayList);

    return  data;
    /*
    for(int bb=0;bb<256;bb++){
      try{
        data[data.length-1]=(byte) bb;
        ECKey tmp_key_more=ECKey.fromPublicOnly(data);
        System.out.println("Try["+bb+"]:"+(char)bb+" ok : "+tmp_key_more.toString());
        return  data;
      } catch (Exception e) {
        //System.out.println("Meet pubkey error:"+e.toString());
      }
    }
    return null;
    */
  } 
  
  public static String shortAddressView(String address){
	  if(address==null || address.length()<=15)
		  return address;
	  else
		  return address.substring(0, 8)+"..."+address.substring( address.length()-4 );
  }

  
  public static long getNowTimestamp() {
    return (new Date()).getTime()/(long)1000;
  }
  
  //���쳣��������ջ׷����Ϣ���浽�ַ�����
  public static String printStackTraceToString(Throwable t) {
  	StringWriter sw = new StringWriter();
  	t.printStackTrace(new PrintWriter(sw, true));
  	return sw.getBuffer().toString();
  }
}
