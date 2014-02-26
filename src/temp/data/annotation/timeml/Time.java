package temp.data.annotation.timeml;

import java.util.List;

import net.sf.json.JSONObject;

import org.jdom.Attribute;
import org.jdom.Element;

import temp.data.annotation.TempDocument;
import temp.data.annotation.nlp.TokenSpan;

public class Time implements TLinkable {
	public enum TimeMLType {
		DATE,
		TIME,
		DURATION,
		SET
	}
	
	public enum TimeMLDocumentFunction {
		CREATION_TIME,
		EXPIRATION_TIME,
		MODIFICATION_TIME,
		PUBLICATION_TIME,
		RELEASE_TIME,
		RECEPTION_TIME,
		NONE
	}
	
	/* NOTE: These are lower case in the spec... It's messy.  Oh well. :( */
	public enum TimeMLValue {
		Duration,
		Date,
		Time,
		WeekDate,
		WeekTime,
		Season,
		PartOfYear,
		PaPrFu
	}
	
	public enum TimeMLMod {
		BEFORE,
		AFTER,
		ON_OR_BEFORE,
		ON_OR_AFTER,
		LESS_THAN,
		MORE_THAN,
		EQUAL_OR_LESS,
		EQUAL_OR_MORE,
		START,
		MID,
		END,
		APPROX
	}

	private String id;
	private TokenSpan tokenSpan;
	private TimeMLType timeMLType;
	private Time startTime;
	private Time endTime;
	private String quant;
	private String duration; // FIXME: Duration format
	private TimeMLDocumentFunction timeMLDocumentFunction;
	private boolean temporalFunction;
	private Time anchorTime;
	private Time valueFromFunction;
	private TimeMLValue timeMLValue;
	private TimeMLMod timeMLMod;
	
	public TLinkable.Type getTLinkableType() {
		return TLinkable.Type.TIME;
	}
	
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	public TimeMLType getTimeMLType() {
		return this.timeMLType;
	}
	
	public Time getStartTime() {
		return this.startTime;
	}
	
	public Time getEndTime() {
		return this.endTime;
	}
	
	public String getQuant() {
		return this.quant;
	}
	
	public String getDuration() {
		return this.duration;
	}
	
	public TimeMLDocumentFunction getTimeMLDocumentFunction() {
		return this.timeMLDocumentFunction;
	}
	
	public boolean getTemporalFunction() {
		return this.temporalFunction;
	}
	
	public Time getAnchorTime() {
		return this.anchorTime;
	}
	
	public Time getValueFromFunction() {
		return this.valueFromFunction;
	}
	
	public TimeMLValue getTimeMLValue() {
		return this.timeMLValue;
	}
	
	public TimeMLMod getTimeMLMod() {
		return this.timeMLMod;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		if (this.id != null)
			json.put("id", this.id);
		if (this.tokenSpan != null)
			json.put("tokenSpan", this.tokenSpan.toJSON());
		if (this.timeMLType != null)
			json.put("timeMLType", this.timeMLType);
		if (this.startTime != null)
			json.put("startTimeId", this.startTime.getId());
		if (this.endTime != null)
			json.put("endTimeId", this.endTime.getId());		
		if (this.quant != null)
			json.put("quant", this.quant);
		if (this.duration != null)
			json.put("duration", this.duration);
		if (this.timeMLDocumentFunction != null)
			json.put("timeMLDocumentFunction", this.timeMLDocumentFunction);
		
		json.put("temporalFunction", this.temporalFunction);
		
		if (this.anchorTime != null)
			json.put("anchorTimeId", this.anchorTime.getId());
		if (this.valueFromFunction != null)
			json.put("valueFromFunctionId", this.valueFromFunction.getId());
		if (this.timeMLValue != null)
			json.put("timeMLValue", this.timeMLValue);
		if (this.timeMLMod != null)
			json.put("timeMLMod", this.timeMLMod);

		return json;
	}
	
	public Element toXML() {
		Element element = new Element("timex");
		
		if (this.id != null)
			element.setAttribute("tid", this.id);
		if (this.tokenSpan != null) {
			element.setAttribute("offset", String.valueOf(this.tokenSpan.getStartTokenIndex() + 1));
			element.setAttribute("length", String.valueOf(this.tokenSpan.getEndTokenIndex() - this.tokenSpan.getStartTokenIndex()));
			element.setAttribute("string", this.tokenSpan.toString());
		}
		
		if (this.timeMLType != null)
			element.setAttribute("type", this.timeMLType.toString());
		if (this.startTime != null)
			element.setAttribute("starttid", this.startTime.getId());
		if (this.endTime != null)
			element.setAttribute("endtid", this.endTime.getId());
		if (this.quant != null)
			element.setAttribute("quant", this.quant);
		if (this.duration != null)
			element.setAttribute("duration", this.duration);
		if (this.timeMLDocumentFunction != null)
			element.setAttribute("docFunction", this.timeMLDocumentFunction.toString());
		
		element.setAttribute("temporalFunction", String.valueOf(this.temporalFunction));
		
		if (this.anchorTime != null)
			element.setAttribute("anchortid", this.anchorTime.getId());
		if (this.valueFromFunction != null)
			element.setAttribute("valueFromFunctionTid", this.valueFromFunction.getId());
		if (this.timeMLValue != null)
			element.setAttribute("value", this.timeMLValue.toString());
		if (this.timeMLMod != null)
			element.setAttribute("mod", this.timeMLMod.toString());
		
		return element;
	}
	
