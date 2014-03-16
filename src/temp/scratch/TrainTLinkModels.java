package temp.scratch;

import java.util.*;

import temp.data.annotation.*;
import temp.data.annotation.nlp.TokenSpan;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.Time;
import temp.model.annotator.timeml.tlink.EdgeFeatures;
import temp.model.annotator.timeml.tlink.TLinkModel;

public class TrainTLinkModels {
	static Set<TimeMLRelType> relations;
	
	public static void main(String[] args){
		TempDocumentSet tDocs = TempDocumentSet.loadFromJSONDirectory("/home/jesse/workspace/temporalOrdering/TemporalOrdering/data/sieve");
		//TestConstraints.testConstraints(tDocs);
		relations = TestConstraints.findRelations(tDocs);
		
		TLinkModel model = new TLinkModel();
		for (TempDocument doc : tDocs.getDocuments()){
			processDoc(doc, model);
		}
	}
	
	// processes one document
	// for each sentence:
	//   for each index:
	//      if it's a time or event:
	//         link it with all times and events in this sentence and next sentence.
	private static void processDoc(TempDocument doc, TLinkModel model){
		for (int i = 0; i < doc.getSentenceCount() - 1; i++){
			List<Event> curEvents = doc.getEvents(i);
			List<Time> curTimes = doc.getTimes(i);
			List<Event> nextEvents = doc.getEvents(i+1);
			List<Time> nextTimes = doc.getTimes(i+1);
			
			for (int j = 0; j < doc.getSentenceTokenCount(i); j++){
				List<Event> curTokenEvent = doc.getEventsForToken(i, j);
				List<Time> curTokenTime = doc.getTimesForToken(i, j);
				for (int k = 0; k < curTokenEvent.size(); k++){
					computeFutureEdgesForEvent(curTokenEvent.get(k), curTimes, nextTimes, model, doc, i);
				}
				// TODO: Implement this for curTokenTime as well. 
			}

			
		}
		
	}

	// generating edges from events to times. This model will be different than the model for events to events.
	private static void computeFutureEdgesForEvent(Event event,
			List<Time> curTimes, List<Time> nextTimes, TLinkModel model, TempDocument doc, int sentenceNum) {
		for (int i = 0; i < curTimes.size(); i++){
			Time time = curTimes.get(i);
			// makes sure the first is before the second
			if (compareTokeIndices(event, time)){
				EdgeFeatures eFeats = new EdgeFeatures(event, time, doc, sentenceNum);
				// goal: choose the highest scoring edge.
				// how to do that:
				//   compute score for each edge
				//   subtract 1 from the features on the predicted edge
				//   add one to the features for the gold edge
				
				//getBestEdge(eFeats, );
			}
		}
		
	}
	
	private static void getBestEdge(EdgeFeatures eFeats) {
		
		
	}

	// compares two TLinkables (probably an event vs a time) and returns true if the first is before the second in the sentence.
	private static boolean compareTokeIndices(TLinkable first, TLinkable second){
		return first.getTokenSpan().getEndTokenIndex() < second.getTokenSpan().getStartTokenIndex();
	}

}
