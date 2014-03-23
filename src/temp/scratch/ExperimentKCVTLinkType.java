package temp.scratch;

import java.io.File;
import java.util.List;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.util.TempProperties;
import ark.data.DataTools;
import ark.data.annotation.DataSet;
import ark.data.annotation.Datum.Tools;
import ark.experiment.ExperimentKCV;
import ark.util.OutputWriter;

public class ExperimentKCVTLinkType {
	public static void main(String[] args) {
		String experimentName = "KCVTLinkType/" + args[0];
		String documentSetName = args[1];
		
		TempProperties properties = new TempProperties();
		String experimentInputPath = new File(properties.getExperimentInputDirPath(), experimentName + ".experiment").getAbsolutePath();
		String experimentOutputPath = new File(properties.getExperimentOutputDirPath(), experimentName).getAbsolutePath(); 
		
		OutputWriter output = new OutputWriter(
				new File(experimentOutputPath + ".debug.out"),
				new File(experimentOutputPath + ".results.out"),
				new File(experimentOutputPath + ".data.out"),
				new File(experimentOutputPath + ".model.out")
			);
		
		DataTools dataTools = new DataTools(output);
		Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools = TLinkDatum.getTimeMLRelTypeTools(dataTools);
		
		String documentSetPath = (new File(properties.getTempDocumentDataDirPath(), documentSetName)).getAbsolutePath();
		TempDocumentSet documentSet = TempDocumentSet.loadFromJSONDirectory(documentSetPath);
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data = new DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType>(datumTools, null);
		
		List<TempDocument> documents = documentSet.getDocuments();
		int i = 0;
		for (TempDocument document : documents) {
			List<TLink> tlinks = document.getTLinks();
			for (TLink tlink : tlinks) {
				String tlinkId = tlink.getId();
				int id = (tlinkId == null) ? i : Integer.valueOf(tlinkId.substring(1));
				
				TLinkDatum<TimeMLRelType> tlinkDatum = new TLinkDatum<TimeMLRelType>(id, tlink, tlink.getTimeMLRelType());
				data.add(tlinkDatum);
				
				i++;
			}
		}
		
		ExperimentKCV<TLinkDatum<TimeMLRelType>, TimeMLRelType> experiment = 
				new ExperimentKCV<TLinkDatum<TimeMLRelType>, TimeMLRelType>(experimentName, experimentInputPath, datumTools);
	
		if (!experiment.run(data))
			output.debugWriteln("Error: Experiment run failed.");
		
	}
}
