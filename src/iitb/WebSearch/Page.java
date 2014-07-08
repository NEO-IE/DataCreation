package iitb.WebSearch;


public class Page {
	String url;
	String text;
	public Page(String url, String text) {
		this.url = url;
		this.text = text;
	}
	public String getText() {
		return text;
	}
	public String getUrl() {
		return url;
	}
	public void savePage() {
		
	}
}
