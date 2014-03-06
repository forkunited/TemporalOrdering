package temp.model.annotator.nlp;

import temp.data.annotation.Language;
import temp.data.annotation.nlp.PoSTag;
import temp.data.annotation.nlp.TypedDependency;
import temp.util.TempProperties;

public abstract class NLPAnnotator {
	protected TempProperties properties;
	protected Language language;
	protected String text;
	
	public abstract boolean setLanguage(Language language);
	public abstract boolean setText(String text);
	public abstract String toString();
	
	public abstract String[][] makeTokens();
	public abstract PoSTag[][] makePoSTags();
	public abstract TypedDependency[][] makeDependencies();
	
	public static NLPAnnotator forLanguage(TempProperties properties, Language language) {
		if (language == Language.English)
			return new NLPAnnotatorStanford(properties);
		else if (language == Language.Spanish)
			return new NLPAnnotatorFreeLing(properties);
		else
			return null;
	}
	
	public static NLPAnnotator fromString(TempProperties properties, String str) {
		if (str.equals("Stanford"))
			return new NLPAnnotatorStanford(properties);
		else if (str.equals("FreeLing"))
			return new NLPAnnotatorFreeLing(properties);
		else
			return null;
	}
}
