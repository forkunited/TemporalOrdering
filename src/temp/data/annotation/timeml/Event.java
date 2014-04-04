package temp.data.annotation.timeml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Element;

import net.sf.json.JSONObject;
import temp.data.annotation.TempDocument;
import ark.data.annotation.nlp.TokenSpan;
import ark.util.Pair;

public class Event implements TLinkable {	
	public enum TimeMLTense {
		FUTURE,
		INFINITIVE,
		PAST,
		PASTPART,
		PRESENT,
		PRESPART,
		NONE
	}
	
	public enum TimeMLAspect {
		PROGRESSIVE,
		
		IMPERFECTIVE, // Spanish only
		IMPERFECTIVE_PROGRESSIVE, // Spanish only
		
		PERFECTIVE,
		PERFECTIVE_PROGRESSIVE,
		NONE
	}
	
	public enum TimeMLPolarity {
		POS,
		NEG
	}
	
	public enum TimeMLClass {
		OCCURRENCE,
		PERCEPTION,
		REPORTING,
		ASPECTUAL,
        STATE,
        I_STATE, 
        I_ACTION,
        NONE
	}
	
	public enum TimeMLPoS {
		ADJECTIVE,
		NOUN,
		VERB,
		PREPOSITION,
		OTHER
	}
	
	public enum TimeMLMood {
		INDICATIVE,
		SUBJUNCTIVE,
		CONDITIONAL,
		IMPERATIVE,
		NONE
	}
	
	public enum TimeMLVerbForm {
		INFINITIVE,
		GERUNDIVE,
		PARTICIPLE,
		NONE
	}

	private String id;
	private TokenSpan tokenSpan;
	private String sourceId;
	private Signal signal;
	private TimeMLTense timeMLTense;
	private TimeMLAspect timeMLAspect;
	private TimeMLPolarity timeMLPolarity = TimeMLPolarity.POS;
	private TimeMLClass timeMLClass;
	private TimeMLMood timeMLMood;
	private TimeMLVerbForm timeMLVerbForm;
	private TimeMLPoS timeMLPoS;
	private String modality;
	private String cardinality;
	
	public Event() {
		
	}
	
	public Event(int id, TokenSpan tokenSpan, TimeMLTense timeMLTense, TimeMLAspect timeMLAspect, TimeMLClass timeMLClass) {
		this(id, tokenSpan, timeMLTense, timeMLAspect, timeMLClass, TimeMLPolarity.POS);
	}
	
	public Event(int id, TokenSpan tokenSpan, TimeMLTense timeMLTense, TimeMLAspect timeMLAspect, TimeMLClass timeMLClass, TimeMLPolarity timeMLPolarity) {
		this(id, tokenSpan, timeMLTense, timeMLAspect, timeMLClass, timeMLPolarity, TimeMLMood.NONE, TimeMLVerbForm.NONE);
	}
	
	public Event(int id, TokenSpan tokenSpan, TimeMLTense timeMLTense, TimeMLAspect timeMLAspect, TimeMLClass timeMLClass, TimeMLPolarity timeMLPolarity, TimeMLMood timeMLMood, TimeMLVerbForm timeMLVerbForm) {
		this(id, tokenSpan, timeMLTense, timeMLAspect, timeMLClass, timeMLPolarity, timeMLMood, timeMLVerbForm, null);
	}
	
	public Event(int id, TokenSpan tokenSpan, TimeMLTense timeMLTense, TimeMLAspect timeMLAspect, TimeMLClass timeMLClass, TimeMLPolarity timeMLPolarity, TimeMLMood timeMLMood, TimeMLVerbForm timeMLVerbForm, TimeMLPoS timeMLPoS) {
		this(id, id, tokenSpan, timeMLTense, timeMLAspect, timeMLClass, timeMLPolarity, timeMLMood, timeMLVerbForm, timeMLPoS);
	}
	
	public Event(int id, int sourceId, TokenSpan tokenSpan, TimeMLTense timeMLTense, TimeMLAspect timeMLAspect, TimeMLClass timeMLClass, TimeMLPolarity timeMLPolarity, TimeMLMood timeMLMood, TimeMLVerbForm timeMLVerbForm, TimeMLPoS timeMLPoS) {
		this.id = "ei" + id;
		this.sourceId = "e" + sourceId;
		this.tokenSpan = tokenSpan;
		this.timeMLTense = timeMLTense;
		this.timeMLAspect = timeMLAspect;
		this.timeMLClass = timeMLClass;
		this.timeMLPolarity = timeMLPolarity;
		this.timeMLMood = timeMLMood;
		this.timeMLVerbForm = timeMLVerbForm;
		this.timeMLPoS = timeMLPoS;
	}
	
