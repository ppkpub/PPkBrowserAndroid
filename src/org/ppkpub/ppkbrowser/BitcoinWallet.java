package org.ppkpub.ppkbrowser;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import org.json.JSONArray;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.spongycastle.util.encoders.Hex;

import org.json.JSONException;
import org.json.JSONObject;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
//import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;

import android.util.Log;
import android.widget.Toast;


public class BitcoinWallet  {
  final public static String mWalletName = "resources_wallet_bitcoin";
	
  public static NetworkParameters params;
  
  private static PPkActivity mMainActivity=null;
  private static Wallet mWallet=null;
  private static JSONObject mObjWalletSet;

  public static String  statusMessage = "";
 
  private static HashMap<String,List<UnspentOutput>> cachedLastUnspentList=new HashMap<String,List<UnspentOutput>>();


  public static void init(PPkActivity main_activity ) {
      //Locale.setDefault(new Locale("en", "US"));
	  mMainActivity=main_activity;
      params = MainNetParams.get();

      try {
    	mWallet = new Wallet(params);
    	mObjWalletSet=new JSONObject();
    	
    	String  wallet_json=mMainActivity.getPrivateData(mWalletName);
    	//Log.d("BitcoinWallet","wallet_json="+wallet_json);
        if (wallet_json!=null) {
          statusMessage = Language.getLangLabel("Found wallet data"); 
          Log.d("BitcoinWallet",statusMessage);
          
          try{
        	  mObjWalletSet=new JSONObject(wallet_json); 
        	  
        	  JSONObject objAddressList=mObjWalletSet.getJSONObject("addresses");
        	  
        	  Iterator iterator = objAddressList.keys();
        	  while(iterator.hasNext()){
				  String address = (String) iterator.next();
				  
				  JSONObject objAddress = objAddressList.getJSONObject(address);
				  String tmp_prv_key_wif = objAddress.getString("private_key_wif");
				  importPrivateKey(tmp_prv_key_wif);
        	  }
          }catch(Exception ex){
        	  Log.d("BitcoinWallet","Load wallet data failed!"+ex.toString());
          }
        } else {
          statusMessage = Language.getLangLabel("Creating new wallet file"); 
          Log.d("BitcoinWallet",statusMessage);
          
          mObjWalletSet.put("encrypted",false);
          //generateNewAddress();
        }
            
      } catch (Exception e) {
        Log.d("BitcoinWallet","Error during init: "+e.toString());
        //e.printStackTrace();
        //System.exit(-1);
      }
    
  }
  
  public static boolean restoreBackupData( String data  ) {
	  try{
		  JSONObject tmpObjWalletSet=new JSONObject(data); 
    	  if(! tmpObjWalletSet.has("addresses"))
    		  return false;
    	  mObjWalletSet=tmpObjWalletSet;
    	  saveWallet();
    	  init( mMainActivity );
    	  return true;
	  } catch (Exception e) {
        Log.d("BitcoinWallet","RestoreBackupData failed: "+e.toString());
        //e.printStackTrace();
      }
	  
	  return false;
  }
  
  public static String getBackupData(  ) {
	  return mMainActivity.getPrivateData(mWalletName);
  }  
  
  public static boolean saveWallet() {
	  try{
		String wallet_json=mObjWalletSet.toString();
	    mMainActivity.putPrivateData(mWalletName,wallet_json);
		return true;
	  } catch (Exception e) {
        Log.d("BitcoinWallet","Save failed: "+e.toString());
        //e.printStackTrace();
        Toast.makeText( mMainActivity.getWindow().getContext(),"BitcoinWallet save failed:"+e.toString(), Toast.LENGTH_SHORT).show();
        return false;
      }
	  
  }
  
  //在本地钱包里新生成一个新地址
  public static String generateNewAddress() throws Exception{
	ECKey newKey = new ECKey();
        
	return importPrivateKey(newKey);
  }

