package temp.data.annotation.timeml;

import java.util.List;

import net.sf.json.JSONObject;

import org.jdom.Attribute;
import org.jdom.Element;

import temp.data.annotation.TempDocument;

public class TLink {
	public enum TimeMLRelType {
		BEFORE,
		AFTER,
		INCLUDES,
		IS_INCLUDED,
		DURING,
		SIMULTANEOUS,
		IAFTER,
		IBEFORE,
		IDENTITY,
		BEGINS,
		ENDS,
		BEGUN_BY,
		ENDED_BY,
		DURING_INV,
		VAGUE
	}
	
	private String id;
	private String origin;
	private TLinkable source;
	private TLinkable target;
	private Signal signal;
	private TimeMLRelType timeMLRelType;
	private String syntax;
	
	public TLink() {
		
	}
	
	public TLink(String id, TLinkable source, TLinkable target, TimeMLRelType timeMLRelType) {
		this.id = id;
		this.source = source;
		this.target = target;
		this.timeMLRelType = timeMLRelType;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getOrigin() {
		return this.origin;
	}
	
	public TLinkable getSource() {
		return this.source;
	}
	
	public TLinkable getTarget() {
		return this.target;
	}
	
	public Signal getSignal() {
		return this.signal;
	}
	
	public TimeMLRelType getTimeMLRelType() {
		return this.timeMLRelType;
	}
	
	public String getSyntax() {
		return this.syntax;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		if (this.id != null)
			json.put("id", this.id);
		if (this.origin != null)
			json.put("origin", origin);
		if (this.source != null) {
			json.put("sourceId", this.source.getId());
			json.put("sourceType", this.source.getTLinkableType().toString());
		}
		if (this.target != null) {
			json.put("targetId", this.target.getId());
			json.put("targetType", this.target.getTLinkableType().toString());
		}
		if (this.signal != null)
			json.put("signalId", this.signal.getId());
		if (this.timeMLRelType != null)
			json.put("timeMLRelType", this.timeMLRelType);
		if (this.syntax != null)
			json.put("syntax", this.syntax);

		return json;
	}
	
	public Element toXML() {
		Element element = new Element("tlink");
		
		if (this.id != null)
			element.setAttribute("id", this.id);
		if (this.origin != null)
			element.setAttribute("origin", this.origin);
		if (this.source != null)
			element.setAttribute("event1", this.source.getId());
		if (this.target != null)
			element.setAttribute("event2", this.target.getId());
		if (this.signal != null)
			element.setAttribute("signalId", this.signal.getId());
		if (this.timeMLRelType != null)
			element.setAttribute("relation", this.timeMLRelType.toString());
		if (this.syntax != null)
			element.setAttribute("syntax", this.syntax);
		if (this.source != null && this.target != null) {
			if (this.source.getTLinkableType().equals(this.target.getTLinkableType())) {
				if (this.source.getTLinkableType().equals(TLinkable.Type.EVENT)) 
					element.setAttribute("type", "ee");
				else
					element.setAttribute("type", "tt");
			} else {
				element.setAttribute("type", "et");
			}
		}
		
		element.setAttribute("closed", "false");
		
		return element;
	}
	
	public static TLink fromJSON(JSONObject json, TempDocument document) {
		TLink tlink = new TLink();
		
		if (json.containsKey("id"))
			tlink.id = json.getString("id");
		if (json.containsKey("origin"))
			tlink.origin = json.getString("origin");
		if (json.containsKey("sourceType"))
			if (TLinkable.Type.valueOf(json.getString("sourceType")).equals(TLinkable.Type.EVENT))
				tlink.source = document.getEvent(json.getString("sourceId"));
			else
				tlink.source = document.getTime(json.getString("sourceId"));
		if (json.containsKey("targetType"))
			if (TLinkable.Type.valueOf(json.getString("targetType")).equals(TLinkable.Type.EVENT))
				tlink.target = document.getEvent(json.getString("targetId"));
			else
				tlink.target = document.getTime(json.getString("targetId"));
		if (json.containsKey("signalId"))
			tlink.signal = document.getSignal(json.getString("signalId"));
		if (json.containsKey("timeMLRelType"))
			tlink.timeMLRelType = TimeMLRelType.valueOf(json.getString("timeMLRelType"));
		if (json.containsKey("syntax"))
			tlink.syntax = json.getString("syntax");

		return tlink;
	}
	
	@SuppressWarnings("unchecked")
	public static TLink fromXML(Element element, TempDocument document) {
		TLink tlink = new TLink();

		boolean hasId = false;
		boolean hasOrigin = false;
		boolean hasSourceId = false;
		boolean hasTargetId = false;
		boolean hasSignalId = false;
		boolean hasRelation = false;
		boolean hasSyntax = false;
			
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("id"))
				hasId = true;
			else if (attribute.getName().equals("origin"))
				hasOrigin = true;
			else if (attribute.getName().equals("event1"))
				hasSourceId = true;
			else if (attribute.getName().equals("event2"))
				hasTargetId = true;
			else if (attribute.getName().equals("signalId"))
				hasSignalId = true;
			else if (attribute.getName().equals("relation"))
				hasRelation = true;
			else if (attribute.getName().equals("syntax"))
				hasSyntax = true;
		}
		
		if (hasId)
			tlink.id = element.getAttributeValue("id");
		if (hasOrigin)
			tlink.origin = element.getAttributeValue("origin");
		if (hasSignalId)
			tlink.signal = document.getSignal(element.getAttributeValue("signalId"));
		if (hasRelation)
			tlink.timeMLRelType = TimeMLRelType.valueOf(element.getAttributeValue("relation"));
		if (hasSyntax)
			tlink.syntax = element.getAttributeValue("syntax");
		
		// FIXME: Relies on event id's starting with "e" and time id's starting with "t"
		if (hasSourceId) {
			String sourceId = element.getAttributeValue("event1");
			if (sourceId.startsWith("e"))
				tlink.source = document.getEvent(sourceId);
			else
				tlink.source = document.getTime(sourceId);
		}
		
		// FIXME: Relies on event id's starting with "e" and time id's starting with "t"
		if (hasTargetId) {
			String targetId = element.getAttributeValue("event2");
			if (targetId.startsWith("e"))
				tlink.target = document.getEvent(targetId);
			else
				tlink.target = document.getTime(targetId);
		}
		
		return tlink;
	}
}
