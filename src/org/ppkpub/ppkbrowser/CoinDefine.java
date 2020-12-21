package org.ppkpub.ppkbrowser;

import org.bitcoinj.core.Address;
import org.json.JSONException;
import org.json.JSONObject;

/** 
 * ���ֶ���
 */  
public class CoinDefine {  
    public final static String COIN_NAME_BITCOIN="ppk:btc/";
    public final static String COIN_NAME_BITCOINCASH="ppk:bch/";
    
    public static JSONObject  getCoinDefine(String coin_name) {
    	try {
	    	JSONObject obj_coin_def=new JSONObject();
	    	
	    	if(COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name)) {
				obj_coin_def.put("symbol", "BTC");
	    		obj_coin_def.put("label_en", "Bitcoin");
	    		obj_coin_def.put("label_cn", "���ر�");
	    		obj_coin_def.put("fee", Config.ppkStandardDataFee );  //�󹤷�
	    		obj_coin_def.put("explorer_url", "https://btc.com/");
	    	}else if(COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name)) {
	    		obj_coin_def.put("symbol", "BCH");
	    		obj_coin_def.put("label_en", "BitcoinCash");
	    		obj_coin_def.put("label_cn", "�����ֽ�");
	    		obj_coin_def.put("fee", 1000 );  //�󹤷�
	    		obj_coin_def.put("explorer_url", "https://bch.btc.com/");
	    	}
	    	
	    	return obj_coin_def;
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
    		return null;
		}
    }
    
    //��ʽ��ͳһ�����Ǯ����ַ
    public static String  formatCoinAddress(String address , String coin_name) {
    	if(address==null || address.length()==0) {
    		return "";
    	}
    	String new_address=getStandardCoinAddress( address ,  coin_name);
    	if(new_address.length()>0) {
    		//����Ч����ͨ��ַ
    		return new_address;
    	}else {
			//���԰��¶��Ź�����ȡ��Ӧ��ʵ��Ǯ����ַ
    		return getRealCoinAddressOfODIN(address, coin_name);
		}
    }
    
    //��ñ�׼��Ǯ����ַ
    protected static String  getStandardCoinAddress(String address , String coin_name) {
    	try {
	    	if(COIN_NAME_BITCOIN.equalsIgnoreCase(coin_name)) {
	    		Address.getParametersFromAddress(address);
	    	}else if(COIN_NAME_BITCOINCASH.equalsIgnoreCase(coin_name)) {
	    		if(address.startsWith("q") || address.startsWith("bitcoincash:"))
	    			address=AddressConverterBitcoinCash.toLegacyAddress(address);
	    		
	    		Address.getParametersFromAddress(address);
	    	}
	    	return address;
    	} catch (Exception e) {
			//���԰��¶��Ź�����ȡ��Ӧ��ʵ��Ǯ����ַ
    		return "";
		}
    }
    

    //���԰��¶��Ź�����ȡ��Ӧ��ʵ��Ǯ����ַ
    protected static String getRealCoinAddressOfODIN(String destination,String coin_type) {
  	  try{
  	      if(destination!=null && destination.length()>0){
                destination = ODIN.formatPPkURI(destination,true);
                if(destination!=null){
                    String ap_resp_content = Util.fetchUriContent(destination);
                    System.out.println("************* BitcoinWallet.transaction() ap_resp_content=");
                    System.out.println(ap_resp_content);
                    JSONObject tmp_dest_info = new JSONObject(ap_resp_content);
                    JSONObject x_wallet_list = tmp_dest_info.optJSONObject("x_wallets");
                    if(x_wallet_list!=null){
                        JSONObject tmp_address_set = x_wallet_list.optJSONObject(coin_type);
                        if(tmp_address_set!=null)
                            destination = tmp_address_set.optString("address","");
                        
                        System.out.println("destination="+destination);
                    }
                }
  	      }
  	  } catch(Exception e){
  	      destination="";
  	  }
  	  return getStandardCoinAddress(destination,coin_type);
    }
}  