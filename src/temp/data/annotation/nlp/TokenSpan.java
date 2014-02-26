package temp.data.annotation.nlp;

import net.sf.json.JSONObject;
import temp.data.annotation.TempDocument;

public class TokenSpan {
	private TempDocument document;
	private int sentenceIndex;
	private int startTokenIndex; // 0-based token index (inclusive)
	private int endTokenIndex; // 0-based token index (exclusive)
	
	public TokenSpan(TempDocument document, int sentenceIndex, int startTokenIndex, int endTokenIndex) {
		this.document = document;
		this.sentenceIndex = sentenceIndex;
		this.startTokenIndex = startTokenIndex;
		this.endTokenIndex = endTokenIndex;
	}
	
	public TempDocument getDocument() {
		return this.document;
	}
	
	public int getSentenceIndex() {
		return this.sentenceIndex;
	}
	
	public int getStartTokenIndex() {
		return this.startTokenIndex;
	}
	
	public int getEndTokenIndex() {
		return this.endTokenIndex;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		for (int i = this.startTokenIndex; i < this.endTokenIndex; i++)
			str.append(this.document.getToken(this.sentenceIndex, i)).append(" ");
		
		return str.toString().trim();
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("startTokenIndex", this.startTokenIndex);
		json.put("endTokenIndex", this.endTokenIndex);
		
		return json;
	}
	
	public static TokenSpan fromJSON(JSONObject json, TempDocument document, int sentenceIndex) {
		return new TokenSpan(
			document,
			sentenceIndex,
			json.getInt("startTokenIndex"),
			json.getInt("endTokenIndex")
		);
	}
}

