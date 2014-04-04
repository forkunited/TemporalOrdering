package temp.model.annotator.timeml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;

import ark.data.annotation.nlp.DependencyParse;
import ark.data.annotation.nlp.TokenSpan;
import ark.data.annotation.nlp.WordNet;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.timeml.Event;
import temp.model.annotator.nlp.NLPAnnotatorFreeLing;
import temp.util.TempProperties;

public class EventAnnotatorChambers extends EventAnnotator {
	private static int MIN_FEATURE_CUT_OFF = 2;
	
	private Classifier<String,String> eventClassifier = null;
	private Classifier<String,String> tenseClassifier = null;
	private Classifier<String,String> aspectClassifier = null;
	private Classifier<String,String> classClassifier = null;
	
	private TempProperties properties;
	private NLPAnnotatorFreeLing freeLingAnnotator;
	
	public EventAnnotatorChambers(TempProperties properties) {
		this.properties = properties;
		this.freeLingAnnotator = new NLPAnnotatorFreeLing(this.properties);
	}
	
	public EventAnnotatorChambers(String modelPath) {
		deserialize(modelPath);
		this.freeLingAnnotator = new NLPAnnotatorFreeLing(this.properties);
	}
	
	@Override
	public Event[][] makeEvents(TempDocument document) {
		if (this.eventClassifier == null)
			return null;
		
		Event[][] events = new Event[document.getSentenceCount()][];
		
		this.freeLingAnnotator.setText(document.getText());
		WordNet.Hypernym[][][] hypernyms = this.freeLingAnnotator.makeTokenHypernyms();
		int eventId = 0;
		for (int i = 0; i < document.getSentenceCount(); i++) {
			List<Event> sentenceEvents = new ArrayList<Event>();
			for (int j = 0; j < document.getSentenceTokenCount(i); j++) {
				Counter<String> features = computeFeatures(document, hypernyms, i, j);
		  		RVFDatum<String,String> datum = new RVFDatum<String,String>(features, null);
		  		if (this.eventClassifier.classOf(datum).equals("1")) {
		  			Event.TimeMLTense timeMLTense = Event.TimeMLTense.valueOf(this.tenseClassifier.classOf(datum));
		  			Event.TimeMLAspect timeMLAspect = Event.TimeMLAspect.valueOf(this.aspectClassifier.classOf(datum));
		  			Event.TimeMLClass timeMLClass = Event.TimeMLClass.valueOf(this.classClassifier.classOf(datum));
		  			
		  			sentenceEvents.add(new Event(eventId, new TokenSpan(document, i, j, j+1), timeMLTense, timeMLAspect, timeMLClass));
		  		}
			}
			events[i] = new Event[sentenceEvents.size()];
			events[i] = sentenceEvents.toArray(events[i]);
		}
		
		return events;
	}

	public boolean train(TempDocumentSet documents, String modelPath) {
		List<TempDocument> documentList = documents.getDocuments();
	    RVFDataset<String, String> eventData  = new RVFDataset<String, String>();
	    RVFDataset<String, String> tenseData  = new RVFDataset<String, String>();
	    RVFDataset<String, String> aspectData = new RVFDataset<String, String>();
	    RVFDataset<String, String> classData  = new RVFDataset<String, String>();
	   
		for (TempDocument document : documentList) {
			freeLingAnnotator.setText(document.getText());
			WordNet.Hypernym[][][] hypernyms = freeLingAnnotator.makeTokenHypernyms();
			if (hypernyms.length != document.getSentenceCount())
				System.err.println("Warning: Mismatch between document sentence count and hypernym sentence count for " + document.getName());
			
			for (int i = 0; i < document.getSentenceCount(); i++) {
				for (int j = 0; j < document.getSentenceTokenCount(i); j++) {
					List<Event> eventsAtToken = document.getEventsForToken(i, j);
					Event eventAtToken = (!eventsAtToken.isEmpty()) ? eventsAtToken.get(0) : null;
					Counter<String> features = computeFeatures(document, hypernyms, i, j);		
					
					eventData.add(new RVFDatum<String, String>(features, (eventAtToken == null) ? "0" : "1"));
		            
					if (eventAtToken != null) {
						if (eventAtToken.getTimeMLTense() != null)
							tenseData.add(new RVFDatum<String, String>(features, eventAtToken.getTimeMLTense().toString()));
						
						if (eventAtToken.getTimeMLAspect() != null)
							aspectData.add(new RVFDatum<String, String>(features, eventAtToken.getTimeMLAspect().toString()));
						
						if (eventAtToken.getTimeMLClass() != null)
							classData.add(new RVFDatum<String, String>(features, eventAtToken.getTimeMLClass().toString()));
					}					
				}
			}
		}
		
		if (EventAnnotatorChambers.MIN_FEATURE_CUT_OFF > 1) {
			eventData.applyFeatureCountThreshold(EventAnnotatorChambers.MIN_FEATURE_CUT_OFF);
			tenseData.applyFeatureCountThreshold(EventAnnotatorChambers.MIN_FEATURE_CUT_OFF);
			aspectData.applyFeatureCountThreshold(EventAnnotatorChambers.MIN_FEATURE_CUT_OFF);
			classData.applyFeatureCountThreshold(EventAnnotatorChambers.MIN_FEATURE_CUT_OFF);
		}
		
		LinearClassifierFactory<String,String> modelFactory = new LinearClassifierFactory<String,String>();
		this.eventClassifier = modelFactory.trainClassifier(eventData);
		this.tenseClassifier = modelFactory.trainClassifier(tenseData);
		this.aspectClassifier = modelFactory.trainClassifier(aspectData);
		this.classClassifier = modelFactory.trainClassifier(classData);
		
		return serialize(modelPath);
	}
	
