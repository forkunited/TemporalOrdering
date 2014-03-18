package temp.data.annotation.timeml;

import java.util.List;

import net.sf.json.JSONObject;

import org.jdom.Attribute;
import org.jdom.Element;

import temp.data.annotation.TempDocument;
import ark.data.annotation.nlp.TokenSpan;

public class Signal {	
	private String id;
	private TokenSpan tokenSpan;
	
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		if (this.id != null)
			json.put("id", this.id);
		if (this.tokenSpan != null)
			json.put("tokenSpan", this.tokenSpan.toJSON());

		return json;
	}
	
	public Element toXML() {
		Element element = new Element("signal");
		
		if (this.id != null)
			element.setAttribute("id", this.id);
		if (this.tokenSpan != null) {
			element.setAttribute("offset", String.valueOf(this.tokenSpan.getStartTokenIndex() + 1));
			element.setAttribute("length", String.valueOf(this.tokenSpan.getEndTokenIndex() - this.tokenSpan.getStartTokenIndex()));
			element.setAttribute("string", this.tokenSpan.toString());
		}
		
		return element;
	}
	
	public static Signal fromJSON(JSONObject json, TempDocument document, int sentenceIndex) {
		Signal signal = new Signal();
		
		if (json.containsKey("id"))
			signal.id = json.getString("id");
		if (json.containsKey("tokenSpan"))
			signal.tokenSpan = TokenSpan.fromJSON(json.getJSONObject("tokenSpan"), document, sentenceIndex);
		
		return signal;
	}
	
	@SuppressWarnings("unchecked")
	public static Signal fromXML(Element element, TempDocument document, int sentenceIndex) {
		Signal signal = new Signal();
			
		boolean hasOffset = false;
		boolean hasLength = false;
		boolean hasId = false;
			
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("offset"))
				hasOffset = true;
			else if (attribute.getName().equals("length"))
				hasLength = true;
			else if (attribute.getName().equals("id"))
				hasId = true;
		}
				
		if (hasOffset) {
			int startTokenIndex = Integer.parseInt(element.getAttributeValue("offset")) - 1;
			int endTokenIndex = startTokenIndex + ((hasLength) ? Integer.parseInt(element.getAttributeValue("length")) : 1);
			signal.tokenSpan = new TokenSpan(document, 
											sentenceIndex, 
											startTokenIndex, 
											endTokenIndex);
		}
		
		if (hasId)
			signal.id = element.getAttributeValue("id");
		
		return signal;
	}
}
