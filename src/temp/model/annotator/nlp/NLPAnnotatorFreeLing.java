package temp.model.annotator.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ark.util.FileUtil;

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
import edu.upc.freeling.TreeDepnode;
import edu.upc.freeling.Ukb;
import edu.upc.freeling.Util;
import edu.upc.freeling.VectorWord;
import temp.data.annotation.Language;
import temp.data.annotation.nlp.PoSTag;
import temp.data.annotation.nlp.TypedDependency;
import temp.data.annotation.nlp.WordNet;
import temp.util.TempProperties;

public class NLPAnnotatorFreeLing extends NLPAnnotator {
	private MacoOptions options;
	private Tokenizer tokenizer;
	private Splitter sentenceSplitter;
	private Maco morphologyAnalyzer;
	private HmmTagger posTagger;
	private ChartParser parser;
	private DepTxala dependencyParser;
	private Nec ner;
	private Senses senseDictionary; 
	private Ukb senseDisambiguator;
    
	private ListWord textWords;
	private ListSentence textSentences;

	private Map<String, List<WordNet.Hypernym>> wordNetHypernymMap;
	
    public NLPAnnotatorFreeLing(TempProperties properties) {
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
		
		loadWordNetHypernymMap();
	}
	
	private void loadLibrary(String library) {
		System.loadLibrary(new File(this.properties.getFreeLingLibraryPath(), "bin/" + library).getPath().replace("\\", "/"));
	}
	