	private Counter<String> computeFeatures(TempDocument document, WordNet.Hypernym[][][] hypernyms, int sentenceIndex, int tokenIndex) {
		Counter<String> features = new ClassicCounter<String>();
		
		String tokenBefore2 = (tokenIndex - 2 >= 0) ? document.getToken(sentenceIndex, tokenIndex-2) : "<s>";
		String tokenBefore1 = (tokenIndex - 1 >= 0) ? document.getToken(sentenceIndex, tokenIndex-1) : "<s>";
		String token =  document.getToken(sentenceIndex, tokenIndex);
		String tokenAfter1 = (tokenIndex + 1 < document.getSentenceTokenCount(sentenceIndex)) ? document.getToken(sentenceIndex, tokenIndex + 1) : "</s>";
		String tokenAfter2 = (tokenIndex + 2 < document.getSentenceTokenCount(sentenceIndex)) ? document.getToken(sentenceIndex, tokenIndex + 2) : "</s>";
		
		features.incrementCount(token);
		features.incrementCount("E_" + tokenBefore1 + "-" + token);
		features.incrementCount("E_" + tokenBefore2 + "-" + tokenBefore1 + "-" + token);
		features.incrementCount("S_" + token + "-" + tokenAfter1);
		features.incrementCount("S_" + token + "-" + tokenAfter1 + "-" + tokenAfter2);
		features.incrementCount("M_" + tokenBefore1 + "-" + token + "-" + tokenAfter2);
		
		features.incrementCount("B_" + tokenBefore1);
		features.incrementCount("B_" + tokenBefore2 + "-" + tokenBefore1);
		features.incrementCount("A_" + tokenAfter1);
		features.incrementCount("A_" + tokenAfter1 + "-" + tokenAfter2);
		
		String posBefore2 = (tokenIndex - 2 >= 0) ? document.getPoSTag(sentenceIndex, tokenIndex-2).toString() : "<s>";
		String posBefore1 = (tokenIndex - 1 >= 0) ? document.getPoSTag(sentenceIndex, tokenIndex-1).toString() : "<s>";
		String pos =  document.getToken(sentenceIndex, tokenIndex);
		String posAfter1 = (tokenIndex + 1 < document.getSentenceTokenCount(sentenceIndex)) ? document.getPoSTag(sentenceIndex, tokenIndex + 1).toString() : "</s>";
		String posAfter2 = (tokenIndex + 2 < document.getSentenceTokenCount(sentenceIndex)) ? document.getPoSTag(sentenceIndex, tokenIndex + 2).toString() : "</s>";

		features.incrementCount(pos);
		features.incrementCount("E_" + posBefore1 + "-" + pos);
		features.incrementCount("E_" + posBefore2 + "-" + posBefore1 + "-" + pos);
		features.incrementCount("S_" + pos + "-" + posAfter1);
		features.incrementCount("S_" + pos + "-" + posAfter1 + "-" + posAfter2);
		features.incrementCount("M_" + posBefore1 + "-" + pos + "-" + posAfter2);
		
		features.incrementCount("B_" + posBefore1);
		features.incrementCount("B_" + posBefore2 + "-" + posBefore1);
		features.incrementCount("A_" + posAfter1);
		features.incrementCount("A_" + posAfter1 + "-" + posAfter2);
	
		if (hypernyms.length == document.getSentenceCount()) {
			if (hypernyms[tokenIndex].length != document.getSentenceTokenCount(sentenceIndex))
				System.err.println("Warning: Mismatch between document and hypernyms for sentence " + sentenceIndex + " of " + document.getName());
			else {
				WordNet.Hypernym[] tokenHypernyms = hypernyms[sentenceIndex][tokenIndex];
				for (int i = 0; i < tokenHypernyms.length; i++) {
					features.incrementCount("HN_" + tokenHypernyms[i]);
				}
			}
		}
		
		DependencyParse parse = document.getDependencyParse(sentenceIndex);
		List<DependencyParse.Dependency> childDependencies = parse.getGovernedDependencies(tokenIndex);
		for (DependencyParse.Dependency dependency : childDependencies) {
			features.incrementCount("DC_" + dependency.getType());
		}
		
		DependencyParse.Dependency parentDependency = parse.getGoverningDependency(tokenIndex);
		features.incrementCount("DP_" + parentDependency.getType());
		
		return features;
	}
	
	private boolean serialize(String modelPath) {
		try {
			IOUtils.writeObjectToFile(this.eventClassifier, modelPath + "-event");
			IOUtils.writeObjectToFile(this.tenseClassifier, modelPath + "-tense");
			IOUtils.writeObjectToFile(this.aspectClassifier, modelPath + "-aspect");
			IOUtils.writeObjectToFile(this.classClassifier, modelPath + "-class");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private boolean deserialize(String modelPath) {
		try {
			this.eventClassifier = (Classifier<String,String>)IOUtils.readObjectFromFile(modelPath + "-event");
			this.tenseClassifier = (Classifier<String,String>)IOUtils.readObjectFromFile(modelPath + "-tense");
			this.aspectClassifier = (Classifier<String,String>)IOUtils.readObjectFromFile(modelPath + "-aspect");
			this.classClassifier = (Classifier<String,String>)IOUtils.readObjectFromFile(modelPath + "-class");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
