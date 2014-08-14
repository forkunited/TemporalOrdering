package temp.scratch;

import java.io.File;
import java.util.*;

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
import ark.data.annotation.structure.DatumStructureCollection;
import ark.experiment.ExperimentGST;
import ark.util.OutputWriter;

/**
 * FIXME: Fill in documentation maybe later
 * 
 * @author Jesse Dodge
 */
public class ExperimentGSTTLinkTypeStructured {
	private static int tlinkId;
	
	public static void main(String[] args) {
		// FIXME: I commented this because it was broken on my computer...
		// I think you just forgot to add this class to repo? - Bill
		//TransitiveConstraintsAllWays tcaw = new TransitiveConstraintsAllWays();
		//tcaw.checkIfAllTwoPairsNeedToBeChecked();
		//System.exit(0);
		
		
		String experimentName = "GSTTLinkType/" + args[0];
		String documentSetName = args[1];
		boolean useTestData = Boolean.valueOf(args[2]);
		boolean checkDisjunctiveConstraints = Boolean.valueOf(args[3]);
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
		
		testConstraintsOnFullData(trainData, devData, testData, output, datumTools, checkDisjunctiveConstraints);

		if (!experiment.run())
			output.debugWriteln("Error: Experiment run failed.");
		testConstraintsOnOutput(experiment.getClassifiedData(), output, datumTools, checkDisjunctiveConstraints);

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
	
	// tests constraints on the training, dev, and test data sets.
	private static void testConstraintsOnFullData(DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> trainData, 
			DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> devData, DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> testData, OutputWriter output,
			Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools, boolean checkDisjunctiveConstraints){
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> fullData = new DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType>(datumTools, null);
		fullData.addAll(trainData);
		fullData.addAll(devData);
		if (testData != null)
			fullData.addAll(testData);
		testConstraints(fullData, output, "training, dev, and test", checkDisjunctiveConstraints);
		
	}
	
	// tests that the output of the experiment has the correct structure.
	private static void testConstraintsOnOutput(
			Map<TLinkDatum<TimeMLRelType>, TimeMLRelType> classifiedData,
			OutputWriter output,
			Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools, boolean checkDisjunctiveConstraints) {
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data = new DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType>(datumTools, null);
		
		for (TLinkDatum<TimeMLRelType> oldDatum : classifiedData.keySet()) {
			TLink newTLink = new TLink(oldDatum.getTLink().getId(), oldDatum.getTLink().getSource(), oldDatum.getTLink().getTarget(), classifiedData.get(oldDatum));
			data.add(new TLinkDatum<TimeMLRelType>(oldDatum.getId(), newTLink, newTLink.getTimeMLRelType()));
		}
		
		testConstraints(data, output, "output", checkDisjunctiveConstraints);		
	}
	
	// to test the set of constraints that should be enforced on each document. 
	// the set of constraints are listed in TLink, the actual constraint checking happens within TLinkGraph, at the DatumStructure level.
	private static void testConstraints(DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data, OutputWriter output, String typeOfData, boolean checkDisjunctiveConstraints){
		DatumStructureCollection<TLinkDatum<TimeMLRelType>, TimeMLRelType> structuredDataset = data.getDatumTools().makeDatumStructureCollection("TLinkGraphDocument", data);
		List<Map<String, Integer>> violations = structuredDataset.checkConstraints(checkDisjunctiveConstraints);
		int totalBroken = 0;
		int total = 0;
		int totalBrokenWithDCT = 0;
		for (Map<String, Integer> v : violations){
			total += v.get("totalTriplets");
			totalBroken += v.get("numTripletsWithTransBroken");
			totalBrokenWithDCT += v.get("numTripletsWithTransBrokenWithDCT");
		}
		output.debugWriteln("");
		output.debugWriteln("Testing constraints on the " + typeOfData + " data:");
		output.debugWriteln("Testing disjunctive closure? " + checkDisjunctiveConstraints);
		output.debugWriteln("Total transitive closures in dataset: " + total);
		output.debugWriteln("Total transitive closures broken: " + totalBroken);
		output.debugWriteln("Total transitive closures broken involving DCT: " + totalBrokenWithDCT);
		output.debugWriteln("");
		
	}
	

}