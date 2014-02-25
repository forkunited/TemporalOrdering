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
}
