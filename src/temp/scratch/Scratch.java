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
import temp.data.annotation.timeml.Time;
import temp.model.annotator.nlp.NLPAnnotatorMultiLanguage;
import temp.model.annotator.timeml.TimeAnnotatorHeidel;
import temp.util.TempProperties;

public class Scratch {
	public static void main(String[] args) {
		TempProperties properties = new TempProperties();
		NLPAnnotatorMultiLanguage annotator = new NLPAnnotatorMultiLanguage(properties, Language.English);
		TempDocument document = TempDocument.createFromText("test", "The dog barks on March 30th 2013. I ran for 10 minutes.", Language.English, Calendar.getInstance().getTime(), annotator);
		TimeAnnotatorHeidel timeAnnotator = new TimeAnnotatorHeidel(properties);
		Time[][] times = timeAnnotator.makeTimes(document);
		for (int i = 0; i < times.length; i++) {
			for (int j = 0; j < times[i].length; j++) {
				System.out.println(times[i][j].toJSON().toString());
			}
		}
	}
}
