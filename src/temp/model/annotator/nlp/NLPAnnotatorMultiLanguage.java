package temp.model.annotator.nlp;

import temp.data.annotation.Language;
import temp.data.annotation.nlp.PoSTag;
import temp.data.annotation.nlp.TypedDependency;
import temp.util.TempProperties;

public class NLPAnnotatorMultiLanguage extends NLPAnnotator {
	private NLPAnnotator spanishAnnotator;
	private NLPAnnotator englishAnnotator;
	
	public NLPAnnotatorMultiLanguage(TempProperties properties, Language language) {
		this.properties = properties;
		setLanguage(language);
	}
	
	
	@Override
	public boolean setLanguage(Language language) {
		this.language = language;
		if (this.language == Language.English)
			this.englishAnnotator = NLPAnnotator.forLanguage(properties, Language.English);
		else if (this.language == Language.Spanish)
			this.spanishAnnotator = NLPAnnotator.forLanguage(properties, Language.Spanish);
		else
			return false;
		setText(this.text);
		return true;
	}

	@Override
	public boolean setText(String text) {
		if (text == null) {
			return true;
		}
		
		if (this.language == Language.English) {
			this.englishAnnotator.setText(text);
		} else if (this.language == Language.Spanish) {
			this.spanishAnnotator.setText(text);
		} else {
			return false;
		}
		
		this.text = text;
		return true;
	}

	@Override
	public String toString() {
		if (this.language == Language.English) {
			return this.englishAnnotator.toString();
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.toString();
		} else {
			return null;
		}
	}

	@Override
	public String[][] makeTokens() {
		if (this.language == Language.English) {
			return this.englishAnnotator.makeTokens();
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.makeTokens();
		} else {
			return null;
		}
	}

	@Override
	public PoSTag[][] makePoSTags() {
		if (this.language == Language.English) {
			return this.englishAnnotator.makePoSTags();
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.makePoSTags();
		} else {
			return null;
		}
	}

	@Override
	public TypedDependency[][] makeDependencies() {
		if (this.language == Language.English) {
			return this.englishAnnotator.makeDependencies();
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.makeDependencies();
		} else {
			return null;
		}
	}
}
