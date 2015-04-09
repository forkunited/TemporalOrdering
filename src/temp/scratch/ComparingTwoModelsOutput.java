package temp.scratch;
import java.io.*;
import java.util.*;

import ark.util.Pair;

public class ComparingTwoModelsOutput {
	public static void main(String[] args){
		String locOfFirstOutput = args[0];
		String locOfSecondOutput = args[1];
		Map<String, Pair<String, String>> firstPreds = readOutput(locOfFirstOutput);
		Map<String, Pair<String, String>> secondPreds = readOutput(locOfSecondOutput);
		compareTwoOutputs(firstPreds, secondPreds);
	}
	
	private static void compareTwoOutputs(Map<String, Pair<String, String>> firstPreds, Map<String, Pair<String, String>> secondPreds){
		String[] labels = {"ANYTHING","AFTER","BEFORE","INCLUDES","IS_INCLUDED","SIMULTANEOUS","VAGUE"};

		for (String label : labels){
			printContingencyTable(firstPreds, secondPreds, label);
		}
	}
	
	private static void printContingencyTable(Map<String, Pair<String, String>> firstPreds, 
			Map<String, Pair<String, String>> secondPreds, String label){
		int truetrue = 0;
		int truefalse = 0;
		int falsetrue = 0;
		int falsefalse = 0;
		
		for (String tlink : firstPreds.keySet()){
			Pair<String, String> f = firstPreds.get(tlink);
			Pair<String, String> s = secondPreds.get(tlink);
			
			if (label.equals("ANYTHING") || f.getSecond().equals(label)){
				// four cases:
				if (f.getFirst().equals(f.getSecond()) && s.getFirst().equals(s.getSecond()))
					truetrue++;
				else if (f.getFirst().equals(f.getSecond()) && !s.getFirst().equals(s.getSecond()))
					truefalse++;
				else if (!f.getFirst().equals(f.getSecond()) && s.getFirst().equals(s.getSecond()))
					falsetrue++;
				else if (!f.getFirst().equals(f.getSecond()) && !s.getFirst().equals(s.getSecond()))
					falsefalse++;
				else 
					throw new IllegalArgumentException("PROBLEM IN COMPARE TWO OUTPUTS!!");
			}
		}
		
		System.out.println(label);
		System.out.println("(model1 \\ model2)\tmodel2 t\tmodel2 f");
		System.out.println("model1 t\t" + truetrue + "\t" + truefalse);
		System.out.println("model1 f\t" + falsetrue + "\t" + falsefalse);
		System.out.println();
	}
	
	private static Map<String, Pair<String, String>> readOutput(String loc){
		// key: tlink name
		// value: pair of <predicted relation, true relation>
		Map<String, Pair<String, String>> predictions = new HashMap<String, Pair<String, String>>();
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(loc));
			String pred = null;
			String gold = null;
			String tlink = null;
			while ((sCurrentLine = br.readLine()) != null) {

				if (sCurrentLine.startsWith("PREDICTED:")){
					pred = sCurrentLine.split(" ")[1];
				} else if (sCurrentLine.startsWith("ACTUAL:"))
					gold = sCurrentLine.split(" ")[1];
				else if (sCurrentLine.startsWith("temp."))
					tlink = sCurrentLine.split("@")[1];
				else if (sCurrentLine.equals("") && pred != null){
					predictions.put(tlink, new Pair<String, String>(pred, gold));
					pred = null;
					gold = null;
					tlink = null;
				}
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return predictions;
	}
}
