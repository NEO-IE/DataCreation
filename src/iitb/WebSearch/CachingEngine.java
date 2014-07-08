package iitb.WebSearch;

public abstract class CachingEngine {
	public abstract ResultList doQuery(String queryString, long numHitsRequested) throws Exception;
}
