package temp.data.annotation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Text;

import ark.model.annotator.nlp.NLPAnnotator;
import ark.util.Pair;
import ark.data.annotation.Language;
import ark.data.annotation.DocumentInMemory;
import ark.data.annotation.nlp.TokenSpan;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.Signal;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.Time;
import temp.data.annotation.timeml.Time.TimeMLDocumentFunction;

public class TempDocument extends DocumentInMemory {
	private Time creationTime;
	
	private Event[][] events;
	private Time[][] times;
	private Signal[][] signals;
	private TLink[] tlinks;
	
	private Map<String, Event> eventMap;
	private Map<String, Time> timeMap;
	private Map<String, Signal> signalMap;	
		
	public TempDocument(JSONObject json) {
		fromJSON(json);
	}
	
	public TempDocument(Element element) {
		fromXML(element);
	}
	
	public TempDocument(String path, StorageType storageType) {
		super(path, storageType);
	}
	
	public TempDocument(String name, String text, Language language, Time creationTime, NLPAnnotator annotator) {
		super(name, text, language, annotator);
		
		initializeTimeML();

		this.creationTime = creationTime;
		this.timeMap.put(this.creationTime.getId(), this.creationTime);
	}
	
	public TempDocument(String name, String text, Language language, Date creationTime, NLPAnnotator annotator) {		
		this(name, 
		   text, 
		   language, 
		   Time.fromDate(creationTime, 0, TimeMLDocumentFunction.CREATION_TIME, null, null), 
		   annotator);
	}
	
	public TempDocument(String name, String[] sentences, Language language, Time creationTime, NLPAnnotator annotator) {
		super(name, sentences, language, annotator);
		
		initializeTimeML();
		
		this.creationTime = creationTime;
		this.timeMap.put(this.creationTime.getId(), this.creationTime);
	}
	
	public TempDocument(String name, String[] sentences, Language language, Date creationTime, NLPAnnotator annotator) {		
		this(name, 
			   sentences, 
			   language, 
			   Time.fromDate(creationTime, 0, TimeMLDocumentFunction.CREATION_TIME, null, null), 
			   annotator);
	}
	
	private void initializeTimeML() {
		this.events = new Event[this.tokens.length][0];
		this.times = new Time[this.tokens.length][0];
		this.signals = new Signal[this.tokens.length][0];
		this.eventMap = new HashMap<String, Event>();
		this.timeMap = new HashMap<String, Time>();
		this.signalMap = new HashMap<String, Signal>();	
		this.tlinks = new TLink[0];
	}
	
	public Time getCreationTime() {
		return this.creationTime;
	}
	
	public List<Time> getTimes() {
		List<Time> times = new ArrayList<Time>();
		for (int i = 0; i < this.times.length; i++) {
			for (int j = 0; j < this.times[i].length; j++) {
				times.add(this.times[i][j]);
			}
		}
		return times;
	}
	
	public List<Time> getTimes(int sentenceIndex) {
		List<Time> times = new ArrayList<Time>();
		for (int j = 0; j < this.times[sentenceIndex].length; j++) {
			times.add(this.times[sentenceIndex][j]);
		}
		return times;
	}
	
	public Time getTime(String id) {
		return this.timeMap.get(id);
	}
	
	public List<Event> getEvents() {
		List<Event> events = new ArrayList<Event>();
		for (int i = 0; i < this.events.length; i++) {
			for (int j = 0; j < this.events[i].length; j++) {
				events.add(this.events[i][j]);
			}
		}
		return events;
	}
	
	public List<Event> getEvents(int sentenceIndex) {
		List<Event> events = new ArrayList<Event>();
		for (int j = 0; j < this.events[sentenceIndex].length; j++) {
			events.add(this.events[sentenceIndex][j]);
		}
		return events;
	}
	
