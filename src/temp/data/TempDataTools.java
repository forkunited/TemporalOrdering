package temp.data;

import java.io.File;

import temp.util.TempProperties;
import ark.data.DataTools;
import ark.util.OutputWriter;
import ark.util.Stemmer;

public class TempDataTools extends DataTools {
	private TempProperties properties;
	public TempDataTools(OutputWriter outputWriter, TempProperties properties) {
		super(outputWriter);
		
		this.properties = properties;
		this.addPath("CregCmd", new Path("CregCmd", properties.getCregCommandPath()));
	
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
					if (parts[i].length() < 3 || parts[i].length() > 25)
						continue;
					
					parts[i] = Stemmer.stem(parts[i]);
					retStr = retStr.append(parts[i]).append("_");
				}
				
				return retStr.toString().trim();
			}
		});
		
	}
	
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
