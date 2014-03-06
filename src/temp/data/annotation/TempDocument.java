package temp.data.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import temp.model.annotator.nlp.NLPAnnotator;
import temp.data.annotation.nlp.PoSTag;
import temp.data.annotation.nlp.TypedDependency;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.Signal;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.Time;

public class TempDocument {
	private static SimpleDateFormat CREATION_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private String name;
	private Language language;
	private String nlpAnnotator;
	private Date creationTime;
	
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
	
	public String getNLPAnnotator() {
		return this.nlpAnnotator;
	}
	
	public Date getCreationTime() {
		return this.creationTime;
	}
	
	public int getSentenceCount() {
		return this.tokens.length;
	}
	
	public int getSentenceTokenCount(int sentenceIndex) {
		return this.tokens[sentenceIndex].length;
	}
	
	public String getText() {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < getSentenceCount(); i++)
			text = text.append(getSentence(i)).append(" ");
		return text.toString().trim();
	}
	
	public String getSentence(int sentenceIndex) {
		StringBuilder sentenceStr = new StringBuilder();
		
		for (int i = 0; i < this.tokens[sentenceIndex].length; i++) {
			if (this.posTags[sentenceIndex][i] != PoSTag.SYM)
				sentenceStr = sentenceStr.append(" ");
			sentenceStr = sentenceStr.append(this.tokens[sentenceIndex][i]);
		}
		return sentenceStr.toString();
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
	
	public List<Event> getEventsForToken(int sentenceIndex, int tokenIndex) {
		List<Event> events = new ArrayList<Event>();
		for (int j = 0; j < this.events[sentenceIndex].length; j++) {
			if (this.events[sentenceIndex][j].getTokenSpan().containsToken(sentenceIndex, tokenIndex))
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
	
	public boolean setEvents(Event[][] events) {
		this.events = new Event[events.length][];
		this.eventMap = new HashMap<String, Event>();
		for (int i = 0; i < events.length; i++) {
			this.events[i] = new Event[events.length];
			for (int j = 0; i < events[i].length; j++) {
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
			this.times[i] = new Time[times.length];
			for (int j = 0; i < times[i].length; j++) {
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
			this.signals[i] = new Signal[signals.length];
			for (int j = 0; i < signals[i].length; j++) {
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
		JSONObject json = new JSONObject();
		JSONArray sentences = new JSONArray();
		
		json.put("name", this.name);
		json.put("language", this.language.toString());
		if (this.creationTime != null)
			json.put("creationTime", TempDocument.CREATION_TIME_FORMAT.format(this.creationTime));
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
				if (this.events[i][j] != null)
					eventsJson.add(this.events[i][j].toJSON());
			sentenceJson.put("events", eventsJson);
			
			JSONArray timesJson = new JSONArray();
			for (int j = 0; j < this.times[i].length; j++)
				if (this.times[i][j] != null)
					timesJson.add(this.times[i][j].toJSON());
			sentenceJson.put("times", timesJson);
			
			JSONArray signalsJson = new JSONArray();
			for (int j = 0; j < this.signals[i].length; j++)
				if (this.signals[i][j] != null)
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
		
		if (this.creationTime != null)
			element.setAttribute("creationTime", TempDocument.CREATION_TIME_FORMAT.format(this.creationTime));
		
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
	
	private void initializeTimeML() {
		this.events = new Event[this.tokens.length][0];
		this.times = new Time[this.tokens.length][0];
		this.signals = new Signal[this.tokens.length][0];
		this.eventMap = new HashMap<String, Event>();
		this.timeMap = new HashMap<String, Time>();
		this.signalMap = new HashMap<String, Signal>();	
		this.tlinks = new TLink[0];
	}
	
	public static TempDocument fromJSON(JSONObject json) {
		TempDocument document = new TempDocument();
		
		document.name = json.getString("name");
		document.language = Language.valueOf(json.getString("language"));
		if (json.has("creationTime")) {
			try {
				document.creationTime = TempDocument.CREATION_TIME_FORMAT.parse(json.getString("creationTime"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		document.nlpAnnotator = json.getString("nlpAnnotator");
		
		JSONArray sentences = json.getJSONArray("sentences");
		document.tokens = new String[sentences.size()][];
		document.posTags = new PoSTag[sentences.size()][];
		document.dependencies = new TypedDependency[sentences.size()][];

		document.initializeTimeML();
		
		JSONArray[] timesJson = new JSONArray[sentences.size()];
		JSONArray[] eventsJson = new JSONArray[sentences.size()];
		Signal[][] signals = new Signal[sentences.size()][];
		
		for (int i = 0; i < sentences.size(); i++) {
			JSONObject sentenceJson = sentences.getJSONObject(i);
			JSONArray tokensJson = sentenceJson.getJSONArray("tokens");
			JSONArray posTagsJson = sentenceJson.getJSONArray("posTags");
			JSONArray dependenciesJson = sentenceJson.getJSONArray("dependencies");
			JSONArray signalsJson = sentenceJson.getJSONArray("signals");
			
			timesJson[i] = sentenceJson.getJSONArray("times");
			eventsJson[i] = sentenceJson.getJSONArray("events");
			
			document.tokens[i] = new String[tokensJson.size()];
			for (int j = 0; j < tokensJson.size(); j++)
				document.tokens[i][j] = tokensJson.getString(j);
			
			document.posTags[i] = new PoSTag[posTagsJson.size()];
			for (int j = 0; j < posTagsJson.size(); j++)
				document.posTags[i][j] = PoSTag.valueOf(posTagsJson.getString(j));
			
			document.dependencies[i] = new TypedDependency[dependenciesJson.size()];
			for (int j = 0; j < dependenciesJson.size(); j++)
				document.dependencies[i][j] = TypedDependency.fromString(dependenciesJson.getString(j), document, i);
			
			signals[i] = new Signal[signalsJson.size()];
			for (int j = 0; j < signalsJson.size(); j++)
				signals[i][j] = Signal.fromJSON(signalsJson.getJSONObject(j), document, i);
		}

		document.setSignals(signals);

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
				for (int j = 0; j < timesJson[i].size(); j++) {
					if (document.times[i].length > j && document.times[i][j] != null)
						times[i][j] = document.times[i][j];
					else
						times[i][j] = Time.fromJSON(timesJson[i].getJSONObject(j), document, i);
					
					if (times[i][j] == null)
						failedToAddTime = true;
				}
			}
			document.setTimes(times);
		} while (failedToAddTime);
		
		Event[][] events = new Event[eventsJson.length][];
		for (int i = 0; i < events.length; i++) {
			for (int j = 0; j < eventsJson[i].size(); j++) {
				events[i][j] = Event.fromJSON(eventsJson[i].getJSONObject(j), document, i);
			}
		}
		document.setEvents(events);
		
		JSONArray tlinksJson = json.getJSONArray("tlinks");
		TLink[] tlinks = new TLink[tlinksJson.size()];
		for (int i = 0; i < tlinksJson.size(); i++)
			tlinks[i] = TLink.fromJSON(tlinksJson.getJSONObject(i), document);
		document.setTLinks(tlinks);
	
		return document;
	}
	
	@SuppressWarnings("unchecked")
	public static TempDocument fromXML(Element element) {
		TempDocument document = new TempDocument();
		
		boolean hasName = false;
		boolean hasLanguage = false;
		boolean hasCreationTime = false;
		boolean hasNlpAnnotator = false;
		
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("name"))
				hasName = true;
			else if (attribute.getName().equals("language"))
				hasLanguage = true;
			else if (attribute.getName().equals("nlpAnnotator"))
				hasNlpAnnotator = true;
			else if (attribute.getName().equals("creationTime"))
				hasCreationTime = true;
		}
		
		if (hasName)
			document.name = element.getAttributeValue("name");
		if (hasLanguage)
			document.language = Language.valueOf(element.getAttributeValue("language"));
		if (hasCreationTime) {
			try {
				document.creationTime = TempDocument.CREATION_TIME_FORMAT.parse(element.getAttributeValue("creationTime"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (hasNlpAnnotator)
			document.nlpAnnotator = element.getAttributeValue("nlpAnnotator");
		
		List<Element> entryElements = (List<Element>)element.getChildren("entry");
		document.tokens = new String[entryElements.size()][];
		document.posTags = new PoSTag[entryElements.size()][];
		document.dependencies = new TypedDependency[entryElements.size()][];
		
		document.initializeTimeML();
		
		List<List<Element>> timexesXML = new ArrayList<List<Element>>();
		List<List<Element>> eventsXML = new ArrayList<List<Element>>();
		Signal[][] signals = new Signal[entryElements.size()][];
		
		for (Element entryElement : entryElements) {
			int sentenceIndex = Integer.parseInt(entryElement.getAttributeValue("sid"));
		
			Element timexesElement = entryElement.getChild("timexes");
			timexesXML.add(timexesElement.getChildren("timex"));
			
			Element eventsElement = entryElement.getChild("events");
			eventsXML.add(eventsElement.getChildren("event"));
			
			Element tokensElement = entryElement.getChild("tokens");
			List<Element> tElements = tokensElement.getChildren("t");
			document.tokens[sentenceIndex] = new String[tElements.size()];
			document.posTags[sentenceIndex] = new PoSTag[tElements.size()];
			for (int j = 0; j < tElements.size(); j++) {
				document.tokens[sentenceIndex][j] = (tElements.get(j).getText().split("\""))[3];
				List<Attribute> tAttributes = (List<Attribute>)tElements.get(j).getAttributes();
				for (Attribute attribute : tAttributes)
					if (attribute.getName().equals("pos"))
						document.posTags[sentenceIndex][j] = PoSTag.valueOf(attribute.getValue());
			}
			
			Element depsElement = entryElement.getChild("deps");
			String[] depStrs = depsElement.getText().split("\n");
			document.dependencies[sentenceIndex] = new TypedDependency[depStrs.length];
			for (int j = 0; j < depStrs.length; j++)
				document.dependencies[sentenceIndex][j] = TypedDependency.fromString(depStrs[j], document, sentenceIndex);
			
			Element signalsElement = entryElement.getChild("signals");
			List<Element> signalElements = signalsElement.getChildren("signal");
			signals[sentenceIndex] = new Signal[signalElements.size()];
			for (int j = 0; j < signalElements.size(); j++)
				signals[sentenceIndex][j] = Signal.fromXML(signalElements.get(j), document, sentenceIndex);
		}
		
		document.setSignals(signals);
				
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
				for (int j = 0; j < timexesXML.get(i).size(); j++) {
					if (document.times[i].length > j && document.times[i][j] != null)
						times[i][j] = document.times[i][j];
					else
						times[i][j] = Time.fromXML(timexesXML.get(i).get(j), document, i);
					
					if (times[i][j] == null)
						failedToAddTime = true;
				}
			}
			document.setTimes(times);
		} while (failedToAddTime);
			
		Event[][] events = new Event[eventsXML.size()][];
		for (int i = 0; i < eventsXML.size(); i++) {
			List<Event> sentenceEvents = new ArrayList<Event>();
			for (int j = 0; j < eventsXML.get(i).size(); j++) {
				sentenceEvents.addAll(Event.fromXML(eventsXML.get(i).get(j), document, i));
			}
			events[i] = new Event[sentenceEvents.size()];
			events[i] = sentenceEvents.toArray(events[i]);
		}
		document.setEvents(events);
		
		List<Element> tlinkElements = (List<Element>)element.getChildren("tlink");
		TLink[] tlinks = new TLink[tlinkElements.size()];
		for (int i = 0; i < tlinkElements.size(); i++) {
			tlinks[i] = TLink.fromXML(tlinkElements.get(i), document);
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
	
	public static TempDocument createFromText(String name, String text, Language language, Date creationTime, NLPAnnotator annotator) {
		TempDocument document = new TempDocument();
		
		annotator.setLanguage(language);
		annotator.setText(text);
		
		document.name = name;
		document.language = language;
		document.creationTime = creationTime;
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
		
		document.initializeTimeML();
		
		return document;
	}
}