	public List<Event> getEventsForToken(int sentenceIndex, int tokenIndex) {
		List<Event> events = new ArrayList<Event>();
		for (int j = 0; j < this.events[sentenceIndex].length; j++) {
			if (this.events[sentenceIndex][j].getTokenSpan().containsToken(sentenceIndex, tokenIndex))
				events.add(this.events[sentenceIndex][j]);
		}
		return events;
	}
	
	public List<Time> getTimesForToken(int sentenceIndex, int tokenIndex) {
		List<Time> times = new ArrayList<Time>();
		for (int j = 0; j < this.times[sentenceIndex].length; j++) {
			if (this.times[sentenceIndex][j].getTokenSpan().containsToken(sentenceIndex, tokenIndex))
				times.add(this.times[sentenceIndex][j]);
		}
		return times;
	}
	
	public Event getEvent(String id) {
		return this.eventMap.get(id);
	}
	
	public List<Signal> getSignals() {
		List<Signal> signals = new ArrayList<Signal>();
		for (int i = 0; i < this.signals.length; i++) {
			for (int j = 0; j < this.signals[i].length; j++) {
				signals.add(this.signals[i][j]);
			}
		}
		return signals;
	}
	
	public List<Signal> getSignals(int sentenceIndex) {
		List<Signal> signals = new ArrayList<Signal>();
		for (int j = 0; j < this.signals[sentenceIndex].length; j++) {
			signals.add(this.signals[sentenceIndex][j]);
		}
		return signals;
	}
	
	public Signal getSignal(String id) {
		return this.signalMap.get(id);
	}
	
	public List<TLink> getTLinks() {
		return Arrays.asList(this.tlinks);
	}
	
	public List<TLink> getTLinks(int sentenceIndex, boolean includeBetweenSentence) {
		List<TLink> tlinks = new ArrayList<TLink>();
		for (int i = 0; i < this.tlinks.length; i++) {
			if ((this.tlinks[i].getSource().getTokenSpan().getSentenceIndex() == sentenceIndex &&
			     this.tlinks[i].getTarget().getTokenSpan().getSentenceIndex() == sentenceIndex)
				||
				(includeBetweenSentence && 
					(this.tlinks[i].getSource().getTokenSpan().getSentenceIndex() == sentenceIndex ||
					 this.tlinks[i].getTarget().getTokenSpan().getSentenceIndex() == sentenceIndex))) {
				
				tlinks.add(this.tlinks[i]);
			}
			
		}
		return tlinks;
	}
	
	public boolean setEvents(Event[][] events) {
		this.events = new Event[events.length][];
		this.eventMap = new HashMap<String, Event>();
		for (int i = 0; i < events.length; i++) {
			this.events[i] = new Event[events[i].length];
			for (int j = 0; j < events[i].length; j++) {
				this.events[i][j] = events[i][j];
				if (this.events[i][j] != null)
					this.eventMap.put(events[i][j].getId(), events[i][j]);
			}
		}
		return true;
	}
	
	public boolean setTimes(Time[][] times) {
		this.times = new Time[times.length][];
		this.timeMap = new HashMap<String, Time>();
		
		if (this.creationTime != null)
			this.timeMap.put(this.creationTime.getId(), this.creationTime);
		
		for (int i = 0; i < times.length; i++) {
			this.times[i] = new Time[times[i].length];
			for (int j = 0; j < times[i].length; j++) {
				this.times[i][j] = times[i][j];
				if (this.times[i][j] != null)
					this.timeMap.put(times[i][j].getId(), times[i][j]);
			}
		}
		return true;
	}
	
	public boolean setSignals(Signal[][] signals) {
		this.signals = new Signal[signals.length][];
		this.signalMap = new HashMap<String, Signal>();
		for (int i = 0; i < signals.length; i++) {
			this.signals[i] = new Signal[signals[i].length];
			for (int j = 0; j < signals[i].length; j++) {
				this.signals[i][j] = signals[i][j];
				if (this.signals[i][j] != null)
					this.signalMap.put(signals[i][j].getId(), signals[i][j]);
			}
		}
		return true;
	}
	
	public boolean setTLinks(List<TLink> tlinks) {
		return setTLinks(tlinks.toArray(new TLink[0]));
	}
	
