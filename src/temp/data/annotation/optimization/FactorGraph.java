package temp.data.annotation.optimization;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.optimization.MyNode;
import temp.data.annotation.optimization.NodeType;
import ark.data.annotation.Datum.Tools.LabelMapping;

public class FactorGraph<L> {
	private Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels;
	private Map<TLinkDatum<L>, L> fixedDatumLabels;
	private Set<L> validLabels;
	private LabelMapping<L> labelMapping;
	private String doc;
	private L[][][] compositionRules;
	private Graph<MyNode<L>, String> factorGraph;
	

	public FactorGraph(
			Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels,
			Map<TLinkDatum<L>, L> fixedDatumLabels, Set<L> validLabels,
			LabelMapping<L> labelMapping, L[][][] compositionRules) {
		this.scoredDatumLabels = scoredDatumLabels;
		this.fixedDatumLabels = fixedDatumLabels;
		this.validLabels = validLabels;
		this.labelMapping = labelMapping;
		this.doc = "";
		this.compositionRules = compositionRules;
	}
	
	public Graph<MyNode<L>, String> getFactorGraph(){
		return this.factorGraph;
	}

	// to add one vertex is the factor graph for each TLink we have in the
	// scoredDatumLabels
	private String getVertexName(TLinkDatum<L> datum) {
		String sourceId = datum.getTLink().getSource().getId();
		String targetId = datum.getTLink().getTarget().getId();
		return "TLink:" + sourceId + "->" + targetId;
	}
	
	//input: three tlinks
	//output: true if they are a transitive tripple of the type A-B, B-C, A-C.
	private boolean testTransOfTriplet(TLinkDatum<L> first, TLinkDatum<L> second, TLinkDatum<L> third){
		return (first.getTLink().getSource().equals(third.getTLink().getSource()) && 
				first.getTLink().getTarget().equals(second.getTLink().getSource()) &&
				second.getTLink().getTarget().equals(third.getTLink().getTarget()));
	}
	
	// TODO: check that looping over permutations of nodes, and rearranging each gives us what we need. 
	//		 it may give us repeats. 
	private void addTransitiveFactors(Graph<MyNode<L>, String> factorGraph){
		Set<MyNode<L>> tLinksInGraph = new HashSet<MyNode<L>>(factorGraph.getVertices());
		for (MyNode<L> first : tLinksInGraph){
			for (MyNode<L> second : tLinksInGraph){
				for (MyNode<L> third : tLinksInGraph){
					boolean transTriplet = testTransOfTriplet(first.getTLink(), second.getTLink(), third.getTLink());
					if (transTriplet) {
						String nameOfTransVertex = "TransConstraint:" + first.getTLink().getId() + "-" + second.getTLink().getId() + "-" + third.getTLink().getId();
						
						MyNode<L> transVertex = new MyNode<L>(nameOfTransVertex, temp.data.annotation.optimization.NodeType.transitiveConstraint);
						
						// adding edges to the factor graph
						factorGraph.addVertex(transVertex);
						factorGraph.addEdge(nameOfTransVertex + "<-->" + getVertexName(first.getTLink()), transVertex, first);
						factorGraph.addEdge(nameOfTransVertex + "<-->" + getVertexName(second.getTLink()), transVertex, second);
						factorGraph.addEdge(nameOfTransVertex + "<-->" + getVertexName(third.getTLink()), transVertex, third);
						
						// adding poniters to the variable nodes into the transitive constraint
						transVertex.addUnbinarizedRels(first, second, third);
					}
				}
			}
		}
	}
	
	// connect the new binary nodes to the transitive vertices
	private void connectNewNodesToTransNodes(MyNode<L> vertex, Map<L, MyNode<L>> newNodes, Graph<MyNode<L>, String> factorGraph){
		for (String incidentEdge : factorGraph.getIncidentEdges(vertex)){
			for (MyNode<L> incidentVertex : factorGraph.getIncidentVertices(incidentEdge)){
				if (incidentVertex.toString().equals(vertex.toString()))
					continue;
				
				incidentVertex.addBinarizedRels(vertex, newNodes);
			}
		}
	}
	
	// remove a given vertex and its edges
	private void removeNodeAndIncidentEdges(MyNode<L> vertex, Graph<MyNode<L>, String> factorGraph){
		Set<String> edgeNames = new HashSet<String>();
		for (String incidentEdge : factorGraph.getIncidentEdges(vertex))
			edgeNames.add(incidentEdge);
		for (String incidentEdge : edgeNames)
			factorGraph.removeEdge(incidentEdge);
		factorGraph.removeVertex(vertex);
	}
	
