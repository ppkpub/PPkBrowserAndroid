package org.ppkpub.ppkbrowser;

import android.util.Log;
import java.math.BigInteger;

import org.bitcoinj.core.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

public class OdinTransctionData {
    public String source=null;
    public String destination=null;
    public BigInteger amount_satoshi=null;
    public BigInteger fee_satoshi=null;
    public String mark_hex=null;
    public String data_hex=null;
    
    public String strSignedTxHex; //被签名的数据
    
	public OdinTransctionData(String source, String destination, BigInteger amount_satoshi, BigInteger fee_satoshi, String mark_hex,String data_hex){
		init(source, destination, amount_satoshi, fee_satoshi, mark_hex,data_hex);
    }
    
    public OdinTransctionData(String odin_data_json ){
        try{
            JSONObject objOdinData=new JSONObject(odin_data_json);
            
            init(
        		objOdinData.getString("source"),
                objOdinData.optString("destination",""),
                BigInteger.valueOf(Config.dustSize),
                BigInteger.valueOf(objOdinData.optLong("fee_satoshi",Config.ppkStandardDataFee)),
                objOdinData.optString("mark_hex",""),
                objOdinData.optString("data_hex","")
            );
        }catch (Throwable e) {
        	Log.d("OdinTransctionData", "error: "+e.toString());
        }
    }
    
    private void init(String source, String destination, BigInteger amount_satoshi, BigInteger fee_satoshi, String mark_hex,String data_hex){
    	this.source=source;
        this.destination=destination;
        this.amount_satoshi=amount_satoshi;
        this.fee_satoshi=fee_satoshi;
        this.mark_hex=mark_hex;
        this.data_hex=data_hex;
    }

    public String toJSONString(){
        try {
        	JSONObject objOdinData = new  JSONObject();     
			objOdinData.put("source",this.source);
			objOdinData.put("destination",this.destination);  
	        objOdinData.put("amount_satoshi",this.amount_satoshi.longValue());  
	        objOdinData.put("fee_satoshi",this.fee_satoshi.longValue());  
	        objOdinData.put("mark_hex",this.mark_hex);  
	        
	        if(this.data_hex!=null)
                objOdinData.put("data_hex",this.data_hex);
            else
                objOdinData.put("data_hex","");
	        
	        return objOdinData.toString();
		} catch (JSONException e) {
			return null;
		}  
    }
    
    public String toString(){
        String tmp_str="";    
        tmp_str += "source:"+this.source+"\n";
        tmp_str += "destination:"+this.destination+"\n";
        tmp_str += "amount_satoshi:"+this.amount_satoshi+"\n";
        tmp_str += "fee_satoshi:"+this.fee_satoshi+"\n";
        tmp_str += "mark_hex:"+this.mark_hex+"\n";
        
        if(this.data_hex!=null)
            tmp_str += "data:"+new String( Util.hexStringToBytes(this.data_hex) )+"\n";
        
        return tmp_str;
    }
    
    public Transaction genSignedTransction() throws Exception{
    	Transaction tx = BitcoinWallet.transaction(this);
        strSignedTxHex=Util.bytesToHexString( tx.bitcoinSerialize() );
        return tx;
    }
    
    public String  genSignedTransctionHex()  throws Exception {
    	Transaction tx=genSignedTransction();
    	
    	return strSignedTxHex;
    }
}
