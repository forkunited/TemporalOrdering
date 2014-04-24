package temp.model.annotator.timeml;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

import ark.data.annotation.Language;
import ark.model.annotator.nlp.NLPAnnotator;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;

import temp.data.annotation.TempDocument;
import temp.data.annotation.timeml.Time;
import temp.model.annotator.nlp.NLPAnnotatorMultiLanguage;
import temp.util.TempProperties;

public class TimeAnnotatorHeidel extends TimeAnnotator {
	private TempProperties properties;
	private HeidelTimeStandalone heidelTime;
	private SAXBuilder xmlBuilder;
	private NLPAnnotator nlpAnnotator;
	
	public TimeAnnotatorHeidel(TempProperties properties) {
		this.properties = properties;
		this.heidelTime = new HeidelTimeStandalone(de.unihd.dbs.uima.annotator.heideltime.resources.Language.ENGLISH, DocumentType.NEWS, OutputType.TIMEML);
		this.xmlBuilder = new SAXBuilder();
	}

	@Override
	public Time[][] makeTimes(TempDocument document) {
		if (!initializeForDocument(document))
			return null;
			
		int sentenceCount = document.getSentenceCount();
		Time[][] times = new Time[sentenceCount][];
		
		try {
			for (int i = 0; i < sentenceCount; i++) {
				String sentenceStr = document.getSentence(i);
				String timeMLStr = null;
				if (document.getCreationTime() == null)
					timeMLStr = this.heidelTime.process(sentenceStr);
				else 
					timeMLStr = this.heidelTime.process(sentenceStr, document.getCreationTime().getValue().getRange().getFirst().getTime());
				
				timeMLStr = timeMLStr.replace("<!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">", "");
				
				Document sentenceDoc = this.xmlBuilder.build(new StringReader(timeMLStr));
				times[i] = buildTimesFromTimeML(sentenceDoc.getRootElement(), document, i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return times;
	}
	
	private boolean initializeForDocument(TempDocument document) {
		if (document.getLanguage() == Language.English)
			this.heidelTime.setLanguage(de.unihd.dbs.uima.annotator.heideltime.resources.Language.ENGLISH);
		else if (document.getLanguage() == Language.Spanish)
			this.heidelTime.setLanguage(de.unihd.dbs.uima.annotator.heideltime.resources.Language.SPANISH);
		else
			return false;
		
		if (document.getCreationTime() == null)
			this.heidelTime.setDocumentType(DocumentType.NARRATIVES);
		else
			this.heidelTime.setDocumentType(DocumentType.NEWS);
		
		if (document.getNLPAnnotator() == null && this.nlpAnnotator == null) {
			this.nlpAnnotator = NLPAnnotatorMultiLanguage.forLanguage(this.properties, document.getLanguage());
		} else if (document.getNLPAnnotator() == null && this.nlpAnnotator != null) {
			// Keep it	
		} else if (document.getNLPAnnotator() != null && this.nlpAnnotator == null) {
			this.nlpAnnotator = NLPAnnotatorMultiLanguage.fromString(this.properties, document.getNLPAnnotator());
		} else if (document.getNLPAnnotator() != null && this.nlpAnnotator != null) {
			if (!document.getNLPAnnotator().equals(this.nlpAnnotator))
				this.nlpAnnotator = NLPAnnotatorMultiLanguage.fromString(this.properties, document.getNLPAnnotator());
		}
		
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	private Time[] buildTimesFromTimeML(Element timeMLElement, TempDocument document, int sentenceIndex) {
		List xmlParts = timeMLElement.getContent();
		List<Time> times = new LinkedList<Time>();
		int curTokenIndex = 0;
		for (int j = 0; j < xmlParts.size(); j++) {
			String text = null;
			Element timexElement = null;
			if (xmlParts.get(j).getClass() == Element.class) { // Timex
				timexElement = (Element)xmlParts.get(j);
				text = timexElement.getText();
			} else { // Outside Timex
				text = ((Text)xmlParts.get(j)).getText();
			}
			
			if (!this.nlpAnnotator.setText(text))
				return null;			
			
			String[][] tokens = this.nlpAnnotator.makeTokens();
			int numTokens = 0;
			if (tokens.length > 0) {
				numTokens = tokens[0].length;
			}
			
			if (timexElement != null)
				times.add(buildTimeFromTimexElement(timexElement, document, sentenceIndex, curTokenIndex, numTokens));
			
			curTokenIndex += numTokens;
		}
		
		Time[] timesArray = new Time[times.size()];
		return times.toArray(timesArray);
	}
	
	private Time buildTimeFromTimexElement(Element timexElement, TempDocument document, int sentenceIndex, int startTokenIndex, int length) {
		timexElement.setAttribute("offset", String.valueOf(startTokenIndex + 1));
		timexElement.setAttribute("length", String.valueOf(length));
		return Time.fromXML(timexElement, document, sentenceIndex, Time.ParseMode.ALL);
	}
}
