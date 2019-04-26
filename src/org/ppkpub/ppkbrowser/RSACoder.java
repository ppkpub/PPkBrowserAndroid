package org.ppkpub.ppkbrowser;

import java.security.Key;  
import java.security.KeyFactory;  
import java.security.KeyPair;  
import java.security.KeyPairGenerator;  
import java.security.PrivateKey;  
import java.security.PublicKey;  
import java.security.Signature;  
import java.security.interfaces.RSAPrivateKey;  
import java.security.interfaces.RSAPublicKey;  
import java.security.spec.PKCS8EncodedKeySpec;  
import java.security.spec.X509EncodedKeySpec;  

import org.json.JSONObject;
//import java.util.HashMap;  
//import java.util.Map;  
  
import javax.crypto.Cipher;  
  
/**  
 * @author liangdong 
 * @version 1.0 
 * @since 1.0 
 */  
public abstract class RSACoder extends Coder {  
    public static final String KEY_ALGORITHM = "RSA";  
    public static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";  
    public static final String DEFAULT_FORMAT="BASE64";
  
    private static final String PUBLIC_KEY = "RSAPublicKey";  
    private static final String PRIVATE_KEY = "RSAPrivateKey";  
    
    public static String PEM_PUB_HEAD="-----BEGIN PUBLIC KEY-----";
    public static String PEM_PUB_END="-----END PUBLIC KEY-----";
  
    /** 
     * ��˽Կ����Ϣ��������ǩ�� 
     *  
     * @param data 
     *            �������� 
     * @param privateKey 
     *            ˽Կ 
     * @param sign_algo 
     *            ָ���㷨����� 
     *            https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Signature
     * @return 
     * @throws Exception 
     */  
    public static String sign(byte[] data, String privateKey) throws Exception {  
      return sign(data, privateKey,DEFAULT_SIGNATURE_ALGORITHM);
    }
    public static String sign(byte[] data, String privateKey,String sign_algo) throws Exception {  
        // ������base64�����˽Կ  
        byte[] keyBytes = decryptBASE64(privateKey);  
  
        // ����PKCS8EncodedKeySpec����  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
  
        // KEY_ALGORITHM ָ���ļ����㷨  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
  
        // ȡ˽Կ�׶���  
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);  
  
        // ��˽Կ����Ϣ��������ǩ��  
        Signature signature = Signature.getInstance(sign_algo);  
        signature.initSign(priKey);  
        signature.update(data);  
  
        return encryptBASE64(signature.sign());  
    }  
  
    /** 
     * У������ǩ�� 
     *  
     * @param data 
     *            �������� 
     * @param publicKey 
     *            ��Կ 
     * @param sign 
     *            ����ǩ�� 
     *  
     * @return У��ɹ�����true ʧ�ܷ���false 
     * @throws Exception 
     *  
     */  
    public static boolean verify(byte[] data, String publicKey, String sign)  
            throws Exception {  
       return verify(data, publicKey, sign,DEFAULT_SIGNATURE_ALGORITHM);
    }

    public static boolean verify(byte[] data, String publicKey, String sign,String sign_algo)  
            throws Exception {  
    	
    	publicKey=parseValidPubKey(KEY_ALGORITHM,publicKey);
    	
  
        // ������base64����Ĺ�Կ  
        byte[] keyBytes = decryptBASE64(publicKey);  
  
        // ����X509EncodedKeySpec����  
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);  
  
        // KEY_ALGORITHM ָ���ļ����㷨  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
  
        // ȡ��Կ�׶���  
        PublicKey pubKey = keyFactory.generatePublic(keySpec);  
  
        Signature signature = Signature.getInstance(sign_algo);  
        signature.initVerify(pubKey);  
        signature.update(data);  
  
        // ��֤ǩ���Ƿ�����  
        return signature.verify(decryptBASE64(sign));  
    }  
  
    /** 
     * ����<br> 
     * ��˽Կ���� 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptByPrivateKey(byte[] data, String key)  
            throws Exception {  
        // ����Կ����  
        byte[] keyBytes = decryptBASE64(key);  
  
        // ȡ��˽Կ  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
  
        // �����ݽ���  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);  
  
        return cipher.doFinal(data);  
    }  
  
    /** 
     * ����<br> 
     * �ù�Կ���� 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptByPublicKey(byte[] data, String key)  
            throws Exception {  
        // ����Կ����  
        byte[] keyBytes = decryptBASE64(key);  
  
        // ȡ�ù�Կ  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
  
        // �����ݽ���  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.DECRYPT_MODE, publicKey);  
  
        return cipher.doFinal(data);  
    }  
  
    /** 
     * ����<br> 
     * �ù�Կ���� 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptByPublicKey(byte[] data, String key)  
            throws Exception {  
        // �Թ�Կ����  
        byte[] keyBytes = decryptBASE64(key);  
  
        // ȡ�ù�Կ  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
  
        // �����ݼ���  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
  
        return cipher.doFinal(data);  
    }  
  
    /** 
     * ����<br> 
     * ��˽Կ���� 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptByPrivateKey(byte[] data, String key)  
            throws Exception {  
        // ����Կ����  
        byte[] keyBytes = decryptBASE64(key);  
  
        // ȡ��˽Կ  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
  
        // �����ݼ���  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
  
        return cipher.doFinal(data);  
    }  
  
    /** 
     * ȡ��˽Կ 
     *  
     * @param keyMap 
     * @return 
     * @throws Exception 
     */  
    public static String getPrivateKey(JSONObject keyMap)  
            throws Exception {  
        return  keyMap.getString(PRIVATE_KEY);  
    }  
  
    /** 
     * ȡ�ù�Կ 
     *  
     * @param keyMap 
     * @return 
     * @throws Exception 
     */  
    public static String getPublicKey(JSONObject keyMap)  
            throws Exception {  
       return  keyMap.getString(PUBLIC_KEY);  
    }  
  
    /** 
     * ��ʼ����Կ 
     *  
     * @return 
     * @throws Exception 
     */  
    public static JSONObject initKey() throws Exception {  
        KeyPairGenerator keyPairGen = KeyPairGenerator  
                .getInstance(KEY_ALGORITHM);  
        keyPairGen.initialize(1024);  
  
        KeyPair keyPair = keyPairGen.generateKeyPair();  
  
        // ��Կ  
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  
  
        // ˽Կ  
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();  
  
        JSONObject  keyMap=new JSONObject();
        keyMap.put(PUBLIC_KEY, encryptBASE64(publicKey.getEncoded()));  
        keyMap.put(PRIVATE_KEY, encryptBASE64(privateKey.getEncoded()));  
        return keyMap;  
    }  
    
   
    /** 
     * ��ȡ��Ч�Ĺ�Կ���� 
     *  
     * @return 
     */  
    public static String parseValidPubKey(String algo,String source){  
      String pubkey=null;
      
      try{
        if(source.indexOf("-----BEGIN PUBLIC KEY-----")>=0){
          int from=source.indexOf("-----BEGIN PUBLIC KEY-----")+"-----BEGIN PUBLIC KEY-----".length();
          int end=source.indexOf("-----END PUBLIC KEY-----")-1;
          if(end<0)
            end=source.length();
        
          pubkey=source.substring(from,end);
        }else{
          pubkey=source;
        }
        
        //ȥ�����з�
      }catch(Exception e){
        System.out.println("RSACoder.parseValidPubKey() failed:"+e);
      }
      
      return pubkey;
    }
    
}  