	// finds those verticies which represent tlinks
	// this <L> is technically hiding the global one. look here: http://stackoverflow.com/questions/4409100/how-to-make-a-java-generic-method-static
	public static <L> Set<MyNode<L>> getSetOfVerticesOfType(Graph<MyNode<L>, String> factorGraph, NodeType type){
		Set<MyNode<L>> vertices = new HashSet<MyNode<L>>();
		for (MyNode<L> vertex : factorGraph.getVertices()){
			if (vertex.getType().equals(type))
				vertices.add(vertex);
		}
		return vertices;
	}
	
	private void binarizeTLinks(Graph<MyNode<L>, String> factorGraph){
		// pseudocode: 
		// for each tlinkvertex:
		//	replace it with one node for each label
		//  make new vertex for one-hot
		//  add edge between each new node and new one-hot vertex
		//  add edge between ecah new node and old trans factor
		// 	remove old node
		
		for (MyNode<L> vertex : getSetOfVerticesOfType(factorGraph, temp.data.annotation.optimization.NodeType.variable)){
			// make new vertices
			Map<L, MyNode<L>> newNodes = new HashMap<L, MyNode<L>>();
			for (L label : validLabels){
				MyNode<L> binaryVariableForOneLabel = new MyNode<L>(vertex.getTLink(), vertex.toString() + label, 
						temp.data.annotation.optimization.NodeType.binarizedVariable, label);
				newNodes.put(label, binaryVariableForOneLabel);
			}
			connectNewNodesToTransNodes(vertex, newNodes, factorGraph);
			removeNodeAndIncidentEdges(vertex, factorGraph);
			
			// adding one-hot
			MyNode<L> oneHot = new MyNode<L>(vertex.getTLink(), vertex.toString() + ":one-hot", temp.data.annotation.optimization.NodeType.onehotConstraint);
			
			factorGraph.addVertex(oneHot);
			// connecting one-hot to new binary nodes
			for (L label : newNodes.keySet()){
				factorGraph.addEdge(oneHot.toString() + "<-->" + newNodes.get(label), oneHot, newNodes.get(label));
			}
			oneHot.addBinarizedRels(newNodes);
		}
	}
	
	private boolean labelsAreInvalid(L firstConjunctLabel, L secondConjunctLabel, L[] consequentLabels){
		return  (consequentLabels[0] == null || (consequentLabels.length > 1) || (!validLabels.contains(firstConjunctLabel) )
				|| (!validLabels.contains(secondConjunctLabel))
				|| (!validLabels.contains(consequentLabels[0])));
	}
	
	// input: a factor graph which, for each transitive tlink triple, has one factor node, which is
	// 			connected to all of the binarized nodes.
	// behavior: adjusts the factor graph so that each constraint of type R & R' => R'' is represented by one node 
	private void fixTransitiveFactorsForBinaryGraph(Graph<MyNode<L>, String> factorGraph){
		
		for (MyNode<L> vertex : getSetOfVerticesOfType(factorGraph, temp.data.annotation.optimization.NodeType.transitiveConstraint)){
			if (vertex.getType().equals(temp.data.annotation.optimization.NodeType.transitiveConstraint)){
				for (L[][] compositionRule : compositionRules) {
					L firstConjunctLabel = compositionRule[0][0];
					L secondConjunctLabel = compositionRule[0][1];
					L[] consequentLabels = compositionRule[1];

					// only deal with the non-disjunctive case 
					// make new node
					// connect that node to the nodes representing the binary things in original vertex
					if (labelsAreInvalid(firstConjunctLabel, secondConjunctLabel, consequentLabels))
						continue;
					L consequentLabel = consequentLabels[0];
					
					MyNode<L> curBinaryTransConstraint = new MyNode<L>(vertex.toString() + ":" + firstConjunctLabel + "," + secondConjunctLabel + "," + consequentLabel, 
							temp.data.annotation.optimization.NodeType.binarizedTransitiveConstraint);
					
					factorGraph.addEdge(curBinaryTransConstraint.toString() + "<-->" + vertex.getBinarizedRel(0, firstConjunctLabel).toString()
							,curBinaryTransConstraint, vertex.getBinarizedRel(0, firstConjunctLabel));					
					factorGraph.addEdge(curBinaryTransConstraint.toString() + "<-->" + vertex.getBinarizedRel(1, secondConjunctLabel).toString()
							,curBinaryTransConstraint, vertex.getBinarizedRel(1, secondConjunctLabel));
					factorGraph.addEdge(curBinaryTransConstraint.toString() + "<-->" + vertex.getBinarizedRel(2, consequentLabel).toString()
							,curBinaryTransConstraint, vertex.getBinarizedRel(2, consequentLabel));
					
					// to add the things to the new binarized trans constraint factor.
					curBinaryTransConstraint.addBinarizedRels(vertex.getBinarizedRel(0, firstConjunctLabel), firstConjunctLabel, 
							vertex.getBinarizedRel(1, secondConjunctLabel), secondConjunctLabel, 
							vertex.getBinarizedRel(2, consequentLabel), consequentLabel);
					
				}
				removeNodeAndIncidentEdges(vertex, factorGraph);
			}
		}
	}