	public boolean setTLinks(TLink[] tlinks) {
		this.tlinks = tlinks.clone();
		return true;
	}
	
	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		
		if (this.creationTime != null)
			json.put("creationTime", this.creationTime.toJSON());
		
		JSONArray sentencesJson = json.getJSONArray("sentences");
		
		for (int i = 0; i < this.tokens.length; i++) {
			JSONArray eventsJson = new JSONArray();
			for (int j = 0; j < this.events[i].length; j++)
				if (this.events[i][j] != null)
					eventsJson.add(this.events[i][j].toJSON());
			sentencesJson.getJSONObject(i).put("events", eventsJson);
			
			JSONArray timesJson = new JSONArray();
			for (int j = 0; j < this.times[i].length; j++)
				if (this.times[i][j] != null)
					timesJson.add(this.times[i][j].toJSON());
			sentencesJson.getJSONObject(i).put("times", timesJson);
			
			JSONArray signalsJson = new JSONArray();
			for (int j = 0; j < this.signals[i].length; j++)
				if (this.signals[i][j] != null)
					signalsJson.add(this.signals[i][j].toJSON());
			sentencesJson.getJSONObject(i).put("signals", signalsJson);
		}
		
		JSONArray tlinksJson = new JSONArray();
		for (int i = 0; i < this.tlinks.length; i++)
			tlinksJson.add(this.tlinks[i].toJSON());
		json.put("tlinks", tlinksJson);
		
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public Element toXML() {
		Element element = super.toXML();
		
		if (this.creationTime != null)
			element.addContent(this.creationTime.toXML());
		
		List<Element> sentenceElements = (List<Element>)element.getChildren("entry");
		
		for (int i = 0; i < this.tokens.length; i++) {
			Element entryElement = sentenceElements.get(i);
			
			Element eventsElement = new Element("events");
			for (int j = 0; j < this.events[i].length; j++) 
				if (this.events[i][j] != null)
					eventsElement.addContent(this.events[i][j].toXML());
			entryElement.addContent(eventsElement);
			
			Element timexesElement = new Element("timexes");
			for (int j = 0; j < this.times[i].length; j++)
				if (this.times[i][j] != null)
					timexesElement.addContent(this.times[i][j].toXML());
			entryElement.addContent(timexesElement);
			
			Element signalsElement = new Element("signals");
			for (int j = 0; j < this.signals[i].length; j++)
				if (this.signals[i][j] != null)
					signalsElement.addContent(this.signals[i][j].toXML());
			entryElement.addContent(signalsElement);
			
			element.addContent(entryElement);
		}
		
		for (int i = 0; i < this.tlinks.length; i++) {
			element.addContent(this.tlinks[i].toXML());
		}
		
		return element;
	}
	
