package temp.data.annotation.timeml;

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
}
