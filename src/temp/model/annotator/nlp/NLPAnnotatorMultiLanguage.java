package temp.model.annotator.nlp;

import ark.data.annotation.Document;
import ark.data.annotation.Language;
import ark.data.annotation.nlp.ConstituencyParse;
import ark.data.annotation.nlp.DependencyParse;
import ark.data.annotation.nlp.PoSTag;
import ark.model.annotator.nlp.NLPAnnotator;
import ark.model.annotator.nlp.NLPAnnotatorStanford;

import temp.util.TempProperties;

/**
 * NLPAnnotatorMultiLanguage supplements a text with NLP 
 * annotations (parses, part-of-speech tags, etc) using an
 * annotation pipeline that is appropriate for a given
 * language.
 * 
 * The FreeLing pipeline is used for the Spanish language,
 * and the interface with this pipeline
 * (temp.model.annotator.nlp.NLPAnnotatorFreeLing) currently
 * only works on Windows machines.
 * 
 * The returned NLP annotations are in the ARKWater 
 * (https://github.com/forkunited/ARKWater)
 * library's format.
 * 
 * Once constructed for a specified language, the annotator can 
 * be used by calling
 * the setText method to set the text to be annotated, and
 * then calling the make[X] methods to retrieve the annotations
 * for that text.
 * 
 * @author Bill McDowell
 * 
 */
public class NLPAnnotatorMultiLanguage extends NLPAnnotator {
	private TempProperties properties;
	private NLPAnnotator spanishAnnotator;
	private NLPAnnotator englishAnnotator;
	
	public NLPAnnotatorMultiLanguage(TempProperties properties, Language language) {
		this.properties = properties;
		setLanguage(language);
	}
	
	/**
	 * @param language
	 * @return true if the appropriate annotation pipeline for the
	 * given language has been instantiated
	 */
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

	/**
	 * @param text
	 * @return true if the annotator has received the text 
	 * and is ready to return annotations for it
	 */
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

	/**
	 * @return a name for the annotator
	 */
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

	/**
	 * @return an array of tokens for each segmented 
	 * sentence of the text.
	 */
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

	/**
	 * @return an array of part-of-speech tags for each segmented 
	 * sentence of the text.
	 */
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
	
	/**
	 * @return a dependency parse for each segmented 
	 * sentence of the text.
	 */
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

	/**
	 * @return a constituency parse for each segmented
	 * sentence of text.
	 */
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
