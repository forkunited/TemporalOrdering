package temp.data;

import java.io.File;

import temp.util.TempProperties;
import ark.data.DataTools;
import ark.util.OutputWriter;
import ark.util.Stemmer;

/**
 * TempDataTools contains data tools (gazetteers, text
 * cleaning functions, etc) for temporal ordering tasks.
 * 
 * @author Bill McDowell
 *
 */
public class TempDataTools extends DataTools {
	private TempProperties properties;
	public TempDataTools(OutputWriter outputWriter, TempProperties properties) {
		super(outputWriter);
		
		this.properties = properties;
		this.addPath("CregCmd", new Path("CregCmd", properties.getCregCommandPath()));
	
		// For cleaning strings, and replacing all white space with "_"
		// Especially useful for features to clean unigrams
		this.addCleanFn(new DataTools.StringTransform() {
			public String toString() {
				return "TempDefaultCleanFn";
			}
			
			@Override
			public String transform(String str) {
				str = str.trim();
				str = str.replaceAll("[\\W&&[^\\s]]+", " ") // replaces all non-alpha-numeric (differs from http://qwone.com/~jason/writing/loocv.pdf)
						 .replaceAll("\\d+.*", "[D]") 
						 .replaceAll("_", " ")
						 .trim()
						 .toLowerCase();
				
				String[] parts = str.split("\\s+");
				StringBuilder retStr = new StringBuilder();
				for (int i = 0; i < parts.length; i++) {
					if (parts[i].length() < 3 || parts[i].length() > 25) // remove short and long tokens
						continue;
					
					parts[i] = Stemmer.stem(parts[i]);
					retStr = retStr.append(parts[i]).append("_");
				}
				
				return retStr.toString().trim();
			}
		});
		
	}
	
	/**
	 * Get path by name given in experiment configuration file.  This
	 * allows the experiments to refer to paths without being machine
	 * specific.  
	 * 
	 * Paths starting with 'CregModel' refer to serialized creg models
	 * stored in the directory specified by 'cregDataDirPath' in
	 * temp.properties.
	 */
	public Path getPath(String name) {
		if (name == null)
			return null;
		if (!name.startsWith("CregModel"))
			return super.getPath(name);
		
		String modelName = name.substring("CregModel/".length());
		String modelPath = (new File(this.properties.getCregDataDirPath(), modelName)).getAbsolutePath();
		return new Path(name, modelPath);
		
	}

}
