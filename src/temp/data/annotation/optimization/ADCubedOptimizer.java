package temp.data.annotation.optimization;

import java.util.*;


import edu.uci.ics.jung.graph.Graph;

public class ADCubedOptimizer<L> {
	
	public void optimize(FactorGraphBuilder<L> graph){
		
		Graph<MyNode<L>, String> factorGraph = graph.getFactorGraph();
		initializeGraph(factorGraph);
		while (stoppingCriteriaNotMet){
			updateQs(factorGraph);
		}
	}
	
	private void updateQs(Graph<MyNode<L>, String> factorGraph){
		for (MyNode<L> curVertex : FactorGraphBuilder.getSetOfVerticesOfType(factorGraph, NodeType.binarizedTransitiveConstraint)){
			// update these
		}
		for (MyNode<L> curVertex : FactorGraphBuilder.getSetOfVerticesOfType(factorGraph, NodeType.onehotConstraint)){
			// update these
		}
	}
	
	// since it's a binarized factor graph, we set each variable node y_i to 1/2 = 1/|y_i|
	private void initializeGraph(Graph<MyNode<L>, String> factorGraph){
		for (MyNode<L> curVertex : FactorGraphBuilder.getSetOfVerticesOfType(factorGraph, NodeType.binarizedVariable)){
			curVertex.setP(.5);
		}
		// to set the lambdas for bot the constriaints, transitive and one-hot 
		initializeLambdas(factorGraph, NodeType.binarizedTransitiveConstraint);
		initializeLambdas(factorGraph, NodeType.onehotConstraint);
	}
	
	private void initializeLambdas(Graph<MyNode<L>, String> factorGraph, NodeType type){
		for (MyNode<L> curVertex : FactorGraphBuilder.getSetOfVerticesOfType(factorGraph, type)){
			// get incident nodes
			for (Map<L, MyNode<L>> curVertexFirstRels: curVertex.getBinarizedRelations()){
				for (L label : curVertexFirstRels.keySet()){
					MyNode<L> curRel = curVertexFirstRels.get(label);
					curVertex.setQ(0, curRel);
				}
			}
		}
	}
	
	private void projectOntoSimplex(){
		
	}

}