  public static String importPrivateKey(ECKey key) throws Exception {
    String address = "";
    Log.d("BitcoinWallet","Importing private key");
    address = key.toAddress(params).toString();
    Log.d("BitcoinWallet","Importing address "+address);
    if (mWallet.getImportedKeys().contains(key)) {
    	mWallet.removeKey(key);
    }
    
    if( mWallet.importKey(key) ){
	    String private_key_wif=key.getPrivateKeyAsWiF(MainNetParams.get());
	    String pub_key_hex=key.getPublicKeyAsHex();
	    
	    JSONObject tmpObjAddress=new JSONObject();
	    tmpObjAddress.put("private_key_wif", private_key_wif);
	    tmpObjAddress.put("pub_key_hex", pub_key_hex);
	    
	    JSONObject objAddressList=mObjWalletSet.optJSONObject("addresses");
	    if(objAddressList==null)
	    	objAddressList = new JSONObject();
	    
	    objAddressList.put(address, tmpObjAddress);
	    mObjWalletSet.put("addresses", objAddressList);
	    
	    if(!mObjWalletSet.has("default"))
	    	mObjWalletSet.put("default", address);
	    
	    saveWallet();
	    return address;
    }else{
    	return null;    
    }
  }
  
  public static String importPrivateKey(String privateKey) throws Exception {
    DumpedPrivateKey dumpedPrivateKey;
    String address = "";
    ECKey key = null;
    Log.d("BitcoinWallet","Importing private key in WIF format");
    try {
      dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privateKey);
      key = dumpedPrivateKey.getKey();
      return importPrivateKey(key);
    } catch (AddressFormatException e) {
      throw new Exception(e.getMessage());
    }
  }

  public static Transaction transaction(OdinTransctionData odin_tx_data) throws Exception {
      return transaction(
        odin_tx_data.source,
        odin_tx_data.destination,
        odin_tx_data.amount_satoshi,
        odin_tx_data.fee_satoshi,
        odin_tx_data.mark_hex,
        Util.hexStringToBytes(odin_tx_data.data_hex )
      );
  }

  public static Transaction transaction(String source, String destination, BigInteger amount_satoshi, BigInteger fee, String markPubkeyHexStr,byte[] data) throws Exception {
	  return transaction(source, destination, amount_satoshi, fee, markPubkeyHexStr,data,false);
  }

  public static Transaction transaction(String source, String destination, BigInteger amount_satoshi, BigInteger fee, String markPubkeyHexStr,byte[] data,boolean isBitcoinCash) throws Exception {
    Transaction tx = new Transaction(params);
    
    int max_op_return_length=Config.MAX_OP_RETURN_LENGTH;
    int max_tx_num =  Config.MAX_MULTISIG_TX_NUM ; 
    int max_multisig_n = Config.MAX_N;
    int ppk_pubkey_embed_data_max_length=Config.PPK_PUBKEY_EMBED_DATA_MAX_LENGTH;
    
    
    if(isBitcoinCash) {
    	//	tx.setVersion(0x40);//for bitcoincash ，不需要设置
    	max_op_return_length=215;
    }

    int max_odin_data_length=(max_multisig_n-2)*ppk_pubkey_embed_data_max_length+(max_multisig_n-1)*ppk_pubkey_embed_data_max_length*(max_tx_num-1)+max_op_return_length;  //支持嵌入的ODIN数据最大字节数;
    
    
    if (!destination.equals("") && amount_satoshi.compareTo(BigInteger.valueOf(Config.dustSize))<0) {
      tx.verify();
      return tx;
    }

    List<Byte> dataArrayList =  Util.toByteArrayList(data);
    
    int odin_data_length = dataArrayList.size();
    boolean isOdinTransaction = odin_data_length>0; //20200329

    BigInteger totalOutput = fee;
    BigInteger totalInput = BigInteger.ZERO;
    
    JSONArray arrayInputUnspents=new JSONArray();

    try {
      if (!destination.equals("") && amount_satoshi.compareTo(BigInteger.ZERO)>0) {
        totalOutput = totalOutput.add(amount_satoshi);
        tx.addOutput(Coin.valueOf(amount_satoshi.longValue()), new Address(params, destination));
      }
    } catch (AddressFormatException e) {
      throw new Exception("Failed to generate output for "+destination);
    }

    ECKey source_key=null; 

    for (ECKey key : mWallet.getImportedKeys()) {
        try {
          if (key.toAddress(params).equals(new Address(params, source))) {
            source_key=key;
            break;
          }
        } catch (AddressFormatException e) {

        }
    }
    
    if(null==source_key)
       return  null;

    //组织多重交易和OpReturn来嵌入所需存放的数据
    if(isOdinTransaction){
      int from = 0;
    
      if(markPubkeyHexStr!=null && markPubkeyHexStr.length()>0) { //如果markPubkeyHexStr参数有效，则表示使用多重签名
	      
	      for (int tt=0; tt==0 || (tt<max_tx_num && from < odin_data_length - max_op_return_length);tt++ ) {
	        List<ECKey> keys = new ArrayList<ECKey>();
	        keys.add(source_key);
	        
	        if(tt==0){ //第一条多重交易的第二个公钥固定为指定特征公钥
	          keys.add(ECKey.fromPublicOnly(Util.hexStringToBytes(markPubkeyHexStr)));
	        }
	        
	        for(int mm=keys.size(); 
	            mm<max_multisig_n && ( ( tt==0 && from < odin_data_length ) || ( tt>0 && from < odin_data_length - max_op_return_length) );
	            mm++,from += ppk_pubkey_embed_data_max_length){
	          int embed_data_length=Math.min(ppk_pubkey_embed_data_max_length, odin_data_length-from); 
	          
	          List<Byte> chunk = new ArrayList<Byte>(dataArrayList.subList(from, from+embed_data_length ));
	          
	          byte[] tmp_pub_key=Util.generateValidPubkey(Util.toByteArray(chunk));
	          
	          if(tmp_pub_key==null){
	              throw new Exception("Unable to generate valid pubkey for embedding data.Please change your request contents!");
	          }
	          
	          keys.add(new ECKey(null,tmp_pub_key));
	        }
	
	        Script script = ScriptBuilder.createMultiSigOutputScript(1, keys);
	        tx.addOutput(Coin.valueOf(BigInteger.valueOf(Config.dustSize).longValue()), script);
	        totalOutput = totalOutput.add(BigInteger.valueOf(Config.dustSize));
	      }
      }
      
      //使用op_return对应的备注脚本空间来嵌入剩余ODIN数据
      int last_data_length= odin_data_length-from;
      
      if(last_data_length>max_op_return_length){
        throw new Exception("Too big embed data.(Should be less than "+max_odin_data_length+" bytes)");
      }else if( last_data_length>0 ){
        List<Byte> chunk = new ArrayList<Byte>(dataArrayList.subList(from, odin_data_length));
        chunk.add(0,(byte) last_data_length);
        chunk.add(0,(byte) 0x6a);
        Script script = new Script(Util.toByteArray(chunk));
        tx.addOutput(Coin.valueOf(BigInteger.valueOf(0).longValue()), script);
      }
    }
    
    List<UnspentOutput> unspents = isBitcoinCash ? 
                                       Util.getBitcoinCashUnspents(source) : Util.getUnspents(source,isOdinTransaction);
                                       
    List<Script> inputScripts = new ArrayList<Script>();      

    Boolean atLeastOneRegularInput = false;
    Integer usedUnspents=0;
    for (UnspentOutput unspent : unspents) {
      String txHash = unspent.txid;

      //byte[] scriptBytes = Hex.decode(unspent.scriptPubKeyHex.getBytes(Charset.forName(Config.BINARY_DATA_CHARSET)));
      byte[] scriptBytes = Util.hexStringToBytes(unspent.scriptPubKeyHex);
      Script script = new Script(scriptBytes);
      //if it's sent to an address and we don't yet have enough inputs or we don't yet have at least one regular input, or if it's sent to a multisig
      //in other words, we sweep up any unused multisig inputs with every transaction

      try {
        if(
          unspent.amt_satoshi.compareTo( BigInteger.valueOf(0) ) > 0 //Avoid TX data exception,20200307
          &&(
            (script.isSentToAddress() && (totalOutput.compareTo(totalInput)>0 || !atLeastOneRegularInput)) 
            || (script.isSentToMultiSig() && ((usedUnspents<2 && !atLeastOneRegularInput)||(usedUnspents<3 && atLeastOneRegularInput ) || fee.compareTo(BigInteger.valueOf(Config.maxFee))==0 ) )
          )
        ){
          if(
        	   mWallet.getTransaction(new Sha256Hash(txHash))==null || mWallet.getTransaction(new Sha256Hash(txHash)).getOutput(unspent.vout).isAvailableForSpending() 
            ) {
            if (script.isSentToAddress()) {
              atLeastOneRegularInput = true;
            }

            Sha256Hash sha256Hash =  new Sha256Hash(txHash);  
            TransactionOutPoint txOutPt = new TransactionOutPoint(params, unspent.vout, sha256Hash);
            
            System.out.println("Spending "+sha256Hash+" "+unspent.vout);
            totalInput = totalInput.add(unspent.amt_satoshi);
            TransactionInput input = new TransactionInput(params, tx, new byte[]{}, txOutPt);
            tx.addInput(input);
            arrayInputUnspents.put(unspent);
            inputScripts.add(script);
            
            usedUnspents++;  //20200307
          }
        }
                  
        if( usedUnspents>=3 && totalInput.compareTo(totalOutput)>=0 )
          //use max 3 unspents  to lower transaction size if possible
          break;
      } catch (Exception e) {
        Log.d("BitcoinWallet","Error during transaction creation: "+e.toString());
        e.printStackTrace();
        throw new Exception("Error during transaction creation: "+e.toString());
      }
    }

    if (!atLeastOneRegularInput) {
      throw new Exception("Not enough standard unspent outputs to cover transaction.");
    }

    if (totalInput.compareTo(totalOutput)<0) {
      Log.d("BitcoinWallet","Not enough inputs. Output: "+totalOutput.toString()+", input: "+totalInput.toString());
      throw new Exception("Not enough BTC to cover transaction of "+String.format("%.8f",totalOutput.doubleValue()/Config.btc_unit)+" BTC.");
    }
    BigInteger totalChange = totalInput.subtract(totalOutput);

    try {
      if (totalChange.compareTo(BigInteger.ZERO)>0) {
        tx.addOutput(Coin.valueOf(totalChange.longValue()), new Address(params, source));
      }
    } catch (AddressFormatException e) {
      throw new Exception("Failed to generate charge output for "+source);
    }

    for (int i = 0; i<tx.getInputs().size(); i++) {

      Script script = inputScripts.get(i);
      TransactionInput input = tx.getInput(i);
      
      UnspentOutput unspent=(UnspentOutput)(arrayInputUnspents.get(i));

      TransactionSignature txSig = isBitcoinCash ? 
    		  tx.calculateWitnessSignature(i, source_key, script, Coin.valueOf(unspent.amt_satoshi.longValue())  , SigHash.ALL, false)
    		  : tx.calculateSignature(i, source_key, script, SigHash.ALL, false);

      if (script.isSentToAddress()) {
        input.setScriptSig(ScriptBuilder.createInputScript(txSig, source_key));
      } else if (script.isSentToMultiSig()) {
        //input.setScriptSig(ScriptBuilder.createMultiSigInputScript(txSig));
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(0);
        builder.data(txSig.encodeToBitcoin());
        input.setScriptSig(builder.build());
      }
    	
    }
    
    tx.verify();

    return tx;
  }

  public static String sendTransaction(String source, String signed_tx_hex) throws Exception {
	  Transaction tx = new Transaction( params , Util.hexStringToBytes(signed_tx_hex));
	  String result=CommonHttpUtil.getInstance().getContentFromUrl(Config.PPK_API_URL+"broadcast.php?hex="+signed_tx_hex);
	  
	  if(result!=null && result.startsWith("OK") ){
		  cacheLastUnspentTransaction(source,tx);
		  return tx.getHashAsString();
	  }else
		  return null;
	  //return sendTransaction(source, tx);
  }
  
  public static String sendBchTransaction(String source, String signed_tx_hex) throws Exception {
	  Transaction tx = new Transaction( params , Util.hexStringToBytes(signed_tx_hex));
	  String broadcast_url="https://pool.viabtc.com/res/tools/broadcast";
	  String post_data="{\"coin\":\"BCH\",\"raw_tx\":\""+signed_tx_hex+"\"}";
	  //String post_data="{\"mimeType\": \"application/json;charset=UTF-8\",\"text\": \"{\\\"coin\\\":\\\"BCH\\\",\\\"raw_tx\\\":\\\""+signed_tx_hex+"\\\"}\"}";
	  
	  System.out.println("broadcast_url="+broadcast_url+"\npost_data="+post_data);
	  String result=CommonHttpUtil.getInstance().sendPostContent(broadcast_url, post_data, "application/json;")  ;
	  System.out.println("result="+result);
	  if(result!=null && result.contains("\"code\": 0") ){
		  //cacheLastUnspentTransaction(source,tx);
		  return tx.getHashAsString();
	  }else
		  return null;
	  //return sendTransaction(source, tx);
  }
  
  /*
  public static boolean sendTransaction(String source, Transaction tx) throws Exception {
    try {
      System.out.println("Try to send transaction:");
      System.out.println(tx.toString());
      
      //待完善调用远端API发送交易
      //CommonHttpUtil.getSourceFromUrl("http://btmdemo.ppkpub.org/odin/broadcast.php?hex="+);

      cacheLastUnspentTransaction(source,tx); 
      return true;
    } catch (Exception e) {
      throw new Exception(Language.getLangLabel("Transaction timed out. Please try again.")+"[2]");
    }    
  }
  */
  //缓存指定地址的最新未花费交易，优先作为下一次交易输入使用,20181220
  public static boolean cacheLastUnspentTransaction(String source,Transaction tx){
    List<UnspentOutput>  lastUnspents = new ArrayList<UnspentOutput> ();
    try{
        int vout=0;
        for (TransactionOutput out : tx.getOutputs()) {
            Script script = out.getScriptPubKey();
            List<ScriptChunk> asm = script.getChunks();
            int asm_num = asm.size();
            BigInteger amount_satoshi = BigInteger.valueOf(out.getValue().getValue());
            
            //System.out.println("vout:"+vout+"\nasm:"+asm.toString()+"\nbtcAmount:"+amount_satoshi);
        
            //如果金额大于0，且是多重签名输出或者是包含指定地址的普通输出，则是有效的UTXO
            boolean isValidOut=false;
            if(amount_satoshi!=BigInteger.ZERO){
                if (asm_num>=5 && asm.get(0).equalsOpCode(0x51) && asm.get(asm_num-2).isOpCode() && asm.get(asm_num-1).equalsOpCode(0xAE)) { 
                    //MULTISIG
                    isValidOut=true;
                }else{
                    Address dest_address = script.getToAddress(params);
                    String destination = dest_address.toString();
                    
                    if(source.equalsIgnoreCase(destination) )
                        isValidOut=true;
                }
                
            }
            
            if(isValidOut){
                UnspentOutput tempUnspentObj=new UnspentOutput();
                
                tempUnspentObj.amt_satoshi=amount_satoshi;
                tempUnspentObj.txid=tx.getHashAsString();
                tempUnspentObj.vout=vout;
                tempUnspentObj.scriptPubKeyHex=Util.bytesToHexString(script.getProgram());
                
                System.out.println("Cache["+source+"]'s utxo: " +tempUnspentObj.toString());
            
                if(tempUnspentObj.scriptPubKeyHex.length()>0){
                  lastUnspents.add(tempUnspentObj);
                }
            }
            
            vout++;
        }
        
        cachedLastUnspentList.put(source,lastUnspents);
        
        return true;
    }catch(Exception e){
        return false;
    }
  }
  
  //获取缓存的最近一次指定地址的未花费交易输出
  public static List<UnspentOutput> getCachedLastUnspents(String source){
    try{  
        return (List<UnspentOutput>) cachedLastUnspentList.get(source);
    }catch(Exception e){
        
    }
    return null;
  }
  
  //清空缓存的未花费交易输出
  public static void clearCachedLastUnspents(){
	  cachedLastUnspentList.clear();
  }


  //获取当前地址列表 2019-01-15
  public static List<String> getAddresses() {
    List<ECKey> keys = mWallet.getImportedKeys();
    List<String> addresses = new ArrayList<String>();
    for(ECKey key : keys) {
      addresses.add(key.toAddress(params).toString());
    }
    return addresses;
  }
  
  //获得当前使用的BTC地址
  public static String getDefaultAddress() {
    return mObjWalletSet.optString("default", null);
  }
  
  //设置当前使用的BTC地址
  public static boolean setDefaultAddress(String address) {
	try {
		mObjWalletSet.put("default", address);
		saveWallet();
		return true;
	} catch (JSONException e) {
		return false;
	}
  }
  
  public static String getAddressOfPrviteKey(String privateKey){
	try {
      DumpedPrivateKey dumpedPrivateKey;
	  ECKey key = null;
		
	  dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privateKey);
	  key = dumpedPrivateKey.getKey();
	  return  key.toAddress(params).toString();
	} catch (AddressFormatException e) {
	  return null;
	}
  }
  
  //获得指定地址的概要数据
  public static JSONObject getAddressSummary(String address) {
    try {
      String result = CommonHttpUtil.getInstance().getContentFromUrl( "http://tool.ppkpub.org/odin/summary.php?address=" +address);
    	
      JSONObject tempResultObject=new JSONObject(result);
      if("OK".equalsIgnoreCase( tempResultObject.getString("status") ))
         return  tempResultObject;
    } catch (Exception e) {
    }
    
    return null;
  }
  
