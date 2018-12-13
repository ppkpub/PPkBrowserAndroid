package org.ppkpub.ppkbrowser;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.HashMap;


import org.json.JSONArray; 
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

public class APoverETH {
  public static String fetchInterest(String ap_url, String interest) {
    try {
        //just for demo
        JSONObject objInterest=new JSONObject(interest);
        interest=objInterest.getJSONObject("interest").getString("uri");
    }catch (Exception e) {

    }
    String str_ap_resp_json=null;
    Log.d("APoverETH","APoverETH.fetchInterest("+ap_url+","+interest+") ...");

    //ȱʡ���� infura.io �ṩ��json-rpc���񣬿����л���geth���ṩ��json-rpc���� http://localhost:8545
    HashMap<String,String> mapEthNetworkJsonRPCs=new HashMap<String,String>();
    mapEthNetworkJsonRPCs.put("mainnet","https://mainnet.infura.io/2M0Ezt8fWNsDZ6wLOAaT");
    mapEthNetworkJsonRPCs.put("ropsten","https://ropsten.infura.io/2M0Ezt8fWNsDZ6wLOAaT");
    mapEthNetworkJsonRPCs.put("infuranet","https://infuranet.infura.io/2M0Ezt8fWNsDZ6wLOAaT");
    mapEthNetworkJsonRPCs.put("kovan","https://kovan.infura.io/2M0Ezt8fWNsDZ6wLOAaT");
    mapEthNetworkJsonRPCs.put("rinkeby","https://rinkeby.infura.io/2M0Ezt8fWNsDZ6wLOAaT");
    
    JsonRpcHttpClient client;
    try {
        //Parse the ap_url like "ethap:rinkeby/0x5c65aab68834c518460a77b32daf5be6ce9fcad7/0xd3317d25"
        String[] arrayTmp=ap_url.split(":");
        arrayTmp=arrayTmp[1].split("/");
        String strEthNet=arrayTmp[0];
        String strEthContractAddress=arrayTmp[1];
        String strEthContractFunctionHash=arrayTmp[2];
        
        //Create the json request object
        JSONObject jsonRequest = new JSONObject();
        
        JSONObject jsonContractInput = new  JSONObject();     
        jsonContractInput.put("to",strEthContractAddress);   
        jsonContractInput.put("data", 
            strEthContractFunctionHash
            +"0000000000000000000000000000000000000000000000000000000000000020"
            +toABIHex(interest)
          );
        
        JSONArray jsonParams = new JSONArray();
        jsonParams.put(jsonContractInput);
        jsonParams.put("latest");
        
        jsonRequest.put("jsonrpc","2.0");
        jsonRequest.put("method", "eth_call"); //��ʱֻ֧�ֲ�Ӱ��������״̬��call���������������������ӵ���send_transaction
        jsonRequest.put("params", jsonParams);
        jsonRequest.put("id", UUID.randomUUID().hashCode());
        
        System.out.println("APoverETH.fetchInterest() jsonRequest:"+jsonRequest.toString());
        
        //ʵ���������ַ��ע�����˵�ַ������
        URL destRpcUrl = new URL( mapEthNetworkJsonRPCs.get( strEthNet ) );
        HttpURLConnection connection = (HttpURLConnection) destRpcUrl.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.connect();

        OutputStream out = connection.getOutputStream();

        out.write(jsonRequest.toString().getBytes());
        out.flush();
        out.close();

        int statusCode = connection.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
          //ͨ����������ȡ����������
          InputStream inStream = connection.getInputStream();
          //�õ����������ݣ��Զ����Ʒ�װ�õ����ݣ�����ͨ����
          byte[] data = Util.readInputStream(inStream);
          
          if(data!=null){
            String str_resp=new String(data);
            System.out.println("APoverETH.fetchInterest() str_resp:"+str_resp);
            JSONObject obj_resp=new JSONObject(str_resp);
            String str_result_hex=obj_resp.getString("result");
            
            str_ap_resp_json=getFirstSegmentFromABIHex(str_result_hex);
          }
        }
    }catch (Throwable e) {
    	Log.d("APoverETH-ERROR","APoverETH.fetchInterest() error: "+e.toString());
    }

    System.out.println("APoverETH.fetchInterest() str_ap_resp_json:"+str_ap_resp_json);
    return str_ap_resp_json;
  }

  //����̫��ABI�淶��һ���ַ���ת��ΪHEX�ı�
  protected static String toABIHex(String input) throws Exception{
    String strHexABI="";
    byte[] data = null;
    List<Byte> dataArrayList = new ArrayList<Byte>();

    try {
      data = input.getBytes(Config.BINARY_DATA_CHARSET);
      dataArrayList = Util.toByteArrayList(data);
    } catch (UnsupportedEncodingException e) {
      return null;
    }
    
    int data_length = dataArrayList.size();
    if( data_length > 0 ){
        //�������ݳ�������ͷHEX
        byte[] tmpLenBytes=new byte[32];
        tmpLenBytes[30]=(byte)(data_length/256);
        tmpLenBytes[31]=(byte)(data_length%256);
        strHexABI=strHexABI+Util.bytesToHexString(tmpLenBytes);
        
        //���ɾ����ַ�������HEX�ı�
        strHexABI=strHexABI+Util.bytesToHexString(data);
    }
    
    //����ַ������ֽڳ��Ȳ���32�ֽڵ���������������Ӧ����0
    if( data_length == 0 || data_length % 32 != 0 ){
      for (int from = data_length % 32; from < 32;from++) {
        strHexABI=strHexABI+"00";
      }
    }
    
    System.out.println("strHexABI:"+strHexABI);
    return strHexABI;
  }
  
  //����̫��ABI�淶��HEX�ı�����ȡ����һ�ε��ַ�����ע����ʼ��0x
  protected static String getFirstSegmentFromABIHex( String str_hex ) throws Exception{
    byte[] tmpBytes=Util.hexStringToBytes(str_hex);
    List<Byte> dataArrayList =  Util.toByteArrayList(tmpBytes);
    
    int content_len = tmpBytes[64] & 0xFF |  
            (tmpBytes[63] & 0xFF) << 8 |  
            (tmpBytes[62] & 0xFF) << 16 |  
            (tmpBytes[61] & 0xFF) << 24;  
    System.out.println("content_len="+content_len);    
    
    byte[] chunk = Util.toByteArray( new ArrayList<Byte>(dataArrayList.subList(65, 65+content_len) ) );
    
    return new String(chunk,Config.PPK_TEXT_CHARSET); 
  }
}