	// here is where we implement alternating directions dual decomposition.
	public void build() {
		// first make a factor graph out of the scored datum labels
		// then check that shit using JUNG

		// EXAMPLE:
		// Graph<V, E> where V is the type of the vertices
		// and E is the type of the edges
		Graph<MyNode<L>, String> factorGraph = new UndirectedSparseGraph<MyNode<L>, String>();
		
		// Add vertices for the TLinks
		for (TLinkDatum<L> datum : scoredDatumLabels.keySet()) { 
			checkDoc(datum);
			//TODO: remove
			factorGraph.addVertex(new MyNode<L>(datum, getVertexName(datum), temp.data.annotation.optimization.NodeType.variable));
		}


		addTransitiveFactors(factorGraph);
		checkTransitiveFactors(factorGraph);
		binarizeTLinks(factorGraph);
		checkBinarizedTransitiveFactors(factorGraph);
		fixTransitiveFactorsForBinaryGraph(factorGraph);
		
		checkGraph(factorGraph);
		
		this.factorGraph = factorGraph;
		
		//printoutGraph(factorGraph);
		//visualizeGraph(factorGraph);
		//System.exit(0);
	}
	
	public void printoutGraph(){
		for (MyNode<L> curNode : factorGraph.getVertices()){
			System.out.println("Name: " + curNode.toString());
			System.out.println("Type: " + curNode.getType());
			System.out.println("TLink: " + curNode.getTLink());
			System.out.println("Binarized Rels: " + curNode.getBinarizedRels());
			System.out.println("Label: " + curNode.getLabel());
			System.out.println("Active label: " + curNode.getActiveLabel());
			System.out.println("Neighbors:");
			for (MyNode<L> neighbors : factorGraph.getNeighbors(curNode)){
				System.out.println(neighbors.toString());
			}
			System.out.println();
		}
	}
	
	// checks to make sure each trans constraint has exactly three neighbors, and they're all not null.
	private void checkTransitiveFactors(Graph<MyNode<L>, String> factorGraph){
		for (MyNode<L> vertex : getSetOfVerticesOfType(factorGraph, temp.data.annotation.optimization.NodeType.transitiveConstraint)){
			if (vertex.getUnbinarizedRelations().size() != 3){
				System.out.println("Problem! We have the wrong number of unbinarized relations in a particular node.");
				System.out.println(vertex);
				System.exit(0);
			}
			for (int i = 0; i < vertex.getUnbinarizedRelations().size(); i++){
				if (vertex.getUnbinarizedRelations().get(i) == null){
					System.out.println("Problem! We have the wrong number of unbinarized relations in a particular node.");
					System.out.println(vertex);
					System.exit(0);
				}
			}
		}
	}
	
	// checks the edges to a binarized node. NOTE: Does this in the binarizedRelations field of the MyNode, not through the edges of the graph
	private void checkBinarizedTransitiveFactors(Graph<MyNode<L>, String> factorGraph){
		for (MyNode<L> vertex : getSetOfVerticesOfType(factorGraph, temp.data.annotation.optimization.NodeType.transitiveConstraint)){
			for (int i = 0; i < vertex.getBinarizedRels().size(); i++){
				if (vertex.getBinarizedRels().get(i).keySet().size() != validLabels.size()){
					System.out.println("Problem! We have the wrong number of binarized relations in a particular node.");
					System.out.println(vertex);
					System.exit(0);
				}
			}
		}
	}
	
	private void checkGraph(Graph<MyNode<L>, String> factorGraph){
		System.out.println("Number of edges: " + factorGraph.getEdgeCount());
		System.out.println("Number of vertices: " + factorGraph.getVertexCount());
		System.out.println();
	}
	
