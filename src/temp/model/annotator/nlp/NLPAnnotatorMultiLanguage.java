package temp.model.annotator.nlp;

import ark.data.annotation.Document;
import ark.data.annotation.Language;
import ark.data.annotation.nlp.ConstituencyParse;
import ark.data.annotation.nlp.DependencyParse;
import ark.data.annotation.nlp.PoSTag;
import ark.model.annotator.nlp.NLPAnnotator;
import ark.model.annotator.nlp.NLPAnnotatorStanford;

import temp.util.TempProperties;

public class NLPAnnotatorMultiLanguage extends NLPAnnotator {
	private TempProperties properties;
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
			this.englishAnnotator = NLPAnnotatorMultiLanguage.forLanguage(properties, Language.English);
		else if (this.language == Language.Spanish)
			this.spanishAnnotator = NLPAnnotatorMultiLanguage.forLanguage(properties, Language.Spanish);
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
	protected PoSTag[][] makePoSTagsInternal() {
		if (this.language == Language.English) {
			return this.englishAnnotator.makePoSTags();
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.makePoSTags();
		} else {
			return null;
		}
	}
	
	@Override
	protected DependencyParse[] makeDependencyParsesInternal(Document document, int sentenceIndexOffset) {
		if (this.language == Language.English) {
			return this.englishAnnotator.makeDependencyParses(document, sentenceIndexOffset);
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.makeDependencyParses(document, sentenceIndexOffset);
		} else {
			return null;
		}
	}


	@Override
	protected ConstituencyParse[] makeConstituencyParsesInternal(Document document, int sentenceIndexOffset) {
		if (this.language == Language.English) {
			return this.englishAnnotator.makeConstituencyParses(document, sentenceIndexOffset);
		} else if (this.language == Language.Spanish) {
			return this.spanishAnnotator.makeConstituencyParses(document, sentenceIndexOffset);
		} else {
			return null;
		}
	}
	
	public static NLPAnnotator forLanguage(TempProperties properties, Language language) {
		if (language == Language.English)
			return new NLPAnnotatorStanford();
		else if (language == Language.Spanish)
			return new NLPAnnotatorFreeLing(properties);
		else
			return null;
	}
	
	public static NLPAnnotator fromString(TempProperties properties, String str) {
		if (str.equals("Stanford"))
			return new NLPAnnotatorStanford();
		else if (str.equals("FreeLing"))
			return new NLPAnnotatorFreeLing(properties);
		else
			return null;
	}
}
