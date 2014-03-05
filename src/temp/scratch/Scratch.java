package temp.scratch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import temp.data.annotation.Language;
import temp.data.annotation.TempDocument;
import temp.model.annotator.nlp.NLPAnnotatorMultiLanguage;
import temp.util.TempProperties;

public class Scratch {
	public static void main(String[] args) {
		Map<String, Language> texts = new HashMap<String, Language>();
		//texts.put("El perro persigue el gato.", Language.Spanish);
		//texts.put("El gato persigue el perro.", Language.Spanish);
		texts.put("Joe bakes a cake.", Language.English);
		texts.put("The cat flies.", Language.English);
		
		TempProperties properties = new TempProperties();
		NLPAnnotatorMultiLanguage annotator = new NLPAnnotatorMultiLanguage(properties, Language.English);
		for (Entry<String, Language> entry : texts.entrySet()) {
			TempDocument document = TempDocument.createFromText("Test", entry.getKey(), entry.getValue(), annotator);
			JSONObject jsonDocument = document.toJSON();
			System.out.println(jsonDocument.toString());
			
			Element element = document.toXML();
			XMLOutputter op = new XMLOutputter(Format.getPrettyFormat());
			try {
				op.output(element, System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
