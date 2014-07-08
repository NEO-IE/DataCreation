package iitb.QuantitySearch.indexing;

import iitb.QuantitySearch.PathConstants;
import iitb.WebSearch.CachingEngine;
import iitb.WebSearch.CachingEngineGoogleNew;
import iitb.WebSearch.PageCache;
import iitb.WebSearch.ResultList;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;


public class IndexSearchResults {

	public static final int MIN_TEXT_LEN = 20;
	
	public static final int SE_GOOGLE = 0;
	public static final int SE_YAHOO = 1;
	public static final int SE_MSN = 2;
	public static final int SE_BOSS = 3;
	public static final int SE_GOOGLE_NEW = 4;
	
	public final int MAX_HITS ;
	
	private final File m_baseDir; 
	Properties m_prop;
	final PageCache m_pageCache;


	public IndexSearchResults(File baseDir) 
	throws IOException, ParserConfigurationException, SAXException {
		
		m_baseDir = baseDir;
		m_prop= new Properties();
		m_prop.load(new FileReader(PathConstants.confFile(m_baseDir)));
		m_pageCache = new PageCache(m_baseDir, m_prop);
		MAX_HITS = Integer.parseInt(m_prop.getProperty("numSearchResults"));
		
		
	//	m_index = new IndexCorpus(m_baseDir, new File(m_baseDir, m_prop.getProperty("indexDir")));
	}
	public ResultList crawlAndIndex(String query, int seacrh_engine) 
	throws Exception {
		
		CachingEngine ce = null;
		switch(seacrh_engine) {
		/*case(SE_GOOGLE): 
			ce=new CachingEngineGoogle(m_baseDir, m_prop);
			break;
		case(SE_YAHOO):
			ce=new CachingEngineYahoo(m_baseDir, m_prop);
			break;
		case(SE_MSN):
			ce=new CachingEngineMSN(m_baseDir, m_prop);
			break;
		case(SE_BOSS):
			ce=new YahooBOSS(m_baseDir, m_prop);
			break;*/
		case(SE_GOOGLE_NEW): 
			ce=new CachingEngineGoogleNew(m_baseDir);
			break;
		}
		
		ResultList hits = ce.doQuery(query, MAX_HITS);
		System.out.println(query+". Total hits="+hits.getNumTotalHits()+". Results="+hits.getHitList().size());
		hits = m_pageCache.fetchAndFill(hits);
		return hits;
	}
	
	public void flushCache() throws TransformerException {
		// TODO Auto-generated method stub
		m_pageCache.flushPageCache();
		
	}
}
