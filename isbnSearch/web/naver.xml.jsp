<%@ page import="org.apache.commons.httpclient.HttpClient,org.apache.commons.httpclient.HttpStatus,org.apache.commons.httpclient.NameValuePair,org.apache.commons.httpclient.methods.GetMethod,java.net.URLDecoder,java.io.*" %><%
	response.setContentType("text/xml; charset=utf-8");

	String output = null;
	String host = "https://openapi.naver.com/v1/search/book_adv.xml?query=book&target=book_adv&d_isbn="+request.getParameter("isbn");
	//String host = "http://openapi.naver.com/search?query=book&target=book_adv&key=863233d90634c26a0a5996c2ed52b3bb&d_isbn=1";
	//String host = "http://openapi.naver.com/search";
	//System.out.println(host);
	HttpClient client = new HttpClient();
	GetMethod getMethod = new GetMethod(host);
	getMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=EUC-KR");
	getMethod.addRequestHeader("X-Naver-Client-Id","cJaJOo2w5P2JXbqhqvU9");
	getMethod.addRequestHeader("X-Naver-Client-Secret","Zae_SW92pY");
	
	NameValuePair query = new NameValuePair("query", "book");
	NameValuePair target = new NameValuePair("target", "book_adv");
	NameValuePair key = new NameValuePair("key", request.getParameter("key"));
	NameValuePair isbn = new NameValuePair("d_isbn", request.getParameter("isbn"));
	//postMethod.setRequestBody(new NameValuePair[] {query, target, key, isbn});
	
	/*
	NameValuePair[] data = {
		new NameValuePair("key", request.getParameter("key")),
		new NameValuePair("d_isbn",request.getParameter("isbn"))};
	postMethod.setRequestBody(data);
	*/
	
	try {
		//System.out.println(postMethod.getParameter("d_isbn"));
		client.executeMethod(getMethod);
		output = getMethod.getResponseBodyAsString().trim();
		out.print(output);
	} catch (IOException e) {
		out.print(e);
	} finally {
		getMethod.releaseConnection();
	}
	
	/*
	client.executeMethod(postMethod);
	if (postMethod.getStatusCode() == HttpStatus.SC_OK)
	{
		output = postMethod.getResponseBodyAsString().trim();
		out.print(output);
	}
	postMethod.releaseConnection();
	*/
%>
