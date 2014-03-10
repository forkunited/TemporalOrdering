package temp.scratch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.nlp.international.Languages.Language;

import ark.util.FileUtil;
import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.nlp.TokenSpan;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.Time;

public class ConstructTempDocumentsTempEval2 {
	public static void main(String[] args) {
		String inputPath = args[0];
		String outputPath = args[1];
		Language language = Language.valueOf(args[2]);
		
		TempDocumentSet documentSet = constructFromDataDirectory(inputPath, language);
		if (documentSet == null)
			return;
		documentSet.saveToJSONDirectory(outputPath);
	}
	
	public static TempDocumentSet constructFromDataDirectory(String directoryPath, Language language) {
		Map<String, String[][]> baseSegmentation = readTSVFile(new File(directoryPath, "base-segmentation.tab").getAbsolutePath());
		Map<String, String[][]> dct = readTSVFile(new File(directoryPath, "dct.tab").getAbsolutePath());
		Map<String, String[][]> timexAttributes = readTSVFile(new File(directoryPath, "timex-attributes.tab").getAbsolutePath());
		Map<String, String[][]> timexExtents = readTSVFile(new File(directoryPath, "timex-extents.tab").getAbsolutePath());
		Map<String, String[][]> eventAttributes = readTSVFile(new File(directoryPath, "event-attributes.tab").getAbsolutePath());
		Map<String, String[][]> eventExtents = readTSVFile(new File(directoryPath, "event-extents.tab").getAbsolutePath());
		Map<String, String[][]> tlinksDctEvent = readTSVFile(new File(directoryPath, "tlinks-dct-events.tab").getAbsolutePath());
		Map<String, String[][]> tlinksEventTimex = readTSVFile(new File(directoryPath, "tlinks-event-timex.tab").getAbsolutePath());
		
		for (String file : baseSegmentation.keySet()) {
			String[][] tokens = tokensFromLines(baseSegmentation.get(file));
			Time creationTime = creationTimeFromLines(dct.get(file));
			//Time[][] times = timesFromLines(document, timexExtents.get(file), timexAttributes.get(file));
			//Event[][] events = eventsFromLines(document, eventExtents.get(file), eventAttributes.get(file));
			
			// Add times and events to document
			
			//TLink[] tlinks = tlinksFromLines(document, tlinksDctEvent.get(file), tlinksEventTimex.get(file));
		
			// Add tokens, creation time, language, name to document
			// Run annotator on document to produce dependencies and posTags
			// Add times, events, tlinks
		}
		
		/* FIXME */
		return null;
	}
	
	private static String[][] tokensFromLines(String[][] lines) {
		Map<Integer, Integer> sentenceIdsToLengths = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < lines.length; i++) {
			int sentenceId = Integer.valueOf(lines[i][1]);
			int tokenId = Integer.valueOf(lines[i][2]);
			if (!sentenceIdsToLengths.containsKey(sentenceId))
				sentenceIdsToLengths.put(sentenceId, tokenId);
			else 
				sentenceIdsToLengths.put(sentenceId, Math.max(tokenId, sentenceIdsToLengths.get(sentenceId)));
		}
		
		String[][] tokens = new String[sentenceIdsToLengths.size()][];
		
		for (int i = 0; i < lines.length; i++) {
			int sentenceIndex = Integer.valueOf(lines[i][1]) - 1;
			int tokenIndex = Integer.valueOf(lines[i][2]) - 1;
			
			if (tokens[sentenceIndex] == null)
				tokens[sentenceIndex] = new String[sentenceIdsToLengths.get(sentenceIndex + 1)];
			
			tokens[sentenceIndex][tokenIndex] = lines[i][3];
			
		}
		
