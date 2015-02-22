package temp.data.annotation.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import temp.data.annotation.TLinkDatum;

enum NodeType {
	variable,
	fixedVariable,
	binarizedVariable,
	transitiveConstraint,
	binarizedTransitiveConstraint,
	onehotConstraint,
	onehotFixedVariableConstraint
}

class MyNode<L> {
	// for the messy name, made for each type of node separately. unique for all nodes, unlike type.
	private String name;
	
	// should be an array of length three, storing relations in the order R, R', R'', where 
	// R & R' => R''
	private List<Map<L, MyNode<L>>> binarizedRelations;
	private List<MyNode<L>> unbinarizedRelations;
	private L label;
	
	private TLinkDatum<L> tLink;
	
	private NodeType type;
	
	// for the ad^3 algorithm
	private double[] p;
	private Map<String, double[]> q;
	private Map<String, double[]> lambda;
	
	// only used by the oneHot nodes, to tell which of the associated binary variables is active
	private L activeLabel;

	// for creating variable nodes, binarizedVar nodes
	public MyNode(TLinkDatum<L> tLink, String name, NodeType type) {
		this(tLink, name, type, null);
	}
	
	// for creating one-hot constraints, transConstraint
	public MyNode(String name, NodeType type){
		this(null, name, type);
	}
	
	// for creating binarized tlinks, so we know which label this particular node represents
	// and for the fixed variables.
	public MyNode(TLinkDatum<L> tLink, String name, NodeType type, L label) {
		this.tLink = tLink;
		this.name = name;
		this.type = type;
		this.label = label;
		initializeBinarizedRelations();
	}

	private void initializeBinarizedRelations(){
		binarizedRelations = new ArrayList<Map<L, MyNode<L>>>();
		binarizedRelations.add(new HashMap<L, MyNode<L>>());
		binarizedRelations.add(new HashMap<L, MyNode<L>>());
		binarizedRelations.add(new HashMap<L, MyNode<L>>());
	}
	
	// get tlink
	public TLinkDatum<L> getTLink(){
		return tLink;
	}
	
	// adding the unbinarized relations.
	public void addUnbinarizedRels(MyNode<L> rOne, MyNode<L> rTwo, MyNode<L> rThree){
		unbinarizedRelations = new ArrayList<MyNode<L>>();
		unbinarizedRelations.add(rOne);
		unbinarizedRelations.add(rTwo);
		unbinarizedRelations.add(rThree);
	}
	
	// useful for when adding a particular three-tuple R, R', R'' for binarized transitivity constraints.
	public void addBinarizedRels(MyNode<L> rOne, L lOne, MyNode<L> rTwo, L lTwo, MyNode<L> rThree, L lThree){
		binarizedRelations = new ArrayList<Map<L, MyNode<L>>>();
		addOneBinarizedRelForBinarizedTrans(lOne, rOne);
		addOneBinarizedRelForBinarizedTrans(lTwo, rTwo);
		addOneBinarizedRelForBinarizedTrans(lThree, rThree);
	}
	
	//helper method just for adding binarized relations
	private void addOneBinarizedRelForBinarizedTrans(L label, MyNode<L> relation){
		Map<L, MyNode<L>> firstRelation = new HashMap<L, MyNode<L>>();
		firstRelation.put(label, relation);
		binarizedRelations.add(firstRelation);
	}
	
	public List<Map<L, MyNode<L>>> getBinarizedRels(){
		return Collections.unmodifiableList(binarizedRelations);
	}
	
	// to get the unbinarized relations
	public List<MyNode<L>> getUnbinarizedRels(){
		return Collections.unmodifiableList(unbinarizedRelations);
	}
	
	// to add the binarized relations
	public void addBinarizedRels(MyNode<L> unbinarizedRel, Map<L, MyNode<L>> binarizedRels){
		for (int i = 0; i < unbinarizedRelations.size(); i++){
			MyNode<L> curUnbinarizedRel = unbinarizedRelations.get(i);
			if (unbinarizedRelations.get(i) != null && unbinarizedRel.toString().equals(curUnbinarizedRel.toString())){
				binarizedRelations.set(i, new HashMap<L, MyNode<L>>(binarizedRels));
				unbinarizedRelations.set(i, null);
			}
		}
	}
	
	// for use with the one-hot constraint
	public void addBinarizedRels(Map<L, MyNode<L>> binarizedRels){
		binarizedRelations = new ArrayList<Map<L, MyNode<L>>>();
		binarizedRelations.add(binarizedRels);
	}
	
	public Map<L, MyNode<L>> getOneHotBinarizedRels(){
		return binarizedRelations.get(0);
	}
	
	public void setP(double[] p){
		this.p = p;
	}
	
	public double[] getP(){
		return p;
	}
	
	public void setQ(double[] qi, MyNode<L> node){
		q.put(node.toString(), qi);
	}
	
	public double[] getQ(MyNode<L> node){
		return q.get(node.toString());
	}
	
	public Map<String, double[]> getQ(){
		return new HashMap<String, double[]>(q);
	}
	
	public void setLambda(String binaryVarName, double[] newValue){
		lambda.put(binaryVarName, newValue);
	}
	
	public double[] getLambda(String binaryVarName){
		return lambda.get(binaryVarName);
	}
	
	public List<MyNode<L>> getUnbinarizedRelations(){
		return Collections.unmodifiableList(unbinarizedRelations);
	}
	
	public MyNode<L> getBinarizedRel(int index, L label){
		return binarizedRelations.get(index).get(label);
	}

	public String toString() {
		return name;
	}
	
	public NodeType getType(){
		return type;
	}
	
	public L getLabel(){
		return label;
	}
	
	// only used for the one-hot constraints
	public void setActiveLabel(L newActiveLabel){
		if (this.type != NodeType.onehotConstraint && this.type != NodeType.onehotFixedVariableConstraint){
			throw new IllegalArgumentException("trying to tell a node which label is active when that node isn't a one-hot node! Bad news bears.");
		}
		activeLabel = newActiveLabel;
	}
	
	// only used for the one-hot constraints
	public L getActiveLabel(){
		return activeLabel;
	}
}