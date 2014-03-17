package temp.data.features;

import java.util.*;

import jesse.util.CounterMap;
import jesse.util.Pair;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.nlp.TokenSpan;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.Time;

public class TemporalConnectives {
	TempDocumentSet tDocs;
	Set<TimeMLRelType> relations;
	
	public TemporalConnectives(TempDocumentSet _tDocs, Set<TimeMLRelType> _relations){
		tDocs = _tDocs;
		relations = _relations;
	}

	public void findConnectives() {
		// generate sentencesByDocByRelation 
		// (which is a map from relation to the set of sentenecs that include that relation)
		Map<TimeMLRelType, Map<String, Set<Integer>>> sentencesByDocByRelation = new HashMap<TimeMLRelType, Map<String, Set<Integer>>>();
		initializeSentencesByDocByRelation(sentencesByDocByRelation);
		int numTLinks = 0;
		int numTLinksBetweenEAndT = 0;
		int numTLinksBetweenEAndTSameSent = 0;
		for (TempDocument doc : tDocs.getDocuments()){
			for (TLink link : doc.getTLinks()){
				numTLinks++;
				Pair<Event, Time> eAndT = getEventAndTime(link);
				if (eAndT == null)
					continue;
				if (eAndT.getFirst().getTokenSpan().getSentenceIndex() == eAndT.getSecond().getTokenSpan().getSentenceIndex()){
					sentencesByDocByRelation.get(link.getTimeMLRelType()).get(doc.getName()).add(eAndT.getFirst().getTokenSpan().getSentenceIndex());
					numTLinksBetweenEAndTSameSent++;
				}
				numTLinksBetweenEAndT++;
				
			}
		}
		
		// FOR TESTING:
		
		for (TimeMLRelType rel : relations){
			System.out.println(sentencesByDocByRelation.get(rel).get("APW19980227.0476.tml"));
		}
		System.out.println("num links: " + numTLinks);
		System.out.println("numTLinksBetweenEAndT: " + numTLinksBetweenEAndT);
		System.out.println("numTLinksBetweenEAndTSameSent: " + numTLinksBetweenEAndTSameSent);
		System.exit(1);
		
		
		
		Pair<Map<TimeMLRelType, CounterMap<String>>, CounterMap<String>> counts = countTokens(sentencesByDocByRelation);
		Map<TimeMLRelType, CounterMap<String>> probFracs = new HashMap<TimeMLRelType, CounterMap<String>>();
		initializeProbFracs(probFracs);
		computeProbWordGivenRelation(counts.getFirst(), counts.getSecond(), probFracs);
		printTopTenByRelation(probFracs);
	}

	private void initializeProbFracs(Map<TimeMLRelType, CounterMap<String>> probFracs){
		for (TimeMLRelType rel : relations){
			probFracs.put(rel, new CounterMap<String>());
		}
	}

	private void initializeSentencesByDocByRelation(
			Map<TimeMLRelType, Map<String, Set<Integer>>> sentencesByDocByRelation) {
		for (TimeMLRelType rel : relations){
			sentencesByDocByRelation.put(rel, new HashMap<String, Set<Integer>>());
			for (TempDocument doc : tDocs.getDocuments()){
				sentencesByDocByRelation.get(rel).put(doc.getName(), new HashSet<Integer>());
			}
		}
	}

	// returns a pair of the event and time that are in a tlink.
	// returns null if the tlink doesn't contain one event and one time.
	private Pair<Event, Time> getEventAndTime(TLink link){
		TLinkable s = link.getSource();
		TLinkable t = link.getTarget();
		if (s.getTLinkableType() == TLinkable.Type.EVENT && t.getTLinkableType() == TLinkable.Type.TIME){
			Time time = (Time) t;
			Event event = (Event) s;
			return Pair.of(event, time);
		} else if (s.getTLinkableType() == TLinkable.Type.TIME && t.getTLinkableType() == TLinkable.Type.EVENT){
			Time time = (Time) s;
			Event event = (Event) t;
			return Pair.of(event, time);
		} else
			return null;
	}
	
	// counts tokens. generates counts by relation.
	private Pair<Map<TimeMLRelType, CounterMap<String>>, CounterMap<String>> countTokens(
			Map<TimeMLRelType, Map<String, Set<Integer>>> sentencesByDocByRelation){
		Map<TimeMLRelType, CounterMap<String>> tokenCountByRelation = new HashMap<TimeMLRelType, CounterMap<String>>();
		initializeTokenCountByRelation(tokenCountByRelation);
		CounterMap<String> tokens = new CounterMap<String>();
		
		// looping over the relations, then the documents, then the sentences to count the tokens
		for (TimeMLRelType rel : sentencesByDocByRelation.keySet()){
			Map<String, Set<Integer>> sentencesByDoc = sentencesByDocByRelation.get(rel);
			// have to loop over tDoc documents, since you can't retrieve them by name.
			for (TempDocument doc : tDocs.getDocuments()){
				for (int sentNum : sentencesByDoc.get(doc.getName())){
					for (int i = 0; i < doc.getSentenceTokenCount(sentNum); i++){
						String curToken = doc.getToken(sentNum, i);
						tokenCountByRelation.get(rel).increment(curToken);
						tokens.increment(curToken);
						
					}
				}
			}
		}
		return Pair.of(tokenCountByRelation, tokens);
	}
	
	// helper method for countTokens
	private void initializeTokenCountByRelation(
			Map<TimeMLRelType, CounterMap<String>> tokenCountByRelation) {
		for (TimeMLRelType rel : relations){
			tokenCountByRelation.put(rel, new CounterMap<String>());
		}
	}
	
	// computes P(word|relation)/P(word). If this number is high, it means this word is likely used with with that relation 
	// more often than it's used in the general corpus
	private void computeProbWordGivenRelation(
			Map<TimeMLRelType, CounterMap<String>> countsByRelation,
			CounterMap<String> tokenCounts, Map<TimeMLRelType, CounterMap<String>> probFracs) {
		
		for (TimeMLRelType rel : countsByRelation.keySet()){
			CounterMap<String> relationCounts = countsByRelation.get(rel);
			for (String token : relationCounts.map.keySet()){
				double probFrac = (relationCounts.value(token) / relationCounts.getTotal()) / (tokenCounts.value(token) / tokenCounts.getTotal());
				probFracs.get(rel).increment(token, probFrac);
			}
		}
	}

	private void printTopTenByRelation(
			Map<TimeMLRelType, CounterMap<String>> probFracs) {
		// print these in order by value
		for (TimeMLRelType rel : relations){
			System.out.println("Number of tokens in " + rel + ": " + probFracs.get(rel).map.keySet().size());
		}
		
		for (TimeMLRelType rel : relations){
			CounterMap<String> relProbFracs = probFracs.get(rel);
			Map<String, Double> sortedByProb = relProbFracs.sortByValue();
			System.out.println();
			System.out.println("Top for " + rel);
			int counter = 0;
			for (String s : sortedByProb.keySet()){
				counter++;
				System.out.println(s + ": " + sortedByProb.get(s));
				if (counter == 10)
					break;
			}
			System.out.println();
		}
		
	}
}






