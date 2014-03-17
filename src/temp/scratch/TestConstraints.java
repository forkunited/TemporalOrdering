package temp.scratch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.Time;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.annotation.timeml.TLinkable.Type;

public class TestConstraints {
	
	public static void testConstraints(TempDocumentSet tDocs){
		//int numRel = findNumRelations(tDocs);
		// goal: Find out if the data holds the same properties. 
		// Constraint one: For each time, there are one or more events with a non-'vague' relation.
		// Constraint two: For each event, there are zero or one times with a non-'vague' relation.
		// to test these: for each event or time, have an array of counts of each type of relation it has. 
		// 				store two global 2-D arrays (event and time) that each have a row for min, a row for average, and a row for max.
		// 				update the arrays for each event or each time.
		// event vs time -> eventid or timeid -> reltype
		Map<TLinkable.Type, Map<String, Map<String, Map<TLink.TimeMLRelType, Integer>>>> counters = 
				new HashMap<TLinkable.Type, Map<String, Map<String, Map<TLink.TimeMLRelType, Integer>>>>();
		initializeCounters(counters, tDocs);
		int numTimes = 0;
		int numEvents = 0;
		int numTlinks = 0;
		int numTlinksBetweenEventAndTime = 0;
		int numDocuments = 0;
		int numSentences = 0;
		
		for (TempDocument doc : tDocs.getDocuments()){
			int docNumTlinksBetweenEventAndTime = countTlinks(doc);
			System.out.println("doc: " + doc.getName());
			System.out.println("number of tlinks: " + doc.getTLinks().size());
			System.out.println("number of tlinks between event and time: " + docNumTlinksBetweenEventAndTime);
			System.out.println("number of sentences: " + doc.getSentenceCount());
			System.out.println("number of times: " + doc.getTimes().size());
			System.out.println("number of events: " + doc.getEvents().size());
			System.out.println();
			numTimes += doc.getTimes().size();
			numEvents += doc.getEvents().size();
			numTlinks += doc.getTLinks().size();
			numTlinksBetweenEventAndTime += docNumTlinksBetweenEventAndTime;
			numDocuments++;
			numSentences += doc.getSentenceCount();
			
			// generate a map from eventID -> array
			// generate a map from timeID -> array
			// for each tlink:
			//   if (it has one event and one time):
			//     update the eventID and timeID array that's in the tlink
			for (TLink tl : doc.getTLinks()){
				// if the tlink is between an event and a time:
				if (tl.getSource().getTLinkableType() != tl.getTarget().getTLinkableType()){
					incrementCounterMap(counters, tl.getSource(), tl.getTimeMLRelType(), doc.getName());
					incrementCounterMap(counters, tl.getTarget(), tl.getTimeMLRelType(), doc.getName());
				}
					
					//System.out.println("Event and time: " + tl.getSource().getTLinkableType() + " " )
				//}
			}
		}
		printDatasetStats(numTimes, numEvents, numDocuments, numSentences, numTlinks, numTlinksBetweenEventAndTime);
		printCounters(counters);
	}
	
	// counts the number of tlinks that exist between events and times in a given document.
	private static int countTlinks(TempDocument doc) {
		int counter = 0; 
		for (TLink t : doc.getTLinks()){
			if (t.getSource().getTLinkableType() != t.getTarget().getTLinkableType())
				counter++;
		}
		return counter;
	}

	private static void printDatasetStats(int numTimes, int numEvents,
			int numDocuments, int numSentences, int numTlinks, int numTlinksBetweenEventAndTime) {
		System.out.println("Corpus statistics!");
		System.out.println("Number of times in docs:                   " + numTimes);
		System.out.println("Number of events in docs:                  " + numEvents);
		System.out.println("Number of documents:                       " + numDocuments);
		System.out.println("Number of sentences:                       " + numSentences);
		System.out.println("Number of tlinks:                          " + numTlinks);
		System.out.println("Number of tilkns between events and times: " + numTlinksBetweenEventAndTime);
	}

