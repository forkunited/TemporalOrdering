package temp.data.annotation.nlp;

import temp.data.annotation.Language;
import temp.util.TempProperties;

public abstract class Annotator {
	protected TempProperties properties;
	protected Language language;
	protected String text;
	
	public abstract boolean setLanguage(Language language);
	public abstract boolean setText(String text);
	public abstract String toString();
	
	public abstract String[][] makeTokens();
	public abstract PoSTag[][] makePoSTags();
	public abstract TypedDependency[][] makeDependencies();
	
	public static Annotator forLanguage(TempProperties properties, Language language) {
		if (language == Language.English)
			return new AnnotatorStanford(properties);
		else if (language == Language.Spanish)
			return new AnnotatorFreeLing(properties);
		else
			return null;
	}
}
