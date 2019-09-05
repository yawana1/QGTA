package asreml.input;

import java.util.ArrayList;
import java.util.List;

import asreml.AsremlTrait;

/**
 * The Class AsremlTabs is used to store a collection of {@link AsremlTab}
 * s.
 */
public class AsremlTabs {

	private List<AsremlTab> tabs = new ArrayList<AsremlTab>();
	private String response ="";
	private List<AsremlTrait>  traits;

	public void setTraits(List<AsremlTrait> traits) {
		this.traits = traits;
		for(AsremlTrait t: traits){
			response += t.getName() +" ";
		}
	}

	public List<AsremlTrait> getTraits() {
		return traits;
	}
	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * Gets the tabs.
	 * 
	 * @return the tabs
	 */
	public List<AsremlTab> getTabs() {
		return tabs;
	}

	/**
	 * Sets the tabs.
	 * 
	 * @param tabs
	 *            the new tabs
	 */
	public void setTabs(List<AsremlTab> tabs) {
		this.tabs = tabs;
	}
	
	/**
	 * Adds the.
	 * 
	 * @param tab
	 *            the tab
	 */
	public void add(AsremlTab tab){
		tabs.add(tab);
	}
	
	/**
	 * Gets the tab at index of list.
	 * 
	 * @param index
	 *            the index
	 * 
	 * @return the tab
	 */
	public AsremlTab getTab(int index){
		return tabs.get(index);
	}
	
	@Override
	public String toString() {
		
		String str = "";
		for(AsremlTab tab : tabs){
			str += "\ntabulate "+ response +" ~";
			str += tab;
			str += " !STATS";
		}
		
		return str;
	}
	
	public int size(){
		return tabs.size();
	}
}
