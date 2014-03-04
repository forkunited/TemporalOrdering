package temp.data.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ark.util.FileUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import temp.data.annotation.nlp.Annotator;
import temp.data.annotation.nlp.PoSTag;
import temp.data.annotation.nlp.TypedDependency;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.Signal;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.Time;

public class TempDocument {
	private String name;
	private Language language;
	private String nlpAnnotator;
	
	private String[][] tokens;
	private PoSTag[][] posTags;
	private TypedDependency[][] dependencies; 
	
	private Event[][] events;
	private Time[][] times;
	private Signal[][] signals;
	private TLink[] tlinks;
	
	private Map<String, Event> eventMap;
	private Map<String, Time> timeMap;
	private Map<String, Signal> signalMap;
	
	public String getName() {
		return this.name;
	}
	
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
		if (tokenIndex < 0)
			return "ROOT";
		else 
			return this.tokens[sentenceIndex][tokenIndex];
	}
	
	public PoSTag getPoSTag(int sentenceIndex, int tokenIndex) {
		return this.posTags[sentenceIndex][tokenIndex];
	}
	
	public List<TypedDependency> getParentDependencies(int sentenceIndex, int tokenIndex) {
		TypedDependency[] tokenDependencies = this.dependencies[sentenceIndex];
		List<TypedDependency> parentDependencies = new ArrayList<TypedDependency>();
		for (int i = 0; i < tokenDependencies.length; i++) {
			if (tokenDependencies[i].getChildTokenIndex() == tokenIndex)
				parentDependencies.add(tokenDependencies[i]);
		}
		return parentDependencies;
	}
	
	public List<TypedDependency> getChildDependencies(int sentenceIndex, int tokenIndex) {
		TypedDependency[] tokenDependencies = this.dependencies[sentenceIndex];
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
	
	public boolean setSignals(int sentenceIndex, List<Signal> signals) {
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
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		JSONArray sentences = new JSONArray();
		
		json.put("name", this.name);
		json.put("language", this.language.toString());
		json.put("nlpAnnotator", this.nlpAnnotator);
		
		for (int i = 0; i < this.tokens.length; i++) {
			JSONObject sentenceJson = new JSONObject();
			
			sentenceJson.put("tokens", JSONArray.fromObject(this.tokens[i]));
	
			JSONArray posTagsJson = new JSONArray();
			for (int j = 0; j < this.posTags[i].length; j++)
				posTagsJson.add(this.posTags[i][j].toString());
			sentenceJson.put("posTags", posTagsJson);
			
			JSONArray dependenciesJson = new JSONArray();
			for (int j = 0; j < this.dependencies[i].length; j++)
				dependenciesJson.add(this.dependencies[i][j].toString());
			sentenceJson.put("dependencies", dependenciesJson);
			
			JSONArray eventsJson = new JSONArray();
			for (int j = 0; j < this.events[i].length; j++)
				eventsJson.add(this.events[i][j].toJSON());
			sentenceJson.put("events", eventsJson);
			
			JSONArray timesJson = new JSONArray();
			for (int j = 0; j < this.times[i].length; j++)
				timesJson.add(this.times[i][j].toJSON());
			sentenceJson.put("times", timesJson);
			
			JSONArray signalsJson = new JSONArray();
			for (int j = 0; j < this.signals[i].length; j++)
				signalsJson.add(this.signals[i][j].toJSON());
			
			sentences.add(sentenceJson);
		}
		json.put("sentences", sentences);
		
		JSONArray tlinksJson = new JSONArray();
		for (int i = 0; i < this.tlinks.length; i++)
			tlinksJson.add(this.tlinks[i]);
		json.put("tlinks", tlinksJson);
		
		return json;
	}
	
	public Element toXML() {
		Element element = new Element("file");
		
		element.setAttribute("name", this.name);
		element.setAttribute("language", this.language.toString());
		element.setAttribute("nlpAnnotator", this.nlpAnnotator);
		
		for (int i = 0; i < this.tokens.length; i++) {
			Element entryElement = new Element("entry");
			entryElement.setAttribute("sid", String.valueOf(i));
			entryElement.setAttribute("file", this.name);
			
			Element sentenceElement = new Element("sentence");
			Element tokensElement = new Element("tokens");
			StringBuilder sentenceStr = new StringBuilder();
			for (int j = 0; j < this.tokens[i].length; j++) {
				Element tokenElement = new Element("t");
				tokenElement.addContent("\" \" \"" + this.tokens[i][j] + "\" \" \"");
				if (this.posTags[i] != null)
					tokenElement.setAttribute("pos", this.posTags[i][j].toString());
				tokensElement.addContent(tokenElement);
				sentenceStr.append(this.tokens[i][j]).append(" ");
			}
			sentenceElement.addContent(sentenceStr.toString().trim());
			entryElement.addContent(sentenceElement);
			entryElement.addContent(tokensElement);
			
			Element parseElement = new Element("parse");
			parseElement.addContent("(ROOT)");
			entryElement.addContent(parseElement);
			
			Element depsElement = new Element("deps");
			StringBuilder depsStr = new StringBuilder();
			for (int j = 0; j < this.dependencies[i].length; j++)
				depsStr.append(this.dependencies[i][j].toString()).append("\n");
			if (depsStr.length() > 0)
				depsStr = depsStr.delete(depsStr.length() - 1, depsStr.length());
			depsElement.addContent(depsStr.toString());
			entryElement.addContent(depsElement);
			
			Element eventsElement = new Element("events");
			for (int j = 0; j < this.events[i].length; j++) 
				eventsElement.addContent(this.events[i][j].toXML());
			entryElement.addContent(eventsElement);
			
			Element timexesElement = new Element("timexes");
			for (int j = 0; j < this.times[i].length; j++)
				timexesElement.addContent(this.times[i][j].toXML());
			entryElement.addContent(timexesElement);
			
			Element signalsElement = new Element("signals");
			for (int j = 0; j < this.signals[i].length; j++)
				signalsElement.addContent(this.signals[i][j].toXML());
			entryElement.addContent(signalsElement);
			
			element.addContent(entryElement);
		}
		
		for (int i = 0; i < this.tlinks.length; i++) {
			element.addContent(this.tlinks[i].toXML());
		}
		
		
		return element;
	}
	
	public boolean saveToJSONFile(String path) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(path));
			
			w.write(toJSON().toString());
			
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean saveToXMLFile(String path) {
		try {
			Document document = new Document();
			document.setRootElement(toXML());
			
			FileOutputStream out = new FileOutputStream(new File(path));
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			
			outputter.output(document, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static TempDocument fromJSON(JSONObject json) {
		TempDocument document = new TempDocument();
		
		document.name = json.getString("name");
		document.language = Language.valueOf(json.getString("language"));
		document.nlpAnnotator = json.getString("nlpAnnotator");
		
		JSONArray sentences = json.getJSONArray("sentences");
		document.tokens = new String[sentences.size()][];
		document.posTags = new PoSTag[sentences.size()][];
		document.dependencies = new TypedDependency[sentences.size()][];
		document.events = new Event[sentences.size()][];
		document.times = new Time[sentences.size()][];
		document.signals = new Signal[sentences.size()][];
		document.eventMap = new HashMap<String, Event>();
		document.timeMap = new HashMap<String, Time>();
		document.signalMap = new HashMap<String, Signal>();
		
		for (int i = 0; i < sentences.size(); i++) {
			JSONObject sentenceJson = sentences.getJSONObject(i);
			JSONArray tokensJson = sentenceJson.getJSONArray("tokens");
			JSONArray posTagsJson = sentenceJson.getJSONArray("posTags");
			JSONArray dependenciesJson = sentenceJson.getJSONArray("dependencies");
			JSONArray signalsJson = sentenceJson.getJSONArray("signals");
			JSONArray timesJson = sentenceJson.getJSONArray("times");
			JSONArray eventsJson = sentenceJson.getJSONArray("events");
			
			document.tokens[i] = new String[tokensJson.size()];
			for (int j = 0; j < tokensJson.size(); j++)
				document.tokens[i][j] = tokensJson.getString(j);
			
			document.posTags[i] = new PoSTag[posTagsJson.size()];
			for (int j = 0; j < posTagsJson.size(); j++)
				document.posTags[i][j] = PoSTag.valueOf(posTagsJson.getString(j));
			
			document.dependencies[i] = new TypedDependency[dependenciesJson.size()];
			for (int j = 0; j < dependenciesJson.size(); j++)
				document.dependencies[i][j] = TypedDependency.fromString(dependenciesJson.getString(j), document, i);
			
			List<Signal> signals = new ArrayList<Signal>(signalsJson.size());
			for (int j = 0; j < signalsJson.size(); j++)
				signals.add(Signal.fromJSON(signalsJson.getJSONObject(j), document, i));
			document.setSignals(i, signals);
			
			/* FIXME: Some times reference others within the same document (as anchors and stuff), so it's 
			 * possible that if they are added in the wrong order, the references will be empty.  The 
			 * following code fixes this issue by repeatedly trying to add all of the times, 
			 * skipping over the ones which reference ones that haven't been added yet.  This isn't a good way 
			 * to do this, but it should be alright, for now, given that the number of times that reference other 
			 * times is small.
			 */
			List<Time> times = new ArrayList<Time>(timesJson.size());
			List<Integer> timesToAdd = new ArrayList<Integer>();
			for (int j = 0; j < timesJson.size(); j++)
				timesToAdd.add(j);
			while (!timesToAdd.isEmpty()) {
				List<Integer> nextTimesToAdd = new ArrayList<Integer>();
				for (int j = 0; j < timesToAdd.size(); j++) {
					Time time = Time.fromJSON(timesJson.getJSONObject(timesToAdd.get(j)), document, i);
					if (time != null) {
						times.add(time);
					} else {
						nextTimesToAdd.add(timesToAdd.get(j));
					}
				}
				document.setTimes(i, times);
				timesToAdd = nextTimesToAdd;
			}
			
			List<Event> events = new ArrayList<Event>(eventsJson.size());
			for (int j = 0; j < eventsJson.size(); j++)
				events.add(Event.fromJSON(eventsJson.getJSONObject(j), document, i));
			document.setEvents(i, events);
		}
		
		JSONArray tlinksJson = json.getJSONArray("tlinks");
		List<TLink> tlinks = new ArrayList<TLink>(tlinksJson.size());
		for (int i = 0; i < tlinksJson.size(); i++)
			tlinks.add(TLink.fromJSON(tlinksJson.getJSONObject(i), document));
		document.setTLinks(tlinks);
	
		return document;
	}
	
	@SuppressWarnings("unchecked")
	public static TempDocument fromXML(Element element) {
		TempDocument document = new TempDocument();
		
		boolean hasName = false;
		boolean hasLanguage = false;
		boolean hasNlpAnnotator = false;
		
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("name"))
				hasName = true;
			else if (attribute.getName().equals("language"))
				hasLanguage = true;
			else if (attribute.getName().equals("nlpAnnotator"))
				hasNlpAnnotator = true;
		}
		
		if (hasName)
			document.name = element.getAttributeValue("name");
		if (hasLanguage)
			document.language = Language.valueOf(element.getAttributeValue("language"));
		if (hasNlpAnnotator)
			document.nlpAnnotator = element.getAttributeValue("nlpAnnotator");
		
		List<Element> entryElements = (List<Element>)element.getChildren("entry");
		document.tokens = new String[entryElements.size()][];
		document.posTags = new PoSTag[entryElements.size()][];
		document.dependencies = new TypedDependency[entryElements.size()][];
		document.events = new Event[entryElements.size()][];
		document.times = new Time[entryElements.size()][];
		document.signals = new Signal[entryElements.size()][];
		document.eventMap = new HashMap<String, Event>();
		document.timeMap = new HashMap<String, Time>();
		document.signalMap = new HashMap<String, Signal>();
		
		for (Element entryElement : entryElements) {
			int sentenceIndex = Integer.parseInt(entryElement.getAttributeValue("sid"));
			
			Element tokensElement = entryElement.getChild("tokens");
			List<Element> tElements = tokensElement.getChildren("t");
			document.tokens[sentenceIndex] = new String[tElements.size()];
			document.posTags[sentenceIndex] = new PoSTag[tElements.size()];
			for (int i = 0; i < tElements.size(); i++) {
				document.tokens[sentenceIndex][i] = (tElements.get(i).getText().split("\""))[3];
				List<Attribute> tAttributes = (List<Attribute>)tElements.get(i).getAttributes();
				for (Attribute attribute : tAttributes)
					if (attribute.getName().equals("pos"))
						document.posTags[sentenceIndex][i] = PoSTag.valueOf(attribute.getValue());
			}
			
			Element depsElement = entryElement.getChild("deps");
			String[] depStrs = depsElement.getText().split("\n");
			document.dependencies[sentenceIndex] = new TypedDependency[depStrs.length];
			for (int i = 0; i < depStrs.length; i++)
				document.dependencies[sentenceIndex][i] = TypedDependency.fromString(depStrs[i], document, sentenceIndex);
			
			Element signalsElement = entryElement.getChild("signals");
			List<Element> signalElements = signalsElement.getChildren("signal");
			List<Signal> signals = new ArrayList<Signal>(signalElements.size());
			for (int i = 0; i < signalElements.size(); i++)
				signals.add(Signal.fromXML(signalElements.get(i), document, sentenceIndex));
			document.setSignals(sentenceIndex, signals);
				
			/* FIXME: Some times reference others within the same document (as anchors and stuff), so it's 
			 * possible that if they are added in the wrong order, the references will be empty.  The 
			 * following code fixes this issue by repeatedly trying to add all of the times, 
			 * skipping over the ones which reference ones that haven't been added yet.  This isn't a good way 
			 * to do this, but it should be alright, for now, given that the number of times that reference other 
			 * times is small.
			 */
			Element timexesElement = entryElement.getChild("timexes");
			List<Element> timexElements = timexesElement.getChildren("timex");
			List<Time> times = new ArrayList<Time>(timexElements.size());
			List<Integer> timesToAdd = new ArrayList<Integer>();
			for (int i = 0; i < timexElements.size(); i++)
				timesToAdd.add(i);
			while (!timesToAdd.isEmpty()) {
				List<Integer> nextTimesToAdd = new ArrayList<Integer>();
				for (int i = 0; i < timesToAdd.size(); i++) {
					Time time = Time.fromXML(timexElements.get(timesToAdd.get(i)), document, sentenceIndex);
					if (time != null) {
						times.add(time);
					} else {
						nextTimesToAdd.add(timesToAdd.get(i));
					}
				}
				document.setTimes(sentenceIndex, times);
				timesToAdd = nextTimesToAdd;
			}
			
				
			Element eventsElement = entryElement.getChild("events");
			List<Element> eventElements = eventsElement.getChildren("event");
			List<Event> events = new ArrayList<Event>(eventElements.size());
			for (int i = 0; i < eventElements.size(); i++)
				events.addAll(Event.fromXML(eventElements.get(i), document, sentenceIndex));
			document.setEvents(sentenceIndex, events);
		}
		
		List<Element> tlinkElements = (List<Element>)element.getChildren("tlink");
		List<TLink> tlinks = new ArrayList<TLink>(tlinkElements.size());
		for (Element tlinkElement : tlinkElements) {
			tlinks.add(TLink.fromXML(tlinkElement, document));
		}
		document.setTLinks(tlinks);
		
		return document;
	}
	
	public static TempDocument loadFromJSONFile(String path) {
		BufferedReader r = FileUtil.getFileReader(path);
		String line = null;
		StringBuffer lines = new StringBuffer();
		try {
			while ((line = r.readLine()) != null) {
				lines.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return TempDocument.fromJSON(JSONObject.fromObject(lines.toString()));
	}
	
	public static TempDocument loadFromXMLFile(String path) {
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try {
			document = builder.build(new File(path));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Element element = document.getRootElement();
		
		return TempDocument.fromXML(element);
	}
	
	public static TempDocument createFromText(String name, String text, Language language, Annotator annotator) {
		TempDocument document = new TempDocument();
		
		annotator.setLanguage(language);
		annotator.setText(text);
		
		document.name = name;
		document.language = language;
		document.nlpAnnotator = annotator.toString();
		
		document.tokens = annotator.makeTokens();
		
		TypedDependency[][] dependencies = annotator.makeDependencies();
		document.dependencies = new TypedDependency[dependencies.length][];
		for (int i = 0; i < dependencies.length; i++) {
			document.dependencies[i] = dependencies[i];
			for (int j = 0; j < dependencies[i].length; j++) {
				document.dependencies[i][j] = new TypedDependency(document, i, 
																  dependencies[i][j].getParentTokenIndex(), 
																  dependencies[i][j].getChildTokenIndex(), 
																  dependencies[i][j].getType());
			}
		}
			
		document.posTags = annotator.makePoSTags();
		
		document.events = new Event[document.tokens.length][0];
		document.times = new Time[document.tokens.length][0];
		document.signals = new Signal[document.tokens.length][0];
		document.eventMap = new HashMap<String, Event>();
		document.timeMap = new HashMap<String, Time>();
		document.signalMap = new HashMap<String, Signal>();
		
		document.tlinks = new TLink[0];
		
		return document;
	}
}
