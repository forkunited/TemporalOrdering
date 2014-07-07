package temp.scratch;

import java.io.File;
import java.util.List;

import temp.data.TempDataTools;
import temp.data.annotation.TLinkDatum;
import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.structure.InferenceRulesTimeMLRelType;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.util.TempProperties;
import ark.data.annotation.DataSet;
import ark.data.annotation.Datum.Tools;
import ark.experiment.ExperimentKCV;
import ark.util.OutputWriter;

/**
 * ExperimentKCVTLinkType takes arguments
 * 
 * [experimentName] - Name of an experiment in 'experiments/KCVTLinkType' to run
 * [documentSetName] - Name of data-set on which to run stored under [tempDocumentDataDirPath] from 'temp.properties' 
 * 
 * And runs the cross-validation experiment specified by the
 * [experimentName].experiment configuration file in 'experiments/KCVTLinkType' on
 * data in [tempDocumentDataDirPath]/[documentSetName] (where [tempDocumentDataDirPath]
 * is specified in 'temp.properties') on the TLink relation-type classification 
 * task.  The cross validation experiment is run by ark.experiment.ExperimentKCV 
 * with an optional grid-search (see documentation in ARKWater for details).
 * 
 * ExperimentKCVTLinkType assumes that the directory
 * [experimentOutputDir]/KCVTLinkType/[documentSetName] also exists to store the output
 * of the experiment (where [experimentOutputDir] is specified in
 * 'temp.properties').
 * 
 * The input experiment configuration files in 'experiments/KCVTLinkType' are
 * mostly named according to the following convention:
 * 
 * [modelName][featureSet].experiment
 * 
 * Where [modelName] refers to a generic model (e.g. SVM) and [featureSet] refers
 * to a set of features (e.g. 'base' for a baseline set of features).  They are parsed
 * by ark.experiment.ExperimentKCV (see ARKWater for documentation on input configuration
 * format).
 * 
 * @author Bill McDowell
 * 
 */
public class ExperimentKCVTLinkType {
	public static void main(String[] args) {
		String experimentName = "KCVTLinkType/" + args[0];
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
		
		Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools = TLinkDatum.getTimeMLRelTypeTools(dataTools, new InferenceRulesTimeMLRelType());
		
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
				new ExperimentKCV<TLinkDatum<TimeMLRelType>, TimeMLRelType>(experimentOutputName, experimentInputPath, data);
	
		long startTime = System.currentTimeMillis();
		if (!experiment.run())
			output.debugWriteln("Error: Experiment run failed.");
		System.out.println();
		System.out.println("The total runtime, in seconds: " + (System.currentTimeMillis() - startTime) / 1000);
		System.out.println("The total runtime, in minutes: " + ((System.currentTimeMillis() - startTime) / 1000) / 60);
		System.out.println("The total runtime, in hours: " + (((System.currentTimeMillis() - startTime) / 1000) / 60) / 60);


	}
}
