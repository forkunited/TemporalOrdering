package temp.data.annotation.optimization;

import java.util.*;

import temp.util.TemporalDecoder;


import temp.data.annotation.TLinkDatum;

public class InterfaceWithADCubed<L> {
	FactorGraph<L> graph;
	ArrayList<MyNode<L>> variables;
	ArrayList<Double> scores;
	ArrayList<ArrayList<Integer>> oneHotConstraints;
	ArrayList<ArrayList<Integer>> transConstraints;
	ArrayList<Double> posteriors;
	Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels;
	Map<String, Integer> locationOfVarInList;
	Set<L> validLabels;
	
	
	
	public InterfaceWithADCubed(FactorGraph<L> graph,
			Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels, Set<L> validLabels) {
		this.graph = graph;
		this.scoredDatumLabels = scoredDatumLabels;
		this.variables = new ArrayList<MyNode<L>>();
		this.scores = new ArrayList<Double>();
		this.oneHotConstraints = new ArrayList<ArrayList<Integer>>();
		this.transConstraints = new ArrayList<ArrayList<Integer>>();
		this.locationOfVarInList = new HashMap<String, Integer>();
		this.validLabels = validLabels;
	}

	public Map<TLinkDatum<L>, L> generateADCubedVariables(boolean firstIter){
		// loop over the variable nodes
		//		add each to the arraylist
		//		add each score to the score arraylist
		// loop over the onehot constraints
		//		add each to the onehot constraints
		// loop over the trans constraints
		//		add each to the trans constraints
		
		// if useExact is true, we will branch and bound to get integer solutions
		boolean useExactInference = !firstIter;
		
		fillInVariables();
		findTransitiveConstraints();
		fillOnehotConstraints();
		makePosteriors();
		new TemporalDecoder().decode_graph(scores, oneHotConstraints, transConstraints, posteriors, false); // normally pass useExactInefrence here
		return generateMapFromTLinkToLabel();
		//printAll();
	}
	
	private Map<TLinkDatum<L>, L> generateMapFromTLinkToLabel(){
		Map<TLinkDatum<L>, Map<L, Double>> tLinksToPosterior = new HashMap<TLinkDatum<L>, Map<L, Double>>();
		for (int i = 0; i < posteriors.size(); i++){
			TLinkDatum<L> curDatum = variables.get(i).getTLink();
			if (!tLinksToPosterior.containsKey(curDatum)){
				tLinksToPosterior.put(curDatum, new HashMap<L, Double>());
			}
			if (tLinksToPosterior.get(curDatum).containsKey(variables.get(i).getLabel())){
				throw new IllegalArgumentException("Probelm! We have two binary variables which correspond to the same label and tlink!!");
			} else {
				tLinksToPosterior.get(curDatum).put(variables.get(i).getLabel(), posteriors.get(i));
			}			
		}
		
		Map<TLinkDatum<L>, L> result = new HashMap<TLinkDatum<L>, L>();
		// for each tlink, find the max in its associated hashmap
		for (TLinkDatum<L> curTLink : tLinksToPosterior.keySet()){
			L maxLabel = findMax(tLinksToPosterior.get(curTLink));
			if (maxLabel == null)
				throw new IllegalArgumentException("Problem finding the max label for a given TLink!!");
			result.put(curTLink, maxLabel);
			
		}
		
		return result;
	}
	
	private L findMax(Map<L, Double> posteriorForOne){
		double max = -1;
		L maxLabel = null; 
		for (L label : posteriorForOne.keySet()){
			if (posteriorForOne.get(label) > max){
				max = posteriorForOne.get(label);
				maxLabel = label;
			}
		}
		return maxLabel;
	}
	
	private void makePosteriors(){
		posteriors = new ArrayList<Double>();
		for (int i = 0; i < variables.size(); i++){
			posteriors.add(0.0);
		}
	}
	
	private void printAll(){
		for (MyNode<L> var : variables){
			System.out.println(var.toString());
		}
		System.out.println();
		for (double score : scores){
			System.out.println(score);
		}
		System.out.println();
		for (List<Integer> constraints : oneHotConstraints){
			for (int i : constraints){
				System.out.print(i);
			}
			System.out.println();
		}
		System.out.println();
		for (List<Integer> constraints : transConstraints){
			for (int i : constraints){
				System.out.print(i);
			}
			System.out.println();
		}
		for (double post : posteriors){
			System.out.println(post);
		}
		System.exit(0);
	}
	
	private void fillInVariables(){
		for (MyNode<L> binVar : FactorGraph.getSetOfVerticesOfType(graph.getFactorGraph(), temp.data.annotation.optimization.NodeType.binarizedVariable)){
			locationOfVarInList.put(binVar.toString(), variables.size());
			variables.add(binVar);
			scores.add(scoredDatumLabels.get(binVar.getTLink()).get(binVar.getLabel()));
		}
	}
	
	private void findTransitiveConstraints(){
		for (MyNode<L> transConstraint : FactorGraph.getSetOfVerticesOfType(graph.getFactorGraph(), temp.data.annotation.optimization.NodeType.binarizedTransitiveConstraint)){
			ArrayList<Integer> constrainedLocations = new ArrayList<Integer>();
			for (Map<L, MyNode<L>> var : transConstraint.getBinarizedRels()){
				for (L label : var.keySet()){
					constrainedLocations.add(locationOfVarInList.get(var.get(label).toString()));
				}
			}
			transConstraints.add(constrainedLocations);
		}
	}
	
	private void fillOnehotConstraints(){
		for (MyNode<L> transConstraint : FactorGraph.getSetOfVerticesOfType(graph.getFactorGraph(), temp.data.annotation.optimization.NodeType.onehotConstraint)){
			ArrayList<Integer> onehot = new ArrayList<Integer>();
			for (Map<L, MyNode<L>> binaryNodes : transConstraint.getBinarizedRels()){
				for (L label : binaryNodes.keySet()){
					onehot.add(locationOfVarInList.get(binaryNodes.get(label).toString()));
				}
			}
			oneHotConstraints.add(onehot);
		}

	}
}
