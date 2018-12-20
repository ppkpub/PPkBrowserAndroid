package org.ppkpub.ppkbrowser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * ͨ��HTTP������
 * ���£�2016-12-3 16:13:27
 * �汾��1.0.0
 * ���ߣ�HoKis 
 * ��Դ��CSDN 
 * ԭ�ģ�https://blog.csdn.net/HoKis/article/details/53445964 
 */
public final class CommonHttpUtil {
   
    //Ĭ�ϳ�ʱʱ�䣨���룩
    private final static int TIME_OUT_MS = 10000;

    //Э������
    private final static String HTTP = "http:";
    private final static String HTTPS = "https:";


    /**
     * �ж�Э�����ͣ�ת������ͬ�ķ������д���
     * @throws IOException 
     */
    private static String chooseProtocol(String url) throws IOException{
        if (url == null) {
            throw new RuntimeException("url shouldn't be null.");
        }

        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
            throw new RuntimeException("url format is not supported.");
        }
        //�����httpЭ��
        if (url.startsWith(HTTP)) {
            return getFromHttp(url);

        }else if (url.startsWith(HTTPS)) {
            //�����httpsЭ��
            return getFromHttps(url);
        }
        return null;
    }

    /**
     * httpЭ���url
     * @param url
     * @return
     * @throws IOException
     */
    private static String getFromHttp(String url) throws IOException{
        HttpURLConnection con = null;
        try {
            // ����ҳ
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setReadTimeout(TIME_OUT_MS);
            con.connect();
            //�ж�״̬��
            if (con.getResponseCode() == 200) {
                //��ȡ��
                return getTextFromCon(con);
            }
        } catch (IOException e) {
            throw new IOException("Connet exception.", e);
        } finally { 
            if (con != null) {
                con.disconnect();
            }
        }

        return null;
    }

    /**
     * httpsЭ���url
     * @param url
     * @return
     * @throws IOException
     */
    private static String getFromHttps(String url) throws IOException{
        HttpsURLConnection httpsConn = null;
        try {
            //����TrustManager ��������
            TrustManager[] tm = {new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {

                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {

                }
            }}; 

            //����SSLContext���󣬲�ʹ������ָ�������ι�������ʼ��
            SSLContext sslContext = SSLContext.getInstance("SSL","SunJSSE"); 
            sslContext.init(null, tm, new SecureRandom());

            //������SSLContext�����еõ�SSLSocketFactory����
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            //����HttpsURLConnection���󣬲�������SSLSocketFactory����
            httpsConn = (HttpsURLConnection)new URL(url).openConnection();
            httpsConn.setSSLSocketFactory(ssf);
            httpsConn.setReadTimeout(TIME_OUT_MS);
            //����
            httpsConn.connect();

            //�ж�״̬��
            if (httpsConn.getResponseCode() == 200) {
                //��ȡ��
                return getTextFromCon(httpsConn);
            }

        } catch (Exception e) {
            throw new IOException("Connet exception.", e);
        }finally{
            if (httpsConn != null) {
                httpsConn.disconnect();
            }

        }
        return null;
    }

    /**
     * �����ж�ȡ����
     * @param con
     * @return
     * @throws IOException
     */
    private static String getTextFromCon(URLConnection con) throws IOException{
        try{
        	BufferedReader rd  = new BufferedReader(new InputStreamReader(con.getInputStream()));
      	    StringBuilder sb = new StringBuilder();
      	    String line;
      	      
      	    while ((line = rd.readLine()) != null)
      	    {
      	        sb.append(line + '\n');
      	    }

            return sb.toString();

        } catch (IOException e) {
            throw new IOException("Read stream exception.", e);
        }
    }


    /**
     * ����URL��ȡ��Դ
     * @param url
     * @return �����ַ�������Դ
     * @throws IOException
     */
    public static String getSourceFromUrl(String url) throws IOException {
        return chooseProtocol(url);
    }


}