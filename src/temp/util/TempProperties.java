package temp.util;

import ark.util.Properties;

public class TempProperties extends Properties {
	private String freeLingLibraryPath;
	private String freeLingDataDirectoryPath;
	private String timeSieveDataDirectoryPath;
	
	public TempProperties() {
		super(new String[] { "temp.properties" } );

		this.freeLingLibraryPath = loadProperty("freeLingLibraryPath");
		this.freeLingDataDirectoryPath = loadProperty("freeLingDataDirectoryPath");
		this.timeSieveDataDirectoryPath = loadProperty("timeSieveDataDirectoryPath");
	}
	
	public String getFreeLingLibraryPath() {
		return this.freeLingLibraryPath;
	}
	
	public String getFreeLingDataDirectoryPath() {
		return this.freeLingDataDirectoryPath;
	}
	
	public String getTimeSieveDataDirectoryPath() {
		return this.timeSieveDataDirectoryPath;
	}
}
