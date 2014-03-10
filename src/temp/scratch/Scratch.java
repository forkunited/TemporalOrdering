package temp.scratch;


import java.util.Calendar;
import java.util.regex.Pattern;

import temp.data.annotation.Language;
import temp.data.annotation.TempDocument;
import temp.data.annotation.nlp.WordNet;
import temp.data.annotation.timeml.Time;
import temp.model.annotator.nlp.NLPAnnotatorFreeLing;
import temp.model.annotator.timeml.TimeAnnotatorHeidel;
import temp.util.TempProperties;

public class Scratch {
	public static void main(String[] args) {
		
		Pattern p = Pattern.compile("(.*)\\((.*)-([0-9']*),(.*)-([0-9']*)\\)");
		
		Matcher m = p.matcher("input");
		
		boolean x = "hi(asdf-1, 200,000-2)".matches("(.*)\\((.*)-([0-9']*),(.*)-([0-9']*)\\)");
		System.out.println();
		
		
		
		/*TempProperties properties = new TempProperties();
		NLPAnnotatorFreeLing annotator = new NLPAnnotatorFreeLing(properties);
		annotator.setLanguage(Language.English);
		annotator.setText("I was at the carnival.");
		WordNet.Hypernym[][][] hypernyms = annotator.makeTokenHypernyms();
		for (int i = 0; i < hypernyms[0].length; i++) {
			for (int j = 0; j < hypernyms[0][i].length; j++) {
				System.out.print(hypernyms[0][i][j] + " ");
			}
			System.out.println();
		}*/
		
		/*****/
		/*
		TempDocument document = TempDocument.createFromText("test", "The dog barks on March 30th 2013. I ran for 10 minutes.", Language.English, Calendar.getInstance().getTime(), annotator);
		TimeAnnotatorHeidel timeAnnotator = new TimeAnnotatorHeidel(properties);
		Time[][] times = timeAnnotator.makeTimes(document);
		for (int i = 0; i < times.length; i++) {
			for (int j = 0; j < times[i].length; j++) {
				System.out.println(times[i][j].toJSON().toString());
			}
		}*/
	}
}
