package temp.data.annotation.optimization;

import java.util.*;

import temp.data.annotation.TLinkDatum;


import edu.uci.ics.jung.graph.Graph;

public class ADCubedOptimizer<L> {
	private Set<L> validLabels;
	private Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels;
	
	public ADCubedOptimizer(Set<L> validLabels, Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels){
		this.validLabels = validLabels;
		this.scoredDatumLabels = scoredDatumLabels;
	}
	
	public void optimize(FactorGraph<L> graph){
		
		Graph<MyNode<L>, String> factorGraph = graph.getFactorGraph();
		initializeGraph(factorGraph);
		int iteration = 0;
		while (stoppingCriteriaNotMet){
			iteration++;
			updateQs(factorGraph, iteration);
		}
	}
	
	private void updateQs(Graph<MyNode<L>, String> factorGraph, int iteration){
		for (MyNode<L> curVertex : FactorGraph.getSetOfVerticesOfType(factorGraph, NodeType.binarizedTransitiveConstraint)){
			Map<String, Double> z_is = generateZs(curVertex, factorGraph, iteration);
			Map<String, Double> projectedZ_is = projectZsOntoSimplex(z_is);
			
		}
		for (MyNode<L> curVertex : FactorGraph.getSetOfVerticesOfType(factorGraph, NodeType.onehotConstraint)){
			// something here like above
			
		}
	}
	
	private Map<String, Double> projectZsOntoSimplex(Map<String, Double> z_is){
		Map<String, Double> projectedZ_is = new HashMap<String, Double>();
		Map<Double, List<String>> sortedZ_is = new TreeMap<Double, List<String>>();
		for (String varName : z_is.keySet()){
			if (!sortedZ_is.containsKey(z_is.get(varName)))
				sortedZ_is.put(z_is.get(varName), new ArrayList<String>());
			sortedZ_is.get(varName).add(varName);
		}
		int rho = 0;
		int j = 0;
		for (double y_0j : sortedZ_is.keySet()){
			j++;
			
			// to compute the sum
			int r = 0;
			double sumUpToR = 0;
			for (double y_0r : sortedZ_is.keySet()){
				r++;
				sumUpToR += y_0r;
				if (r == j)
					break;
			}
			
			if (y_0j - (1.0/j)*(sumUpToR-1)<= 0){
				rho = j;
			}
		}
		
		int r = 0;
		double sumUpToRho = 0;
		for (double y_0r : sortedZ_is.keySet()){
			r++;
			sumUpToRho += y_0r;
			if (r == rho)
				break;
		}
		double tao = (1/rho)*(sumUpToRho - 1);
		
		for (String varName : z_is.keySet()){
			projectedZ_is.put(varName, Math.max(z_is.get(varName) - tao,0));
		}
		
		return projectedZ_is;
	}
	
	private Map<String, Double> generateZs(MyNode<L> curFactor, Graph<MyNode<L>, String> factorGraph, int iteration){
		// to first generate the a_i
		Map<String, double[]> a_is = new HashMap<String, double[]>();
		for (MyNode<L> curVariable : factorGraph.getNeighbors(curFactor)){
			double score = scoredDatumLabels.get(curVariable.getTLink()).get(curVariable.getLabel());
			double curVariableActive = curVariable.getP()[1] + stepSize(iteration)*(curFactor.getLambda(curVariable.toString())[1] + score);
			double curVariableInactive = curVariable.getP()[0] + stepSize(iteration)*(curFactor.getLambda(curVariable.toString())[0]);
			double[] a_i = {curVariableInactive, curVariableActive};
			a_is.put(curVariable.toString(), a_i);
		}
		
		// to generate the z_is.
		Map<String, Double> z_is = new HashMap<String, Double>();
		for (String varName : a_is.keySet()){
			z_is.put(varName, (a_is.get(varName)[0] + 1 - a_is.get(varName)[1]) / 2);
		}
		
		return z_is;
	}
	
	// since it's a binarized factor graph, we set each variable node y_i to 1/2 = 1/|y_i|
	private void initializeGraph(Graph<MyNode<L>, String> factorGraph){
		for (MyNode<L> curVertex : FactorGraph.getSetOfVerticesOfType(factorGraph, NodeType.binarizedVariable)){
			curVertex.setP(1/validLabels.size());
		}
		
		// to set the lambdas for bot the constriaints, transitive and one-hot 
		initializeLambdas(factorGraph, NodeType.binarizedTransitiveConstraint);
		initializeLambdas(factorGraph, NodeType.onehotConstraint);
	}
	
	private void initializeLambdas(Graph<MyNode<L>, String> factorGraph, NodeType type){
		for (MyNode<L> curConstraintVertex : FactorGraph.getSetOfVerticesOfType(factorGraph, type)){
			for (MyNode<L> adjacentNode : factorGraph.getNeighbors(curConstraintVertex)){
				curConstraintVertex.setLambda(adjacentNode.toString(), 0);
			}
		}
	}
	
	private double stepSize(int iteration){
		return 1.0/iteration;
	}
}
