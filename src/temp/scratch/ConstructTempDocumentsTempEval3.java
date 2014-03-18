package temp.scratch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ark.data.annotation.Language;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;


public class ConstructTempDocumentsTempEval3 {
	public static void main(String[] args) {
		String inputPath = args[0];
		String outputPath = args[1];
		Language language = Language.valueOf(args[2]);
		
		File inputDirectory = new File(inputPath);
		File[] files = inputDirectory.listFiles();
		for (File file : files) {
			if (!file.getName().endsWith(".tml"))
				continue;
			
			TempDocument document = constructFromFile(file);
			
			TempDocumentSet documentSet = new TempDocumentSet();
			documentSet.addDocument(document);
			documentSet.saveToJSONDirectory(outputPath);
		}
	}

	private static TempDocument constructFromFile(File inputFile) {
		Element timeMLElement = loadXMLFromFile(inputFile);
		
		
		return null;
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
