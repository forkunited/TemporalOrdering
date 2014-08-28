package temp.data.annotation.timeml;

import java.util.List;

import net.sf.json.JSONObject;

import org.jdom.Attribute;
import org.jdom.Element;

import temp.data.annotation.TempDocument;

/**
 * TLink represents a TimeML TLink--a temporal
 * link between a pair of events/times.
 * 
 * See http://timeml.org/site/index.html for details.
 * 
 * @author Bill McDowell
 * 
 */
public class TLink {
	public enum Type {
		EVENT_EVENT,
		EVENT_TIME,
		TIME_TIME
	}
	
	public enum Position {
		DCT,
		WITHIN_SENTENCE,
		BETWEEN_SENTENCE
	}
	
	public enum TimeMLRelType {

		OVERLAPS,      // Additional relation for transitivity (shown at http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
		OVERLAPPED_BY, // Additional relation for transitivity (shown at http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
		
		OVERLAP, // TempEval2
		BEFORE_OR_OVERLAP, // TempEval2
		OVERLAP_OR_AFTER, // TempEval2
		
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
	
	public enum TimeRelType {

		BEFORE,
		IS_INCLUDED,
		INCLUDES,
		SIMULTANEOUS,
		AFTER,
		OVERLAPS,      // Additional relation for transitivity (shown at http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
		OVERLAPPED_BY, // Additional relation for transitivity (shown at http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
		VAGUE
	}
	
