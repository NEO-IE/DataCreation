package iitb.QuantitySearch;


import java.io.File;

public final class PathConstants {
	
	
	public static final String OPTIONS_CONF = "options.conf";
	public static final String STOP_WORD_FILE = "stopwords.txt";
	public static final String QUERIES_XML_FILE = "queries.xml";
	public static final String QUERIES_TXT_FILE = "queries.txt";
	public static final String SNIPPET_XML_FILE = "snippets.xml";
	
	public static final String GOOGLE_RESPONSE_DIR = "GooglePages";
	public static final String GOOGLE_HITS_URL_FILE = "urls_google";

	public static final String LUCENE_DIR = "LuceneIndex";

	public static final String HTML_FORM_DIR = "HTMLForms";
	
	public static final String SERVLET_BASEDIR_VARNAME = "baseDir";
	
	public static final String SCILAB_DIR = "Scilab";
	public static final String SCILAB_TEMPLATE_FILE = SCILAB_DIR + File.separatorChar + "template.sce";
	

	public static final String INTERVAL_DIR = "Interval";
	
	public static File confFile(File baseDir) {
		return new File(baseDir, OPTIONS_CONF);
	}
	public static File stopWordsFile(File baseDir) {
		return new File(baseDir, STOP_WORD_FILE);
	}
	public static File unitXMLFile(File baseDir) {
		return new File(baseDir, "units.xml");
	}
}
