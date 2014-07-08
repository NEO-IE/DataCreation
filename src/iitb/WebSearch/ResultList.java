package iitb.WebSearch;

import java.util.ArrayList;

public class ResultList {
	String queryString;
	long numTotalHits= 0;
	ArrayList<ResultElement> hitList;
	public ResultList(String queryString) {
		this.queryString = queryString;
		hitList = new ArrayList<ResultElement>();
	}
	public ArrayList<ResultElement> getHitList() {
		return hitList;
	}
	public long getNumTotalHits() {
		return numTotalHits;
	}
	public String getQueryString() {
		return queryString;
	}
}
