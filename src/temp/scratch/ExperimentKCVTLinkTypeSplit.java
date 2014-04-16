package temp.scratch;

import java.io.File;
import java.util.List;

import temp.data.TempDataTools;
import temp.data.annotation.TLinkDatum;
import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.cost.TimeMLRelTypeSplit;
import temp.data.annotation.timeml.TLink;
import temp.util.TempProperties;
import ark.data.annotation.DataSet;
import ark.data.annotation.Datum.Tools;
import ark.experiment.ExperimentKCV;
import ark.util.OutputWriter;

public class ExperimentKCVTLinkTypeSplit {
	public static void main(String[] args) {
		String experimentName = "KCVTLinkTypeSplit/" + args[0];
		String documentSetName = args[1];
		String experimentOutputName = documentSetName + "/" + experimentName;

		TempProperties properties = new TempProperties();
		String experimentInputPath = new File(properties.getExperimentInputDirPath(), experimentName + ".experiment").getAbsolutePath();
		String experimentOutputPath = new File(properties.getExperimentOutputDirPath(), experimentOutputName).getAbsolutePath(); 
		
		OutputWriter output = new OutputWriter(
				new File(experimentOutputPath + ".debug.out"),
				new File(experimentOutputPath + ".results.out"),
				new File(experimentOutputPath + ".data.out"),
				new File(experimentOutputPath + ".model.out")
			);
		
		TempDataTools dataTools = new TempDataTools(output, properties);
		dataTools.addToParameterEnvironment("DOCUMENT_SET", documentSetName);
		
		Tools<TLinkDatum<TimeMLRelTypeSplit>, TimeMLRelTypeSplit> datumTools = TLinkDatum.getTimeMLRelTypeSplitTools(dataTools);
		
		String documentSetPath = (new File(properties.getTempDocumentDataDirPath(), documentSetName)).getAbsolutePath();
		TempDocumentSet documentSet = TempDocumentSet.loadFromJSONDirectory(documentSetPath);
		DataSet<TLinkDatum<TimeMLRelTypeSplit>, TimeMLRelTypeSplit> data = new DataSet<TLinkDatum<TimeMLRelTypeSplit>, TimeMLRelTypeSplit>(datumTools, null);
		
		List<TempDocument> documents = documentSet.getDocuments();
		int i = 0;
		for (TempDocument document : documents) {
			List<TLink> tlinks = document.getTLinks();
			for (TLink tlink : tlinks) {
				String tlinkId = tlink.getId();
				int id = (tlinkId == null) ? i : Integer.valueOf(tlinkId.substring(1));
				
				TimeMLRelTypeSplit splitType = TimeMLRelTypeSplit.valueOf(tlink.getTimeMLRelType().toString() + "_" + String.valueOf((i % 2)));
				TLinkDatum<TimeMLRelTypeSplit> tlinkDatum = new TLinkDatum<TimeMLRelTypeSplit>(id, tlink, splitType);
				data.add(tlinkDatum);
				
				i++;
			}
		}
		
		ExperimentKCV<TLinkDatum<TimeMLRelTypeSplit>, TimeMLRelTypeSplit> experiment = 
				new ExperimentKCV<TLinkDatum<TimeMLRelTypeSplit>, TimeMLRelTypeSplit>(experimentOutputName, experimentInputPath, data);
	
		if (!experiment.run())
			output.debugWriteln("Error: Experiment run failed.");
		
	}
}
