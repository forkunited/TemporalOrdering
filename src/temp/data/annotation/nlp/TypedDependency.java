package temp.data.annotation.nlp;

import temp.data.annotation.TempDocument;

public class TypedDependency {
	private TempDocument document;
	private int sentenceIndex;
	private int parentTokenIndex;
	private int childTokenIndex;
	
	// FreeLing dependency types: http://devel.cpl.upc.edu/freeling/svn/trunk/doc/grammars/ca+esLABELINGtags 
	// Stanford dependency types: http://nlp.stanford.edu/software/dependencies_manual.pdf
	private String type;
	
	public TypedDependency(TempDocument document, int sentenceIndex, int parentTokenIndex, int childTokenIndex, String type) {
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
	
	public String getType() {
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
		String type = typeAndRest[0];
		
		String rest = typeAndRest[1].substring(0, typeAndRest[1].length() - 1);
		String[] parentAndChild = rest.split(",");
		
		int parentTokenIndex = Integer.parseInt(parentAndChild[0].substring(parentAndChild[0].lastIndexOf("-") + 1).replace("'", "").trim()) - 1;
		int childTokenIndex = Integer.parseInt(parentAndChild[1].substring(parentAndChild[1].lastIndexOf("-") + 1).replace("'",  "").trim()) - 1;
		
		return new TypedDependency(document, sentenceIndex, parentTokenIndex, childTokenIndex, type);
	}
}
