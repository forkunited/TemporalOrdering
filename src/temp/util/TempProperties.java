package temp.util;

import ark.util.ARKProperties;

public class TempProperties extends ARKProperties {
	private String freeLingLibraryPath;
	private String freeLingDataDirectoryPath;
	
	public TempProperties() {
		super(new String[] { "temp.properties" } );

		this.freeLingLibraryPath = loadProperty("freeLingLibraryPath");
		this.freeLingDataDirectoryPath = loadProperty("freeLingDataDirectoryPath");
	}
	
	public String getFreeLingLibraryPath() {
		return this.freeLingLibraryPath;
	}
	
	public String getFreeLingDataDirectoryPath() {
		return this.freeLingDataDirectoryPath;
	}
}