	private static void initializeCounters(
			Map<Type, Map<String, Map<String, Map<TimeMLRelType, Integer>>>> counters,
			TempDocumentSet tDocs) {
		counters.put(TLinkable.Type.EVENT, new HashMap<String, Map<String, Map<TimeMLRelType, Integer>>>());
		counters.put(TLinkable.Type.TIME, new HashMap<String, Map<String, Map<TimeMLRelType, Integer>>>());
		
		for (TempDocument doc : tDocs.getDocuments()){
			counters.get(Type.EVENT).put(doc.getName(), new HashMap<String, Map<TimeMLRelType, Integer>>());
			counters.get(Type.TIME).put(doc.getName(), new HashMap<String, Map<TimeMLRelType, Integer>>());
			for (Event e : doc.getEvents()){
				counters.get(Type.EVENT).get(doc.getName()).put(e.getId(), new HashMap<TimeMLRelType, Integer>());
			}
			for (Time t : doc.getTimes()){
				counters.get(Type.TIME).get(doc.getName()).put(t.getId(), new HashMap<TimeMLRelType, Integer>());
			}				
		}
	}
	


	private static void printCounters(Map<Type, Map<String, Map<String, Map<TimeMLRelType, Integer>>>> counters){
		// from # of non-'vague' links -> # of events with that number of links
		Map<Integer, Integer> numLinksToNumEvents = new HashMap<Integer, Integer>();
		Map<Integer, Integer> numLinksToNumTimes = new HashMap<Integer, Integer>();
		Map<String, Map<String, Map<TimeMLRelType, Integer>>> events = counters.get(Type.EVENT);
		Map<String, Map<String, Map<TimeMLRelType, Integer>>> times = counters.get(Type.TIME);

		countLinks(events, numLinksToNumEvents, "event");
		countLinks(times, numLinksToNumTimes, "time");
		
		System.out.println("NumLinksToNumEvents: " + numLinksToNumEvents);
		System.out.println("NumLinksToNumTimes: " + numLinksToNumTimes);
	}
	
	private static void countLinks(
			Map<String, Map<String, Map<TimeMLRelType, Integer>>> countOfLinks,
			Map<Integer, Integer> numLinksToNumLinkable, String typeOfLinkable) {
		
		// for document:
		for (String docId : countOfLinks.keySet()){
	
			// for eventID or timeID:
			for (String linkableId : countOfLinks.get(docId).keySet()){	
				// will be the key in numLinksToNumLinkable
				int thisIDCounter = 0;
				
				Map<TimeMLRelType, Integer> typeToInt = countOfLinks.get(docId).get(linkableId);
				// for relation with that ID:
				for (TimeMLRelType t : typeToInt.keySet()){
					if (t == TimeMLRelType.IS_INCLUDED)
						thisIDCounter += typeToInt.get(t);
				}
				incrementNumLinksToNumLinkable(thisIDCounter, numLinksToNumLinkable);
				
				if ((thisIDCounter == 4) && typeOfLinkable.equals("time")){
					System.out.println("TimeID: " + linkableId);
					System.out.println("DocID: " + docId);
					System.out.println("Number of links: " + typeToInt);
					System.out.println();
				}
				
				
			}
		}
		
	}

	private static void incrementNumLinksToNumLinkable(int thisIDCounter,
			Map<Integer, Integer> numLinksToNumLinkable) {
		if (!numLinksToNumLinkable.containsKey(thisIDCounter))
			numLinksToNumLinkable.put(thisIDCounter, 1);
		else
			numLinksToNumLinkable.put(thisIDCounter, numLinksToNumLinkable.get(thisIDCounter) + 1);
	}
	
	public static Set<TimeMLRelType> findRelations(TempDocumentSet tDocs){
		Set<TimeMLRelType> types = new HashSet<TimeMLRelType>();
		for (TempDocument doc : tDocs.getDocuments()){
			for (TLink tl : doc.getTLinks()){
				types.add(tl.getTimeMLRelType());
			}
		}
		return types;
	}

	// Finds the number of relations used in the set of documents.
	private static int findNumRelations(TempDocumentSet tDocs){
		return findRelations(tDocs).size();
	}
	
	// to add one to the counter for that ID
	// event vs time -> eventid or timeid -> reltype -> int
	private static void incrementCounterMap(Map<Type, Map<String, Map<String, Map<TimeMLRelType, Integer>>>> counters, 
			TLinkable tl, TimeMLRelType relType, String docId){
		Map<String, Map<String, Map<TimeMLRelType, Integer>>> docIdToLinkableId = counters.get(tl.getTLinkableType());
		
		Map<String, Map<TimeMLRelType, Integer>> idToRelType = docIdToLinkableId.get(docId);
		
		Map<TimeMLRelType, Integer> typeToInt = idToRelType.get(tl.getId());
		
		if (!typeToInt.containsKey(relType))
			typeToInt.put(relType, 1);
		else
			typeToInt.put(relType, typeToInt.get(relType) + 1);
			
	}

}
