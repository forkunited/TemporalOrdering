package temp.data.annotation.timeml;

import temp.data.annotation.nlp.TokenSpan;

public interface TLinkable {
	public enum Type {
		EVENT,
		TIME
	}
	
	String getId();
	Type getTLinkableType();
	TokenSpan getTokenSpan();
}
