package iitb.WebSearch;

//import iitb.QuantitySearch.Legacy.util.NekoHTMLHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;*/

/*import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;*/
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
/*import javax.net.ssl.SSLSession;*/
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
/*import javax.xml.transform.TransformerConfigurationException;*/
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/*import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;*/
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/*import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;*/
public class PageCache {
	
	
	
	final int maxPageLength;
	final File baseDir, pageCacheDir, pageCacheFile, htmlBaseDir;
	final int pageFetchTimeout, pageFetchParallelism;
	String proxy_user;
	String proxy_password;
	
	public HashMap<String, String> urlToLocation;
	Document doc;
	Element index;

    public PageCache(File baseDir, Properties config) throws ParserConfigurationException, SAXException, IOException {
        this.baseDir = baseDir;
		pageCacheDir = new File(baseDir, config.getProperty("WebSearch.pageCacheDir"));
		pageCacheFile = new File(pageCacheDir.toString()+"/", config.getProperty("WebSearch.pageCacheFile"));
		htmlBaseDir = new File(baseDir, config.getProperty("WebSearch.htmlBaseDir"));
		
		proxy_user = config.getProperty("ProxyUser");
		proxy_password = config.getProperty("ProxyPassword");
		
		urlToLocation = new HashMap<String, String>();
        if (!pageCacheDir.isDirectory()) 
        	pageCacheDir.mkdir();
        if(pageCacheFile.isFile()){
        	fetchPageCache(pageCacheFile);
        }else{
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        	doc = dBuilder.newDocument();
        	index = doc.createElement("index");
	        doc.appendChild(index);
        }
        	
        if(!htmlBaseDir.isDirectory()){
        	htmlBaseDir.mkdir();
        }
    	maxPageLength = Integer.parseInt(config.getProperty("WebSearch.maxPageLength"));
    	pageFetchParallelism = Integer.parseInt(config.getProperty("WebSearch.pageFetchParallelism"));
    	pageFetchTimeout = Integer.parseInt(config.getProperty("WebSearch.pageFetchTimeout"));
    }


    void fetchPageCache(File pageCacheFile) throws ParserConfigurationException, SAXException, IOException{
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	doc = dBuilder.parse(pageCacheFile);
    	
    	NodeList nl = doc.getElementsByTagName("index");
		if(nl == null){
			index = doc.createElement("index");
        	doc.appendChild(index);
		}else{
			index = (Element) nl.item(0);
		}
    	
    	NodeList nList = doc.getElementsByTagName("page");
    	for (int temp = 0; temp < nList.getLength(); temp++) {
    		 
    		Node nNode = nList.item(temp);
     
    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
     
    			Element eElement = (Element) nNode;
     
    			String url = eElement.getElementsByTagName("url").item(0).getTextContent();
    			String location = eElement.getElementsByTagName("location").item(0).getTextContent();
    			urlToLocation.put(url, location);
    			
    		}
    	}
    	
    }
    
    
    public void flushPageCache() throws TransformerException{
    	
    	System.out.println("Flushing the page Cache: "+pageCacheFile);
    	// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(pageCacheFile);
 
		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
 
		transformer.transform(source, result);
    }
    	
    void addElementToPageCache(String hitURL, String htmlfile){
    	Element newPage = doc.createElement("page");
        
        Element url = doc.createElement("url");
        url.appendChild(doc.createTextNode(hitURL));
        Element location = doc.createElement("location");
        location.appendChild(doc.createTextNode(htmlfile));
        
        newPage.appendChild(url);
        newPage.appendChild(location);
        
        
       /* NodeList nl = doc.getElementsByTagName("index");
        Element index;
        if(nl == null){
        	index = doc.createElement("index");
	        doc.appendChild(index);
        }else{
        	index = (Element) nl.item(0);
        }*/
        index.appendChild(newPage);
    }

    public ResultList fetchAndFill(ResultList rl) throws InterruptedException {
    	 
    	ResultList actualFetch = new ResultList(rl.getQueryString());
    	actualFetch.numTotalHits = rl.numTotalHits;
    	
    	
    	final MultiThreadedHttpConnectionManager connectionManager;
        connectionManager = new MultiThreadedHttpConnectionManager();
    	final HttpClient httpclient = new HttpClient(connectionManager);
    	httpclient.getParams().setParameter("http.socket.timeout", new Integer(pageFetchTimeout));
    	System.getProperties().put("http.proxyHost", "netmon.iitb.ac.in");
		System.getProperties().put("http.proxyPort", "80");
		System.getProperties().put("http.proxyUser", proxy_user);
		System.getProperties().put("http.proxyPassword", proxy_password);
      	if (System.getProperty("http.proxyHost") != null) {
      		final String httpProxyHost = System.getProperty("http.proxyHost");
      		final int httpProxyPort = Integer.parseInt(System.getProperty("http.proxyPort", "80"));
	        httpclient.getHostConfiguration().setProxy(httpProxyHost, httpProxyPort);
	        if (System.getProperty("http.proxyUser") != null) {
		        AuthScope authScope = new AuthScope(httpProxyHost, httpProxyPort);
		        final String httpProxyUser = System.getProperty("http.proxyUser");
		        final String httpProxyPassword = System.getProperty("http.proxyPassword");
		        UsernamePasswordCredentials upc = new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword);
		        httpclient.getState().setProxyCredentials(authScope, upc);
	        }
      	}
