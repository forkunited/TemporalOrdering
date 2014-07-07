package temp.data.annotation.timeml;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.jdom.Attribute;
import org.jdom.Element;

import temp.data.annotation.TempDocument;
import ark.data.annotation.nlp.TokenSpan;
import ark.util.Pair;

/**
 * Time represents a TimeML Timex (temporal expression).
 * 
 * See http://timeml.org/site/index.html for details.
 * 
 * @author Bill McDowell
 * 
 */
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
	
	public enum ParseMode {
		// Determines whether parsing from JSON, TimeML, XML looks at referenced times
		// in source document when parsing
		NO_REFERENCED_TIMES,
		ALL
	}

	private String id;
	private TokenSpan tokenSpan;
	private TimeMLType timeMLType;
	private Time startTime;
	private Time endTime;
	private String quant;
	private String freq;
	private NormalizedTimeValue value;
	private TimeMLDocumentFunction timeMLDocumentFunction = TimeMLDocumentFunction.NONE;
	private boolean temporalFunction;
	private Time anchorTime;
	private Time valueFromFunction;
	private TimeMLMod timeMLMod;
	
	public Time() {
		
	}
	
	public Time(String id, String value, TokenSpan tokenSpan, TimeMLType timeMLType) {
		this.id  = id;
		this.value = new NormalizedTimeValue(value);
		this.tokenSpan = tokenSpan;
		this.timeMLType = timeMLType;
		this.timeMLDocumentFunction = TimeMLDocumentFunction.NONE;
	}
	
	public Time(String id, String value, TimeMLType timeMLType, TimeMLDocumentFunction timeMLDocumentFunction) {
		this.id = id;
		this.value = new NormalizedTimeValue(value);
		this.timeMLType = timeMLType;
		this.timeMLDocumentFunction = timeMLDocumentFunction;
	}
	
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
	
	/**
	 * @return a NormalizedTimeValue representing the 
	 * grounded time-interval referenced by the Time
	 * expression
	 */
	public NormalizedTimeValue getValue() {
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
	
	/**
	 * @param time
	 * @return TLink relation type between this Time and the given
	 * time according to their grounded time intervals.  
	 */
	public TLink.TimeMLRelType getRelationToTime(Time time) {
		if (this.timeMLType != Time.TimeMLType.DATE && this.timeMLType != Time.TimeMLType.TIME)
			return TLink.TimeMLRelType.VAGUE;
		if (time.timeMLType != Time.TimeMLType.DATE && this.timeMLType != Time.TimeMLType.TIME)
			return TLink.TimeMLRelType.VAGUE;
		
		Time creationTime = null;
		if (time.getTokenSpan().getDocument().getName().equals(this.getTokenSpan().getDocument().getName())) {
			// FIXME: This only gets document creation time if they're in the same document...
			// But it's also possible to draw inferences if in separate documents
			creationTime = ((TempDocument)time.getTokenSpan().getDocument()).getCreationTime();
		}
		
		if (this.value.getReference() != NormalizedTimeValue.Reference.NONE 
				|| time.value.getReference() != NormalizedTimeValue.Reference.NONE) {
			if (creationTime == null)
				return TLink.TimeMLRelType.VAGUE;
			// Relate this to creation and time to creation based on past
			// and future references
			int thisCt = 0, timeCt = 0; 
			
			if (this.value.getReference() != NormalizedTimeValue.Reference.NONE) {
				if (this.value.getReference() == NormalizedTimeValue.Reference.FUTURE)
					thisCt = 1;
				else if (this.value.getReference() == NormalizedTimeValue.Reference.PAST)
					thisCt = -1;
				else
					return TLink.TimeMLRelType.VAGUE;
			} else {
				TLink.TimeMLRelType thisCtRelation = getRelationToTime(creationTime);
				if (thisCtRelation == TLink.TimeMLRelType.VAGUE)
					return TLink.TimeMLRelType.VAGUE;
				else if (thisCtRelation == TLink.TimeMLRelType.BEFORE)
					thisCt = -1;
				else if (thisCtRelation == TLink.TimeMLRelType.AFTER)
					thisCt = 1;
				else
					return TLink.TimeMLRelType.VAGUE;
			}
			
			if (time.value.getReference() != NormalizedTimeValue.Reference.NONE) {
				if (time.value.getReference() == NormalizedTimeValue.Reference.FUTURE)
					timeCt = 1;
				else if (time.value.getReference() == NormalizedTimeValue.Reference.PAST)
					timeCt = -1;
				else
					return TLink.TimeMLRelType.VAGUE;
			} else {
				TLink.TimeMLRelType timeCtRelation = time.getRelationToTime(creationTime);
				if (timeCtRelation == TLink.TimeMLRelType.VAGUE)
					return TLink.TimeMLRelType.VAGUE;
				else if (timeCtRelation == TLink.TimeMLRelType.BEFORE)
					timeCt = -1;
				else if (timeCtRelation == TLink.TimeMLRelType.AFTER)
					timeCt = 1;
				else
					return TLink.TimeMLRelType.VAGUE;
			}
			
			if (thisCt < timeCt)
				return TLink.TimeMLRelType.BEFORE;
			else if (timeCt < thisCt)
				return TLink.TimeMLRelType.AFTER;
			else
				return TLink.TimeMLRelType.VAGUE;
		}
		
		
		Pair<Calendar, Calendar> thisInterval = this.value.getRange();
		Pair<Calendar, Calendar> timeInterval = time.value.getRange();
		
		if (thisInterval == null || timeInterval == null)
			return TLink.TimeMLRelType.VAGUE;
		
		int startStart = thisInterval.getFirst().compareTo(timeInterval.getFirst());
		int startEnd = thisInterval.getFirst().compareTo(timeInterval.getSecond());
		int endStart = thisInterval.getSecond().compareTo(timeInterval.getFirst());
		int endEnd = thisInterval.getSecond().compareTo(timeInterval.getSecond());
		
		if (startStart == 0 && endEnd == 0)
			return TLink.TimeMLRelType.SIMULTANEOUS;
		else if (endStart <= 0)
			return TLink.TimeMLRelType.BEFORE;
		else if (startEnd >= 0)
			return TLink.TimeMLRelType.AFTER;
		else if (startStart < 0 && endEnd > 0)
			return TLink.TimeMLRelType.INCLUDES;
		else if (startStart > 0 && endEnd < 0)
			return TLink.TimeMLRelType.IS_INCLUDED;
		else if (startStart > 0 && startEnd < 0 && endEnd > 0)
			return TLink.TimeMLRelType.OVERLAPPED_BY;
		else if (startStart < 0 && endStart > 0 && endEnd < 0)
			return TLink.TimeMLRelType.OVERLAPS;
		else
			return TLink.TimeMLRelType.VAGUE;
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
			json.put("value", this.value.toString());
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
			element.setAttribute("value", this.value.toString());
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
	
	public Element toTimeML() {
		Element element = new Element("TIMEX3");
		
		if (this.id != null)
			element.setAttribute("tid", this.id);
		if (this.timeMLType != null)
			element.setAttribute("type", this.timeMLType.toString());
		if (this.timeMLDocumentFunction != null)
			element.setAttribute("functionInDocument", this.timeMLDocumentFunction.toString());
		if (this.startTime != null)
			element.setAttribute("startPoint", this.startTime.getId());
		if (this.endTime != null)
			element.setAttribute("endPoint", this.endTime.getId());
		if (this.quant != null)
			element.setAttribute("quant", this.quant);
		if (this.freq != null)
			element.setAttribute("freq", this.freq);
		
		element.setAttribute("temporalFunction", String.valueOf(this.temporalFunction));
		
		if (this.value != null)
			element.setAttribute("value", this.value.toString());
		else if (this.valueFromFunction != null)
			element.setAttribute("valueFromFunction", this.valueFromFunction.getId());
		
		if (this.timeMLMod != null)
			element.setAttribute("mod", this.timeMLMod.toString());
		if (this.anchorTime != null)
			element.setAttribute("anchorTimeID", this.anchorTime.getId());
		
		if (this.tokenSpan != null)
			element.setText(this.tokenSpan.toString());
		
		return element;
	}
	
	public static Time fromJSON(JSONObject json, TempDocument document, int sentenceIndex, ParseMode parseMode) {
		Time time = new Time();
		
		if (json.containsKey("id"))
			time.id = json.getString("id");
		if (json.containsKey("tokenSpan"))
			time.tokenSpan = TokenSpan.fromJSON(json.getJSONObject("tokenSpan"), document, sentenceIndex);
		if (json.containsKey("timeMLType"))
			time.timeMLType = TimeMLType.valueOf(json.getString("timeMLType"));
		if (json.containsKey("startTimeId") && json.getString("startTimeId").length() > 0  && parseMode != ParseMode.NO_REFERENCED_TIMES) {
			Time startTime = document.getTime(json.getString("startTimeId"));
			if (startTime == null)
				return null;
			time.startTime = startTime; 
		}
		if (json.containsKey("endTimeId") && json.getString("endTimeId").length() > 0 && parseMode != ParseMode.NO_REFERENCED_TIMES) {
			Time endTime = document.getTime(json.getString("endTimeId"));
			if (endTime == null)
				return null;
			time.endTime = 	endTime;
		}
		if (json.containsKey("value"))
			time.value = new NormalizedTimeValue(json.getString("value"));
		if (json.containsKey("freq"))
			time.freq = json.getString("freq");
		if (json.containsKey("quant"))
			time.quant = json.getString("quant");
		if (json.containsKey("timeMLDocumentFunction"))
			time.timeMLDocumentFunction = TimeMLDocumentFunction.valueOf(json.getString("timeMLDocumentFunction"));
		if (json.containsKey("temporalFunction"))
			time.temporalFunction = json.getBoolean("temporalFunction");
		if (json.containsKey("anchorTimeId") && json.getString("anchorTimeId").length() > 0 && parseMode != ParseMode.NO_REFERENCED_TIMES) {
			Time anchorTime = document.getTime(json.getString("anchorTimeId"));
			if (anchorTime == null)
				return null;
			time.anchorTime = anchorTime;
		}
		if (json.containsKey("valueFromFunctionId") && json.getString("valueFromFunctionId").length() > 0 && parseMode != ParseMode.NO_REFERENCED_TIMES) {
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
	public static Time fromXML(Element element, TempDocument document, int sentenceIndex, ParseMode parseMode) {
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
			else if (attribute.getName().equals("starttid") && parseMode != ParseMode.NO_REFERENCED_TIMES)
				hasStartTimeId = true;
			else if (attribute.getName().equals("endtid") && parseMode != ParseMode.NO_REFERENCED_TIMES)
				hasEndTimeId = true;
			else if (attribute.getName().equals("quant"))
				hasQuant = true;
			else if (attribute.getName().equals("docFunction"))
				hasTimeMLDocumentFunction = true;
			else if (attribute.getName().equals("temporalFunction"))
				hasTemporalFunction = true;
			else if (attribute.getName().equals("anchortid") && parseMode != ParseMode.NO_REFERENCED_TIMES)
				hasAnchorTimeId = true;
			else if (attribute.getName().equals("valueFromFunctionTid") && parseMode != ParseMode.NO_REFERENCED_TIMES)
				hasValueFromFunctionId = true;
			else if (attribute.getName().equals("freq"))
				hasFreq = true;
			else if (attribute.getName().equals("value"))
				hasValue = true;
			else if (attribute.getName().equals("mod") && attribute.getValue().length() > 0)
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
			time.value = new NormalizedTimeValue(element.getAttributeValue("value"));
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
	
	public static Time fromTimeML(Element element, TempDocument document, TokenSpan tokenSpan, ParseMode parseMode) {
		Time time = new Time();
		
		String id = element.getAttributeValue("tid");
		String type = element.getAttributeValue("type");
		String functionInDocument = element.getAttributeValue("functionInDocument");
		String startPoint = element.getAttributeValue("startPoint");
		String endPoint = element.getAttributeValue("endPoint");
		String quant = element.getAttributeValue("quant");
		String freq = element.getAttributeValue("freq");
		String temporalFunction = element.getAttributeValue("temporalFunction");
		String value = element.getAttributeValue("value");
		String valueFromFunction = element.getAttributeValue("valueFromFunction");
		String mod = element.getAttributeValue("mod");
		String anchorTimeID = element.getAttributeValue("anchorTimeID");
		
		if (id != null)
			time.id = id;
		if (type != null)
			time.timeMLType = TimeMLType.valueOf(type);
		if (functionInDocument != null)
			time.timeMLDocumentFunction = TimeMLDocumentFunction.valueOf(functionInDocument);
		if (startPoint != null && parseMode != ParseMode.NO_REFERENCED_TIMES) {
			time.startTime = document.getTime(startPoint);
			if (time.startTime == null)
				return null;
		}
		if (endPoint != null && parseMode != ParseMode.NO_REFERENCED_TIMES) {
			time.endTime = document.getTime(endPoint);
			if (time.endTime == null)
				return null;
		}
		if (quant != null)
			time.quant = quant;
		if (freq != null)
			time.freq = freq;
		if (temporalFunction != null)
			time.temporalFunction = Boolean.valueOf(temporalFunction);
		
		if (value != null)
			time.value = new NormalizedTimeValue(value);
		else if (valueFromFunction != null && parseMode != ParseMode.NO_REFERENCED_TIMES) {
			time.valueFromFunction = document.getTime(valueFromFunction);
			if (time.valueFromFunction == null)
				return null;
		}
		
		if (mod != null)
			time.timeMLMod = TimeMLMod.valueOf(mod);
		if (anchorTimeID != null && parseMode != ParseMode.NO_REFERENCED_TIMES) {
			time.anchorTime = document.getTime(anchorTimeID);
			if (time.anchorTime == null)
				return null;
		}
		
		time.tokenSpan = tokenSpan;
		
		return time;
	}
	
	public static Time fromDate(Date date, 
								int id, 
								TimeMLDocumentFunction documentFunction, 
								TempDocument document, 
								TokenSpan tokenSpan) {
		Element timeElement = new Element("timex");
		
		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD");
		String value = format.format(date);
		int sentenceIndex = -1;
		int offset = -1;
		int length = 0;
		if (tokenSpan != null) {
			sentenceIndex = tokenSpan.getSentenceIndex();
			offset = tokenSpan.getStartTokenIndex();
			length = tokenSpan.getEndTokenIndex() - tokenSpan.getStartTokenIndex();
		}
			
		timeElement.setAttribute("offset", String.valueOf(offset));
		timeElement.setAttribute("length", String.valueOf(length));
		
		timeElement.setAttribute("id", "t" + id);
		timeElement.setAttribute("value", value);
		timeElement.setAttribute("type", "DATE");
		timeElement.setAttribute("docFunction", documentFunction.toString());
		timeElement.setAttribute("temporalFunction", "false");
		
		return fromXML(timeElement, document, sentenceIndex, ParseMode.ALL);
	}
}