	public Element toTimeML() {
		Element element = new Element("TimeML");
		Element extraInfoElement = new Element("EXTRAINFO");
		StringBuilder extraInfoText = new StringBuilder();
		if (this.name != null) {
			Element docIDElement = new Element("DOCID");
			docIDElement.setText(this.name);
			extraInfoText.append(this.name);
			element.addContent(docIDElement);
		}
		
		if (this.creationTime != null) {
			Element dctElement = new Element("DCT");
			dctElement.addContent(this.creationTime.toTimeML());
			extraInfoText.append(" ").append(this.creationTime.getValue().toString());
			element.addContent(dctElement);
		}

		element.addContent(extraInfoElement);

		Element textElement = new Element("TEXT");
		Map<String, Pair<Element, TokenSpan>> sourceEvents = new HashMap<String, Pair<Element, TokenSpan>>();
		List<Element> eventInstances = new ArrayList<Element>();
		String[][] tokensToSourceEvents = new String[this.tokens.length][];
		String[][] tokensToTimes = new String[this.tokens.length][];
		String[][] tokensToSignals = new String[this.tokens.length][];
		
		for (int i = 0; i < this.tokens.length; i++) {
			tokensToSourceEvents[i] = new String[this.tokens[i].length];
			tokensToTimes[i] = new String[this.tokens[i].length];
			tokensToSignals[i] = new String[this.tokens[i].length];
		
			// Collect events for sentence
			for (int j = 0; j < this.events.length; j++) {
				eventInstances.add(this.events[i][j].toTimeML(sourceEvents));
				TokenSpan tokenSpan = this.events[i][j].getTokenSpan();
				for (int k = tokenSpan.getStartTokenIndex(); k < tokenSpan.getEndTokenIndex(); k++)
					tokensToSourceEvents[i][k] = this.events[i][j].getSourceId();
			}
			
			// Collect times for sentence
			for (int j = 0; j < this.times.length; j++) {
				TokenSpan tokenSpan = this.times[i][j].getTokenSpan();
				for (int k = tokenSpan.getStartTokenIndex(); k < tokenSpan.getEndTokenIndex(); k++)
					tokensToTimes[i][k] = this.times[i][j].getId();
			}
			
			// Collect signals for sentence
			for (int j = 0; j < this.signals.length; j++) {
				TokenSpan tokenSpan = this.signals[i][j].getTokenSpan();
				for (int k = tokenSpan.getStartTokenIndex(); k < tokenSpan.getEndTokenIndex(); k++)
					tokensToSignals[i][k] = this.signals[i][j].getId();
			}
			
			// Build text with events and times for sentence
			StringBuilder segmentText = new StringBuilder();
			for (int j = 0; j < this.tokens.length; j++) {
				if (tokensToSourceEvents[i][j] == null && tokensToTimes[i][j] == null) {
					segmentText = segmentText.append(this.tokens[i][j]).append(" ");
				} else if (tokensToSourceEvents[i][j] != null) {
					if (j != 0 && !tokensToSourceEvents[i][j].equals(tokensToSourceEvents[i][j-1]))
						continue;
					
					textElement.addContent(segmentText.toString());
					segmentText = new StringBuilder();
					textElement.addContent(sourceEvents.get(tokensToSourceEvents[i][j]).getFirst());
				} else if (tokensToTimes[i][j] != null) {
					if (j != 0 && !tokensToTimes[i][j].equals(tokensToTimes[i][j-1]))
						continue;			
					
					textElement.addContent(segmentText.toString());
					segmentText = new StringBuilder();
					textElement.addContent(this.timeMap.get(tokensToTimes[i][j]).toTimeML());
				} else if (tokensToSignals[i][j] != null) {
					if (j != 0 && !tokensToSignals[i][j].equals(tokensToSignals[i][j-1]))
						continue;			
					
					textElement.addContent(segmentText.toString());
					segmentText = new StringBuilder();
					textElement.addContent(this.signalMap.get(tokensToSignals[i][j]).toTimeML());
				}
			}
			
			textElement.addContent(segmentText.toString());
		}
		
		element.addContent(textElement);
		
		Element lastExtraInfoElement = new Element("LASTEXTRAINFO");
		element.addContent(lastExtraInfoElement);
		
		for (Element eventInstance : eventInstances)
			element.addContent(eventInstance);
		
		for (TLink tlink : this.tlinks)
			element.addContent(tlink.toTimeML());
			
		return element;
	}
	
