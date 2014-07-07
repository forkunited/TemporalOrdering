package temp.scratch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ark.data.annotation.nlp.PoSTag;
import ark.model.annotator.nlp.NLPAnnotatorStanford;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;

/**
 * ConstructTempDocumentsTimeSieve takes the following arguments:
 * 
 * [inputPath] - Path to TimeSieve 'infofile' source dataset XML file
 * [outputPath] - Path to output directory for serialized TempDocumentSet
 * 
 * And constructs a temp.data.annotation.TempDoccumentSet from 
 * the 'infofile' at [inputPath], 
 * serializing it as JSON files to the [outputPath] directory.
 * 
 * The output TempDocumentSet represents the input TimeSieve 'infofile' 
 * documents extended
 * with various NLP annotations (parses, PoSTags, tokenizations, etc).
 * The NLP annotations are generated using 
 * ark.model.annotator.nlp.NLPAnnotatorStanford (which assumes the input
 * file represents an English dataset).
 * 
 * @author Bill McDowell
 *
 */
public class ConstructTempDocumentsTimeSieve {
	public static void main(String[] args) {
		String inputPath = args[0];
		String outputPath = args[1];
		
		TempDocumentSet documentSet = constructFromInfoFile(inputPath);
		if (documentSet == null)
			return;
		documentSet.saveToJSONDirectory(outputPath);
	}
	
	@SuppressWarnings("unchecked")
	public static TempDocumentSet constructFromInfoFile(String filePath) {
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try {
			document = builder.build(new File(filePath));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		NLPAnnotatorStanford nlpAnnotator = new NLPAnnotatorStanford();
		TempDocumentSet documentSet = new TempDocumentSet();
		Element element = document.getRootElement();
		List<Element> fileElements = (List<Element>)element.getChildren("file");
		int i = 0;
		for (Element fileElement : fileElements) {
			TempDocument tempDocument = new TempDocument(fileElement);
			
			PoSTag[][] documentTags = new PoSTag[tempDocument.getSentenceCount()][];
			for (int j = 0; j < tempDocument.getSentenceCount(); j++) {
				nlpAnnotator.setText(tempDocument.getSentence(j));
				PoSTag[][] sentenceTags = nlpAnnotator.makePoSTags();
				documentTags[j] = new PoSTag[tempDocument.getSentenceTokenCount(j)];
				int k = 0;
				for (int r = 0; r < sentenceTags.length; r++) {
					for (int c = 0; c < sentenceTags[r].length; c++) {
						documentTags[j][k] = sentenceTags[r][c];
						k++;
					}
				}
			}

			if (!tempDocument.setPoSTags(documentTags))
				return null;
			
			documentSet.addDocument(tempDocument);
			System.out.println("Loaded document " + tempDocument.getName() +  " (" + i + ").");
			i++;
		}
		
		return documentSet;
	}
}
