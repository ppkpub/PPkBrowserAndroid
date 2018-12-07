package org.ppkpub.ppkbrowser;
/*
 * Name:BrowserPageNode.java
 * Writer:bitsjx
 * Date:2018-11-26
 * Time:00:20
 * Function:implement a Node of LinkList
 * */
public class BrowserPageNode {
	//ҳ�������
	private String pagename="";
	//urlΪ��ַʵ�� 
	private String url="";
	//nextΪ��һ���ڵ�
	private BrowserPageNode next=null;
	//preΪǰһ���ڵ�
	private BrowserPageNode pre=null;
	
	//get��set����
	public String getPagename() {
		return pagename;
	}
	public void setPagename(String pagename) {
		this.pagename = pagename;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public BrowserPageNode getNext() {
		return next;
	}
	public void setNext(BrowserPageNode next) {
		this.next = next;
	}
	public BrowserPageNode getPre() {
		return pre;
	}
	public void setPre(BrowserPageNode pre) {
		this.pre = pre;
	}
	
	//��дĬ�Ϲ��캯��
	public BrowserPageNode()
	{
    //System.out.println("BrowserPageNode blank");
		this.pagename="";
		this.url="";
		this.next=null;
		this.pre=null;
	}
	//�Զ��幹�캯��
	public BrowserPageNode(String pagename,String urladdress)
	{
    //System.out.println("BrowserPageNode:"+pagename+","+urladdress);
		this.pagename=pagename;
		this.url=urladdress;
		this.next=null;
		this.pre=null;
	}
}
