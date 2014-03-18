package temp.data.annotation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import ark.model.annotator.nlp.NLPAnnotator;
import ark.data.annotation.Language;
import ark.data.annotation.DocumentInMemory;

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
		
		this.creationTime = creationTime;
		
		initializeTimeML();
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
	
		this.creationTime = creationTime;
		
		initializeTimeML();
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
		return setTLinks((TLink[])(tlinks.toArray()));
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
	
	protected boolean fromJSON(JSONObject json) {
		if (!super.fromJSON(json))
			return false;
		
		JSONArray sentences = json.getJSONArray("sentences");

		initializeTimeML();
		
		if (json.has("creationTime")) {
			this.creationTime = Time.fromJSON(json.getJSONObject("creationTime"), this, -1);
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

		/* FIXME: Some times reference others within the same document (as anchors and stuff), so it's 
		 * possible that if they are added in the wrong order, the references will be empty.  The 
		 * following code fixes this issue by repeatedly trying to add all of the times, 
		 * skipping over the ones which reference ones that haven't been added yet.  This isn't a good way 
		 * to do this, but it should be alright, for now, given that the number of times that reference other 
		 * times is small.
		 */
		boolean failedToAddTime;
		do {
			failedToAddTime = false;
			Time[][] times = new Time[timesJson.length][];
			for (int i = 0; i < timesJson.length; i++) {
				times[i] = new Time[timesJson[i].size()];
				for (int j = 0; j < timesJson[i].size(); j++) {
					if (this.times[i].length > j && this.times[i][j] != null)
						times[i][j] = this.times[i][j];
					else {
						times[i][j] = Time.fromJSON(timesJson[i].getJSONObject(j), this, i);
					}
					
					if (times[i][j] == null)
						failedToAddTime = true;
				}
			}
			this.setTimes(times);
		} while (failedToAddTime);
		
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
			this.creationTime = Time.fromXML(creationTimeElements.get(0), this, -1);
			if (this.creationTime != null)
				this.timeMap.put(this.creationTime.getId(), this.creationTime);
		}
		
		/* FIXME: Some times reference others within the same document (as anchors and stuff), so it's 
		 * possible that if they are added in the wrong order, the references will be empty.  The 
		 * following code fixes this issue by repeatedly trying to add all of the times, 
		 * skipping over the ones which reference ones that haven't been added yet.  This isn't a good way 
		 * to do this, but it should be alright, for now, given that the number of times that reference other 
		 * times is small.
		 */
		boolean failedToAddTime;
		do {
			failedToAddTime = false;
			Time[][] times = new Time[timexesXML.size()][];
			for (int i = 0; i < timexesXML.size(); i++) {
				times[i] = new Time[timexesXML.get(i).size()];
				for (int j = 0; j < timexesXML.get(i).size(); j++) {
					if (this.times[i].length > j && this.times[i][j] != null)
						times[i][j] = this.times[i][j];
					else {
						times[i][j] = Time.fromXML(timexesXML.get(i).get(j), this, i);
					}
						
					if (times[i][j] == null)
						failedToAddTime = true;
				}
			}
			this.setTimes(times);
		} while (failedToAddTime);
			
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
}
