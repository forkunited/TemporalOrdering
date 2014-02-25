package temp.data.annotation.timeml;

import temp.data.annotation.nlp.TokenSpan;

public class Signal {	
	private String id;
	private TokenSpan tokenSpan;
	
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
}
