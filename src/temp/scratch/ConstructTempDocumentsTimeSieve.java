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
			nlpAnnotator.setText(tempDocument.getText());
			PoSTag[][] tags = nlpAnnotator.makePoSTags();
			if (!tempDocument.setPoSTags(tags))
				return null;
			
			documentSet.addDocument(tempDocument);
			System.out.println("Loaded document " + tempDocument.getName() +  " (" + i + ").");
			i++;
		}
		
		return documentSet;
	}
}
