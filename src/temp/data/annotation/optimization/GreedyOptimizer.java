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
	private int numOneHotVerts;
	private int numFixedVerts;
	
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

	// input: nothing
	// output: takes the graph I have tries to find a MAP assignment.
	// things to print out: average number of iterations for the K graphs
	//						average number of changes per iteration
	//						average number of changes in the first iteration
	//						average number of changes in the first five iterations
	public Map<TLinkDatum<L>, L> optimize(){
		double avgNumIters = 0;
		int maxNumIters = -1;
		int minNumIters = Integer.MAX_VALUE;
		double avgNumChangesPerIter = 0;
		double avgNumChangesFirstIter = 0;
		double avgNumChangesFirstFiveIter = 0;
		double[] avgNumValidMoves = new double[6];
		double[] avgNumValidMovesFirstIter = new double[6];
		
		double maxScore = Double.NEGATIVE_INFINITY;
		
		// to build the factor graph
		FactorGraph<L> graph = new FactorGraph<L>(scoredDatumLabels, 
				fixedDatumLabels, validLabels, labelMapping, compositionRules);
		graph.build();
		graph.initializeGraph();
		numFixedVerts = FactorGraph.getSetOfVerticesOfType(graph.getFactorGraph(), NodeType.onehotFixedVariableConstraint).size();
		Map<TLinkDatum<L>, L> bestAssignment = makeMapFromTLinkToLabels(graph.getFactorGraph());
		for (int i = 0; i < numInitialGraphs; i++){
			
			graph.initializeGraph();
			// for tracking the number of changes at each iteration
			int numIters = 0;
			int numChangesPerIter = 0;
			int numChangesFirstIter = 0;
			int numChangesFirstFiveIter = 0;
			double[] avgNumValidMovesOneGraph = new double[6];
			double[] avgNumValidMovesOneGrgaphFirstIter = new double[6];
			
			boolean foundValidMoveThisIter = false;
			do {
				foundValidMoveThisIter = false;
				// loop over the one-hot variables in a given graph
				Set<MyNode<L>> oneHotVerts = FactorGraph.getSetOfVerticesOfType(graph.getFactorGraph(), 
						temp.data.annotation.optimization.NodeType.onehotConstraint);
				numOneHotVerts = oneHotVerts.size();
				for (MyNode<L> oneHot : oneHotVerts){
					// choose highest scoring label for this node
					TreeMap<Double, List<L>> sortedLabels = new TreeMap<Double, List<L>>();
					fillSortedLabels(sortedLabels, oneHot, graph);
					
					// to count the number of valid moves, to be printed to console
					int numValidMoves = countNumValidMoves(oneHot, graph);
					avgNumValidMovesOneGraph[numValidMoves] += 1.0/oneHotVerts.size();
					if (numIters < 1){
						avgNumValidMovesOneGrgaphFirstIter[numValidMoves] += 1.0/oneHotVerts.size();
					}
					
					boolean foundMove = false;
					for (double score : sortedLabels.descendingKeySet()){
						for (L label : sortedLabels.get(score)){
							if (validMove(oneHot, label, graph)){

								oneHot.setActiveLabel(label);
								foundMove = true;
								foundValidMoveThisIter = true;
								numChangesPerIter += 1;
								if (numIters < 1){
									numChangesFirstIter += 1;
								}
								if (numIters < 5)
									numChangesFirstFiveIter += 1;
								break;
							}
						}
						if (foundMove)
							break;
					}
				}
				/*if (numChangesPerIter > 1 && numIters == 1){
					System.out.println("Num iters: " + numIters + ", and foundMove: " + foundValidMoveThisIter);
					System.out.println("First iteration value of numChangesPerIter: " + numChangesPerIter);
					System.out.println("First iteration value of numChangesFirstIter: " + numChangesFirstIter);
					System.out.println("First iteration value of numChangesFirstFiveIter: " + numChangesFirstFiveIter);
					System.exit(0);
				}*/
				numIters += 1;
			} while (foundValidMoveThisIter);
			double currentScore = computeScore(graph);
			if (currentScore > maxScore){
				maxScore = currentScore;
				bestAssignment = makeMapFromTLinkToLabels(graph.getFactorGraph());
			}
			
			// to update the counters for the number of iterations and number of changes
			avgNumIters += numIters / 100.0;
			if (maxNumIters < numIters)
				maxNumIters = numIters;
			if (minNumIters > numIters)
				minNumIters = numIters;
			avgNumChangesPerIter += (numChangesPerIter / numIters) / 100.0;
			avgNumChangesFirstIter += numChangesFirstIter / 100.0;
			avgNumChangesFirstFiveIter += (numChangesFirstFiveIter / Math.min(5.0, numIters)) / 100.0;
			for (int k = 0; k < avgNumValidMovesOneGraph.length; k++){
				avgNumValidMovesOneGraph[k] = avgNumValidMovesOneGraph[k] / numIters;
			}
			for (int k = 0; k < avgNumValidMoves.length; k++){
				avgNumValidMoves[k] += avgNumValidMovesOneGraph[k] / 100.0;
			}
			for (int k = 0; k < avgNumValidMovesFirstIter.length; k++){
				avgNumValidMovesFirstIter[k] += avgNumValidMovesOneGrgaphFirstIter[k] / 100.0;
			}
		}
		
		printInfoToConsole(avgNumIters, maxNumIters, minNumIters, avgNumChangesPerIter, 
				avgNumChangesFirstIter, avgNumChangesFirstFiveIter, maxScore, avgNumValidMoves, avgNumValidMovesFirstIter);
		return bestAssignment;
	}

	private void printInfoToConsole(double avgNumIters, double maxNumIters, double minNumIters, 
			double avgNumChangesPerIter, double avgNumChangesFirstIter, double avgNumChangesFirstFiveIter, double maxScore, 
			double[] avgNumValidMoves, double[] avgNumValidMovesFirstIter) {
		roundAvgNumValidMoves(avgNumValidMoves);
		roundAvgNumValidMoves(avgNumValidMovesFirstIter);
		
		System.out.println("Found one highest scoring assignment out of " + numInitialGraphs + " graphs! Score = " + 
				Math.round(maxScore) + ", # variables: " + numOneHotVerts + ", # fixed vars: " + numFixedVerts);
		System.out.println("Number of iterations: Average: " + avgNumIters + ", Min: " + minNumIters + ", Max: " + maxNumIters);
		System.out.println("Average number of changes per iteration: " + avgNumChangesPerIter);
		System.out.println("Average number of changes in the first iteration: " + avgNumChangesFirstIter);
		System.out.println("Average number of changes in the first five iterations: " + avgNumChangesFirstFiveIter);
		System.out.println("Average number of available moves across iterations and graphs: " + Arrays.toString(avgNumValidMoves));
		System.out.println("Average number of available moves in the first iteration across graphs: " + 
				Arrays.toString(avgNumValidMovesFirstIter));
		System.out.println();
	}
	
	private void roundAvgNumValidMoves(double[] avgNumValidMoves) {
		for (int i = 0; i < avgNumValidMoves.length; i++){
			avgNumValidMoves[i] = Math.round(1000*avgNumValidMoves[i]) / 1000.0;
		}
		
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
			if (oneHotForRel.getType() == NodeType.onehotConstraint || oneHotForRel.getType() == NodeType.onehotFixedVariableConstraint){
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
	
	// computes the number of valid changes that can be made to the current node without breaking transitivity
	private int countNumValidMoves(MyNode<L> oneHot, FactorGraph<L> graph) {
		int numMoves = 0;
		for (L label : validLabels){
			if (validMove(oneHot, label, graph) && oneHot.getActiveLabel() != label){
				numMoves++;
			}
		}
		return numMoves;
	}
}