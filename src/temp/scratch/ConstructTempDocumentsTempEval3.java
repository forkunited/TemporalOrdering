package temp.scratch;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ark.data.annotation.Language;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.model.annotator.nlp.NLPAnnotatorMultiLanguage;
import temp.util.TempProperties;

/**
 * ConstructTempDocumentsTempEval3 takes the following arguments:
 * 
 * [inputPath] - Path to TempEval3 source data set directory
 * [outputPath] - Path to output directory for serialized TempDocumentSet
 * [language] - Language of the input TempEval2 data
 * 
 * And constructs a temp.data.annotation.TempDoccumentSet from 
 * the TempEval3 [language] data at the [inputPath] directory, 
 * serializing it as JSON files to the [outputPath] directory.
 * 
 * The output TempDocumentSet represents TempEval3 documents extended
 * with various NLP annotations (parses, PoSTags, tokenizations, etc).
 * The NLP annotations are generated using 
 * temp.model.annotator.nlp.NLPAnnotatorMultiLanguage.
 * 
 * IMPORTANT: I (Bill) believe this class is currently buggy, and doesn't 
 * produce an entirely correct conversion of the TempEval3 data.  I 
 * remember that I became frustrated with getting this to work, and I instead
 * converted the TempEval3 data to the NLP annotated JSON format using 
 * Nate Chambers' TimeSieve 'infofile' version 
 * of the TempEval3 data instead of the original
 * official version.  You can re-do this conversion by running 
 * temp.scratch.ConstructTempDocumentsTimeSieve on the TempEval3 'infofile'.
 * This might be on cab cluster with the other temporal ordering data,
 * or you can ask me for a copy if you can't find it on cab.
 * 
 * @author Bill McDowell
 *
 */
public class ConstructTempDocumentsTempEval3 {
	public static void main(String[] args) {
		String inputPath = args[0];
		String outputPath = args[1];
		Language language = Language.valueOf(args[2]);
		
		File inputDirectory = new File(inputPath);
		File[] files = inputDirectory.listFiles();
		NLPAnnotatorMultiLanguage nlpAnnotator = new NLPAnnotatorMultiLanguage(new TempProperties(), language);
		for (File file : files) {
			if (!file.getName().endsWith(".tml")) {
				System.out.println(file.getName() + " does not end with '.tml'.  Skipping...");
				continue;
			}
			
			if ((new File(outputPath, file.getName() + ".json")).exists()) {
				System.out.println("Output for " + file.getName() + " already exists.  Skipping...");
				continue;
			}
			
			System.out.print("Loading TimeML document " + file.getName() + "... ");
			TempDocument document = TempDocument.fromTimeML(loadXMLFromFile(file), nlpAnnotator, language);
			System.out.println("Done.");
			
			TempDocumentSet documentSet = new TempDocumentSet();
			documentSet.addDocument(document);
			documentSet.saveToJSONDirectory(outputPath);
		}
	}
	
	private static Element loadXMLFromFile(File file) {
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try {
			document = builder.build(new File(file.getAbsolutePath()));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return document.getRootElement();
	}
	
}