//获得指定BCH地址的概要数据
  public static JSONObject getBchAddressSummary(String address) {
    try {
      String result = CommonHttpUtil.getInstance().getContentFromUrl( "http://tool.ppkpub.org/odin/summary_bch.php?address=" +address);
    	
      JSONObject tempResultObject=new JSONObject(result);
      if("OK".equalsIgnoreCase( tempResultObject.getString("status") ))
         return  tempResultObject;
    } catch (Exception e) {
    }
    
    return null;
  }
  
  /**
   * @param msg 要签名的信息
   * @param address 签名用的地址
   * @return base64编码的签名
   */
  public static String signMsg(String msg,String address) {
	ECKey source_key=null; 

    for (ECKey key : mWallet.getImportedKeys()) {
        try {
          if (key.toAddress(params).equals(new Address(params, address))) {
            source_key=key;
            break;
          }
        } catch (AddressFormatException e) {

        }
    }
    
    if(null==source_key)
       return null;

    return source_key.signMessage(msg);
  }
  
  /**
   * @param msg 明文
   * @param signatureMsg 签名好的信息(base64编码)
   * @param pubkey 公钥(HEX编码)
   * @return
   */
  public static boolean verifyMessage(String msg,String signature_of_msg, String pubkey_hex) {
      boolean result = false;
      ECKey ecKey = ECKey.fromPublicOnly(Util.hexStringToBytes(pubkey_hex));
      try {
          ecKey.verifyMessage(msg, signature_of_msg);
          result = true;
      } catch (SignatureException e) {
          result = false;
          e.printStackTrace();
      } 
      
      return result;
  }
  
  //获得指定地址的公钥（HEX编码）
  public static String getPubkeyHex(String address) {
	ECKey source_key=null; 

    for (ECKey key : mWallet.getImportedKeys()) {
        try {
          if (key.toAddress(params).equals(new Address(params, address))) {
            source_key=key;
            break;
          }
        } catch (AddressFormatException e) {

        }
    }
    
    if(null==source_key)
       return null;
    return source_key.getPublicKeyAsHex() ;
  }
  
  //获得指定地址的私钥（HEX编码）
  public static String getPrvkeyHex(String address) {
	ECKey source_key=null; 

    for (ECKey key : mWallet.getImportedKeys()) {
        try {
          if (key.toAddress(params).equals(new Address(params, address))) {
            source_key=key;
            break;
          }
        } catch (AddressFormatException e) {

        }
    }
    
    if(null==source_key)
       return null;
    return source_key.getPrivateKeyAsHex() ;
  }
}
