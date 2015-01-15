package temp.data.annotation.optimization;

import java.util.*;
import java.util.Map.Entry;

import ark.data.annotation.Datum.Tools.LabelMapping;

import edu.uci.ics.jung.graph.Graph;

import temp.data.annotation.TLinkDatum;


/*
 * Consider: Taking the binarized graph from GraphBuilderForADCubed, then finding correct moves via that.
 * This is probably a good idea, because then i have to make sure my graph is correct.
 */

public class GreedyOptimizer<L>{
	private Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels;
	private Map<TLinkDatum<L>, L> fixedDatumLabels;
	private Set<L> validLabels;
	private LabelMapping<L> labelMapping;
	private L[][][] compositionRules;
	private int numInitialGraphs;
	
	public GreedyOptimizer(
			Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels,
			Map<TLinkDatum<L>, L> fixedDatumLabels, Set<L> validLabels,
			LabelMapping<L> labelMapping, L[][][] compositionRules,
			int numInitialGraphs) {
		this.scoredDatumLabels = scoredDatumLabels;
		this.fixedDatumLabels = fixedDatumLabels;
		this.validLabels = validLabels;
		this.labelMapping = labelMapping;
		this.compositionRules = compositionRules;
		this.numInitialGraphs = numInitialGraphs;
	}

	public Map<TLinkDatum<L>, L> optimize(){

		double maxScore = Double.NEGATIVE_INFINITY;
		FactorGraph<L> bestGraph = null;
		for (int i = 0; i < numInitialGraphs; i++){
			FactorGraph<L> graph = new FactorGraph<L>(scoredDatumLabels, 
					fixedDatumLabels, validLabels, labelMapping, compositionRules);
			graph.build();
			graph.initializeGraph();
			boolean foundValidMoveThisIter = false;
			do {
				foundValidMoveThisIter = false;
				// loop over the one-hot variables in a given graph
				for (MyNode<L> oneHot : FactorGraph.getSetOfVerticesOfType(graph.getFactorGraph(), temp.data.annotation.optimization.NodeType.onehotConstraint)){
					// choose highest scoring label for this node
					TreeMap<Double, List<L>> sortedLabels = new TreeMap<Double, List<L>>();
					fillSortedLabels(sortedLabels, oneHot, graph);
					
					boolean foundMove = false;
					for (double score : sortedLabels.descendingKeySet()){
						for (L label : sortedLabels.get(score)){
							if (validMove(oneHot, label, graph)){

								oneHot.setActiveLabel(label);
								foundMove = true;
								foundValidMoveThisIter = true;
								break;
							}
						}
						if (foundMove)
							break;
					}
				}
			} while (foundValidMoveThisIter);
			double currentScore = computeScore(graph);
			if (currentScore > maxScore){
				maxScore = currentScore;
				bestGraph = graph;
			}
		}
		System.out.println("Found one highest scoring assignment! Score = " + maxScore);
		return makeMapFromTLinkToLabels(bestGraph.getFactorGraph());
	}
	
	/*
	private Map<TLinkDatum<L>, L> findHighestScoringAssignment(){
		double maxScore = Double.NEGATIVE_INFINITY;
		int indexOfMaxScore = -1;		
		for (int i = 0; i < graphs.size(); i++){
			FactorGraph<L> graph = graphs.get(i);
			double score = computeScore(graph);
			if (score > maxScore){
				maxScore = score;
				indexOfMaxScore = i;
			}
		}
		return makeMapFromTLinkToLabels(graphs.get(indexOfMaxScore).getFactorGraph());
	}*/
	
	private Map<TLinkDatum<L>, L> makeMapFromTLinkToLabels(Graph<MyNode<L>, String> graph){
		Map<TLinkDatum<L>, L> MAP = new HashMap<TLinkDatum<L>, L>();
		for (MyNode<L> oneHot : FactorGraph.getSetOfVerticesOfType(graph, temp.data.annotation.optimization.NodeType.onehotConstraint)){
			MAP.put(oneHot.getTLink(), oneHot.getActiveLabel());
		}
		return MAP;
	}
	
