package org.ppkpub.ppkbrowser;
/*
 * Name:BrowserPageList.java
 * Writer:bitsjx
 * Date:2018-11-26
 * Time:00:26
 * Function:Construct a LinkList to save History and BookMarks Nodes
 * */
public class BrowserPageList {
	//ͷ�ڵ�
	private BrowserPageNode head;
	//���ɽڵ�
	private BrowserPageNode link;
	//��¼��ǰ�Ľڵ�����λ��
	private BrowserPageNode linkpointer;
	public BrowserPageList()
	{
		head=new BrowserPageNode();
		link=head;
		linkpointer=head;
	}
	//����µ�ҳ�浽����
	public void addURL(String pagename,String urladdress)
	{
		BrowserPageNode node=new BrowserPageNode(pagename,urladdress);
		link=head;
		while(link.getNext()!=null)
		{
			link=link.getNext();
		}
		link.setNext(node);
		node.setPre(link);
		node.setNext(null);
		linkpointer=node;
	}
	//������µ�����ͷ
	public BrowserPageNode getLastPageNode()
	{
		return this.head;
	}
	
  //��õ�ǰҳ��
	public BrowserPageNode getCurrentPageNode()
	{
		return this.linkpointer;
	}
  
	//����ĳ��pagename��Ӧ��URL
	public String getURL(String pagename)
	{
		link=head;
		String url="";
		while(!((link.getNext()).getPagename()).equalsIgnoreCase(pagename))
		{
			link=link.getNext();
		}
		url=(link.getNext()).getUrl();
		linkpointer=link;
		return url;
	}
	
	//��ȡĳ��URL��Ӧ��pagename
	public String getPageName(String urladdress)
	{
		link=head;
		String pagename="";
		while(!(link.getNext().getUrl()).equalsIgnoreCase(urladdress))
		{
			link=link.getNext();
		}
		pagename=link.getNext().getPagename();
		linkpointer=link;
		return pagename;
	}
	//��ȡ��ǰpage��ǰһ��pageurl
	public String getPrePageURL( )
	{
		link=linkpointer.getPre();
		linkpointer=link;
		return link.getUrl();
	}
	//��ȡ��ǰpage����һ��pageurl
	public String getNextPageURL()
	{
		link=linkpointer.getNext();
		linkpointer=link;
		return link.getUrl();
	}
	//����ĳ��pageurl��ַ�Ƿ����
	public boolean isPageUrlExist(String pageurl)
	{
		link=this.head.getNext();
		//����Ƿ��ҵ�
		boolean isfind=false;
		String tmpUrl="";
		while(link!=null)
		{
			tmpUrl=link.getUrl();
			if(tmpUrl.equalsIgnoreCase(pageurl))
			{
				isfind=true;
				break;
			}
			link=link.getNext();
		}
		System.out.println("���ұ�ҳ���:"+isfind);
		return isfind;
	}
	//�ж�ǰһ��page�Ƿ����
	public boolean isPrePageExist( )
	{
		boolean isfind=false;
		link=head;
		link=linkpointer.getPre();
		if(link!=null&&link!=head)
		{
			isfind=true;
		}
		System.out.println("����ǰҳ���:"+isfind);
		return isfind;
	}
	//�жϺ�һ��page�Ƿ����
	public boolean isNextPageExist( )
	{
		boolean isfind=false;
		link=head;
		link=linkpointer.getNext();
		if(link!=null)
		{
			isfind=true;
		}
		System.out.println("���Һ�ҳ���:"+isfind);
		return isfind;
	}
}
