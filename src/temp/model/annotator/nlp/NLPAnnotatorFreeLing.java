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
import java.util.Stack;

import ark.util.FileUtil;
import ark.util.Pair;
import ark.model.annotator.nlp.NLPAnnotator;
import ark.data.annotation.Document;
import ark.data.annotation.Language;
import ark.data.annotation.nlp.ConstituencyParse;
import ark.data.annotation.nlp.DependencyParse;
import ark.data.annotation.nlp.PoSTag;
import ark.data.annotation.nlp.TokenSpan;
import ark.data.annotation.nlp.WordNet;
import ark.data.annotation.nlp.ConstituencyParse.Constituent;
import ark.data.annotation.nlp.DependencyParse.Dependency;
import ark.data.annotation.nlp.DependencyParse.Node;

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
import edu.upc.freeling.TreeNode;
import edu.upc.freeling.Ukb;
import edu.upc.freeling.Util;
import edu.upc.freeling.VectorWord;

import temp.util.TempProperties;

/**
 * NLPAnnotatorFreeLing supplements a text with NLP 
 * annotations (parses, part-of-speech tags, etc) using 
 * the FreeLing NLP pipeline.  This pipeline works on several
 * languages including English and Spanish.
 * 
 * The NLPAnnotatorFreeLing class currently only works on Windows
 * machines, but can be fixed to work on other platforms
 * as well.
 * 
 * The returned NLP annotations are in the ARKWater 
 * (https://github.com/forkunited/ARKWater)
 * library's format.
 * 
 * Once constructed for a specified language, the annotator 
 * object can be used by calling
 * the setText method to set the text to be annotated, and
 * then calling the make[X] methods to retrieve the annotations
 * for that text.
 * 
 * @author Bill McDowell
 *
 */
public class NLPAnnotatorFreeLing extends NLPAnnotator {
	private TempProperties properties;
	
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
	
	/**
	 * @param properties
	 *
	 * Sets up the FreeLing pipeline, assuming that the CLASSPATH
	 * and other environment variables are set appropriately (see
	 * FreeLing documentation)
	 */
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
		String wordNetFilePath = new File(this.properties.getFreeLingDataDirPath(), "common/wn30.src").getAbsolutePath();
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
	
	/**
	 * @param language
	 * @return true if the language of the annotator is successfully 
	 * set.  If this method is not called explicitly, then the default is 
	 * Spanish (since this is the language we were using FreeLing for primarily)
	 */
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

	    String langPath = new File(this.properties.getFreeLingDataDirPath(), langStr).getAbsolutePath();
	    String commonPath = new File(this.properties.getFreeLingDataDirPath(), "common").getAbsolutePath();
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
	
	/**
	 * @param text
	 * @return true if the annotator has received the text 
	 * and is ready to return annotations for it
	 */
	public boolean setText(String text) {
		this.text = text;
		
		this.textWords = this.tokenizer.tokenize(text);
		this.textSentences = this.sentenceSplitter.split(this.textWords, false);
		
		this.morphologyAnalyzer.analyze(this.textSentences);
		if (!this.disabledPoSTags)
			this.posTagger.analyze(this.textSentences);
	    this.ner.analyze(this.textSentences);
	    this.senseDictionary.analyze(this.textSentences);
	    this.senseDisambiguator.analyze(this.textSentences);
	    if (!this.disabledConstituencyParses)
	    	this.parser.analyze(this.textSentences);
	    if (!this.disabledDependencyParses)
	    	this.dependencyParser.analyze(this.textSentences);
		return true;
	}
	
	/**
	 * @return an array of tokens for each segmented 
	 * sentence of the text.
	 */
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
	
