package org.ppkpub.ppkbrowser;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptTools
{
  // 128 bit key
  private byte[] key = null;
  
  // 16 bytes vector
  private String initVector = "ZEM84JFYEJDHMKG0";
 
  
  public EncryptTools(String password) throws Exception{
    this.key=Coder.encryptMD5(password.getBytes());
  }
  
  /**
   * 对原文加密
   * 
   * @param key
   * @param initVector
   * @param strSource
   * @return
   */
  private String encrypt(byte[] key, String initVector, String strSource)
  {
    String strResult = null;
    try
    {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
 
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
 
      byte[] encrypted = cipher.doFinal(strSource.getBytes());
      strResult = Coder.encryptBASE64(encrypted);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      strResult = null;
    }
 
    return strResult;
  }
 
  /**
   * 对密文解密
   * 
   * @param key
   * @param initVector
   * @param encrypted
   * @return
   */
  private String decrypt(byte[] key, String initVector, String encrypted)
  {
    try
    {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
 
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
 
      byte[] original = cipher.doFinal( Coder.decryptBASE64(encrypted));
 
      return new String(original);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
 
    return null;
  }
 
  /**
   * AES 加密<br>
   * 传入需要加密的原文
   * 
   * @param strSource
   * @return
   */
  public String encrypt(String strText)
  {
    return encrypt(key, initVector, strText);
  }
 
  /**
   * AES 解密<br>
   * 输入密文
   * 
   * @param strCiphertext
   * @return
   */
  public String decrypt(String strCiphertext)
  {
    return decrypt(key, initVector, strCiphertext);
  }
}