	public TLinkable.Type getTLinkableType() {
		return TLinkable.Type.EVENT;
	}
	
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	public String getSourceId() {
		return this.sourceId;
	}
	
	public Signal getSignal() {
		return this.signal;
	}
	
	public TimeMLTense getTimeMLTense() {
		return this.timeMLTense;
	}
	
	public TimeMLAspect getTimeMLAspect() {
		return this.timeMLAspect;
	}
	
	public TimeMLPolarity getTimeMLPolarity() {
		return this.timeMLPolarity;
	}
	
	public TimeMLClass getTimeMLClass() {
		return this.timeMLClass;
	}
	
	public TimeMLPoS getTimeMLPoS() {
		return this.timeMLPoS;
	}
	
	public TimeMLMood getTimeMLMood() {
		return this.timeMLMood;
	}
	
	public TimeMLVerbForm getTimeMLVerbForm() {
		return this.timeMLVerbForm;
	}
	
	public String getModality() {
		return this.modality;
	}
	
	public String getCardinality() {
		return this.cardinality;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		if (this.id != null)
			json.put("id", this.id);
		if (this.tokenSpan != null)
			json.put("tokenSpan", this.tokenSpan.toJSON());
		if (this.sourceId != null)
			json.put("sourceId", this.sourceId);
		if (this.signal != null)
			json.put("signalId", this.signal.getId()); 
		if (this.timeMLTense != null)
			json.put("timeMLTense", this.timeMLTense.toString());
		if (this.timeMLAspect != null)
			json.put("timeMLAspect", this.timeMLAspect.toString());
		if (this.timeMLPolarity != null)
			json.put("timeMLPolarity", this.timeMLPolarity.toString());
		if (this.timeMLClass != null)
			json.put("timeMLClass", this.timeMLClass.toString());
		if (this.timeMLPoS != null)
			json.put("timeMLPoS", this.timeMLPoS.toString());
		if (this.timeMLMood != null)
			json.put("timeMLMood", this.timeMLMood.toString());
		if (this.timeMLVerbForm != null)
			json.put("timeMLVerbForm", this.timeMLVerbForm.toString());
		if (this.modality != null)
			json.put("modality", this.modality);
		if (this.cardinality != null)
			json.put("cardinality", this.cardinality);
		
		return json;
	}
	
	public Element toXML() {
		Element element = new Element("event");
		
		if (this.id != null)
			element.setAttribute("eiid", this.id);
		if (this.tokenSpan != null) {
			element.setAttribute("offset", String.valueOf(this.tokenSpan.getStartTokenIndex() + 1));
			element.setAttribute("length", String.valueOf(this.tokenSpan.getEndTokenIndex() - this.tokenSpan.getStartTokenIndex()));
			element.setAttribute("string", this.tokenSpan.toString());
		}
		if (this.sourceId != null)
			element.setAttribute("id", this.sourceId);
		if (this.signal != null)
			element.setAttribute("signalId", this.signal.getId()); 
		if (this.timeMLTense != null)
			element.setAttribute("tense", this.timeMLTense.toString());
		if (this.timeMLAspect != null)
			element.setAttribute("aspect", this.timeMLAspect.toString());
		if (this.timeMLPolarity != null)
			element.setAttribute("polarity", this.timeMLPolarity.toString());
		if (this.timeMLClass != null)
			element.setAttribute("class", this.timeMLClass.toString());
		if (this.timeMLPoS != null)
			element.setAttribute("pos", this.timeMLPoS.toString());
		if (this.timeMLMood != null)
			element.setAttribute("mood", this.timeMLMood.toString());
		if (this.timeMLVerbForm != null)
			element.setAttribute("vform", this.timeMLVerbForm.toString());
		if (this.modality != null)
			element.setAttribute("modality", this.modality);
		if (this.cardinality != null)
			element.setAttribute("cardinality", this.cardinality);
		
		return element;
	}
	
