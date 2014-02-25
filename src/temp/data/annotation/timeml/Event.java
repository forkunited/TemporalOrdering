package temp.data.annotation.timeml;

import temp.data.annotation.nlp.TokenSpan;

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
        I_ACTION
	}
	
	public enum TimeMLPoS {
		ADJECTIVE,
		NOUN,
		VERB,
		PREPOSITION,
		OTHER
	}

	private String id;
	private TokenSpan tokenSpan;
	private String sourceId;
	private Signal signal;
	private TimeMLTense timeMLTense;
	private TimeMLAspect timeMLAspect;
	private TimeMLPolarity timeMLPolarity;
	private TimeMLClass timeMLClass;
	private TimeMLPoS timeMLPoS;
	private String modality;
	private String cardinality;
	
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
	
	public String getModality() {
		return this.modality;
	}
	
	public String getCardinality() {
		return this.cardinality;
	}
}
