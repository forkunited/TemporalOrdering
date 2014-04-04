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
			
				
			TempDocument document = TempDocument.fromTimeML(loadXMLFromFile(file), nlpAnnotator, language);
			
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
