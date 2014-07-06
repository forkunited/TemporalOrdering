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

/**
 * ExperimentKCVTLinkTypeSplit takes arguments
 * 
 * [experimentName] - Name of an experiment in 'experiments/KCVTLinkTypeSplit' to run
 * [documentSetName] - Name of data-set on which to run stored under [tempDocumentDataDirPath] from 'temp.properties' 
 * 
 * And runs the grid-search-test experiment specified by the
 * [experimentName].experiment configuration file in 'experiments/KCVTLinkTypeSplit' on
 * data in [tempDocumentDataDirPath]/[documentSetName] (where [tempDocumentDataDirPath]
 * is specified in 'temp.properties') on the TLink split-relation-type classification 
 * task.  The cross validation experiment is run by ark.experiment.ExperimentKCV 
 * with an optional grid-search (see documentation in ARKWater for details).
 * 
 * ExperimentKCVTLinkTypeSplit assumes that the directory
 * [experimentOutputDir]/KCVTLinkTypeSplit/[documentSetName] also exists to store the output
 * of the experiment (where [experimentOutputDir] is specified in
 * 'temp.properties').
 * 
 * The input experiment configuration files in 'experiments/KCVTLinkTypeSplit' are
 * mostly named according to the following convention:
 * 
 * [modelName][featureSet].experiment
 * 
 * Where [modelName] refers to a generic model (e.g. SVM) and [featureSet] refers
 * to a set of features (e.g. 'base' for a baseline set of features).  They are parsed
 * by ark.experiment.ExperimentKCV (see ARKWater for documentation on input configuration
 * format).
 * 
 * The TLink split-relation-type classification task is the same as the TLink relation-
 * type task except that every relation-type R is split into relation-types R_0 and R_1
 * (from temp.data.annotation.cost.TimeMLRelTypeSplit),
 * and R_0 and R_1 are randomly assigned to relations that actually have type R.  The 
 * purpose of this is to test whether the cost learning models 
 * can recover from random label splitting.  You may need to import
 * cost.jar (compiled from the repository at 
 * https://github.com/forkunited/CostFunctionLearning) to get the cost learning 
 * models to work, since these were recently removed from ARKWater and placed in
 * their own library.
 * 
 * @author Bill McDowell
 * 
 */
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