	// NOTE: Currently only for TimeBank-Dense relations + overlaps/overlapped_by
	// Each sub-array represents a rule of the form (r_1\wedge r_2)->(r_3\vee .. r_n)
	// The first array in each sub-array contain r_1 and r_2
	// The second array in each sub-array contsins r_3...r_n
	// null means nothing can be inferred (disjunction over all)
	private static final TimeMLRelType[][][] timeMLRelTypeCompositionRules = {
		{ { TimeMLRelType.BEFORE, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.INCLUDES }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.AFTER }, { null } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.VAGUE }, { null } },
		
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED} },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.INCLUDES }, { null } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.IS_INCLUDED } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPS }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.INCLUDES, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.INCLUDES }, { TimeMLRelType.INCLUDES } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.INCLUDES } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.AFTER }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPS }, { TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.INCLUDES }, { TimeMLRelType.INCLUDES } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.SIMULTANEOUS } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS }, { TimeMLRelType.OVERLAPS } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.OVERLAPPED_BY } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.AFTER, TimeMLRelType.BEFORE }, { null } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.INCLUDES }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.OVERLAPS }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.OVERLAPS } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.AFTER }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPS }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.INCLUDES }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.OVERLAPPED_BY } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.OVERLAPS }, { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE }, { null } },
		
		{ { TimeMLRelType.VAGUE, TimeMLRelType.BEFORE }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.IS_INCLUDED }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.INCLUDES }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.SIMULTANEOUS }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.AFTER }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.OVERLAPS }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.OVERLAPPED_BY }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.VAGUE }, { null } },
	};
	
	/**
	 * @returns an array representing TLink relation type 
	 * composition rules of the form: 
	 * 
	 * ((r(l')=t') and (r(l'')=t'')) implies or_i(r(l''')=t_i)
	 * 
	 * Where r(l)=t means that TLink l has relation t and
	 * 'or_i' is a disjunction over propositions indexed by i.
	 * These are the rules referred to as 'Transitivity' and
	 * 'Disjunctive Transitivity' in the 
	 * papers/TemporalOrderingNotes.pdf document.  They were 
	 * derived from Allen's interval algebra
	 * (see http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
	 * 
	 * Each rule is stored as a two-dimensional array of length 2.
	 * The first element of this array contains t' and t'', and
	 * the second element of the array contains t_i for each i.  
	 * 
	 */
	public static final TimeMLRelType[][][] getTimeMLRelTypeCompositionRules() {
		return TLink.timeMLRelTypeCompositionRules;
	}
	
	/**
	 * Represents converse rules for TimeML relationship-types. The
	 * rules are of the form:
	 * 
	 * (r(l')=t') implies (r(l'')=t'')
	 * 
	 * Where l' is a link in the reverse direction of l'' but
	 * between the same events/times, and 
	 * r(l)=t means that TLink l has relation t.
	 * These are the rules referred to as 'Converse' in the
	 * papers/TemporalOrderingNotes.pdf document.  They were 
	 * derived from Allen's interval algebra
	 * (see http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
	 * 
	 * @param timeMLRelType (t')
	 * @return converse of timeMLRelType (t'')
	 *
	 */
	public static TimeMLRelType getConverseTimeMLRelType(TimeMLRelType timeMLRelType) {
		if (timeMLRelType == TimeMLRelType.OVERLAPS)
			return TimeMLRelType.OVERLAPPED_BY;
		else if (timeMLRelType == TimeMLRelType.OVERLAPPED_BY)
			return TimeMLRelType.OVERLAPS;
		else if (timeMLRelType == TimeMLRelType.BEFORE)
			return TimeMLRelType.AFTER;
		else if (timeMLRelType == TimeMLRelType.AFTER)
			return TimeMLRelType.BEFORE;
		else if (timeMLRelType == TimeMLRelType.INCLUDES)
			return TimeMLRelType.IS_INCLUDED;
		else if (timeMLRelType == TimeMLRelType.IS_INCLUDED)
			return TimeMLRelType.INCLUDES;
		else if (timeMLRelType == TimeMLRelType.SIMULTANEOUS)
			return TimeMLRelType.SIMULTANEOUS;
		else if (timeMLRelType == TimeMLRelType.OVERLAP)
			return TimeMLRelType.OVERLAP;
		else if (timeMLRelType == TimeMLRelType.BEFORE_OR_OVERLAP)
			return TimeMLRelType.OVERLAP_OR_AFTER;
		else if (timeMLRelType == TimeMLRelType.OVERLAP_OR_AFTER)
			return TimeMLRelType.BEFORE_OR_OVERLAP;
		else if (timeMLRelType == TimeMLRelType.DURING)
			return TimeMLRelType.DURING_INV;
		else if (timeMLRelType == TimeMLRelType.DURING_INV)
			return TimeMLRelType.DURING;
		else if (timeMLRelType == TimeMLRelType.IAFTER)
			return TimeMLRelType.IBEFORE;
		else if (timeMLRelType == TimeMLRelType.IDENTITY)
			return TimeMLRelType.IDENTITY;
		else if (timeMLRelType == TimeMLRelType.BEGINS)
			return TimeMLRelType.BEGUN_BY;
		else if (timeMLRelType == TimeMLRelType.BEGUN_BY)
			return TimeMLRelType.BEGINS;
		else if (timeMLRelType == TimeMLRelType.ENDS)
			return TimeMLRelType.ENDED_BY;
		else if (timeMLRelType == TimeMLRelType.ENDED_BY)
			return TimeMLRelType.ENDS;
		else
			return TimeMLRelType.VAGUE;
	}
	
	private String id;
	private String origin;
	private TLinkable source;
	private TLinkable target;
	private TimeMLRelType timeMLRelType;
	private Signal signal;
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
	
	public TimeMLRelType getConverseTimeMLRelType() {
		return TLink.getConverseTimeMLRelType(this.timeMLRelType);
	}
	
	public String getSyntax() {
		return this.syntax;
	}
	
	public Position getPosition() {
		int sourceSentenceIndex = this.source.getTokenSpan().getSentenceIndex();
		int targetSentenceIndex = this.target.getTokenSpan().getSentenceIndex();
		
		if (sourceSentenceIndex < 0 || targetSentenceIndex < 0)
			return Position.DCT;
		else if (sourceSentenceIndex != targetSentenceIndex)
			return Position.BETWEEN_SENTENCE;
		else 
			return Position.WITHIN_SENTENCE;
	}
	
	public Type getType() {
		TLinkable.Type sourceType = this.source.getTLinkableType();
		TLinkable.Type targetType = this.target.getTLinkableType();
		
		if (sourceType == TLinkable.Type.EVENT && targetType == TLinkable.Type.EVENT)
			return Type.EVENT_EVENT;
		else if (sourceType == TLinkable.Type.TIME && targetType == TLinkable.Type.TIME)
			return Type.TIME_TIME;
		else
			return Type.EVENT_TIME;
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
	
	public Element toTimeML() {
		Element element = new Element("TLINK");
		
		if (this.id != null)
			element.setAttribute("lid", this.id);
		if (this.origin != null)
			element.setAttribute("origin", this.origin);
		
		if (this.source != null && this.source.getTLinkableType() == TLinkable.Type.EVENT)
			element.setAttribute("eventInstanceID", this.source.getId());
		else if (this.source != null && this.source.getTLinkableType() == TLinkable.Type.TIME)
			element.setAttribute("timeID", this.source.getId());
			
		if (this.target != null && this.target.getTLinkableType() == TLinkable.Type.EVENT)
			element.setAttribute("relatedToEventInstance", this.target.getId());
		else if (this.target != null && this.target.getTLinkableType() == TLinkable.Type.TIME)
			element.setAttribute("relatedToTime", this.target.getId());
		
		if (this.signal != null)
			element.setAttribute("signalID", this.signal.getId());
		if (this.timeMLRelType != null)
			element.setAttribute("relType", this.timeMLRelType.toString());
		
		if (this.syntax != null)
			element.setAttribute("syntax", this.syntax);
		
		return element;
	}
	
	public static TLink fromJSON(JSONObject json, TempDocument document) {
		TLink tlink = new TLink();
		
		if (json.containsKey("id")) {
			tlink.id = json.getString("id");
		}
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
		
		if (hasId) {
			tlink.id = element.getAttributeValue("id");
		}
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
	
	public static TLink fromTimeML(Element element, TempDocument document) {
		TLink tlink = new TLink();

		String id = element.getAttributeValue("lid");
		String origin = element.getAttributeValue("origin");
		String eventInstanceID = element.getAttributeValue("eventInstanceID");
		String timeID = element.getAttributeValue("timeID");
		String relatedToEventInstance = element.getAttributeValue("relatedToEventInstance");
		String relatedToTime = element.getAttributeValue("relatedToTime");
		String signalId = element.getAttributeValue("signalId");
		String relType = element.getAttributeValue("relType");
		String syntax = element.getAttributeValue("syntax");
		
		if (id != null)
			tlink.id = id;
		if (origin != null)
			tlink.origin = origin;
		if (eventInstanceID != null) 
			tlink.source = document.getEvent(eventInstanceID);
		if (timeID != null)
			tlink.source = document.getTime(timeID);
		if (relatedToEventInstance != null)
			tlink.target = document.getEvent(relatedToEventInstance);
		if (relatedToTime != null)
			tlink.target = document.getTime(relatedToTime);
		if (signalId != null)
			tlink.signal = document.getSignal(signalId);
		if (relType != null)
			tlink.timeMLRelType = TimeMLRelType.valueOf(relType);
		if (syntax != null)
			tlink.syntax = syntax;
		
		return tlink;
	}
}
