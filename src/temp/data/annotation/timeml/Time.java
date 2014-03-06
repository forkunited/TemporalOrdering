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
	private String freq;
	private String value;
	private TimeMLDocumentFunction timeMLDocumentFunction = TimeMLDocumentFunction.NONE;
	private boolean temporalFunction;
	private Time anchorTime;
	private Time valueFromFunction;
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
	
	public String getFreq() {
		return this.freq;
	}
	
	public String getValue() {
		return this.value;
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
		if (this.freq != null)
			json.put("freq", this.freq);
		if (this.value != null)
			json.put("value", this.value);
		if (this.quant != null)
			json.put("quant", this.quant);
		if (this.timeMLDocumentFunction != null)
			json.put("timeMLDocumentFunction", this.timeMLDocumentFunction);
		
		json.put("temporalFunction", this.temporalFunction);
		
		if (this.anchorTime != null)
			json.put("anchorTimeId", this.anchorTime.getId());
		if (this.valueFromFunction != null)
			json.put("valueFromFunctionId", this.valueFromFunction.getId());
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
		if (this.freq != null)
			element.setAttribute("freq", this.freq);
		if (this.value != null)
			element.setAttribute("value", this.value);
		if (this.quant != null)
			element.setAttribute("quant", this.quant);
		if (this.timeMLDocumentFunction != null)
			element.setAttribute("docFunction", this.timeMLDocumentFunction.toString());
		
		element.setAttribute("temporalFunction", String.valueOf(this.temporalFunction));
		
		if (this.anchorTime != null)
			element.setAttribute("anchortid", this.anchorTime.getId());
		if (this.valueFromFunction != null)
			element.setAttribute("valueFromFunctionTid", this.valueFromFunction.getId());
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
		if (json.containsKey("startTimeId") && json.getString("startTimeId").length() > 0) {
			Time startTime = document.getTime(json.getString("startTimeId"));
			if (startTime == null)
				return null;
			time.startTime = startTime; 
		}
		if (json.containsKey("endTimeId") && json.getString("endTimeId").length() > 0) {
			Time endTime = document.getTime(json.getString("endTimeId"));
			if (endTime == null)
				return null;
			time.endTime = 	endTime;
		}
		if (json.containsKey("value"))
			time.value = json.getString("value");
		if (json.containsKey("freq"))
			time.freq = json.getString("freq");
		if (json.containsKey("quant"))
			time.quant = json.getString("quant");
		if (json.containsKey("timeMLDocumentFunction"))
			time.timeMLDocumentFunction = TimeMLDocumentFunction.valueOf(json.getString("timeMLDocumentFunction"));
		if (json.containsKey("temporalFunction"))
			time.temporalFunction = json.getBoolean("temporalFunction");
		if (json.containsKey("anchorTimeId") && json.getString("anchorTimeId").length() > 0) {
			Time anchorTime = document.getTime(json.getString("anchorTimeId"));
			if (anchorTime == null)
				return null;
			time.anchorTime = anchorTime;
		}
		if (json.containsKey("valueFromFunctionId") && json.getString("valueFromFunctionId").length() > 0) {
			Time valueFromFunction = document.getTime(json.getString("valueFromFunctionId"));
			if (valueFromFunction == null)
				return null;
			time.valueFromFunction = valueFromFunction;
		}
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
		boolean hasFreq = false;
		boolean hasValue = false;
		boolean hasQuant = false;
		boolean hasTimeMLDocumentFunction = false;
		boolean hasTemporalFunction = false;
		boolean hasAnchorTimeId = false;
		boolean hasValueFromFunctionId = false;
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
			else if (attribute.getName().equals("docFunction"))
				hasTimeMLDocumentFunction = true;
			else if (attribute.getName().equals("temporalFunction"))
				hasTemporalFunction = true;
			else if (attribute.getName().equals("anchortid"))
				hasAnchorTimeId = true;
			else if (attribute.getName().equals("valueFromFunctionTid"))
				hasValueFromFunctionId = true;
			else if (attribute.getName().equals("freq"))
				hasFreq = true;
			else if (attribute.getName().equals("value"))
				hasValue = true;
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
		if (hasStartTimeId) {
			String starttid = element.getAttributeValue("starttid");
			if (starttid.length() > 0) {
				Time startTime = document.getTime(starttid);
				if (startTime == null)
					return null;
				time.startTime = startTime;
			}
		}
		if (hasEndTimeId) {
			String endtid = element.getAttributeValue("endttid");
			if (endtid.length() > 0) {
				Time endTime = document.getTime(endtid);
				if (endTime == null)
					return null;
				time.endTime = endTime;
			}
		}
		if (hasFreq)
			time.freq = element.getAttributeValue("freq");
		if (hasValue)
			time.value = element.getAttributeValue("value");
		if (hasQuant)
			time.quant = element.getAttributeValue("quant");
		if (hasTimeMLDocumentFunction && element.getAttributeValue("docFunction").length() > 0)
			time.timeMLDocumentFunction = TimeMLDocumentFunction.valueOf(element.getAttributeValue("docFunction"));
		if (hasTemporalFunction && element.getAttributeValue("temporalFunction").length() > 0)
			time.temporalFunction = Boolean.parseBoolean(element.getAttributeValue("temporalFunction"));
		if (hasAnchorTimeId) {
			String anchortid = element.getAttributeValue("anchortid");
			if (anchortid.length() > 0) {
				Time anchorTime = document.getTime(anchortid);
				if (anchorTime == null)
					return null;
				time.anchorTime = anchorTime;
			}
		}
		if (hasValueFromFunctionId) {
			String valueFromFunctionTid = element.getAttributeValue("valueFromFunctionTid");
			if (valueFromFunctionTid.length() > 0) {
				Time valueFromFunction = document.getTime(valueFromFunctionTid);
				if (valueFromFunction == null)
					return null;
				time.valueFromFunction = valueFromFunction;
			}
		}
		if (hasTimeMLMod)
			time.timeMLMod = TimeMLMod.valueOf(element.getAttributeValue("mod"));
		
		return time;
	}
}
