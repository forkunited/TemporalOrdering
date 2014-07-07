package temp.scratch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ark.model.annotator.nlp.NLPAnnotator;
import ark.data.annotation.Language;
import ark.data.annotation.nlp.TokenSpan;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.Time;

import temp.model.annotator.nlp.NLPAnnotatorMultiLanguage;
import temp.util.TempProperties;

/**
 * ConstructTempDocumentsTempEval2 takes the following arguments:
 * 
 * [inputPath] - Path to TempEval2 source data set directory
 * [outputPath] - Path to output directory for serialized TempDocumentSet
 * [language] - Language of the input TempEval2 data
 * 
 * And constructs a temp.data.annotation.TempDoccumentSet from 
 * the TempEval2 [language] data at the [inputPath] directory, 
 * serializing it as JSON files to the [outputPath] directory.
 * 
 * The output TempDocumentSet represents TempEval2 documents extended
 * with various NLP annotations (parses, PoSTags, tokenizations, etc).
 * The NLP annotations are generated using 
 * temp.model.annotator.nlp.NLPAnnotatorMultiLanguage, and this
 * class uses the Stanford library for the English data and the 
 * FreeLing library for the Spanish data.  NLPAnnotatorMultiLanguage
 * uses NLPAnnotatorFreeLing as an interface for the FreeLing library,
 * and the implementation of this interface currently only works
 * on Windows machines.  So ConstructTempDocumentsTempEval2 will
 * currently fail on the Spanish TempEval2 unless you're set up 
 * with FreeLing on a Windows machine.
 * 
 * @author Bill McDowell
 *
 */
public class ConstructTempDocumentsTempEval2 {
	public static void main(String[] args) {
		String inputPath = args[0];
		String outputPath = args[1];
		Language language = Language.valueOf(args[2]);
		
		constructFromDataDirectory(inputPath, outputPath, language);
	}
	
	public static boolean constructFromDataDirectory(String directoryPath, String outputPath, Language language) {
		Map<String, String[][]> baseSegmentation = readTSVFile(new File(directoryPath, "base-segmentation.tab").getAbsolutePath());
		Map<String, String[][]> dct = readTSVFile(new File(directoryPath, "dct.tab").getAbsolutePath());
		Map<String, String[][]> timexAttributes = readTSVFile(new File(directoryPath, "timex-attributes.tab").getAbsolutePath());
		Map<String, String[][]> timexExtents = readTSVFile(new File(directoryPath, "timex-extents.tab").getAbsolutePath());
		Map<String, String[][]> eventAttributes = readTSVFile(new File(directoryPath, "event-attributes.tab").getAbsolutePath());
		Map<String, String[][]> eventExtents = readTSVFile(new File(directoryPath, "event-extents.tab").getAbsolutePath());
		Map<String, String[][]> tlinksDctEvent = readTSVFile(new File(directoryPath, "tlinks-dct-events.tab").getAbsolutePath());
		Map<String, String[][]> tlinksEventTimex = readTSVFile(new File(directoryPath, "tlinks-event-timex.tab").getAbsolutePath());
		
		NLPAnnotatorMultiLanguage nlpAnnotator = new NLPAnnotatorMultiLanguage(new TempProperties(), language);
		
		for (String file : baseSegmentation.keySet()) {
			if ((new File(outputPath, file + ".json")).exists()) {
				System.out.println("Output for " + file + " already exists.  Skipping...");
				continue;
			}
			
			TempDocumentSet documentSet = new TempDocumentSet();
			
			System.out.print("Loading document " + file + "... ");
			String[][] fileTokens = tokensFromLines(baseSegmentation.get(file));
			String[][] fileDct = dct.containsKey(file) ? dct.get(file) : new String[0][0];
			String[][] fileTimexAttributes = timexAttributes.containsKey(file) ? timexAttributes.get(file) : new String[0][0];
			String[][] fileTimexExtents = timexExtents.containsKey(file) ? timexExtents.get(file) : new String[0][0];
			String[][] fileEventAttributes = eventAttributes.containsKey(file) ? eventAttributes.get(file) : new String[0][0];
			String[][] fileEventExtents = eventExtents.containsKey(file) ? eventExtents.get(file) : new String[0][0];
			String[][] fileTlinksDctEvent = tlinksDctEvent.containsKey(file) ? tlinksDctEvent.get(file) : new String[0][0];
			String[][] fileTlinksEventTimex = tlinksEventTimex.containsKey(file) ? tlinksEventTimex.get(file) : new String[0][0];

			String[] fileSentences = sentencesFromTokens(fileTokens);
			int[][] fileToAnnotationTokenMap = buildFileToAnnotationTokenMap(fileTokens, fileSentences, nlpAnnotator);
			
			Time creationTime = creationTimeFromLines(fileDct);
			TempDocument document = new TempDocument(file, fileSentences, language, creationTime, nlpAnnotator);
			
			Time[][] times = timesFromLines(document, fileTimexExtents, fileTimexAttributes, fileToAnnotationTokenMap);
			Event[][] events = eventsFromLines(document, fileEventExtents, fileEventAttributes, fileToAnnotationTokenMap);
			
			document.setTimes(times);
			document.setEvents(events);
			
			TLink[] tlinks = tlinksFromLines(document, fileTlinksDctEvent, fileTlinksEventTimex);
			document.setTLinks(tlinks);
			documentSet.addDocument(document);
			System.out.println(" Done.");
			
			documentSet.saveToJSONDirectory(outputPath);
		}
		
		return true;
	}
	