	private void loadWordNetHypernymMap() {
		String wordNetFilePath = new File(this.properties.getFreeLingDataDirectoryPath(), "common/wn30.src").getAbsolutePath();
		this.wordNetHypernymMap = new HashMap<String, List<WordNet.Hypernym>>();
		
		try {
			BufferedReader r = FileUtil.getFileReader(wordNetFilePath);
			String line = null;
			while ((line = r.readLine()) != null) {
				String lineParts[] = line.split(" ");
				
				if (lineParts.length < 4 || lineParts[3].equals("-"))
					continue;
				
				String sense1 = lineParts[0];
				String sense2 = lineParts[1];
				String hypernyms[] = lineParts[3].split(":");
				
				if (!this.wordNetHypernymMap.containsKey(sense1))
					this.wordNetHypernymMap.put(sense1, new ArrayList<WordNet.Hypernym>());
				if (!this.wordNetHypernymMap.containsKey(sense2))
					this.wordNetHypernymMap.put(sense2, new ArrayList<WordNet.Hypernym>());
			
				for (int i = 0; i < hypernyms.length; i++) {
					this.wordNetHypernymMap.get(sense1).add(WordNet.hypernymFromString(hypernyms[i]));
					this.wordNetHypernymMap.get(sense2).add(WordNet.hypernymFromString(hypernyms[i]));
				}
			}
			
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean setLanguage(Language language) {
		if (language == this.language)
			return true;
		
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
	    this.dependencyParser = new DepTxala(langPath + "/dep/dependences.dat", this.parser.getStartSymbol());
	    this.ner = new Nec(langPath + "/nerc/nec/nec-ab-poor1.dat");
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
		this.posTagger.analyze(this.textSentences);
	    this.ner.analyze(this.textSentences);
	    this.senseDictionary.analyze(this.textSentences);
	    this.senseDisambiguator.analyze(this.textSentences);
	    this.parser.analyze(this.textSentences);
	    this.dependencyParser.analyze(this.textSentences);
		
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
		PoSTag[][] tags = new PoSTag[(int)this.textSentences.size()][];
		ListSentenceIterator iterator = new ListSentenceIterator(this.textSentences);
		int i = 0;
		while (iterator.hasNext()) {
			Sentence sentence = iterator.next();
			VectorWord words = sentence.getWords();
			tags[i] = new PoSTag[(int)words.size()];
			
			for (int j = 0; j < words.size(); j++) {
				String tag = words.get(j).getTag();
				if (tag.equals("CC"))
					tags[i][j] = PoSTag.CC;
				else if (tag.startsWith("Z"))
					tags[i][j] = PoSTag.CD;
				else if (tag.startsWith("D"))
					tags[i][j] = PoSTag.DT;
				else if (tag.equals("CS") || tag.startsWith("S"))
					tags[i][j] = PoSTag.IN;
				else if (tag.matches("A.S..."))
					tags[i][j] = PoSTag.JJS;
				else if (tag.startsWith("A"))
					tags[i][j] = PoSTag.JJ;
				else if (tag.matches("NP.S..."))
					tags[i][j] = PoSTag.NNP;
				else if (tag.matches("NP.P..."))
					tags[i][j] = PoSTag.NNPS;
				else if (tag.matches("N..P..."))
					tags[i][j] = PoSTag.NNS;
				else if (tag.startsWith("N"))
					tags[i][j] = PoSTag.NN;
				else if (tag.startsWith("PP"))
					tags[i][j] = PoSTag.PRP;
				else if (tag.startsWith("PX"))
					tags[i][j] = PoSTag.PRP$;
				else if (tag.startsWith("R"))
					tags[i][j] = PoSTag.RB;
				else if (tag.startsWith("F"))
					tags[i][j] = PoSTag.SYM;
				else if (tag.startsWith("PE") || tag.equals("I"))
					tags[i][j] = PoSTag.UH;
				else if (tag.matches("V..P3S."))
					tags[i][j] = PoSTag.VBZ;
				else if (tag.matches("V..P.S."))
					tags[i][j] = PoSTag.VBP;
				else if (tag.matches("V.PI...") || tag.matches("V.PS..."))
					tags[i][j] = PoSTag.VBN;
				else if (tag.matches("V.G....") || tag.matches("V.PP..."))
					tags[i][j] = PoSTag.VBG;
				else if (tag.matches("V..I...") || tag.matches("V..S..."))
					tags[i][j] = PoSTag.VBD;
				else if (tag.startsWith("V"))
					tags[i][j] = PoSTag.VB;
				else if (tag.startsWith("PT") || tag.startsWith("PR"))
					tags[i][j] = PoSTag.WP;
				else
					tags[i][j] = PoSTag.Other;
				
			}
			
			i++;
		}
		
		return tags;
	}
	
	public TypedDependency[][] makeDependencies() {
		TypedDependency[][] dependencies = new TypedDependency[(int)this.textSentences.size()][];
		ListSentenceIterator iterator = new ListSentenceIterator(this.textSentences);
		int i = 0;
		while (iterator.hasNext()) {
			List<TypedDependency> dependencyList = new LinkedList<TypedDependency>(); 
			Sentence sentence = iterator.next();
			Queue<TreeDepnode> treeNodes = new LinkedList<TreeDepnode>();
			treeNodes.add(sentence.getDepTree());
			
			boolean isRoot = true;
			while (!treeNodes.isEmpty()) {
				TreeDepnode nextNode = treeNodes.remove();
				
				int parentTokenIndex = -1;
				if (!isRoot) {
					parentTokenIndex = (int)nextNode.getParent().getInfo().getWord().getPosition();
				}
				
				int childTokenIndex = (int)nextNode.getInfo().getWord().getPosition();
				String type = nextNode.getInfo().getLabel();
					
				dependencyList.add(new TypedDependency(null, i, parentTokenIndex, childTokenIndex, type));
				
				int numChildren = (int)nextNode.numChildren();
				for (int j = 0; j < numChildren; j++) {
					treeNodes.add(nextNode.nthChildRef(j));
				}
				
				isRoot = false;
			}
			
			dependencies[i] = new TypedDependency[dependencyList.size()];
			dependencies[i] = dependencyList.toArray(dependencies[i]);
			
			i++;
		}
		return dependencies;
	}
	
	public WordNet.Hypernym[][][] makeTokenHypernyms() {
		WordNet.Hypernym[][][] hypernyms = new WordNet.Hypernym[(int)this.textSentences.size()][][];
		ListSentenceIterator iterator = new ListSentenceIterator(this.textSentences);
		int i = 0;
		while (iterator.hasNext()) {
			Sentence sentence = iterator.next();
			VectorWord words = sentence.getWords();
			hypernyms[i] = new WordNet.Hypernym[(int)words.size()][];
			
			for (int j = 0; j < words.size(); j++) {
				String[] senses = words.get(j).getSensesString().split("/");
				Set<WordNet.Hypernym> hypernymsForWord = new HashSet<WordNet.Hypernym>();
				for (int k = 0; k < senses.length; k++) {
					if (senses[k].length() == 0)
						continue;
					String sense = senses[k].substring(0, senses[k].indexOf(':'));
					if (this.wordNetHypernymMap.containsKey(sense))
						hypernymsForWord.addAll(this.wordNetHypernymMap.get(sense));
				}
				
				hypernyms[i][j] = new WordNet.Hypernym[hypernymsForWord.size()];
				hypernyms[i][j] = hypernymsForWord.toArray(hypernyms[i][j]);
			}
			
			i++;
		}
		
		return hypernyms;
	}
}
