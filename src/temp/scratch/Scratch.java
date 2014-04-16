package temp.scratch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ark.data.DataTools;
import ark.data.annotation.nlp.WordNet;
import ark.data.annotation.Datum.Tools;
import ark.data.annotation.Language;
import ark.data.feature.FeatureNGramSentence;
import ark.data.feature.FeaturizedDataSet;
import ark.util.OutputWriter;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;

import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.annotation.timeml.Time;
import temp.model.annotator.nlp.NLPAnnotatorFreeLing;
import temp.model.annotator.timeml.TimeAnnotatorHeidel;
import temp.util.TempProperties;

public class Scratch {
	public static void main(String[] args) throws IOException {		
		/*TempProperties properties = new TempProperties();
		TempDataTools dataTools = new TempDataTools(new OutputWriter(), properties);
		
		Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools = TLinkDatum.getTimeMLRelTypeTools(dataTools);
		
		String documentSetPath = (new File(properties.getTempDocumentDataDirPath(), "TimeBankDenseUnofficial")).getAbsolutePath();
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
				
				if (i > 25)
					break;
			}
			
			if (i > 25)
				break;
		}
		
		
		List<DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType>> partition = data.makePartition(new double[] {1.0/8, 1.0/8, 1.0/8, 1.0/8, 1.0/8, 1.0/8, 1.0/8, 1.0/8 }, new Random());
		for (int j = 0; j < partition.size(); j++) {
			for (TLinkDatum<TimeMLRelType> datum : partition.get(j)) {
				System.out.print(datum.getId() + " ");
			}
			System.out.flush();
			System.out.print("\n");
		}*/
		
		///
		
		TempDocumentSet set = TempDocumentSet.loadFromJSONDirectory("");
		List<TempDocument> documents = set.getDocuments();
		
		OutputWriter output = new OutputWriter();
		DataTools dataTools = new DataTools(output);
		Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools = TLinkDatum.getTimeMLRelTypeTools(dataTools);
		FeaturizedDataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data = new FeaturizedDataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType>("test", 1, null, null);
		for (TempDocument document : documents) {
			List<TLink> tlinks = document.getTLinks();
			for (int i = 0; i < tlinks.size(); i++) {
				TLinkDatum<TLink.TimeMLRelType> datum = new TLinkDatum<TLink.TimeMLRelType>(i, tlinks.get(i), tlinks.get(i).getTimeMLRelType());
				data.add(datum);
			}
		}
		
		FeatureNGramSentence<TLinkDatum<TimeMLRelType>, TimeMLRelType> feature = new FeatureNGramSentence<TLinkDatum<TimeMLRelType>, TimeMLRelType>();
		BufferedReader reader = new BufferedReader(new StringReader("NGramSentence(minFeatureOccurrence=1, n=1, cleanFn=DefaultCleanFn, tokenExtractor=SourceTokenSpan)"));
		feature.deserialize(reader, true, false, datumTools, null, false);
		data.addFeature(feature);

		for (TLinkDatum<TimeMLRelType> datum : data) {
			Map<Integer, Double> featureValues = data.getFeatureVocabularyValues(datum);
			Map<Integer, String> featureNames = data.getFeatureVocabularyNamesForIndices(featureValues.keySet());
		
			System.out.println("Datum " + datum.getId() + ": ");
			for (Entry<Integer, Double> featureValue : featureValues.entrySet()) {
				System.out.println(featureNames.get(featureValue.getKey()) + " = " + featureValue.getValue());
			}
			System.out.println();
		}
		
		///
		
		Pattern p = Pattern.compile("(.*)\\((.*)\\-([0-9']*),(.*)\\-([0-9']*)\\)");
		Matcher m = p.matcher("det(hand-4, the-2)");		
		m.matches();
		System.out.println(m.group(1));
		
		///
		
		TempProperties properties = new TempProperties();
		NLPAnnotatorFreeLing annotator = new NLPAnnotatorFreeLing(properties);
		annotator.setLanguage(Language.English);
		annotator.setText("I was at the carnival.");
		WordNet.Hypernym[][][] hypernyms = annotator.makeTokenHypernyms();
		for (int i = 0; i < hypernyms[0].length; i++) {
			for (int j = 0; j < hypernyms[0][i].length; j++) {
				System.out.print(hypernyms[0][i][j] + " ");
			}
			System.out.println();
		}
		
		///
		
		TempDocument document = new TempDocument("test", "The dog barks on March 30th 2013. I ran for 10 minutes.", Language.English, Calendar.getInstance().getTime(), annotator);
		TimeAnnotatorHeidel timeAnnotator = new TimeAnnotatorHeidel(properties);
		Time[][] times = timeAnnotator.makeTimes(document);
		for (int i = 0; i < times.length; i++) {
			for (int j = 0; j < times[i].length; j++) {
				System.out.println(times[i][j].toJSON().toString());
			}
		}
		
	}
}