//      	----------- Abhirut
//      	AuthScope authScope = new AuthScope("netmon.iitb.ac.in", 80);
//      	UsernamePasswordCredentials upc = new UsernamePasswordCredentials("abhirut", "world*2011");
//        httpclient.getState().setProxyCredentials(authScope, upc);
//        
        
        //----------------------------
        connectionManager.getParams().setConnectionTimeout(pageFetchTimeout);
        connectionManager.getParams().setMaxTotalConnections(pageFetchParallelism);
        ExecutorService threadExecutor = Executors.newFixedThreadPool(pageFetchParallelism);
        int pageRank = 1;
        for (ResultElement re : rl.getHitList()) {
        	if ( urlToLocation.containsKey(re.getUrl()) ) { //this URL is already beeen processed.
	            System.err.println("ppp "+re.getUrl()+" : Processed");
        		if(urlToLocation.get(re.getUrl()).equals("NA")){ //url is invalid HTML file..
        			re.state = false;
        		}else{
        			re.state = true;
        			re.location = getLocationFromUrl(htmlBaseDir.toString(), re.getUrl());
        		}
        	}else{
        		//        	System.out.println("Trying url - "+re.getUrl());
        		FetchTextTask qt = new FetchTextTask(re.getUrl(), httpclient, re);
        		threadExecutor.execute(qt);
        	}
        	re.pageRank = pageRank;
        	actualFetch.getHitList().add(re);
        	pageRank ++;
        }
        while (connectionManager.getConnectionsInPool() > 0) {
        	System.err.println("alive " + connectionManager.getConnectionsInPool() + " connections");
        	Thread.sleep(pageFetchTimeout);
        	connectionManager.closeIdleConnections(pageFetchTimeout);
        	connectionManager.deleteClosedConnections();
        }
        threadExecutor.shutdown();
        threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
       
        return actualFetch;
    }
    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
    
    public String getLocationFromUrl(String htmlBaseDir, String url){
    	return htmlBaseDir + "/" +url.substring(7).replace('/', '_');
    }
    
	class FetchTextTask implements Runnable {
		final String hitURL;
		final HttpClient httpclient;
		ResultElement re;
		public boolean state  = false;
		FetchTextTask(String hitURL, HttpClient httpclient) {
			this.hitURL = hitURL;
			this.httpclient = httpclient;
		}
		FetchTextTask(String hitURL, HttpClient httpclient, ResultElement re) {
			this.hitURL = hitURL;
			this.httpclient = httpclient;
			this.re = re;
		}
		public void run() {
			// configure the SSLContext with a TrustManager
	        try {
			SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
	        SSLContext.setDefault(ctx);
	        }
	        catch(Exception e) {
	        	
	        }
			// and then from inside some thread executing a method
	        GetMethod get = new GetMethod(hitURL);
	        get.getParams().setParameter("http.socket.timeout", new Integer(pageFetchTimeout));
	        try {
	            httpclient.executeMethod(get);
	            if (get.getStatusCode() != 200) {
		            System.err.println("???? [" + get.getStatusCode() + "] " + hitURL);
		            urlToLocation.put(hitURL, "NA");
		            re.state = false;
		            re.location = "NA";
		            return;
	            }
	            Header contentTypeHeader = get.getResponseHeader("Content-type");
	            if (contentTypeHeader == null) {
	            	System.err.println("???? [" + contentTypeHeader + "] " + hitURL);
	            	urlToLocation.put(hitURL, "NA");
	            	re.location = "NA";
	            	re.state = false;
	            	return;
	            }
	            String contentTypeString = contentTypeHeader.getValue();
	            if (!contentTypeString.contains("text/html")) {
	            	System.err.println("???? [" + contentTypeString + "] " + hitURL);
	            	urlToLocation.put(hitURL, "NA");
	            	re.location = "NA";
	            	re.state = false;
	            	return;
	            }
	            
	            //System.err.println("!!!! [" + get.getStatusCode() + "] " + hitURL + "..." + contentTypeString);
	            String pageBodyString = get.getResponseBodyAsString();
				//save the page body
				String htmlfile = getLocationFromUrl(htmlBaseDir.toString(), hitURL);
				System.out.println("Dumped: "+htmlfile);
				BufferedWriter pw = new BufferedWriter( new FileWriter( new File(htmlfile)));
				pw.write(pageBodyString);
				pw.close();

	            urlToLocation.put(hitURL, htmlfile);
	            
	            //adding element to the pageCache. 
	            
	            addElementToPageCache(hitURL, htmlfile);
	            
	            System.err.println("#### "+hitURL + " Processed");
	            re.state = true;
	            re.location = htmlfile;
	        }
	        catch (Exception anyex) {
	        	// UnComment this to prevent retry of the previous unsuccessful fetch.
	        	/*urlToLocation.put(hitURL, "NA");
	        	 * re.state = false;
	        	 * re.location = "NA";
	        	 * */
	        	System.err.println("@@@@ " + hitURL + " " + anyex.getMessage());
	        	anyex.printStackTrace();
	        }
	        finally {
	        	get.abort();
	            // be sure the connection is released back to the connection manager
	            get.releaseConnection();
	        }
		}
	}
}
