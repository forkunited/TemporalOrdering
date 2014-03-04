package temp.scratch;

import java.io.IOException;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import temp.data.annotation.Language;
import temp.data.annotation.TempDocument;
import temp.data.annotation.nlp.AnnotatorFreeLing;
import temp.util.TempProperties;

public class Scratch {
	public static void main(String[] args) {
		String[] texts = {"El perro persigue el gato.", "El gato persigue el perro."};
		
		TempProperties properties = new TempProperties();
		AnnotatorFreeLing freeLingAnnotator = new AnnotatorFreeLing(properties);
		for (int i = 0; i < texts.length; i++) {
			TempDocument document = TempDocument.createFromText("Test", texts[i], Language.Spanish, freeLingAnnotator);
			//JSONObject jsonDocument = document.toJSON();
			//System.out.println(jsonDocument.toString());
			
			Element element = document.toXML();
			XMLOutputter op = new XMLOutputter(Format.getPrettyFormat());
			try {
				op.output(element, System.out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
