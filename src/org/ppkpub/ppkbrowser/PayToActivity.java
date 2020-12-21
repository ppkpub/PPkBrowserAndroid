package org.ppkpub.ppkbrowser;
//��Activity�е��ã�������°�ť���ᵯ���Ի�����ʾ�汾�ź����������������½�UpdateActivity.java

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class PayToActivity extends Activity {
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout .activity_payto);
	    
		TextView textDesc = (TextView) findViewById(R.id.payto_status_desc);
		
        try {
    	    ImageButton button_back = (ImageButton) findViewById(R.id.payto_btn_back);
    	    button_back.setOnClickListener(new View.OnClickListener() {
    	        @Override
    	        public void onClick(View v) {
    	        	gotoURI(null);
    	        }
    	    });

    	    //����Ƿ�ΪschemeЭ��������ת����
	        Intent intent =getIntent();
	        if(intent!=null) {
	        	Uri obj_uri =intent.getData();
	        	if(obj_uri!=null) {
	        		String in_uri= obj_uri.toString();
	        		textDesc.setText("Scheme="+intent.getScheme()+"  in_uri="+in_uri);
	        		//textDesc.setText("Payto "+in_uri);
	        		gotoURI( getPPkPayToolURI(in_uri)  );
	        	}else {
	        		textDesc.setText("Invalid dest URI");
	        		gotoURI( Config.ppkPayToolURI );
	        	}
	        }

        }catch(Exception e) {
        	textDesc.setText("scheme error:  "+e.toString());
        }
	}
	
	
	public void gotoURI(String destURI){
		/* �½�һ��Intent���� */
        Intent intent = new Intent();
        if(destURI!=null)
        	intent.putExtra("uri",destURI);    
        
        /* ָ��intentҪ�������������� */
        intent.setClass(PayToActivity.this, PPkActivity.class);
        /* ���������� */
        startActivity(intent);
        /* �رյ�ǰ��Activity */
        PayToActivity.this.finish();
	}
	
	public static String getPPkPayToolURI(String payto_uri)   {
		try {
			String dest_odin_uri =  ODIN.formatPPkURI(payto_uri.substring( payto_uri.indexOf("ppk:") ), true);
			return Config.ppkPayToolURI +"?ppkpayto="+ java.net.URLEncoder.encode(dest_odin_uri, Config.PPK_TEXT_CHARSET ) ;
		} catch (UnsupportedEncodingException e) {
			return Config.ppkPayToolURI;
		}
	}

}