package temp.scratch;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.components.ResultFormatter;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;

import temp.data.annotation.Language;
import temp.data.annotation.TempDocument;
import temp.model.annotator.nlp.NLPAnnotatorMultiLanguage;
import temp.util.TempProperties;

public class Scratch {
	public static void main(String[] args) {
		/*HeidelTimeStandalone h = new HeidelTimeStandalone(de.unihd.dbs.uima.annotator.heideltime.resources.Language.SPANISH, DocumentType.NARRATIVES, OutputType.TIMEML);
		try {
			System.out.println(h.process("En 5 de Mayo, yo estoy en un casa.  Six days later, I'll eat pancakes.  Today, I'll bake a cake."));
		} catch (DocumentCreationTimeMissingException e) {
			// TODO Auto-generated catch block <!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">
			e.printStackTrace();
		}*/
		
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(new StringReader("<?xml version=\"1.0\"?>\n<TimeML>En <TIMEX3 tid=\"t2\" type=\"DATE\" value=\"XXXX-05-05\">5 de Mayo</TIMEX3>.</TimeML>"));
			Element el = doc.getRootElement();
			List x = el.getContent();
			for (int i = 0; i < x.size(); i++) {
				if (x.get(i).getClass() == Element.class)
					System.out.println(x.get(i));
			}
			/*
			List<Element> c = el.getChildren("TIMEX3");
			System.out.println(el.getContentSize());
			System.out.println(c.get(0).getText());*/
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*Map<String, Language> texts = new HashMap<String, Language>();
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
		}*/
	}
}