	private static String[][] tokensFromLines(String[][] lines) {
		Map<Integer, Integer> sentenceIdsToLengths = new HashMap<Integer, Integer>();
		
		int maxSentenceIndex = 0;
		for (int i = 0; i < lines.length; i++) {
			int sentenceId = Integer.valueOf(lines[i][1]);
			int tokenId = Integer.valueOf(lines[i][2]);
			
			maxSentenceIndex = Math.max(sentenceId - 1, maxSentenceIndex);
			
			if (!sentenceIdsToLengths.containsKey(sentenceId))
				sentenceIdsToLengths.put(sentenceId, tokenId);
			else 
				sentenceIdsToLengths.put(sentenceId, Math.max(tokenId, sentenceIdsToLengths.get(sentenceId)));
		}
		
		String[][] tokens = new String[maxSentenceIndex + 1][];
		for (int i = 0; i < tokens.length; i++) {
			int sentenceId = i + 1;
			if (sentenceIdsToLengths.containsKey(sentenceId))
				tokens[i] = new String[sentenceIdsToLengths.get(i + 1)];
			else
				tokens[i] = new String[0];
			
			for (int j = 0; j < tokens[i].length; j++)
				tokens[i][j] = "";
		}
		
		for (int i = 0; i < lines.length; i++) {
			int sentenceIndex = Integer.valueOf(lines[i][1]) - 1;
			int tokenIndex = Integer.valueOf(lines[i][2]) - 1;
			tokens[sentenceIndex][tokenIndex] = lines[i][3];
		}
		
		// HACK: Add missing punctuation because Freeling seems to not work if it's missing
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length == 0)
				continue;
			String lastToken = tokens[i][tokens[i].length - 1];
			if (!lastToken.equals(".") && !lastToken.equals("?") && !lastToken.equals("!")) {
				tokens[i] = Arrays.copyOf(tokens[i], tokens[i].length + 1);
				tokens[i][tokens[i].length - 1] = ".";
			}
		}
		
		return tokens;
	}
	
	private static String[] sentencesFromTokens(String[][] fileTokens) {
		String[] sentences = new String[fileTokens.length];
		for (int i = 0; i < fileTokens.length; i++) {
			StringBuilder sentenceText = new StringBuilder();
			for (int j = 0; j < fileTokens[i].length; j++) {
				sentenceText = sentenceText.append(fileTokens[i][j]).append(" ");
			}
			sentences[i] = sentenceText.toString().trim();
		}
		return sentences;
	}
	
	private static int[][] buildFileToAnnotationTokenMap(String[][] fileTokens, String[] fileSentences, NLPAnnotator nlpAnnotator) {
		int[][] tokenMap = new int[fileTokens.length][];
		
		for (int i = 0; i < tokenMap.length; i++) {
			nlpAnnotator.setText(fileSentences[i]);
			String[][] annotationTokens = nlpAnnotator.makeTokens();
			if (annotationTokens.length > 1)
				throw new IllegalArgumentException("Annotator split TempEval2 sentence... :(");
			
			tokenMap[i] = new int[fileTokens[i].length];
			int annotationTokenIndex = 0;
			StringBuilder fileTokenSpan = new StringBuilder();
			for (int j = 0; j < fileTokens[i].length; j++) {
				fileTokenSpan = fileTokenSpan.append(fileTokens[i][j]);
				tokenMap[i][j] = annotationTokenIndex;
				if (fileTokenSpan.toString().equals(annotationTokens[0][annotationTokenIndex])) {
					annotationTokenIndex++;
					fileTokenSpan = new StringBuilder();
				} else {
					fileTokenSpan.append("_");
				}
			}
		}
		
		return tokenMap;
	}
	
	private static Time creationTimeFromLines(String[][] lines) {
		return new Time("t0", lines[0][1], Time.TimeMLType.DATE, Time.TimeMLDocumentFunction.CREATION_TIME);
	}
	
	private static Time[][] timesFromLines(TempDocument document, String[][] timexExtentLines, String[][] timexAttributeLines, int[][] fileToAnnotationTokenMap) {
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
			String lineValue = (timexAttributeLines[i].length < 8) ? "" : timexAttributeLines[i][7];
			if (timexAttributeLines[i][6].equals("val")) {
				timeToValue.put(timeId, lineValue.equals("") ? "XXXX-XX-XX" : lineValue);
			} else if (timexAttributeLines[i][6].equals("type")) {
				timeToType.put(timeId, lineValue.equals("") ? Time.TimeMLType.DATE : Time.TimeMLType.valueOf(lineValue));
			}
		}
		
		Time[][] times = new Time[document.getSentenceCount()][];
		for (int i = 0; i < times.length; i++) {
			times[i] = new Time[sentenceTimeIds.get(i).size()];
			for (int j = 0; j < times[i].length; j++) {
				String timeId = sentenceTimeIds.get(i).get(j);
				String value = timeToValue.get(timeId);
				Time.TimeMLType timeMLType = timeToType.get(timeId);
				
				int minAnnotationTokenIndex = fileToAnnotationTokenMap[i][timeToMinTokenIndex.get(timeId).intValue()];
				int maxAnnotationTokenIndex = fileToAnnotationTokenMap[i][timeToMaxTokenIndex.get(timeId).intValue()];
				TokenSpan tokenSpan = new TokenSpan(document, i, minAnnotationTokenIndex, maxAnnotationTokenIndex + 1);
				
				times[i][j] = new Time(timeId, value, tokenSpan, timeMLType);
			}
		}	
		
		return times;
	}
	
	private static Event[][] eventsFromLines(TempDocument document, String[][] eventExtentLines, String[][] eventAttributeLines, int[][] fileToAnnotationTokenMap) {
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
		Map<String, Event.TimeMLMood> eventToMood = new HashMap<String, Event.TimeMLMood>();
		Map<String, Event.TimeMLVerbForm> eventToVerbForm = new HashMap<String, Event.TimeMLVerbForm>();
	
		for (int i = 0; i < eventAttributeLines.length; i++) {
			String eventId = eventAttributeLines[i][4];
			String lineValue = (eventAttributeLines[i].length < 8) ? "NONE" : eventAttributeLines[i][7];
			if (eventAttributeLines[i][6].equals("tense")) {
				eventToTense.put(eventId, Event.TimeMLTense.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("aspect")) {
				eventToAspect.put(eventId, Event.TimeMLAspect.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("class")) {
				eventToClass.put(eventId, Event.TimeMLClass.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("polarity")) {
				eventToPolarity.put(eventId, Event.TimeMLPolarity.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("mood")) {
				eventToMood.put(eventId, Event.TimeMLMood.valueOf(lineValue));
			} else if (eventAttributeLines[i][6].equals("vform")) {
				eventToVerbForm.put(eventId, Event.TimeMLVerbForm.valueOf(lineValue));
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
				Event.TimeMLMood timeMLMood = eventToMood.get(eventId);
				Event.TimeMLVerbForm timeMLVerbForm = eventToVerbForm.get(eventId);
				
				int minAnnotationTokenIndex = fileToAnnotationTokenMap[i][eventToMinTokenIndex.get(eventId).intValue()];
				int maxAnnotationTokenIndex = fileToAnnotationTokenMap[i][eventToMaxTokenIndex.get(eventId).intValue()];
				TokenSpan tokenSpan = new TokenSpan(document, i, minAnnotationTokenIndex, maxAnnotationTokenIndex + 1);
				
				events[i][j] = new Event(Integer.valueOf(eventId.substring(1)), tokenSpan, timeMLTense, timeMLAspect, timeMLClass, timeMLPolarity, timeMLMood, timeMLVerbForm);
			}
		}	
		
		return events;
	}
	
	private static TLink[] tlinksFromLines(TempDocument document, String[][] tlinksDctEvent, String[][] tlinksEventTimex) {
		TLink[] tlinks = new TLink[tlinksDctEvent.length + tlinksEventTimex.length];
		
		int id = 0;
		
		for (int i = 0; i < tlinksDctEvent.length; i++) {
			String eventId = "ei" + tlinksDctEvent[i][1].substring(1);
			TLink.TimeMLRelType type = TLink.TimeMLRelType.valueOf(tlinksDctEvent[i][3].toUpperCase().replaceAll("\\-",  "_"));
			Event event = document.getEvent(eventId);
			Time creationTime = document.getCreationTime();
			
			tlinks[id] = new TLink("l" + id, event, creationTime, type);
			
			id++;
		}
		
		for (int i = 0; i < tlinksEventTimex.length; i++) {
			String eventId = "ei" + tlinksEventTimex[i][1].substring(1);
			String timeId = tlinksEventTimex[i][2];
			TLink.TimeMLRelType type = TLink.TimeMLRelType.valueOf(tlinksEventTimex[i][3].toUpperCase().replaceAll("\\-", "_"));
			
			Event event = document.getEvent(eventId);
			Time time = document.getTime(timeId);
			
			tlinks[id] = new TLink("l" + id, event, time, type);
			
			id++;
		}
		
		return tlinks;
	}
	
	private static Map<String, String[][]> readTSVFile(String path) {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
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
			
			r.close();
			
			return groupedLines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
