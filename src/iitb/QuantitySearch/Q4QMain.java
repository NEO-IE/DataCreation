	package iitb.QuantitySearch;


import iitb.QuantitySearch.indexing.IndexSearchResults;
import iitb.WebSearch.ResultElement;
import iitb.WebSearch.ResultList;
 /*import iitb.QuantitySearch.indexing.Page;
import iitb.QuantitySearch.indexing.QueryProcessor;
import iitb.QuantitySearch.indexing.Snippet;
import iitb.QuantitySearch.indexing.SupportedUnits;
import iitb.QuantitySearch.ranking.InferenceBase;
import iitb.QuantitySearch.ranking.IntervalMerit;
import iitb.QuantitySearch.ranking.LetorInterface;*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Q4QMain {

	public static int NUM_RESULTS = 10;
	
	
	private Properties m_config;
	private File m_baseDir;
	private IndexSearchResults index;
	
/*	private int pageCount = 0;
	private String HTMLfile_base_dir = "HTMLfiles_new/";
*/	
	public static String metadataFile = "data/fetched_result.xml";
	public static String goldDataFile = "data/worldbank_vals_1.csv";
	Document metadataDoc;
	Element metaindex;


	public Q4QMain(String baseDir) throws IOException, ParserConfigurationException, SAXException {

		m_baseDir = new File(baseDir);
		
		//Fetching configuration
		m_config = new Properties();
		FileInputStream propInputStream = new FileInputStream(PathConstants.confFile(m_baseDir));
		m_config.load(propInputStream);
		propInputStream.close();
		
		readMetaData(metadataFile);
		
		
	}
	
	public void readMetaData(String metadataFile) throws ParserConfigurationException, SAXException, IOException{
		File f1 = new File(metadataFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		if(f1.isFile()){
			metadataDoc = dBuilder.parse(metadataFile);
			NodeList nl = metadataDoc.getElementsByTagName("metadata");
			if(nl == null){
				metaindex = metadataDoc.createElement("metadata");
	        	metadataDoc.appendChild(metaindex);
			}else{
				metaindex = (Element) nl.item(0);
			}
		}else{
			metadataDoc = dBuilder.newDocument();	
			metaindex = metadataDoc.createElement("metadata");
        	metadataDoc.appendChild(metaindex);
		}
	}
	
	public static void main(String args[]) throws Exception {

		long stTime = System.currentTimeMillis();
		
		Q4QMain qM = new Q4QMain("/mnt/a99/d0/ashishm/workspace/DataCreation");
		
		ArrayList<String> queries = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(goldDataFile));
		String line;
		while ((line = br.readLine()) != null){
			String[] vals = line.split("\t");
			String entity = vals[0];
			String attribute = (vals[1].split("\\("))[0];
			//System.out.println(attribute + " of " + entity);
			queries.add(attribute + "of " + entity);	
		}
		try{
			//processing query. Fetching top pages.
			for(int i = 0; i < queries.size(); i++){
				System.out.println("QUERY " + i +" : " + queries.get(i));	
				qM.processQueryPage(queries.get(i), 0.1F, 4);
			}
		}catch(Exception e){
			System.out.println("Exception: "+e);
			e.printStackTrace();
		}finally{
			
			if(qM.index != null){
				qM.index.flushCache();
				qM.flushMetadataCache();
			}
		}
		
		System.out.println("Finished total Crawling: "+(System.currentTimeMillis()-stTime)/1000);
	}
	
	//processes the query 
	public void processQueryPage(String query, float tolerance, int search_engine) throws Exception{
		long stTime = System.currentTimeMillis();

		if(index == null)
			index = new IndexSearchResults(m_baseDir);
		//fetch the top results. Relevant pages are dumped according to PageCache.java
		ResultList hits = index.crawlAndIndex(query, search_engine);
		System.out.println("Finished crawling and indexing. Time taken: "+(System.currentTimeMillis()-stTime)/1000);
		
		//adding information about this query to meta data XML file.
		addToMetadata(query, hits);
	}
	
	private void addToMetadata(String queryString, ResultList hits) {
		// TODO Auto-generated method stub
					
		Element query = metadataDoc.createElement("query");
    	
    	Element querystr = metadataDoc.createElement("querystring");
    	querystr.appendChild(metadataDoc.createTextNode(queryString));
    	query.appendChild(querystr);
        
        for(ResultElement rs: hits.getHitList()){
        	
        	Element result = metadataDoc.createElement("results");
        	Element url = metadataDoc.createElement("url");
        	url.appendChild(metadataDoc.createTextNode(rs.getUrl()));
        	result.appendChild(url);
        	
        	Element state = metadataDoc.createElement("state");
        	state.appendChild(metadataDoc.createTextNode(""+rs.state));
        	result.appendChild(state);
        	
        	Element location = metadataDoc.createElement("location");
        	location.appendChild(metadataDoc.createTextNode(rs.location));
        	result.appendChild(location);
        	
        	Element pageRank = metadataDoc.createElement("pagerank");
        	pageRank.appendChild(metadataDoc.createTextNode(""+rs.pageRank));
        	result.appendChild(pageRank);
        	
        	Element title = metadataDoc.createElement("title");
        	title.appendChild(metadataDoc.createTextNode(rs.getTitle()));
        	result.appendChild(title);
        	
        	query.appendChild(result);
        }
        metaindex.appendChild(query);
	}
	
	void flushMetadataCache() throws TransformerException{
    	
    	// write the content into xml file
		System.out.println("Flushing the meta data: "+metadataFile);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(metadataDoc);
		StreamResult result = new StreamResult(metadataFile);
		
		transformer.transform(source, result);

    }

}
