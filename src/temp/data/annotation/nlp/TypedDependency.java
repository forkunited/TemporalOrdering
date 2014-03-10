package temp.data.annotation.nlp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import temp.data.annotation.TempDocument;

public class TypedDependency {
	private static Pattern dependencyPattern = Pattern.compile("(.*)\\((.*)\\-([0-9']*),(.*)\\-([0-9']*)\\)");
	
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
		
		Matcher m = TypedDependency.dependencyPattern.matcher(str);
		
		if (!m.matches())
			return null;
		
		String type = m.group(1).trim();
		Integer parentTokenIndex = Integer.parseInt(m.group(3).replace("'", "").trim());
		Integer childTokenIndex = Integer.parseInt(m.group(5).replace("'", "").trim());
		
		return new TypedDependency(document, sentenceIndex, parentTokenIndex, childTokenIndex, type);
	}
}
