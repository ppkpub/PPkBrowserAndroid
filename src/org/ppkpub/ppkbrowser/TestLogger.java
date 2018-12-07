package org.ppkpub.ppkbrowser;

import android.util.Log;

public class TestLogger{
	String mClassName=null;
	
	public TestLogger(){
		this.mClassName="";
	}
	
	public TestLogger(Class classname){
		this.mClassName=classname.toString();
	}
	
	public void info(String str_info){
		Log.d("[INFO,"+this.mClassName+"]",str_info);
	}
	
	public void error(String str_info){
		Log.d("[ERROR,"+this.mClassName+"]",str_info);
	}
}