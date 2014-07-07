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
import ark.experiment.ExperimentGST;
import ark.util.OutputWriter;

/**
 * ExperimentGSTTLinkType takes arguments
 * 
 * [experimentName] - Name of an experiment in 'experiments/GSTTLinkType' to run
 * [documentSetName] - Name of data-set on which to run stored under [tempDocumentDataDirPath] from 'temp.properties' 
 * [useTestData] - Indicator of whether or not to evaluate trained models on test set
 * 
 * And runs the grid-search-test experiment specified by the
 * [experimentName].experiment configuration file in 'experiments/GSTTLinkType' on
 * data in [tempDocumentDataDirPath]/[documentSetName] (where [tempDocumentDataDirPath]
 * is specified in 'temp.properties') on the TLink relation-type classification 
 * task.  The grid-search-test experiment is run by ark.experiment.ExperimentGST 
 * (see documentation in ARKWater for details) to train models on a training set,
 * perform a grid search on the dev set, and optionally evaluate on a test set. 
 * 
 * ExperimentGSTTLinkType assumes that the directory
 * [experimentOutputDir]/GSTTLinkType/[documentSetName] also exists to store the output
 * of the experiment (where [experimentOutputDir] is specified in
 * 'temp.properties'), and also that [tempDocumentDataDirPath]/[documentSetName]
 * contains 'train', 'dev', and 'test' subdirectories for the train/dev/test
 * splits.
 * 
 * The input experiment configuration files in 'experiments/GSTTLinkType' are
 * mostly named according to the following convention:
 * 
 * [modelName][featureSet].experiment
 * 
 * Where [modelName] refers to a generic model (e.g. SVM) and [featureSet] refers
 * to a set of features (e.g. 'base' for a baseline set of features).  They are parsed
 * by ark.experiment.ExperimentGST (see ARKWater for documentation on input configuration
 * format).
 * 
 * @author Bill McDowell
 * 
 */
public class ExperimentGSTTLinkType {
	private static int tlinkId;
	
	public static void main(String[] args) {
		String experimentName = "GSTTLinkType/" + args[0];
		String documentSetName = args[1];
		boolean useTestData = Boolean.valueOf(args[2]);
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
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> trainData = loadDataFromDirectory(documentSetPath + "/train", datumTools);
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> devData = loadDataFromDirectory(documentSetPath + "/dev", datumTools);
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> testData = null;
		if (useTestData)
			testData = loadDataFromDirectory(documentSetPath + "/test", datumTools);
		
		ExperimentGST<TLinkDatum<TimeMLRelType>, TimeMLRelType> experiment = 
				new ExperimentGST<TLinkDatum<TimeMLRelType>, TimeMLRelType>(experimentOutputName, experimentInputPath, trainData, devData, testData);
	
		if (!experiment.run())
			output.debugWriteln("Error: Experiment run failed.");

	}
	
	private static DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> loadDataFromDirectory(String path, Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools) {
		TempDocumentSet documentSet = TempDocumentSet.loadFromJSONDirectory(path);
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data = new DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType>(datumTools, null);
		
		List<TempDocument> documents = documentSet.getDocuments();
		for (TempDocument document : documents) {
			List<TLink> tlinks = document.getTLinks();
			for (TLink tlink : tlinks) {
				String storedTlinkId = tlink.getId();
				int id = (storedTlinkId == null) ? tlinkId : Integer.valueOf(storedTlinkId.substring(1));
				
				TLinkDatum<TimeMLRelType> tlinkDatum = new TLinkDatum<TimeMLRelType>(id, tlink, tlink.getTimeMLRelType());
				data.add(tlinkDatum);
				
				tlinkId++;
			}
		}
		
		return data;
	}
}