	// to compute the score of a given graph
	private double computeScore(FactorGraph<L> graph){
		double score = 0;
		for (MyNode<L> oneHot : FactorGraph.getSetOfVerticesOfType(graph.getFactorGraph(), temp.data.annotation.optimization.NodeType.onehotConstraint)){
			score += scoredDatumLabels.get(oneHot.getTLink()).get(oneHot.getActiveLabel());
		}
		return score;
	}
	
	private boolean validMove(MyNode<L> oneHot, L label, FactorGraph<L> graph){
		MyNode<L> binaryVarNode = oneHot.getOneHotBinarizedRels().get(label);
		for (MyNode<L> constraint : graph.getFactorGraph().getNeighbors(binaryVarNode)){
			if (constraint.getType() != NodeType.binarizedTransitiveConstraint){
				continue;
			}
			MyNode<L> firstRel;
			MyNode<L> secondRel;
			MyNode<L> consequentRel;
			if (getNodeFromWithinConstraint(0, constraint) == binaryVarNode){
				firstRel = binaryVarNode;
				secondRel = getNodeFromWithinConstraint(1, constraint);
				consequentRel = getNodeFromWithinConstraint(2, constraint);
			} else if (getNodeFromWithinConstraint(1, constraint) == binaryVarNode){
				firstRel = getNodeFromWithinConstraint(0, constraint);
				secondRel = binaryVarNode;
				consequentRel = getNodeFromWithinConstraint(2, constraint);
			} else if (getNodeFromWithinConstraint(2, constraint) == binaryVarNode){
				firstRel = getNodeFromWithinConstraint(0, constraint);
				secondRel = getNodeFromWithinConstraint(1, constraint);
				consequentRel = binaryVarNode;
			} else
				throw new IllegalArgumentException("Problem when finding neighbors to constraint in graph!");
			if (!validTriplet(firstRel, secondRel, consequentRel, graph)){
				return false;
			}
		}
		return true;
	}
	
	
	// tests a particular set of three nodes to see if the associated one-hot nodes are activated
	// returns false only if the first and second relations are active and the consequent isn't. 
	private boolean validTriplet(MyNode<L> firstRel, MyNode<L> secondRel, MyNode<L> consequentRel, FactorGraph<L> graph){
		boolean firstActive = isBinaryVariableActive(firstRel, graph.getFactorGraph());
		boolean secondActive = isBinaryVariableActive(secondRel, graph.getFactorGraph());
		boolean consequentActive = isBinaryVariableActive(consequentRel, graph.getFactorGraph());
		return !(firstActive && secondActive && !consequentActive);
	}
	
	private boolean isBinaryVariableActive(MyNode<L> node, Graph<MyNode<L>, String> graph){
		for (MyNode<L> oneHotForRel : graph.getNeighbors(node)){
			if (oneHotForRel.getType() == NodeType.onehotConstraint){
				return (oneHotForRel.getActiveLabel() == node.getLabel());
			}
		}
		throw new IllegalStateException("Apparently there's a binary variable node without a one-hot vector. Bad news bears!");
	}
	
	// gets the ith node from within the list of binarized variables within a given constraint node
	private MyNode<L> getNodeFromWithinConstraint(int i, MyNode<L> constraint){
		for (L labelForWhichToGetNode : constraint.getBinarizedRels().get(i).keySet()){
			return constraint.getBinarizedRels().get(i).get(labelForWhichToGetNode);
		}
		return null;
	}
	
	private void fillSortedLabels(TreeMap<Double, List<L>> sortedLabels, MyNode<L> oneHot, FactorGraph<L> graph) {
		double currentScore = scoredDatumLabels.get(oneHot.getTLink()).get(oneHot.getActiveLabel());
		for (L label : scoredDatumLabels.get(oneHot.getTLink()).keySet()){
			double scoreImprovement = scoredDatumLabels.get(oneHot.getTLink()).get(label)-currentScore;
			if (scoreImprovement > 0){
				if (sortedLabels.containsKey(scoreImprovement)){
					sortedLabels.get(scoreImprovement).add(label);
				} else{
					ArrayList<L> newList = new ArrayList<L>();
					newList.add(label);
					sortedLabels.put(scoreImprovement, newList);
				}
			}
		}
	}
}