	/**
	 * @return an array of part-of-speech tags for each segmented 
	 * sentence of the text.  This maps the FreeLing PoS tags into
	 * PennTreeBank PoS tags used by ARKWater.
	 */
	protected PoSTag[][] makePoSTagsInternal() {
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
	
	/**
	 * @return a dependency parse for each segmented 
	 * sentence of the text.
	 */
	protected DependencyParse[] makeDependencyParsesInternal(Document document, int sentenceIndexOffset) {
		DependencyParse[] parses = new DependencyParse[(int)this.textSentences.size()];
		ListSentenceIterator iterator = new ListSentenceIterator(this.textSentences);
		int i = 0;
		while (iterator.hasNext()) { 
			Sentence sentence = iterator.next();
			Queue<TreeDepnode> treeNodes = new LinkedList<TreeDepnode>();
			
			Map<Integer, Pair<List<DependencyParse.Dependency>, List<DependencyParse.Dependency>>> nodesToDeps = new HashMap<Integer, Pair<List<DependencyParse.Dependency>, List<DependencyParse.Dependency>>>();
			parses[i] = new DependencyParse(document, sentenceIndexOffset + i, null, null);
			int maxIndex = 0;

			treeNodes.add(sentence.getDepTree());
			boolean isRoot = true;
			while (!treeNodes.isEmpty()) {
				TreeDepnode nextNode = treeNodes.remove();
				
				int govIndex = -1;
				if (!isRoot) {
					govIndex = (int)nextNode.getParent().getInfo().getWord().getPosition();
				}
				
				int depIndex = (int)nextNode.getInfo().getWord().getPosition();
				String type = nextNode.getInfo().getLabel();
					
				maxIndex = Math.max(depIndex, Math.max(govIndex, maxIndex));
				
				DependencyParse.Dependency dependency = parses[i].new Dependency(govIndex, depIndex, type);
				
				if (!nodesToDeps.containsKey(govIndex))
					nodesToDeps.put(govIndex, new Pair<List<Dependency>, List<Dependency>>(new ArrayList<Dependency>(), new ArrayList<Dependency>()));
				if (!nodesToDeps.containsKey(depIndex))
					nodesToDeps.put(depIndex, new Pair<List<Dependency>, List<Dependency>>(new ArrayList<Dependency>(), new ArrayList<Dependency>()));
				
				nodesToDeps.get(govIndex).getSecond().add(dependency);
				nodesToDeps.get(depIndex).getFirst().add(dependency);
				
				int numChildren = (int)nextNode.numChildren();
				for (int j = 0; j < numChildren; j++) {
					treeNodes.add(nextNode.nthChildRef(j));
				}
				
				isRoot = false;
			}
			
			Node[] tokenNodes = new Node[maxIndex+1];
			for (int j = 0; j < tokenNodes.length; j++)
				if (nodesToDeps.containsKey(j))
					tokenNodes[j] = parses[i].new Node(j, nodesToDeps.get(j).getFirst().toArray(new Dependency[0]), nodesToDeps.get(j).getSecond().toArray(new Dependency[0]));
			
			Node rootNode = parses[i].new Node(-1, new Dependency[0], nodesToDeps.get(-1).getSecond().toArray(new Dependency[0]));
			parses[i] = new DependencyParse(document, i + sentenceIndexOffset, rootNode, tokenNodes);
			
			i++;
		}
		return parses;
	}

	/**
	 * @return a constituency parse for each segmented 
	 * sentence of the text.
	 */
	protected ConstituencyParse[] makeConstituencyParsesInternal(Document document, int sentenceIndexOffset) {
		ConstituencyParse[] parses = new ConstituencyParse[(int)this.textSentences.size()];
		ListSentenceIterator iterator = new ListSentenceIterator(this.textSentences);
		int i = 0;
		while (iterator.hasNext()) { 
			Sentence sentence = iterator.next();
			Queue<TreeNode> treeNodes = new LinkedList<TreeNode>();
			Constituent root = null;
			Stack<Pair<TreeNode, List<Constituent>>> constituents = new Stack<Pair<TreeNode, List<Constituent>>>();
			
			parses[i] = new ConstituencyParse(document, i + sentenceIndexOffset, null);
			treeNodes.add(sentence.getParseTree());
			while (!treeNodes.isEmpty()) {
				TreeNode nextNode = treeNodes.remove();
				
				if (!constituents.isEmpty() && !nextNode.equals(constituents.peek().getFirst())) {
					Pair<TreeNode, List<Constituent>> currentNeighbor = constituents.pop();
					ConstituencyParse.Constituent constituent = parses[i].new Constituent(currentNeighbor.getFirst().getInfo().getLabel(), currentNeighbor.getSecond().toArray(new ConstituencyParse.Constituent[0]));
					constituents.peek().getSecond().add(constituent);
				}
				
				if (nextNode.numChildren() == 0) {
					int tokenIndex = (int)nextNode.getInfo().getWord().getPosition();
					String label = nextNode.getInfo().getWord().getTag();
					
					ConstituencyParse.Constituent constituent = parses[i].new Constituent(label, new TokenSpan(null, i, tokenIndex, tokenIndex + 1));
					if (!constituents.isEmpty())
						constituents.peek().getSecond().add(constituent);
					else
						root = constituent;
				} else {
					constituents.push(new Pair<TreeNode, List<Constituent>>(nextNode, new ArrayList<Constituent>()));
					int numChildren = (int)nextNode.numChildren();
					for (int j = numChildren - 1; j >= 0 ; j--) {
						treeNodes.add(nextNode.nthChildRef(j));
					}
				}
			}
			
			if (!constituents.isEmpty()) {
				Pair<TreeNode, List<Constituent>> rootTree = constituents.pop();
				root = parses[i].new Constituent(rootTree.getFirst().getInfo().getLabel(), rootTree.getSecond().toArray(new ConstituencyParse.Constituent[0]));
			}
			
			parses[i] = new ConstituencyParse(document, sentenceIndexOffset + i, root);
			
			i++;
		}
		return parses;
	}
	
	/**
	 * @return an array of hypernyms for each token of each segmented 
	 * sentence of the text.
	 */
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
