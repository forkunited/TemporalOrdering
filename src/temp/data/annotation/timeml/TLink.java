package temp.data.annotation.timeml;

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
}