	protected boolean fromJSON(JSONObject json) {
		if (!super.fromJSON(json))
			return false;
		
		JSONArray sentences = json.getJSONArray("sentences");

		initializeTimeML();
		
		if (json.has("creationTime")) {
			this.creationTime = Time.fromJSON(json.getJSONObject("creationTime"), this, -1, Time.ParseMode.ALL);
			if (this.creationTime != null)
				this.timeMap.put(this.creationTime.getId(), this.creationTime);
		}
		
		JSONArray[] timesJson = new JSONArray[sentences.size()];
		JSONArray[] eventsJson = new JSONArray[sentences.size()];
		Signal[][] signals = new Signal[sentences.size()][];
		
		for (int i = 0; i < sentences.size(); i++) {
			JSONObject sentenceJson = sentences.getJSONObject(i);
			JSONArray signalsJson = null;
			if (sentenceJson.has("signals"))
				signalsJson = sentenceJson.getJSONArray("signals");
			
			timesJson[i] = sentenceJson.getJSONArray("times");
			eventsJson[i] = sentenceJson.getJSONArray("events");
			
			if (signalsJson != null) {
				signals[i] = new Signal[signalsJson.size()];
				for (int j = 0; j < signalsJson.size(); j++)
					signals[i][j] = Signal.fromJSON(signalsJson.getJSONObject(j), this, i);
			} else {
				signals[i] = new Signal[0];
			}
		}

		this.setSignals(signals);

		Time[][] times = new Time[timesJson.length][];
		for (int i = 0; i < timesJson.length; i++) {
			times[i] = new Time[timesJson[i].size()];
			for (int j = 0; j < timesJson[i].size(); j++) {
				times[i][j] = Time.fromJSON(timesJson[i].getJSONObject(j), this, i, Time.ParseMode.NO_REFERENCED_TIMES);
				if (times[i][j] == null)
					return false;
			}
		}
		this.setTimes(times);
		
		for (int i = 0; i < times.length; i++) {
			for (int j = 0; j < times[i].length; j++) {
				times[i][j] = Time.fromJSON(timesJson[i].getJSONObject(j), this, i, Time.ParseMode.ALL);
				if (times[i][j] == null)
					return false;
			}
		}
		this.setTimes(times);
		
		Event[][] events = new Event[eventsJson.length][];
		for (int i = 0; i < events.length; i++) {
			events[i] = new Event[eventsJson[i].size()];
			for (int j = 0; j < eventsJson[i].size(); j++) {
				events[i][j] = Event.fromJSON(eventsJson[i].getJSONObject(j), this, i);
			}
		}
		this.setEvents(events);
		
		JSONArray tlinksJson = json.getJSONArray("tlinks");
		TLink[] tlinks = new TLink[tlinksJson.size()];
		for (int i = 0; i < tlinksJson.size(); i++)
			tlinks[i] = TLink.fromJSON(tlinksJson.getJSONObject(i), this);
		this.setTLinks(tlinks);
	
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean fromXML(Element element) {
		if (!super.fromXML(element))
			return false;
		
		initializeTimeML();
		
		List<Element> entryElements = (List<Element>)element.getChildren("entry");
		
		List<List<Element>> timexesXML = new ArrayList<List<Element>>();
		List<List<Element>> eventsXML = new ArrayList<List<Element>>();
		Signal[][] signals = new Signal[entryElements.size()][];
		
		for (Element entryElement : entryElements) {
			int sentenceIndex = Integer.parseInt(entryElement.getAttributeValue("sid"));
		
			Element timexesElement = entryElement.getChild("timexes");
			timexesXML.add((List<Element>)timexesElement.getChildren("timex"));
			
			Element eventsElement = entryElement.getChild("events");
			eventsXML.add((List<Element>)eventsElement.getChildren("event"));
			
			Element signalsElement = entryElement.getChild("signals");
			if (signalsElement != null) {
				List<Element> signalElements = signalsElement.getChildren("signal");
				signals[sentenceIndex] = new Signal[signalElements.size()];
				for (int j = 0; j < signalElements.size(); j++)
					signals[sentenceIndex][j] = Signal.fromXML(signalElements.get(j), this, sentenceIndex);
			} else {
				signals[sentenceIndex] = new Signal[0];
			}
		}
		
		setSignals(signals);
				
		List<Element> creationTimeElements = (List<Element>)element.getChildren("timex");
		if (creationTimeElements.size() > 0) {
			this.creationTime = Time.fromXML(creationTimeElements.get(0), this, -1, Time.ParseMode.ALL);
			if (this.creationTime != null)
				this.timeMap.put(this.creationTime.getId(), this.creationTime);
		}
		
		Time[][] times = new Time[timexesXML.size()][];
		for (int i = 0; i < timexesXML.size(); i++) {
			times[i] = new Time[timexesXML.get(i).size()];
			for (int j = 0; j < timexesXML.get(i).size(); j++) {
				times[i][j] = Time.fromXML(timexesXML.get(i).get(j), this, i, Time.ParseMode.NO_REFERENCED_TIMES);
				if (times[i][j] == null)
					return false;
			}
		}
		this.setTimes(times);
		
		for (int i = 0; i < times.length; i++) {
			for (int j = 0; j < times[i].length; j++) {
				times[i][j] = Time.fromXML(timexesXML.get(i).get(j), this, i, Time.ParseMode.ALL);
				if (times[i][j] == null)
					return false;
			}
		}
		this.setTimes(times);
			
		Event[][] events = new Event[eventsXML.size()][];
		for (int i = 0; i < eventsXML.size(); i++) {
			List<Event> sentenceEvents = new ArrayList<Event>();
			for (int j = 0; j < eventsXML.get(i).size(); j++) {
				sentenceEvents.addAll(Event.fromXML(eventsXML.get(i).get(j), this, i));
			}
			events[i] = new Event[sentenceEvents.size()];
			events[i] = sentenceEvents.toArray(events[i]);
		}
		this.setEvents(events);
		
		List<Element> tlinkElements = (List<Element>)element.getChildren("tlink");
		TLink[] tlinks = new TLink[tlinkElements.size()];
		for (int i = 0; i < tlinkElements.size(); i++) {
			tlinks[i] = TLink.fromXML(tlinkElements.get(i), this);
		}
		setTLinks(tlinks);
		
		return true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TempDocument fromTimeML(Element element, NLPAnnotator nlpAnnotator, Language language) {
		String name = element.getChild("DOCID").getText() + ".tml";
		Time creationTime = Time.fromTimeML(element.getChild("DCT").getChild("TIMEX3"), null, null, Time.ParseMode.ALL);
		
		Element textElement = element.getChild("TEXT"); 
		StringBuilder text = new StringBuilder();
		List textParts = textElement.getContent();
		for (int i = 0; i < textParts.size(); i++) {
			if (textParts.get(i).getClass() == Element.class) {
				text.append(((Element)textParts.get(i)).getText());
			} else { // Outside Timex
				text.append(((Text)textParts.get(i)).getText());
			}
		}
		
		TempDocument document = new TempDocument(name, text.toString(), language, creationTime, nlpAnnotator);

		int sentenceTokenIndex = 0;
		int docSentenceIndex = 0;
		Map<Integer, List<Pair<Element, TokenSpan>>> sentenceSignals = new HashMap<Integer, List<Pair<Element, TokenSpan>>>();
		Map<Integer, List<Pair<Element, TokenSpan>>> sentenceTimes = new HashMap<Integer, List<Pair<Element, TokenSpan>>>();
		Map<String, Pair<Element, TokenSpan>> sourceEvents = new HashMap<String, Pair<Element, TokenSpan>>();
		for (int i = 0; i < textParts.size(); i++) {
			String textPart = null;
			Element tmlElement = null;
			if (textParts.get(i).getClass() == Element.class) { // event, signal, or time
				tmlElement = (Element)textParts.get(i);
				textPart = tmlElement.getText();
			} else { // Outside event, signal, or time
				textPart = ((Text)textParts.get(i)).getText();
			}
			
			if (!nlpAnnotator.setText(textPart))
				return null;			
				
			String[][] tokens = nlpAnnotator.makeTokens();
			if (tokens.length > 1) {
				if (tmlElement != null) // Shouldn't have sentence splits inside event, signal, or time
					return null;
				docSentenceIndex += (tokens.length - 1);
				sentenceTokenIndex = 0;
			}
			
			if (tmlElement != null) {
				TokenSpan tokenSpan = new TokenSpan(document, docSentenceIndex, sentenceTokenIndex, sentenceTokenIndex + tokens[0].length);
				if (tmlElement.getName().equals("SIGNAL")) {
					if (!sentenceSignals.containsKey(docSentenceIndex))
						sentenceSignals.put(docSentenceIndex, new ArrayList<Pair<Element, TokenSpan>>());
					sentenceSignals.get(docSentenceIndex).add(new Pair<Element, TokenSpan>(tmlElement, tokenSpan));
				} else if (tmlElement.getName().equals("TIMEX3")) {
					if (!sentenceTimes.containsKey(docSentenceIndex))
						sentenceTimes.put(docSentenceIndex, new ArrayList<Pair<Element, TokenSpan>>());
					sentenceTimes.get(docSentenceIndex).add(new Pair<Element, TokenSpan>(tmlElement, tokenSpan));
				} else if (tmlElement.getName().equals("EVENT")) {
					sourceEvents.put(tmlElement.getAttributeValue("eid"), new Pair<Element, TokenSpan>(tmlElement, tokenSpan));
				}
			}
			
			if (tokens.length > 0)
				sentenceTokenIndex += tokens[tokens.length - 1].length;
		}
		
		Signal[][] signals = new Signal[document.tokens.length][];
		for (int i = 0; i < document.tokens.length; i++) {
			if (!sentenceSignals.containsKey(i)) {
				signals[i] = new Signal[0];
				continue;
			}
			
			signals[i] = new Signal[sentenceSignals.get(i).size()];
			for (int j = 0; j < signals[i].length; j++)
				signals[i][j] = Signal.fromTimeML(sentenceSignals.get(i).get(j).getFirst(), document, sentenceSignals.get(i).get(j).getSecond());
		}
		document.setSignals(signals);
		
		Time[][] times = new Time[document.tokens.length][];
		for (int i = 0; i < document.tokens.length; i++) {
			if (!sentenceTimes.containsKey(i)) {
				times[i] = new Time[0];
				continue;
			}
			
			times[i] = new Time[sentenceTimes.get(i).size()];
			for (int j = 0; j < times[i].length; j++) {
				times[i][j] = Time.fromTimeML(sentenceTimes.get(i).get(j).getFirst(), document, sentenceTimes.get(i).get(j).getSecond(), Time.ParseMode.NO_REFERENCED_TIMES);
				if (times[i][j] == null)
					return null;
			}
		}
		document.setTimes(times);
		
		for (int i = 0; i < times.length; i++) {
			for (int j = 0; j < times[i].length; j++) {
				times[i][j] = Time.fromTimeML(sentenceTimes.get(i).get(j).getFirst(), document, sentenceTimes.get(i).get(j).getSecond(), Time.ParseMode.ALL);
				if (times[i][j] == null)
					return null;
			}
		}
		document.setTimes(times);
		
		List<Element> instanceElements = element.getChildren("MAKEINSTANCE");
		Map<Integer, List<Event>> sentenceEvents = new HashMap<Integer, List<Event>>();
		for (Element instanceElement : instanceElements) {
			Event event = Event.fromTimeML(instanceElement, document, sourceEvents);
			if (!sentenceEvents.containsKey(event.getTokenSpan().getSentenceIndex()))
				sentenceEvents.put(event.getTokenSpan().getSentenceIndex(), new ArrayList<Event>());
			sentenceEvents.get(event.getTokenSpan().getSentenceIndex()).add(event);
		}
		
		Event[][] events = new Event[document.tokens.length][];
		for (int i = 0; i < events.length; i++) {
			if (!sentenceEvents.containsKey(i)) {
				events[i] = new Event[0];
				continue;
			}
			
			events[i] = sentenceEvents.get(i).toArray(new Event[0]);
		}
		document.setEvents(events);
		
		List<Element> tlinkElements = element.getChildren("TLINK");
		List<TLink> tlinks = new ArrayList<TLink>();
		for (Element tlinkElement : tlinkElements) {
			tlinks.add(TLink.fromTimeML(tlinkElement, document));
		}
		document.setTLinks(tlinks);
		
		return document;
	}
}