	public Element toTimeML(Map<String, Pair<Element, TokenSpan>> sourceEvents) {
		Element eventElement = new Element("EVENT");
		Element instanceElement = new Element("MAKEINSTANCE");
		
		if (this.id != null)
			instanceElement.setAttribute("eiid", this.id);
		if (this.sourceId != null) {
			eventElement.setAttribute("eid", this.sourceId);
			instanceElement.setAttribute("eventID", this.sourceId);
		}
		if (this.signal != null)
			instanceElement.setAttribute("signalID", this.signal.getId()); 
		if (this.timeMLTense != null)
			instanceElement.setAttribute("tense", this.timeMLTense.toString());
		if (this.timeMLAspect != null)
			instanceElement.setAttribute("aspect", this.timeMLAspect.toString());
		if (this.timeMLPolarity != null)
			instanceElement.setAttribute("polarity", this.timeMLPolarity.toString());
		if (this.timeMLClass != null)
			eventElement.setAttribute("class", this.timeMLClass.toString());
		if (this.timeMLPoS != null)
			instanceElement.setAttribute("pos", this.timeMLPoS.toString());
		if (this.timeMLMood != null)
			instanceElement.setAttribute("mood", this.timeMLMood.toString());
		if (this.timeMLVerbForm != null)
			instanceElement.setAttribute("vform", this.timeMLVerbForm.toString());
		if (this.modality != null)
			instanceElement.setAttribute("modality", this.modality);
		if (this.cardinality != null)
			instanceElement.setAttribute("cardinality", this.cardinality);
		
		if (this.sourceId != null && this.tokenSpan != null && !sourceEvents.containsKey(this.sourceId)) {
			eventElement.setText(this.tokenSpan.toString());
			sourceEvents.put(this.sourceId, new Pair<Element, TokenSpan>(eventElement, this.tokenSpan));
		}
		
		return instanceElement;
	}
	
