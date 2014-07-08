package iitb.WebSearch;

public class ResultElement {
	String title, snippet, url;
	public int pageRank;
	public String location;
	public boolean state;

/*	public ResultElement(com.msn.search.soap.Result msnResult) {
		this.title = msnResult.getTitle();
		this.snippet = msnResult.getDescription();
		this.url = msnResult.getUrl();
	}

	public ResultElement(com.google.soap.search.GoogleSearchResultElement googleResult) {
		this.title = googleResult.getTitle();
		this.snippet = googleResult.getSnippet();
		this.url = googleResult.getURL();
	}
*/	
	
	public ResultElement(String url, String title, String snippet) {
		this.url = url;
		this.title = title;
		this.snippet = snippet;
	}
	public ResultElement() {
		// TODO Auto-generated constructor stub
	}
	public String getSnippet() {
		return snippet;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ResultElement)) return false;
		ResultElement ore = (ResultElement) obj;
		return url.equals(ore.url);
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}
}
