package iitb.WebSearch;

import iitb.QuantitySearch.PathConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;



/*
 * Abhirut - 30 July, 2013
 */

public class CachingEngineGoogleNew extends CachingEngine {
	
	Properties m_prop;
	File m_baseDir;
	String GoogleAPIKey;
	String proxy_user;
	String proxy_password;
	
	public CachingEngineGoogleNew(File baseDir) throws FileNotFoundException, IOException {
		// TODO Auto-generated constructor stub
		m_baseDir = baseDir;
		m_prop= new Properties();
		m_prop.load(new FileReader(PathConstants.confFile(m_baseDir)));
		
		GoogleAPIKey = m_prop.getProperty("GoogleAPIKey");
		proxy_user = m_prop.getProperty("ProxyUser");
		proxy_password = m_prop.getProperty("ProxyPassword");
	}

	@Override
	public ResultList doQuery(String queryString, long numHitsRequested) throws Exception {
		ResultList rl = new ResultList(queryString);
	   // String key="AIzaSyBJOWwpgFTQeGO2OBHLTUBallUVeVv4vZw";
	    String query=queryString;
	    // Convert spaces to +, etc. to make a valid URL
		query = URLEncoder.encode(query, "UTF-8");
	    URL url = new URL(
	            "https://www.googleapis.com/customsearch/v1?key=" + GoogleAPIKey + "&cx=002562180100428196093:yezmoy4ozro&q="+ query + "&alt=json");
	    
	    /*Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("netmon.iitb.ac.in", 80));*/
		
	    URLConnection connection = url.openConnection();
		
/*		Authenticator authenticator = new Authenticator() {

	        public PasswordAuthentication getPasswordAuthentication() {
	            return (new PasswordAuthentication(proxy_user,
	                    proxy_password.toCharArray()));
	        }
	    };
	    Authenticator.setDefault(authenticator);
*/
	    String output;
	    try{
	    	java.util.Scanner s = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A");
	    
	    output = s.next();
	    JSONObject joResult = new JSONObject(output);
	    JSONArray jaItems = joResult.getJSONArray("items");
	    rl.numTotalHits = joResult.getJSONObject("searchInformation").getInt("totalResults");
	    System.out.println("Search Results");
		for(int i=0;i<jaItems.length();i++) {
		    JSONObject resultObj = jaItems.getJSONObject(i);

		    ResultElement re = new ResultElement(resultObj.getString("link"),resultObj.getString("title"),resultObj.getString("snippet"));
		    rl.getHitList().add(re);
		}
	    }catch(Exception e){
	    	System.out.println("Caught Exception: "+e);
	    	e.printStackTrace();
	    }
	    return rl;
	}
}
