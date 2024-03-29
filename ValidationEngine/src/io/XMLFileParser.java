/*
 * 
 */
package io;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLFileParser extends DefaultHandler {
	
	static Logger objLogger = Logger.getLogger(XMLFileParser.class.getName());
	private boolean bolCrop;
	private String strCropName;
	private boolean bolTrial;
	private String strTrialName;
	private boolean bolExperimentNames;
	private Set<String> setExperimentNames;
	private boolean bolString;
	private boolean bolClass;
	private String strClassName;
	private boolean bolSeason;
	private String strSeasonName;
	private boolean bolStage;
	private String strStageName;
	private boolean bolFirstModel;
	private boolean bolFirstModelName;
	private String strFirstModelName;
	private boolean bolSecondModel;
	private boolean bolSecondModelName;
	private String strSecondModelName;
	private boolean bolThirdModel;
	private boolean bolThirdModelName;
	private String strThirdModelName;
	private SAXParserFactory objSAXParserFactory;
	private SAXParser objSAXParser;
	
	public XMLFileParser(Path objInFilePath) {
		FileInputStream objFileInputStream;
		
		this.bolCrop = false;
		this.strCropName = "";
		this.bolTrial = false;
		this.strTrialName = "";
		this.bolExperimentNames = false;
		this.setExperimentNames = new HashSet<String>();
		this.bolString = false;
		this.bolClass = false;
		this.strClassName = "";
		this.bolSeason = false;
		this.strSeasonName = "";
		this.bolStage = false;
		this.strStageName = "";
		this.bolFirstModel = false;
		this.bolFirstModelName = false;
		this.strFirstModelName = "";
		this.bolSecondModel = false;
		this.bolSecondModelName = false;
		this.strSecondModelName = "";
		this.bolThirdModel = false;
		this.bolThirdModelName = false;
		this.strThirdModelName = "";
		
		this.objSAXParserFactory = SAXParserFactory.newInstance();
		try {
			objFileInputStream = new FileInputStream(new File(objInFilePath.toString()));
			this.objSAXParser = this.objSAXParserFactory.newSAXParser();
			this.objSAXParser.parse(objFileInputStream, 
					                this);
			objFileInputStream.close();
		}
		catch (Exception e) {
			objLogger.error("ModelXMLFileParser.ModelXMLFileParser", 
                            e);
		}
	}
	
	@Override public void startElement(String url,
			                           String localName,
			                           String qName,
			                           Attributes attributes) throws SAXException {
		if (qName.equals("crop")) {
			this.bolCrop = true;
		}
		
		if (qName.equals("trialName")) {
			this.bolTrial = true;
		}
				
		if (qName.equals("experimentNames")) {
			this.bolExperimentNames = true;
		}
		else {
			if ((this.bolExperimentNames == true) && 
				(qName.equals("String"))) {
				this.bolString = true;
			}
		}
						
		if (qName.equals("className")) {
			this.bolClass = true;
		}

		if (qName.equals("seasonName")) {
			this.bolSeason = true;
		}

		if (qName.equals("analysisStage")) {
			this.bolStage = true;
		}

		if ((this.bolFirstModel == false) &&
			(qName.equals("model"))) {
			this.bolFirstModel = true;
		}
		else {
			if ((this.bolFirstModel == true) && 
				(qName.equals("name")) && 
				(this.strFirstModelName.equals(""))) {
				this.bolFirstModelName = true;
			}
			else {
				if ((this.bolSecondModel == false) && 
					(qName.equals("model"))) {
					this.bolSecondModel = true;
				}	
				else {
					if ((this.bolSecondModel == true) &&
						(qName.equals("name"))&& 
						(this.strSecondModelName.equals(""))) {
						this.bolSecondModelName = true;
					}else{
						
						if ((this.bolThirdModel == false) && 
								(qName.equals("model"))) {
								this.bolThirdModel = true;
							}	
							else {
								if ((this.bolThirdModel == true) &&
									(qName.equals("name"))) {
									this.bolThirdModelName = true;
								}
							}
						
						
						
					}
				}
			}
		}
	}
	
	@Override public void characters(char ch[], 
			                         int start, 
			                         int length) throws SAXException {
		if (this.bolCrop == true) {
			this.strCropName = new String(ch, 
					                      start, 
					                      length);
			this.bolCrop = false;
		}
		
		if (this.bolTrial == true) {
			this.strTrialName = new String(ch, 
				                          start, 
				                          length);
			this.bolTrial = false;
		}
		
		if ((this.bolExperimentNames == true) &&
			(this.bolString == true)) {
				this.setExperimentNames.add(new String(ch, 
				                                       start, 
				                                       length));
				this.bolString = false;
		}
		
		if ((this.bolClass == true) && 
			(this.strClassName.equals(""))) {
			this.strClassName = new String(ch, 
				                           start, 
				                           length);
		}
		
		if (this.bolSeason == true) {
			this.strSeasonName = new String(ch, 
			                                start, 
								            length);
			this.bolSeason = false;
		}

		if (this.bolStage == true) {
			this.strStageName = new String(ch, 
									       start, 
									       length);
			this.bolStage = false;
		}
		
		if ((this.bolFirstModel == true) && 
			(this.bolFirstModelName == true) && 
			(this.strFirstModelName.equals(""))) {
			this.strFirstModelName = new String(ch, 
					                            start, 
					                            length);
		}

		if ((this.bolSecondModel == true) && 
			(this.bolSecondModelName == true) && 
			(this.strSecondModelName.equals(""))) {
			this.strSecondModelName = new String(ch, 
							                     start, 
							                     length);
		}

		
		if ((this.bolThirdModel == true) && 
				(this.bolThirdModelName == true) && 
				(this.strThirdModelName.equals(""))) {
				this.strThirdModelName = new String(ch, 
								                     start, 
								                     length);
			}
		
		
		
	}
		
	@Override public void endElement(String url,
			                         String localName,
			                         String qName) throws SAXException {
		if (qName.equals("experimentNames")) {
			this.bolExperimentNames = false;
		}
	}
	
	public String getCropName() {
		return this.strCropName;
	}
	
	public String getTrialName() {
		return this.strTrialName;
	}
	
	public Set<String> getExperimentNames() {
		return this.setExperimentNames;
	}

	public String getClassName() {
		return this.strClassName;
	}
	
	public String getSeasonName() {
		return this.strSeasonName;
	}
	
	public String getStageName() {
		return this.strStageName;
	}
	
	public String getFirstModelName() {
		return this.strFirstModelName;
	}

	public String getSecondModelName() {
		return this.strSecondModelName;
	}
	
	public String getThirdModelName() {
		return this.strThirdModelName;
	}
}
