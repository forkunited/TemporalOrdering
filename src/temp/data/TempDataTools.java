package temp.data;

import java.io.File;

import temp.util.TempProperties;
import ark.data.DataTools;
import ark.util.OutputWriter;

public class TempDataTools extends DataTools {
	private TempProperties properties;
	public TempDataTools(OutputWriter outputWriter, TempProperties properties) {
		super(outputWriter);
		
		this.properties = properties;
		this.addPath("CregCmd", new Path("CregCmd", properties.getCregCommandPath()));
	
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
