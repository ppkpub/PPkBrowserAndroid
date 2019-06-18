package org.ppkpub.ppkbrowser;

import org.json.JSONException;
import org.json.JSONObject;

/** 
 * 币种定义
 */  
public class CoinDefine {  
    public final static String COIN_NAME_BITCOIN="BITCOIN";
    public final static String COIN_NAME_BITCOINCASH="ppk:bch/";
    
    public static JSONObject  getCoinDefine(String coin_name) {
    	try {
	    	JSONObject obj_coin_def=new JSONObject();
	    	
	    	if(COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name)) {
				obj_coin_def.put("symbol", "BTC");
	    		obj_coin_def.put("label_en", "Bitcoin");
	    		obj_coin_def.put("label_cn", "比特币");
	    		obj_coin_def.put("fee", Config.ppkStandardDataFee );  //矿工费
	    		obj_coin_def.put("explorer_url", "https://btc.com/");
	    	}else if(COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name)) {
	    		obj_coin_def.put("symbol", "BCH");
	    		obj_coin_def.put("label_en", "BitcoinCash");
	    		obj_coin_def.put("label_cn", "比特现金");
	    		obj_coin_def.put("fee", 1000 );  //矿工费
	    		obj_coin_def.put("explorer_url", "https://bch.btc.com/");
	    	}
	    	
	    	return obj_coin_def;
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
    		return null;
		}
    }
}  