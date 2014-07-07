package temp.data.annotation.timeml;

import ark.data.annotation.nlp.TokenSpan;

/**
 * TLinkable represents an entity linked by
 * a TimeML TLink (either an event or a time)
 * 
 * @author Bill McDowell
 * 
 */
public interface TLinkable {
	public enum Type {
		EVENT,
		TIME
	}
	
	/**
	 * @return an identifier for the TLinkable
	 */
	String getId(); 
	
	/**
	 * @return a value indicating whether the TLinkable
	 * is an event or a time
	 */
	Type getTLinkableType();
	
	/**
	 * @return a token-span in the text corresponing to the
	 * entity
	 */
	TokenSpan getTokenSpan();
}
