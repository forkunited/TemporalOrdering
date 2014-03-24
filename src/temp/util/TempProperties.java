package temp.util;

import ark.util.Properties;

public class TempProperties extends Properties {
	private String freeLingLibraryPath;
	private String freeLingDataDirPath;
	
	private String tempDocumentDataDirPath;
	
	private String cregDataDirPath;
	private String cregCommandPath;
	
	private String experimentInputDirPath;
	private String experimentOutputDirPath;
	
	public TempProperties() {
		super(new String[] { "temp.properties" } );
		
		this.freeLingLibraryPath = loadProperty("freeLingLibraryPath");
		this.freeLingDataDirPath = loadProperty("freeLingDataDirPath");
		
		this.tempDocumentDataDirPath = loadProperty("tempDocumentDataDirPath");
		
		this.cregDataDirPath = loadProperty("cregDataDirPath");
		this.cregCommandPath = loadProperty("cregCommandPath");
		
		this.experimentInputDirPath = loadProperty("experimentInputDirPath");
		this.experimentOutputDirPath = loadProperty("experimentOutputDirPath");
	}
	
	public String getFreeLingLibraryPath() {
		return this.freeLingLibraryPath;
	}
	
	public String getFreeLingDataDirPath() {
		return this.freeLingDataDirPath;
	}
	
	public String getTempDocumentDataDirPath() {
		return this.tempDocumentDataDirPath;
	}
	
	public String getCregDataDirPath() {
		return this.cregDataDirPath;
	}
	
	public String getCregCommandPath() {
		return this.cregCommandPath;
	}

	public String getExperimentInputDirPath() {
		return this.experimentInputDirPath;
	}
	
	public String getExperimentOutputDirPath() {
		return this.experimentOutputDirPath;
	}
}