	public static Time fromJSON(JSONObject json, TempDocument document, int sentenceIndex) {
		Time time = new Time();
		
		if (json.containsKey("id"))
			time.id = json.getString("id");
		if (json.containsKey("tokenSpan"))
			time.tokenSpan = TokenSpan.fromJSON(json.getJSONObject("tokenSpan"), document, sentenceIndex);
		if (json.containsKey("timeMLType"))
			time.timeMLType = TimeMLType.valueOf(json.getString("timeMLType"));
		if (json.containsKey("startTimeId"))
			time.startTime = document.getTime(json.getString("startTimeId"));
		if (json.containsKey("endTimeId"))
			time.endTime = document.getTime(json.getString("endTimeId"));	
		if (json.containsKey("quant"))
			time.quant = json.getString("quant");
		if (json.containsKey("duration"))
			time.duration = json.getString("duration");
		if (json.containsKey("timeMLDocumentFunction"))
			time.timeMLDocumentFunction = TimeMLDocumentFunction.valueOf(json.getString("timeMLDocumentFunction"));
		if (json.containsKey("temporalFunction"))
			time.temporalFunction = json.getBoolean("temporalFunction");
		if (json.containsKey("anchorTimeId"))
			time.anchorTime = document.getTime(json.getString("anchorTimeId"));
		if (json.containsKey("valueFromFunctionId"))
			time.valueFromFunction = document.getTime(json.getString("valueFromFunctionId"));
		if (json.containsKey("timeMLValue"))
			time.timeMLValue = TimeMLValue.valueOf(json.getString("timeMLValue"));
		if (json.containsKey("timeMLMod"))
			time.timeMLMod = TimeMLMod.valueOf(json.getString("timeMLMod"));
		
		return time;
	}
	
	@SuppressWarnings("unchecked")
	public static Time fromXML(Element element, TempDocument document, int sentenceIndex) {
		Time time = new Time();
			
		boolean hasOffset = false;
		boolean hasLength = false;
		boolean hasId = false;
		boolean hasTimeMLType = false;
		boolean hasStartTimeId = false;
		boolean hasEndTimeId = false;
		boolean hasQuant = false;
		boolean hasDuration = false;
		boolean hasTimeMLDocumentFunction = false;
		boolean hasTemporalFunction = false;
		boolean hasAnchorTimeId = false;
		boolean hasValueFromFunctionId = false;
		boolean hasTimeMLValue = false;
		boolean hasTimeMLMod = false;
		
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("offset"))
				hasOffset = true;
			else if (attribute.getName().equals("length"))
				hasLength = true;
			else if (attribute.getName().equals("tid"))
				hasId = true;
			else if (attribute.getName().equals("type"))
				hasTimeMLType = true;
			else if (attribute.getName().equals("starttid"))
				hasStartTimeId = true;
			else if (attribute.getName().equals("endtid"))
				hasEndTimeId = true;
			else if (attribute.getName().equals("quant"))
				hasQuant = true;
			else if (attribute.getName().equals("duration"))
				hasDuration = true;
			else if (attribute.getName().equals("docFunction"))
				hasTimeMLDocumentFunction = true;
			else if (attribute.getName().equals("temporalFunction"))
				hasTemporalFunction = true;
			else if (attribute.getName().equals("anchortid"))
				hasAnchorTimeId = true;
			else if (attribute.getName().equals("valueFromFunctionTid"))
				hasValueFromFunctionId = true;
			else if (attribute.getName().equals("value"))
				hasTimeMLValue = true;
			else if (attribute.getName().equals("mod"))
				hasTimeMLMod = true;
		}

		if (hasOffset) {
			int startTokenIndex = Integer.parseInt(element.getAttributeValue("offset")) - 1;
			int endTokenIndex = startTokenIndex + ((hasLength) ? Integer.parseInt(element.getAttributeValue("length")) : 1);
			time.tokenSpan = new TokenSpan(document, 
											sentenceIndex, 
											startTokenIndex, 
											endTokenIndex);
		}
		
		if (hasId)
			time.id = element.getAttributeValue("tid");
		
		if (hasTimeMLType)
			time.timeMLType = TimeMLType.valueOf(element.getAttributeValue("type"));
		if (hasStartTimeId)
			time.startTime = document.getTime(element.getAttributeValue("starttid"));
		if (hasEndTimeId)
			time.endTime = document.getTime(element.getAttributeValue("endttid"));
		if (hasQuant)
			time.quant = element.getAttributeValue("quant");
		if (hasDuration)
			time.duration = element.getAttributeValue("duration");
		if (hasTimeMLDocumentFunction && element.getAttributeValue("docFunction").length() > 0)
			time.timeMLDocumentFunction = TimeMLDocumentFunction.valueOf(element.getAttributeValue("docFunction"));
		if (hasTemporalFunction && element.getAttributeValue("temporalFunction").length() > 0)
			time.temporalFunction = Boolean.parseBoolean(element.getAttributeValue("temporalFunction"));
		if (hasAnchorTimeId)
			time.anchorTime = document.getTime(element.getAttributeValue("anchortid"));
		if (hasValueFromFunctionId)
			time.valueFromFunction = document.getTime(element.getAttributeValue("valueFromFunctionTid"));
		if (hasTimeMLValue)
			time.timeMLValue = TimeMLValue.valueOf(element.getAttributeValue("value"));
		if (hasTimeMLMod)
			time.timeMLMod = TimeMLMod.valueOf(element.getAttributeValue("mod"));
		
		return time;
	}
}
