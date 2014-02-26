package temp.data.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import temp.data.annotation.nlp.PoSTag;
import temp.data.annotation.nlp.TypedDependency;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.Signal;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.Time;

public class TempDocument {
	private Language language;
	
	private String[][] tokens;
	private PoSTag[][] posTags;
	/* sentence index => token index => parents and children */
	private Map<Integer, TypedDependency[]>[] dependencies; 
	
	private Event[][] events;
	private Time[][] times;
	private Signal[][] signals;
	private TLink[] tlinks;
	
	private Map<String, Event> eventMap;
	private Map<String, Time> timeMap;
	private Map<String, Signal> signalMap;
	
	public Language getLanguage() {
		return this.language;
	}
	
	public int getSentenceCount() {
		return this.tokens.length;
	}
	
	public int getSentenceTokenCount(int sentenceIndex) {
		return this.tokens[sentenceIndex].length;
	}
	
	public String getToken(int sentenceIndex, int tokenIndex) {
		return this.tokens[sentenceIndex][tokenIndex];
	}
	
	public PoSTag getPoSTag(int sentenceIndex, int tokenIndex) {
		return this.posTags[sentenceIndex][tokenIndex];
	}
	
	public List<TypedDependency> getParentDependencies(int sentenceIndex, int tokenIndex) {
		TypedDependency[] tokenDependencies = this.dependencies[sentenceIndex].get(tokenIndex);
		List<TypedDependency> parentDependencies = new ArrayList<TypedDependency>(tokenDependencies.length);
		for (int i = 0; i < tokenDependencies.length; i++) {
			if (tokenDependencies[i].getChildTokenIndex() == tokenIndex)
				parentDependencies.add(tokenDependencies[i]);
		}
		return parentDependencies;
	}
	
	public List<TypedDependency> getChildDependencies(int sentenceIndex, int tokenIndex) {
		TypedDependency[] tokenDependencies = this.dependencies[sentenceIndex].get(tokenIndex);
		List<TypedDependency> childDependencies = new ArrayList<TypedDependency>(tokenDependencies.length);
		for (int i = 0; i < tokenDependencies.length; i++) {
			if (tokenDependencies[i].getParentTokenIndex() == tokenIndex)
				childDependencies.add(tokenDependencies[i]);
		}
		return childDependencies;
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
	
	public boolean setEvents(int sentenceIndex, List<Event> events) {
		if (sentenceIndex > this.tokens.length)
			return false;
		this.events[sentenceIndex] = (Event[])(events.toArray());
		for (int i = 0; i < this.events[sentenceIndex].length; i++)
			this.eventMap.put(this.events[sentenceIndex][i].getId(), this.events[sentenceIndex][i]);	
		return true;
	}
	
	public boolean setTimes(int sentenceIndex, List<Time> times) {
		if (sentenceIndex > this.tokens.length)
			return false;
		this.times[sentenceIndex] = (Time[])(times.toArray());
		for (int i = 0; i < this.times[sentenceIndex].length; i++)
			this.timeMap.put(this.times[sentenceIndex][i].getId(), this.times[sentenceIndex][i]);	
		return true;
	}
	
	public boolean setSignals(int sentenceIndex, List<Event> signals) {
		if (sentenceIndex > this.tokens.length)
			return false;
		this.signals[sentenceIndex] = (Signal[])(signals.toArray());
		for (int i = 0; i < this.signals[sentenceIndex].length; i++)
			this.signalMap.put(this.signals[sentenceIndex][i].getId(), this.signals[sentenceIndex][i]);	
		return true;
	}
	
	public boolean setTLinks(List<TLink> tlinks) {
		this.tlinks = (TLink[])(tlinks.toArray());
		return true;
	}
	
	public boolean saveToJSONFile(String path) {
		/* FIXME */
		return true;
	}
	
	public boolean saveToXMLFile(String path) {
		/* FIXME */
		return true;
	}
	
	public static TempDocument loadFromJSONFile(String path) {
		/* FIXME */
		return null;
	}
	
	public static TempDocument loadFromXMLFile(String path) {
		/* FIXME */
		return null;
	}
	
	public static TempDocument createFromText(String text, Language language) {
		/* FIXME */
		return null;
	}
}
