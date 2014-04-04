package temp.model.annotator.timeml.tlink;

import java.util.*;

import temp.data.annotation.TempDocument;
//import ark.data.annotation.nlp.TypedDependency;
import temp.data.annotation.timeml.TLinkable;


// FIXME: Need to refactor this if we are using it for something.
public class EdgeFeatures {
	Set<String> indicatorFeatures;
/*	private final TLinkable first;
	private final TLinkable second;
*/
	public EdgeFeatures(TLinkable first, TLinkable second, TempDocument doc, int sentenceNum){
		indicatorFeatures = new HashSet<String>();
/*		this.first = first;
		this.second = second;
	*/	
		computeFeatures(doc, sentenceNum);
	}
	
	
	private void computeFeatures(TempDocument doc, int sentenceNum){
		//computeDependencyPath(doc, sentenceNum);
	}
/*
	
	private void computeDependencyPath(TempDocument doc, int sentenceNum) {
		List<TypedDependency> childrenOfFirst = 
				doc.getChildDependencies(sentenceNum, first.getTokenSpan().getStartTokenIndex());
		List<TypedDependency> parentsOfFirst = 
				doc.getParentDependencies(sentenceNum, first.getTokenSpan().getStartTokenIndex());
		
		addOneStepFeats(childrenOfFirst, "firstParentOfSecond");
		addOneStepFeats(parentsOfFirst, "firstChildOfSecond");
	}


	private void addOneStepFeats(List<TypedDependency> childrenOfFirst,
			String featName) {
		for (int i = 0; i < childrenOfFirst.size(); i++){
			TypedDependency child = childrenOfFirst.get(i);
			if (child.getChildTokenIndex() == second.getTokenSpan().getStartTokenIndex()){
				indicatorFeatures.add("firstParentOfChild");
			}
		}
		
	}*/
}
