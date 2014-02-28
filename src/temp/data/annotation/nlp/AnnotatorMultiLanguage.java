package temp.data.annotation.nlp;

import temp.data.annotation.Language;
import temp.util.TempProperties;

public class AnnotatorMultiLanguage extends Annotator {
	private Annotator spanishAnnotator;
	private Annotator englishAnnotator;
	
	public AnnotatorMultiLanguage(TempProperties properties, Language language) {
		this.properties = properties;
		
		this.spanishAnnotator = Annotator.forLanguage(properties, Language.Spanish);
		this.englishAnnotator = Annotator.forLanguage(properties, Language.English);
		
		setLanguage(language);
	}
	
	
	@Override
	public boolean setLanguage(Language language) {
		this.language = language;
		setText(this.text);
		return true;
	}

	@Override
	public boolean setText(String text) {
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


	@Override
	public PhraseParseTree makeParse() {
		if (this.language == Language.English) {
			return this.englishAnnotator.makeParse();
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.makeParse();
		} else {
			return null;
		}
	}

}
