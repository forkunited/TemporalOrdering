package temp.data.annotation.nlp;

import java.io.File;

import edu.upc.freeling.ChartParser;
import edu.upc.freeling.DepTxala;
import edu.upc.freeling.HmmTagger;
import edu.upc.freeling.ListSentence;
import edu.upc.freeling.ListSentenceIterator;
import edu.upc.freeling.ListWord;
import edu.upc.freeling.Maco;
import edu.upc.freeling.MacoOptions;
import edu.upc.freeling.Nec;
import edu.upc.freeling.Senses;
import edu.upc.freeling.Sentence;
import edu.upc.freeling.Splitter;
import edu.upc.freeling.Tokenizer;
import edu.upc.freeling.Ukb;
import edu.upc.freeling.Util;
import edu.upc.freeling.VectorWord;
import temp.data.annotation.Language;
import temp.util.TempProperties;

public class AnnotatorFreeLing extends Annotator {
	private MacoOptions options;
    private Tokenizer tokenizer;
    private Splitter sentenceSplitter;
    private Maco morphologyAnalyzer;
    private HmmTagger posTagger;
    private ChartParser parser;
    private DepTxala dependencyParser;
    private Senses senseDictionary;
    private Ukb senseDisambiguator;
    
    private ListWord textWords;
    private ListSentence textSentences;
	
	public AnnotatorFreeLing(TempProperties properties) {
		this.properties = properties;
		
		loadLibrary("zlibwapi");
		
		loadLibrary("zlibwapi");
		loadLibrary("zlibwapid");
		loadLibrary("icudt49");
		loadLibrary("icuuc49");  
		loadLibrary("icule49");
		loadLibrary("icuin49");
		loadLibrary("iculx49");
		loadLibrary("icutu49");  
		loadLibrary("icuio49");
		loadLibrary("testplug");
		loadLibrary("foma");
		loadLibrary("freeling");
		loadLibrary("freeling_javaAPI");
		
		Util.initLocale( "default" );
		
		setLanguage(Language.Spanish);
	}
	
	private void loadLibrary(String library) {
		System.loadLibrary(new File(properties.getFreeLingLibraryPath(), "bin/" + library).getPath());
	}
	
	public boolean setLanguage(Language language) {
		String langStr = null;
		if (language == Language.English)
			langStr = "en";
		else if (language == Language.Spanish)
			langStr = "es";
		else
			return false;
		
	    // Create options set for maco analyzer.
	    // Default values are Ok, except for data files.
	    this.options = new MacoOptions(langStr);

	    this.options.setActiveModules(false, true, true, true, 
			                               true, true, true, 
			                               true, true, true);

	    String langPath = new File(this.properties.getFreeLingDataDirectoryPath(), langStr).getAbsolutePath();
	    String commonPath = new File(this.properties.getFreeLingDataDirectoryPath(), "common").getAbsolutePath();
	    this.options.setDataFiles(
		  "", 
		  langPath + "/locucions.dat", 
		  langPath + "/quantities.dat",
		  langPath + "/afixos.dat",
		  langPath + "/probabilitats.dat",
		  langPath + "/dicc.src",
		  langPath + "/np.dat",
		  commonPath + "/punct.dat"
	    );
	    
	    this.tokenizer = new Tokenizer(langPath + "/tokenizer.dat");
	    this.sentenceSplitter = new Splitter(langPath + "/splitter.dat");
	    this.morphologyAnalyzer = new Maco(this.options);
	    this.posTagger= new HmmTagger(langPath + "/tagger.dat", true, 2);
	    this.parser = new ChartParser(langPath + "/chunker/grammar-chunk.dat");
	    this.dependencyParser = new DepTxala(langPath + "/dep/dependences.dat", parser.getStartSymbol());
	    this.senseDictionary = new Senses(langPath + "/senses.dat");
	    this.senseDisambiguator = new Ukb(langPath + "/ukb.dat");
	    
		this.language = language;
		return true;
	}
	
	public String toString() {
		return "FreeLing";
	}
	
	public boolean setText(String text) {
		this.text = text;
		
		this.textWords = this.tokenizer.tokenize(text);
		this.textSentences = this.sentenceSplitter.split(this.textWords, false);
		this.morphologyAnalyzer.analyze(this.textSentences);
		
		return true;
	}
	
	public String[][] makeTokens() {
		String[][] tokens = new String[(int)this.textSentences.size()][];
		ListSentenceIterator iterator = new ListSentenceIterator(this.textSentences);
		int i = 0;
		while (iterator.hasNext()) {
			Sentence sentence = iterator.next();
			VectorWord words = sentence.getWords();
			tokens[i] = new String[(int)words.size()];
			
			for (int j = 0; j < words.size(); j++) {
				tokens[i][j] = words.get(j).getForm();
			}
			
			i++;
		}
		
		return tokens;
	}
	
	public PoSTag[][] makePoSTags() {
		/* FIXME */
		return null;
	}
	
	public TypedDependency[][] makeDependencies() {
		/* FIXME */
		return null;
	}

	public PhraseParseTree makeParse() {
		// TODO Auto-generated method stub
		return null;
	}
}