		return tokens;
	}
	
	private static Time creationTimeFromLines(String[][] lines) {
		return new Time("t0", lines[0][1], Time.TimeMLType.DATE, Time.TimeMLDocumentFunction.CREATION_TIME);
	}
	
	private static Time[][] timesFromLines(TempDocument document, String[][] timexExtentLines, String[][] timexAttributeLines) {
		List<List<String>> sentenceTimeIds = new ArrayList<List<String>>(document.getSentenceCount());
		for (int i = 0; i < document.getSentenceCount(); i++)
			sentenceTimeIds.add(new ArrayList<String>());
		
		Map<String, Integer> timeToMinTokenIndex = new HashMap<String, Integer>(); 
		Map<String, Integer> timeToMaxTokenIndex = new HashMap<String, Integer>(); 
		for (int i = 0; i < timexExtentLines.length; i++) {
			int sentenceIndex = Integer.valueOf(timexExtentLines[i][1]) - 1;
			int tokenIndex = Integer.valueOf(timexExtentLines[i][2]) - 1;
			String timeId = timexExtentLines[i][4];
		
			sentenceTimeIds.get(sentenceIndex).add(timeId);
			
			if (!timeToMinTokenIndex.containsKey(timeId)) {
				timeToMinTokenIndex.put(timeId, tokenIndex);
				timeToMaxTokenIndex.put(timeId, tokenIndex);
			} else {
				timeToMinTokenIndex.put(timeId, Math.min(tokenIndex, timeToMinTokenIndex.get(timeId)));
				timeToMaxTokenIndex.put(timeId, Math.max(tokenIndex, timeToMaxTokenIndex.get(timeId)));
			}
		}

		
		Map<String, String> timeToValue = new HashMap<String, String>();
		Map<String, Time.TimeMLType> timeToType = new HashMap<String, Time.TimeMLType>();
		for (int i = 0; i < timexAttributeLines.length; i++) {
			String timeId = timexAttributeLines[i][4];
			String lineValue = timexAttributeLines[i][7];
			if (timexAttributeLines[i][6].equals("val")) {
				timeToValue.put(timeId, lineValue);
			} else if (timexAttributeLines[i][6].equals("type")) {
				timeToType.put(timeId, Time.TimeMLType.valueOf(lineValue));
			}
		}
		
		Time[][] times = new Time[document.getSentenceCount()][];
		for (int i = 0; i < times.length; i++) {
			times[i] = new Time[sentenceTimeIds.get(i).size()];
			for (int j = 0; j < times[i].length; j++) {
				String timeId = sentenceTimeIds.get(i).get(j);
				String value = timeToValue.get(timeId);
				Time.TimeMLType timeMLType = timeToType.get(timeId);
				TokenSpan tokenSpan = new TokenSpan(document, i, timeToMinTokenIndex.get(timeId).intValue(), timeToMaxTokenIndex.get(timeId) + 1);
				times[i][j] = new Time(timeId, value, tokenSpan, timeMLType);
			}
		}	
		
		return times;
	}
	
	private static Event[][] eventsFromLines(TempDocument document, String[][] eventExtentLines, String[][] eventAttributeLines) {
		List<List<String>> sentenceEventIds = new ArrayList<List<String>>(document.getSentenceCount());
		for (int i = 0; i < document.getSentenceCount(); i++)
			sentenceEventIds.add(new ArrayList<String>());
		
		Map<String, Integer> eventToMinTokenIndex = new HashMap<String, Integer>(); 
		Map<String, Integer> eventToMaxTokenIndex = new HashMap<String, Integer>(); 
		for (int i = 0; i < eventExtentLines.length; i++) {
			int sentenceIndex = Integer.valueOf(eventExtentLines[i][1]) - 1;
			int tokenIndex = Integer.valueOf(eventExtentLines[i][2]) - 1;
			String eventId = eventExtentLines[i][4];
		
			sentenceEventIds.get(sentenceIndex).add(eventId);
			
			if (!eventToMinTokenIndex.containsKey(eventId)) {
				eventToMinTokenIndex.put(eventId, tokenIndex);
				eventToMaxTokenIndex.put(eventId, tokenIndex);
			} else {
				eventToMinTokenIndex.put(eventId, Math.min(tokenIndex, eventToMinTokenIndex.get(eventId)));
				eventToMaxTokenIndex.put(eventId, Math.max(tokenIndex, eventToMaxTokenIndex.get(eventId)));
			}
		}

		Map<String, Event.TimeMLTense> eventToTense = new HashMap<String, Event.TimeMLTense>();
		Map<String, Event.TimeMLAspect> eventToAspect = new HashMap<String, Event.TimeMLAspect>();
		Map<String, Event.TimeMLClass> eventToClass = new HashMap<String, Event.TimeMLClass>();
		Map<String, Event.TimeMLPolarity> eventToPolarity = new HashMap<String, Event.TimeMLPolarity>();
		
		for (int i = 0; i < eventAttributeLines.length; i++) {
			String eventId = eventAttributeLines[i][4];
			String lineValue = eventAttributeLines[i][7];
			if (eventAttributeLines[i][6].equals("tense")) {
				eventToTense.put(eventId, Event.TimeMLTense.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("aspect")) {
				eventToAspect.put(eventId, Event.TimeMLAspect.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("class")) {
				eventToClass.put(eventId, Event.TimeMLClass.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("polarity")) {
				eventToPolarity.put(eventId, Event.TimeMLPolarity.valueOf(lineValue));
			}
		}
		
		Event[][] events = new Event[document.getSentenceCount()][];
		for (int i = 0; i < events.length; i++) {
			events[i] = new Event[sentenceEventIds.get(i).size()];
			for (int j = 0; j < events[i].length; j++) {
				String eventId = sentenceEventIds.get(i).get(j);
				Event.TimeMLTense timeMLTense = eventToTense.get(eventId);
				Event.TimeMLAspect timeMLAspect = eventToAspect.get(eventId);
				Event.TimeMLClass timeMLClass = eventToClass.get(eventId);
				Event.TimeMLPolarity timeMLPolarity = eventToPolarity.get(eventId);
				
				TokenSpan tokenSpan = new TokenSpan(document, i, eventToMinTokenIndex.get(eventId).intValue(), eventToMaxTokenIndex.get(eventId) + 1);
				
				events[i][j] = new Event(Integer.valueOf(eventId.substring(1)), tokenSpan, timeMLTense, timeMLAspect, timeMLClass, timeMLPolarity);
			}
		}	
		
		return events;
	}
	
	private static TLink[] tlinksFromLines(TempDocument document, String[][] tlinksDctEvent, String[][] tlinksEventTimex) {
		TLink[] tlinks = new TLink[tlinksDctEvent.length + tlinksEventTimex.length];
		
		for (int i = 0; i < tlinksDctEvent.length; i++) {
			/* FIXME */
		}
		
		for (int i = 0; i < tlinksEventTimex.length; i++) {
			String eventId = tlinksEventTimex[i][1];
			String timeId = tlinksEventTimex[i][2];
			TLink.TimeMLRelType type = TLink.TimeMLRelType.valueOf(tlinksEventTimex[i][3].toUpperCase());
			
			Event event = document.getEvent(eventId);
			Time time = document.getTime(timeId);
			/* FIXME */
		}
		
		return tlinks;
	}
	
	private static Map<String, String[][]> readTSVFile(String path) {
		try {
			BufferedReader r = FileUtil.getFileReader(path);
			String line = null;
			Map<String, List<String[]>> namesToLines = new HashMap<String, List<String[]>>();
			while ((line = r.readLine()) != null) {
				String[] lineParts = line.split("\t");
				String name = lineParts[0];
				if (!namesToLines.containsKey(name))
					namesToLines.put(name, new ArrayList<String[]>());
				namesToLines.get(name).add(lineParts);
			}
			
			Map<String, String[][]> groupedLines = new HashMap<String, String[][]>();
			for (Entry<String, List<String[]>> entry : namesToLines.entrySet()) {
				String[][] lines = new String[entry.getValue().size()][];
				lines = entry.getValue().toArray(lines);
				groupedLines.put(entry.getKey(),  lines);
			}
			
			return groupedLines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
