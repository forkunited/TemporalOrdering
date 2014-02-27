package temp.data.annotation.nlp;

import temp.data.annotation.TempDocument;

public class TypedDependency {
	public enum Type {
		/* FIXME */
	}
	
	private TempDocument document;
	private int sentenceIndex;
	private int parentTokenIndex;
	private int childTokenIndex;
	private Type type;
	
	public TypedDependency(TempDocument document, int sentenceIndex, int parentTokenIndex, int childTokenIndex, Type type) {
		this.document = document;
		this.sentenceIndex = sentenceIndex;
		this.parentTokenIndex = parentTokenIndex;
		this.childTokenIndex = childTokenIndex;
		this.type = type;
	}
	
	public TempDocument getDocument() {
		return this.document;
	}
	
	public int getParentTokenIndex() {
		return this.parentTokenIndex;
	}
	
	public int getChildTokenIndex() {
		return this.childTokenIndex;
	}
	
	public int getSentenceIndex() {
		return this.sentenceIndex;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public String toString() {
		return this.type + 
			   "(" 
				+ this.document.getToken(this.sentenceIndex, this.parentTokenIndex) + "-" + (this.parentTokenIndex + 1) +
				", " + this.document.getToken(this.sentenceIndex, this.childTokenIndex) + "-" + (this.childTokenIndex + 1) + 
				")";
	}
	
	public static TypedDependency fromString(String str, TempDocument document, int sentenceIndex) {
		str = str.trim();
		
		String[] typeAndRest = str.split("\\(");
		TypedDependency.Type type = Type.valueOf(typeAndRest[0]);
		
		String rest = typeAndRest[1].substring(0, str.length() - 1);
		String[] parentAndChild = rest.split(",");
		int parentTokenIndex = Integer.parseInt(parentAndChild[0].trim().split("\\-")[1]) - 1;
		int childTokenIndex = Integer.parseInt(parentAndChild[1].trim().split("\\-")[1]) - 1;
		
		return new TypedDependency(document, sentenceIndex, parentTokenIndex, childTokenIndex, type);
	}
}