	private void visualizeGraph(Graph<MyNode<L>, String> factorGraph){
		//System.out.println("\n\n\nfactor graph:");
		//System.out.println(factorGraph.toString());
		
		 // The Layout<V, E> is parameterized by the vertex and edge types
		 Layout<MyNode<L>, String> layout = new CircleLayout<MyNode<L>, String>(factorGraph);
		 layout.setSize(new Dimension(900,900)); // sets the initial size of the space
		 // The BasicVisualizationServer<V,E> is parameterized by the edge types
		 BasicVisualizationServer<MyNode<L>,String> vv = 
		 new BasicVisualizationServer<MyNode<L>,String>(layout);
		 vv.setPreferredSize(new Dimension(950,950)); //Sets the viewing area size
		 
		 JFrame frame = new JFrame("Simple Graph View");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.getContentPane().add(vv); 
		 frame.pack();
		 frame.setVisible(true); 
		 
		 while (3==3){
			 
		 }
	}

	// to make sure for each time the approximatelyOptimize method is called we
	// are only looking at TLinkDatums which are in one doc.
	private void checkDoc(TLinkDatum<L> datum) {
		if (doc.equals(""))
			doc = datum.getTLink().getSource().getTokenSpan().getDocument()
					.getName();
		if (!doc.equals(datum.getTLink().getSource().getTokenSpan()
				.getDocument().getName())) {
			System.out
					.println("THERE'S MORE THAN ONE DOCUMENT REPRESENTED IN THE THINGS WE'RE TRYING TO OPTIMIZE!! "
							+ "BAD. IN AlternatingDirectionsDualDecomp");
			System.exit(0);
		}
	}
	
	// sets a graph to a random initialization
	public void initializeGraph(){
		if (this.factorGraph == null){
			throw new IllegalStateException("Trying to initialize a graph that hasn't been built yet.");
		}
		Random r = new Random(10);
		// for each one-hot constraint, set the associated variable to a random value.
		List<L> validLabelsList = new ArrayList<L>();
		validLabelsList.addAll(this.validLabels);
		for (MyNode<L> oneHot : FactorGraph.getSetOfVerticesOfType(this.factorGraph, NodeType.onehotConstraint)){
			initializeUnifromlyAtRandom(oneHot, r, validLabelsList);
		}

	}
		
	private void initializeUnifromlyAtRandom(MyNode<L> curNode, Random r, List<L> validLabelsList){
		double randNum = Math.floor(r.nextDouble() * 6);
		curNode.setActiveLabel(validLabelsList.get((int)randNum));
	}
}


// example of how to use graph
/*
g.addVertex("First");
g.addVertex("Second");
g.addVertex("Third");
// Add some edges. From above we defined these to be of type String
// Note that the default is for undirected edges.
g.addEdge("First-Second", "First", "Second"); // Note that Java 1.5
												// auto-boxes primitives
g.addEdge("Third-First", "Third", "First");
// Let's see what we have. Note the nice output from the
// SparseMultigraph<V,E> toString() method
System.out.println("\n\n");
System.out.println("The graph g = " + g.toString());
// Note that we can use the same nodes and edges in two different
// graphs.
Graph<Integer, String> g2 = new SparseMultigraph<Integer, String>();
g2.addVertex((Integer) 1);
g2.addVertex((Integer) 2);
g2.addVertex((Integer) 3);
g2.addEdge("Edge-A", 1, 3);
g2.addEdge("Edge-B", 2, 3, EdgeType.DIRECTED);
g2.addEdge("Edge-C", 3, 2, EdgeType.DIRECTED);
g2.addEdge("Edge-P", 2, 3); // A parallel edge
System.out.println("The graph g2 = " + g2.toString());
System.out.println("\n\n");


// input: three tlinks
	// output: if they're a transitive triple, the ordering of the triple.
	// 			if not, null.
	// this doesn't seem necessary if i instead just loop over all the tlinks
	private ArrayList<TLinkDatum<L>> transitivityApplies(TLinkDatum<L> first,
			TLinkDatum<L> second, TLinkDatum<L> third) {
		// test all permutations of tlinks.
		// a given triplet should only be valid at most once.
		ArrayList<TLinkDatum<L>> tlinks = new ArrayList<TLinkDatum<L>>();
		tlinks.add(first);
		tlinks.add(second);
		tlinks.add(third);
		for (int i = 0; i < tlinks.size(); i++){
			for (int j = 0; j < tlinks.size(); j++){
				for (int k = 0; k < tlinks.size(); k++){
					if (i == j || j == k || k == i)
						continue;
					if (testTransOfTriplet(tlinks.get(i), tlinks.get(j), tlinks.get(k))){
						ArrayList<TLinkDatum<L>> orderedLinks = new ArrayList<TLinkDatum<L>>();
						orderedLinks.add(tlinks.get(i));
						orderedLinks.add(tlinks.get(j));
						orderedLinks.add(tlinks.get(k));
						return orderedLinks;
					}
				}
			}
		}
		return null;
	}

*
*
*
*/