	public static Event fromJSON(JSONObject json, TempDocument document, int sentenceIndex) {
		Event event = new Event();
		
		if (json.containsKey("id"))
			event.id = json.getString("id");
		if (json.containsKey("tokenSpan"))
			event.tokenSpan = TokenSpan.fromJSON(json.getJSONObject("tokenSpan"), document, sentenceIndex);
		if (json.containsKey("sourceId"))
			event.sourceId = json.getString("sourceId");
		if (json.containsKey("signalId"))
			event.signal = document.getSignal(json.getString("signalId")); 
		if (json.containsKey("timeMLTense"))
			event.timeMLTense = TimeMLTense.valueOf(json.getString("timeMLTense"));
		if (json.containsKey("timeMLAspect"))
			event.timeMLAspect = TimeMLAspect.valueOf(json.getString("timeMLAspect"));
		if (json.containsKey("timeMLPolarity"))
			event.timeMLPolarity = TimeMLPolarity.valueOf(json.getString("timeMLPolarity"));
		if (json.containsKey("timeMLClass"))
			event.timeMLClass = TimeMLClass.valueOf(json.getString("timeMLClass"));
		if (json.containsKey("timeMLPoS"))
			event.timeMLPoS = TimeMLPoS.valueOf(json.getString("timeMLPoS"));
		if (json.containsKey("timeMLMood"))
			event.timeMLMood = TimeMLMood.valueOf(json.getString("timeMLMood"));
		if (json.containsKey("timeMLVerbForm"))
			event.timeMLVerbForm = TimeMLVerbForm.valueOf(json.getString("timeMLVerbForm"));
		if (json.containsKey("modality"))
			event.modality = json.getString("modality");
		if (json.containsKey("polarity"))
			event.cardinality = json.getString("cardinality");
		return event;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Event> fromXML(Element element, TempDocument document, int sentenceIndex) {
		String[] eiids = element.getAttributeValue("eiid").split(",");
		List<Event> events = new ArrayList<Event>();
		for (String eiid : eiids) {
			Event event = new Event();
			
			boolean hasOffset = false;
			boolean hasLength = false;
			boolean hasId = false;
			boolean hasSignalId = false;
			boolean hasTense = false;
			boolean hasAspect = false;
			boolean hasClass = false;
			boolean hasPoS = false;
			boolean hasMood = false;
			boolean hasVForm = false;
			boolean hasModality = false;
			boolean hasCardinality = false;
			
			List<Attribute> attributes = (List<Attribute>)element.getAttributes();
			for (Attribute attribute : attributes)
				if (attribute.getName().equals("offset"))
					hasOffset = true;
				else if (attribute.getName().equals("length"))
					hasLength = true;
				else if (attribute.getName().equals("id"))
					hasId = true;
				else if (attribute.getName().equals("signalId"))
					hasSignalId = true;
				else if (attribute.getName().equals("tense"))
					hasTense = true;
				else if (attribute.getName().equals("aspect"))
					hasAspect = true;
				else if (attribute.getName().equals("class"))
					hasClass = true;
				else if (attribute.getName().equals("pos"))
					hasPoS = true;
				else if (attribute.getName().equals("mood"))
					hasMood = true;
				else if (attribute.getName().equals("vform"))
					hasVForm = true;
				else if (attribute.getName().equals("modality"))
					hasModality = true;
				else if (attribute.getName().equals("cardinality"))
					hasCardinality = true;
			
			event.id = eiid;
			
			if (hasOffset) {
				int startTokenIndex = Integer.parseInt(element.getAttributeValue("offset")) - 1;
				int endTokenIndex = startTokenIndex + ((hasLength) ? Integer.parseInt(element.getAttributeValue("length")) : 1);
				event.tokenSpan = new TokenSpan(document, 
												sentenceIndex, 
												startTokenIndex, 
												endTokenIndex);
			}
			
			if (hasId)
				event.sourceId = element.getAttributeValue("id");
			if (hasSignalId)
				event.signal = document.getSignal(element.getAttributeValue("signalId"));
			if (hasTense)
				event.timeMLTense = TimeMLTense.valueOf(element.getAttributeValue("tense"));
			if (hasAspect)
				event.timeMLAspect = TimeMLAspect.valueOf(element.getAttributeValue("aspect"));
			if (hasClass)
				event.timeMLClass = TimeMLClass.valueOf(element.getAttributeValue("class"));
			if (hasPoS)
				event.timeMLPoS = TimeMLPoS.valueOf(element.getAttributeValue("pos"));
			if (hasMood)
				event.timeMLMood = TimeMLMood.valueOf(element.getAttributeValue("mood"));
			if (hasVForm)
				event.timeMLVerbForm = TimeMLVerbForm.valueOf(element.getAttributeValue("vform"));
			if (hasModality)
				event.modality = element.getAttributeValue("modality");
			if (hasCardinality)
				event.cardinality = element.getAttributeValue("cardinality");
			
			events.add(event);
		}
		return events;
	}
	
	public static Event fromTimeML(Element instanceElement, TempDocument document, Map<String, Pair<Element, TokenSpan>> sourceEvents) {
		Event event = new Event();
		String eiid = instanceElement.getAttributeValue("eiid");
		String eventID = instanceElement.getAttributeValue("eventID");
		String signalID = instanceElement.getAttributeValue("signalID");
		String tense = instanceElement.getAttributeValue("tense");
		String aspect = instanceElement.getAttributeValue("aspect");
		String polarity = instanceElement.getAttributeValue("polarity");
		String pos = instanceElement.getAttributeValue("pos");
		String mood = instanceElement.getAttributeValue("mood");
		String vform = instanceElement.getAttributeValue("vform");
		String modality = instanceElement.getAttributeValue("modality");
		String cardinality = instanceElement.getAttributeValue("cardinality");
		
		if (eiid != null)
			event.id = eiid;
		
		if (eventID != null) {
			if (!sourceEvents.containsKey(eventID))
				return null;
			Pair<Element, TokenSpan> sourceEvent = sourceEvents.get(eventID);
			String timeMLClass = sourceEvent.getFirst().getAttributeValue("class");
			event.sourceId = eventID;
			event.timeMLClass = TimeMLClass.valueOf(timeMLClass);
			event.tokenSpan = sourceEvent.getSecond();
		}
		
		if (signalID != null) {
			event.signal = document.getSignal(signalID);
			if (event.signal == null)
				return null;
		}
		
		if (tense != null)
			event.timeMLTense = TimeMLTense.valueOf(tense);
		if (aspect != null)
			event.timeMLAspect = TimeMLAspect.valueOf(aspect);
		if (polarity != null)
			event.timeMLPolarity = TimeMLPolarity.valueOf(polarity);
		if (pos != null)
			event.timeMLPoS = TimeMLPoS.valueOf(pos);
		if (mood != null)
			event.timeMLMood = TimeMLMood.valueOf(mood);
		if (vform != null)
			event.timeMLVerbForm = TimeMLVerbForm.valueOf(vform);
		if (modality != null)
			event.modality = modality;
		if (cardinality != null)
			event.cardinality = cardinality;
		
		return event;
	}
}
