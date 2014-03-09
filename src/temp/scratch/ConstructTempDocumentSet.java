package temp.scratch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ark.util.FileUtil;

import temp.data.annotation.TempDocument;
import temp.data.annotation.TempDocumentSet;
import temp.data.annotation.nlp.PoSTag;
import temp.model.annotator.nlp.NLPAnnotatorStanford;
import temp.util.TempProperties;

public class ConstructTempDocumentSet {
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
		
		NLPAnnotatorStanford nlpAnnotator = new NLPAnnotatorStanford(new TempProperties());
		TempDocumentSet documentSet = new TempDocumentSet();
		Element element = document.getRootElement();
		List<Element> fileElements = (List<Element>)element.getChildren("file");
		int i = 0;
		for (Element fileElement : fileElements) {
			TempDocument tempDocument = TempDocument.fromXML(fileElement);
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
	
	public static TempDocument constructFromTempEval2(String directoryPath) {
		Map<String, String[][]> baseSegmentation = readTSVFile(new File(directoryPath, "base-segmentation.tab").getAbsolutePath());
		Map<String, String[][]> dct = readTSVFile(new File(directoryPath, "dct.tab").getAbsolutePath());
		Map<String, String[][]> eventAttributes = readTSVFile(new File(directoryPath, "event-attributes.tab").getAbsolutePath());
		Map<String, String[][]> eventExtents = readTSVFile(new File(directoryPath, "event-extents.tab").getAbsolutePath());
		Map<String, String[][]> timexAttributes = readTSVFile(new File(directoryPath, "timex-attributes.tab").getAbsolutePath());
		Map<String, String[][]> timexExtents = readTSVFile(new File(directoryPath, "timex-extents.tab").getAbsolutePath());
		Map<String, String[][]> tlinksDctEvents = readTSVFile(new File(directoryPath, "tlinks-dct-events.tab").getAbsolutePath());
		Map<String, String[][]> tlinksEventTimex = readTSVFile(new File(directoryPath, "tlinks-event-timex.tab").getAbsolutePath());
		
		for (String file : baseSegmentation.keySet()) {
			
		}
		
		/* FIXME */
		return null;
	}
	
	private static Map<String, String[][]> readTSVFile(String path) {
		try {
			BufferedReader r = FileUtil.getFileReader(path);
			String line = null;
			Map<String, List<String[]>> namesToLines = new HashMap<String, List<String[]>>();
			while ((line = r.readLine()) != null) {
				String[] lineParts = line.split("\t");
				String name = lineParts[0];
				if (!namesToLines.containsKey(name))
					namesToLines.put(name, new ArrayList<String[]>());
				namesToLines.get(name).add(lineParts);
			}
			
			Map<String, String[][]> groupedLines = new HashMap<String, String[][]>();
			for (Entry<String, List<String[]>> entry : namesToLines.entrySet()) {
				String[][] lines = new String[entry.getValue().size()][];
				lines = entry.getValue().toArray(lines);
				groupedLines.put(entry.getKey(),  lines);
			}
			
			return groupedLines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
