package temp.data.annotation.nlp;

import temp.data.annotation.Language;
import temp.util.TempProperties;

public class AnnotatorStanford extends Annotator {
	public AnnotatorStanford(TempProperties properties) {
		this.properties = properties;
		setLanguage(Language.English);
	}
	
	public boolean setLanguage(Language language) {
		if (language != Language.English)
			return false;
		this.language = language;
		return true;
	}
	
	public String toString() {
		return "Stanford";
	}
	
	public boolean setText(String text) {
		this.text = text;
		return true;
	}
	
	public String[][] makeTokens() {
		/* FIXME */
		return null;
	}
	
	public PoSTag[][] makePoSTags() {
		/* FIXME */
		return null;
	}
	
	public TypedDependency[][] makeDependencies() {
		/* FIXME */
		return null;
	}
}
