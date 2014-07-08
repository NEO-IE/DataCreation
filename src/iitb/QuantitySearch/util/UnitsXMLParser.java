package iitb.QuantitySearch.util;

import iitb.QuantitySearch.PathConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UnitsXMLParser {

	public static String[] getUnitForms(File baseDir, String unit) {

		String[] forms = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(PathConstants.unitXMLFile(baseDir));

			NodeList nl = dom.getDocumentElement().getChildNodes();
			for(int i=0; i<nl.getLength(); i++) {
				if(nl.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;

				NodeList ch_nodes = nl.item(i).getChildNodes();
				for(int j=0; j<ch_nodes.getLength(); j++)
					if(ch_nodes.item(j).getNodeName().equals("standardUnitName")) {
						if(ch_nodes.item(j).getFirstChild().getNodeValue().equalsIgnoreCase(unit)) {
							forms = getUnitForms(nl.item(i));
							break;
						}
					}
					else if(ch_nodes.item(j).getNodeName().equals("suffixLemmaSet")) {
						NodeList form_list = ch_nodes.item(j).getChildNodes();
						for(int k=0; k<form_list.getLength(); k++) {
							if(form_list.item(k).getNodeType() == Node.ELEMENT_NODE && 
									form_list.item(k).getFirstChild().getNodeValue().equalsIgnoreCase(unit)) {
								forms = getUnitForms(nl.item(i));
								break;
							}
						}
					}
			}
		}
		catch(IOException e) {}
		catch(ParserConfigurationException e) {}
		catch(SAXException e){}

		if(forms == null)
			forms = new String[]{unit};

		return forms;
	}
	private static String[] getUnitForms(Node n) {
		ArrayList<String> al = new ArrayList<String>();

		NodeList ch_nodes = n.getChildNodes();
		for(int j=0; j<ch_nodes.getLength(); j++) {
			if(ch_nodes.item(j).getNodeName().equals("standardUnitName")) {
				al.add(ch_nodes.item(j).getFirstChild().getNodeValue());
			}
			else if(ch_nodes.item(j).getNodeName().equals("suffixLemmaSet")) {
				NodeList form_list = ch_nodes.item(j).getChildNodes();
				for(int k=0; k<form_list.getLength(); k++) {
					if(form_list.item(k).getNodeType() == Node.ELEMENT_NODE)
						al.add(form_list.item(k).getFirstChild().getNodeValue());
				}
			}
		}

		return al.toArray(new String[]{});
	}
	public static HashMap<String, String> loadPrefixLemmaSet(File baseDir) {
		return loadStandardUnitForms(baseDir, new String[]{"prefixLemmaSet"});
	}
	public static HashMap<String, String> loadSuffixLemmaSet(File baseDir) {
		return loadStandardUnitForms(baseDir, new String[]{"suffixLemmaSet"});
	}	
	public static HashMap<String, String> loadStandardUnitForms(File baseDir) {
		return loadStandardUnitForms(baseDir, new String[]{"prefixLemmaSet", "suffixLemmaSet"});
	}
	public static HashMap<String, String> loadStandardUnitForms(File baseDir, String[] nottypes) {
		
		HashMap<String, String> standUnitForms = new HashMap<String, String>();

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(PathConstants.unitXMLFile(baseDir));

			NodeList nl = dom.getDocumentElement().getChildNodes();
			for(int i=0; i<nl.getLength(); i++) {
				if(nl.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;

				String stdUnit = null;
				NodeList ch_nodes = nl.item(i).getChildNodes();
				for(int j=0; j<ch_nodes.getLength(); j++) {
					String chnodeName = ch_nodes.item(j).getNodeName(); 
					if(chnodeName.equals("standardUnitName")) {
						stdUnit = ch_nodes.item(j).getFirstChild().getNodeValue();
					}
					else if(isInList(chnodeName, nottypes)) {
						NodeList form_list = ch_nodes.item(j).getChildNodes();
						for(int k=0; k<form_list.getLength(); k++) {
							if(form_list.item(k).getNodeType() == Node.ELEMENT_NODE)   {
								String key = form_list.item(k).getFirstChild().getNodeValue();
								standUnitForms.put(key.toLowerCase(), stdUnit);
							}
						}
					}
				}
			}
		}
		catch(IOException e) {return null;}
		catch(ParserConfigurationException e) {return null;}
		catch(SAXException e){e.printStackTrace(); return null;}
		
		return standUnitForms;
	}
	public static boolean isInList(String key, String[] list) {
		
		for(String s:list)
			if(s.equals(key))
				return true;
		
		return false;
	}
	public static void main(String[] args) 
	throws Exception{

		String[] forms = getUnitForms(new File(args[0]), "km");

		for(String f:forms)
			System.out.println(f);
	}
}