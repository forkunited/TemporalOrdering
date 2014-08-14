package temp.data.annotation.structure;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryCPLEX;


import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLink;
import ark.data.annotation.Datum.Tools;
import ark.data.annotation.Datum.Tools.LabelMapping;
import ark.data.annotation.Document;
import ark.data.annotation.structure.DatumStructure;
import ark.util.OutputWriter;

/**
 * TLinkGraph is a graph with TLinkDatums as directed
 * edges and times/events as vertices.  
 * 
 * @author Bill McDowell
 *
 * @param <L> TLink label type
 */
public class TLinkGraph<L> extends DatumStructure<TLinkDatum<L>, L> {	
	/**
	 * LabelInferecenceRules represents a set of rules for 
	 * drawing inferences about the labels assigned 
	 * to TLinks given the labels assigned to other
	 * TLinks in the same TLinkGraph.  For example, this 
	 * is currently implemented by 
	 * temp.data.annotation.structure.InferenceRulesTimeMLRelType to 
	 * represent rules for assigning TimeML relationship types
	 * to TLinks.
	 * 
	 * @author Bill McDowell
	 *
	 * @param <L> TLink label type
	 */
	public interface LabelInferenceRules<L> {
		L[][][] getCompositionRules();
		L getConverse(L label);
		L getRuleBasedFixedLabel(TLinkDatum<L> datum);
		String getGenericName();
	}
	
	// Maps event/time id 1 to event/time id 2 to the TLink between them
	// If there is a link from e1 to e2, then there will be no link
	// from e2 to e1 in this map (at least in existing data sets)
	private Map<String, Map<String, TLinkDatum<L>>> adjacencyMap;
	private Set<String> tlinkableIds; // Ids of events and times
	private LabelInferenceRules<L> labelInferenceRules;
	
	public TLinkGraph(String id, Tools<TLinkDatum<L>, L> datumTools, LabelInferenceRules<L> labelInferenceRules) {
		super(id);
		this.labelInferenceRules = labelInferenceRules;
		this.adjacencyMap = new HashMap<String, Map<String, TLinkDatum<L>>>();
		this.tlinkableIds = new HashSet<String>();
		addDatumStructureOptimizer(new OptimizerInference(datumTools, labelInferenceRules, false, false));
		addDatumStructureOptimizer(new OptimizerInference(datumTools, labelInferenceRules, false, true));
		addDatumStructureOptimizer(new OptimizerInference(datumTools, labelInferenceRules, true, false));
		addDatumStructureOptimizer(new OptimizerInference(datumTools, labelInferenceRules, true, true));
	}

	/**
	 * @param id1 - first event/time id
	 * @param id2 - second event/time id
	 * @return TLink from id1 to id2 or from id2 to id1---whichever one exists
	 */
	public TLinkDatum<L> getTLinkBetween(String id1, String id2) {
		if (this.adjacencyMap.containsKey(id1) && this.adjacencyMap.get(id1).containsKey(id2))
			return adjacencyMap.get(id1).get(id2);
		else if (this.adjacencyMap.containsKey(id2) && this.adjacencyMap.get(id2).containsKey(id1))
			return this.adjacencyMap.get(id2).get(id1);
		else
			return null;
	}
	
	/**
	 * @param e TLink
	 * @return true if e was successfully added to the graph
	 */
	@Override
	public boolean add(TLinkDatum<L> e) {
		String sourceId = e.getTLink().getSource().getId();
		String targetId = e.getTLink().getTarget().getId();
		
		this.tlinkableIds.add(sourceId);
		this.tlinkableIds.add(targetId);
		
		if (!this.adjacencyMap.containsKey(sourceId))
			this.adjacencyMap.put(sourceId, new HashMap<String, TLinkDatum<L>>());
		this.adjacencyMap.get(sourceId).put(targetId, e);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends TLinkDatum<L>> c) {
		for (TLinkDatum<L> datum : c)
			add(datum);
		return true;
	}

	@Override
	public void clear() {
		this.adjacencyMap = new HashMap<String, Map<String, TLinkDatum<L>>>();
		this.tlinkableIds = new HashSet<String>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		TLinkDatum<L> tlinkDatum = (TLinkDatum<L>)o;
		String sourceId = tlinkDatum.getTLink().getSource().getId();
		String targetId = tlinkDatum.getTLink().getTarget().getId();
		
		return this.adjacencyMap.containsKey(sourceId) && this.adjacencyMap.get(sourceId).containsKey(targetId);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.adjacencyMap.isEmpty();
	}

	@Override
	public Iterator<TLinkDatum<L>> iterator() {
		return new TLinkIterator();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return number of TLinks in the graph
	 */
	@Override
	public int size() {
		int size = 0;
		for (Map<String, TLinkDatum<L>> map : this.adjacencyMap.values())
			size += map.size();
		return size;
	}

	@Override
	public Object[] toArray() {
		Object[] array = new Object[size()];
		
		int i = 0;
		for (TLinkDatum<L> datum : this) {
			array[i] = datum;
			i++;
		}
		
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size())
			a = (T[])Array.newInstance(a.getClass().getComponentType(), size());
		
		int i = 0;
		for (TLinkDatum<L> datum : this) {
			a[i] = (T)datum;
			i++;
		}
		
		if (i < a.length)
			a[i] = null;

		return a;
	}

	@Override
	public Map<String, Integer> constraintsHold(boolean useDisjunctiveConstraints) {		
		int numTransBroken = 0;
		int numTransBrokenWithDCT = 0;
		int totalNum = 0;
		
		Set<Set<String>> alreadySeenTriplet = new HashSet<Set<String>>();
		for (String tlinkableId1 : tlinkableIds) {
			for (String tlinkableId2 : tlinkableIds) {
				for (String tlinkableId3 : tlinkableIds){
					if (tlinkableId1.equals(tlinkableId2) || tlinkableId2.equals(tlinkableId3) || tlinkableId3.equals(tlinkableId1))
						continue;
										
					if (!(adjacencyMap.containsKey(tlinkableId1) && adjacencyMap.get(tlinkableId1).containsKey(tlinkableId2)
							&& adjacencyMap.containsKey(tlinkableId2) && adjacencyMap.get(tlinkableId2).containsKey(tlinkableId3)
							&& adjacencyMap.get(tlinkableId1).containsKey(tlinkableId3)))
						continue;
					// TODO: remove this check. it seems to be working.
					Set<String> thisTriplet= new HashSet<String>();
					thisTriplet.add(tlinkableId1);
					thisTriplet.add(tlinkableId2);
					thisTriplet.add(tlinkableId3);
					if (alreadySeenTriplet.contains(thisTriplet))
						throw new IllegalStateException("When checking constraints, " + 
								"this relation triplet has already been examined! problem with the \'if ... continue\' statement in constraintsHold!");
	
					alreadySeenTriplet.add(thisTriplet);
					
					
					L label1 = adjacencyMap.get(tlinkableId1).get(tlinkableId2).getLabel();
					L label2 = adjacencyMap.get(tlinkableId2).get(tlinkableId3).getLabel();
					L label3 = adjacencyMap.get(tlinkableId1).get(tlinkableId3).getLabel();
					
					
					
					L[][][] relationConstraints = this.labelInferenceRules.getCompositionRules();
					
					boolean transitivityHolds = false;
					
					for (L[][] compositionRule : relationConstraints) {
						L firstConjunctLabel = compositionRule[0][0];
						L secondConjunctLabel = compositionRule[0][1];
						L[] consequentLabels = compositionRule[1];
						

						if (label1 != null && !firstConjunctLabel.equals(label1))
							continue;
						if (label2 != null && !secondConjunctLabel.equals(label2))
							continue;
						if (consequentLabels[0] == null || (consequentLabels.length > 1 && !useDisjunctiveConstraints)){
							transitivityHolds = true;
							continue;
						}
							
						
						for (int i = 0; i < consequentLabels.length; i++){
							if (label3.equals(consequentLabels[i])){
								transitivityHolds = true;

							}
						}
					}


					if (!transitivityHolds){
						numTransBroken++;
						System.out.println("Bad combo!:");
						System.out.println("R1 = " + label1 + " & R2 = " + label2 + " & R3 = " + label3);
						System.out.println(tlinkableId1 + ", " + tlinkableId2 + ", " + tlinkableId3);
						System.out.println();
						if (tlinkableId1.equals("t0") || tlinkableId2.equals("t0") || tlinkableId3.equals("t0"))
							numTransBrokenWithDCT++;
					}
					totalNum++;
				}
			}
		}
	
		Map<String, Integer> violations = new HashMap<String, Integer>();
		violations.put("totalTriplets", totalNum);
		violations.put("numTripletsWithTransBroken", numTransBroken);
		violations.put("numTripletsWithTransBrokenWithDCT", numTransBrokenWithDCT);
		return violations;
	}

	/**
	 * OptimizerInference uses an ILP to optimize an objective function 
	 * subject to some constraints.  The objective function is 
	 * defined in terms of assignments of labels to TLinks, and the constraints
	 * are given by a set of label inference rules (see TLinkGraph.LabelInferenceRules interface).
	 * The objective and constraints are described in detail in papers/TemporalOrderingNotes.pdf
	 * 
	 * @author Bill McDowell
	 */
	protected class OptimizerInference implements DatumStructureOptimizer<TLinkDatum<L>, L> {
		private LabelInferenceRules<L> labelInferenceRules;
		
		// Indicates whether to include compositional (transitive) rules that include disjunctions
		private boolean includeDisjunctiveRules; 
		
	    // Indicates whether to include rules governing labels that are determined by properties of the TLinks.
		// Currently these just include rules based on the time intervals referenced by temporal expressions
		private boolean includeRuleBasedFixedLabels;
		private Tools<TLinkDatum<L>, L> datumTools;
		
		public OptimizerInference(Tools<TLinkDatum<L>, L> datumTools, LabelInferenceRules<L> labelInferenceRules, boolean includeDisjunctiveRules, boolean includeRuleBasedFixedLabels) {
			super();
			this.labelInferenceRules = labelInferenceRules;
			this.includeDisjunctiveRules = includeDisjunctiveRules;
			this.includeRuleBasedFixedLabels = includeRuleBasedFixedLabels;
			this.datumTools = datumTools;
		}
		
		// plan: break this into methods, rewrite the section on constraint stuff.
		// idea: fixed label constraints is for those labels between TIME and TIME
		/**
		 * @param scoredDatumLabels - scores for assignments of labels to TLinks (u vector in papers/TemporalOrderingNotes.pdf)
		 * @param fixedDatumLabels - labels that should be fixed to TLinks regardless of the objective function value.
		 *							 These allow the within-sentence model to constrain the labels of the 
		 *                           between sentence model 
		 * @param validLabels - a set of labels that optimize is allowed to assign
		 * @param labelMapping - a mapping function from all possible labels to validLabels
		 * @return an optimal mapping from TLinks to labels according to the objective function 
		 * defined in papers/TemporalOrderingNotes.pdf. 
		 */
		public Map<TLinkDatum<L>, L> optimize(Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels, Map<TLinkDatum<L>, L> fixedDatumLabels, Set<L> validLabels, LabelMapping<L> labelMapping) {
			OutputWriter output = this.datumTools.getDataTools().getOutputWriter();
			
			
			if (true)
				return actuallyUnstructuredSetup(scoredDatumLabels);

			
			
			L[][][] compositionRules = this.labelInferenceRules.getCompositionRules(); 

			SolverFactory factory = new SolverFactoryCPLEX();
			factory.setParameter(Solver.VERBOSE, 0); 
			factory.setParameter(Solver.TIMEOUT, Integer.MAX_VALUE);
			
			Set<L> allLabels = new HashSet<L>();
			allLabels.addAll(validLabels);
			
			// this might not be necessary
			for (L[][] rule : compositionRules)
				for (L label : rule[1]) // Add labels from consequents of rules
					if (label != null)
						allLabels.add(label);
			
			Set<String> tlinkableIds = new HashSet<String>();
			tlinkableIds.addAll(TLinkGraph.this.tlinkableIds);
			tlinkableIds.add("t0"); // Add in dct even if not in graph
			
			Problem problem = new Problem();
			
			Document graphDocument = TLinkGraph.this.iterator().next().getTLink().getSource().getTokenSpan().getDocument();
			
			Map<String, Map<String, L>> fixedAdjacencyMap = new HashMap<String, Map<String, L>>();
			
			// Fixed label constraints (these ensure that some labels are definitely 
			// assigned to TLinks regardless of the objective function value)
			// These allow the within-sentence model to constrain the labels of the 
			// between sentence model 
			for (Entry<TLinkDatum<L>, L> entry : fixedDatumLabels.entrySet()) {
				String tlinkableId1 = entry.getKey().getTLink().getSource().getId();
				String tlinkableId2 = entry.getKey().getTLink().getTarget().getId();
				
				Document entryDocument = entry.getKey().getTLink().getSource().getTokenSpan().getDocument();
				if (!graphDocument.getName().equals(entryDocument.getName()))
					continue;
				
				if (!tlinkableIds.contains(tlinkableId1) || !tlinkableIds.contains(tlinkableId2))
					continue;
			
				String tlinkVar = "t_" + tlinkableId1 + "_" + tlinkableId2 + "_" + entry.getValue();
				
				Linear fixedLabelConstraint = new Linear();
				fixedLabelConstraint.add(1, tlinkVar);
				problem.add(fixedLabelConstraint, ">=", 1);
			
				if (!fixedAdjacencyMap.containsKey(tlinkableId1))
					fixedAdjacencyMap.put(tlinkableId1, new HashMap<String, L>());
				if (!fixedAdjacencyMap.containsKey(tlinkableId2))
					fixedAdjacencyMap.put(tlinkableId2, new HashMap<String, L>());
				fixedAdjacencyMap.get(tlinkableId1).put(tlinkableId2, entry.getValue());
				fixedAdjacencyMap.get(tlinkableId2).put(tlinkableId1, this.labelInferenceRules.getConverse(entry.getValue()));
			}
			
			// Objective function and rule based (grounded time interval) constraints
			// Constraint 4 from section 1.1 of papers/TemporalOrderingNotes.pdf.
			Linear objective = new Linear();
			for (Entry<TLinkDatum<L>, Map<L, Double>> datumEntry : scoredDatumLabels.entrySet()) {
				TLink tlink = datumEntry.getKey().getTLink();
				String tlinkableId1 = tlink.getSource().getId();
				String tlinkableId2 = tlink.getTarget().getId();
				
				if (!tlinkableIds.contains(tlinkableId1) || !tlinkableIds.contains(tlinkableId2))
					continue;
				
				String tlinkVarPrefix = "t_" + tlinkableId1 + "_" + tlinkableId2 + "_";
				Map<L, Double> labelValues = datumEntry.getValue();
				
				// Objective function
				double minValue = Double.MAX_VALUE;
				for (Entry<L, Double> labelEntry : labelValues.entrySet()) {
					double value = labelEntry.getValue();//maxLabelMagnitude; // Scale value
					objective.add(value, tlinkVarPrefix + labelEntry.getKey());
					minValue = Math.min(minValue, value);
				}
				for (L label : allLabels) {
					if (!labelValues.containsKey(label))
						objective.add(minValue - 1.0, tlinkVarPrefix + label); // Disprefer non-valid labels
				}
				
				// Constraint 4 from section 1.1 of papers/TemporalOrderingNotes.pdf.
				if (this.includeRuleBasedFixedLabels) {
					// if the tlink is of type TIME_TIME, there is only one appropriate relation.
					L label = this.labelInferenceRules.getRuleBasedFixedLabel(datumEntry.getKey());
					if (label != null) {
						Linear fixedLabelConstraint = new Linear();
						fixedLabelConstraint.add(1, tlinkVarPrefix + label);
						problem.add(fixedLabelConstraint, ">=", 1);
						
						if (!fixedAdjacencyMap.containsKey(tlinkableId1))
							fixedAdjacencyMap.put(tlinkableId1, new HashMap<String, L>());
						if (!fixedAdjacencyMap.containsKey(tlinkableId2))
							fixedAdjacencyMap.put(tlinkableId2, new HashMap<String, L>());
						fixedAdjacencyMap.get(tlinkableId1).put(tlinkableId2, label);
						fixedAdjacencyMap.get(tlinkableId2).put(tlinkableId1, this.labelInferenceRules.getConverse(label));
					}
				}
			}
			
			problem.setObjective(objective, OptType.MAX);
			
			for (String tlinkableId1 : tlinkableIds) {
				for (String tlinkableId2 : tlinkableIds) {
					if (tlinkableId1.equals(tlinkableId2))
						continue;
					
					String tlinkVarPrefix1 = "t_" + tlinkableId1 + "_" + tlinkableId2 + "_";
					String tlinkConverseVarPrefix1 = "t_" + tlinkableId2 + "_" + tlinkableId1 + "_";
					L fixedLabel1 = null;
					// fixedLabel1 would be in fixedAdjacencyMap if tlinkableId1 and tlinkableId2 are both times. possibly also otherwise
					if (fixedAdjacencyMap.containsKey(tlinkableId1) && fixedAdjacencyMap.get(tlinkableId1).containsKey(tlinkableId2))
						fixedLabel1 = fixedAdjacencyMap.get(tlinkableId1).get(tlinkableId2);
						
					Linear singleLabelConstraint = new Linear();
					
					for (L label : allLabels) {
						// t_id1_id2_l \in {0, 1}
						// Constraint 1 from section 1.1 of papers/TemporalOrderingNotes.pdf.
						String tlinkVar = tlinkVarPrefix1 + label;
						problem.setVarType(tlinkVar, Integer.class);
						problem.setVarUpperBound(tlinkVar, 1);
						problem.setVarLowerBound(tlinkVar, 0);
						
						// Single label for each tlink
						// Constraint 2 from section 1.1 of papers/TemporalOrderingNotes.pdf.
						singleLabelConstraint.add(1, tlinkVar);
						
						// Converse constraint
						// Constraint 3 from section 1.1 of papers/TemporalOrderingNotes.pdf.
						String tlinkConverseVar = tlinkConverseVarPrefix1 + this.labelInferenceRules.getConverse(label);
						Linear converseConstraint = new Linear();
						converseConstraint.add(1, tlinkVar);
						converseConstraint.add(-1, tlinkConverseVar);
						problem.add(converseConstraint, "<=", 0);
					}
					
					// Single label for each tlink
					// Constraint 2 from section 1.1 of papers/TemporalOrderingNotes.pdf.
					problem.add(singleLabelConstraint, "=", 1);
					
					// Transitive constraints
					// Constraint 5 and 6 from section 1.1 of papers/TemporalOrderingNotes.pdf.
					for (String tlinkableId3 : tlinkableIds) {
						if (tlinkableId2.equals(tlinkableId3) || tlinkableId1.equals(tlinkableId3))
							continue;
						
						// Don't infer relations unless there are datums for them in this graph
						if (!(adjacencyMap.containsKey(tlinkableId1) && adjacencyMap.get(tlinkableId1).containsKey(tlinkableId3)) 
								&& !(adjacencyMap.containsKey(tlinkableId3) && adjacencyMap.get(tlinkableId3).containsKey(tlinkableId1)))
							continue;
							
						String tlinkVarPrefix2 = "t_" + tlinkableId2 + "_" + tlinkableId3 + "_";
						String tlinkVarPrefix3 = "t_" + tlinkableId1 + "_" + tlinkableId3 + "_";
						
						L fixedLabel2 = null;
						if (fixedAdjacencyMap.containsKey(tlinkableId2) && fixedAdjacencyMap.get(tlinkableId2).containsKey(tlinkableId3))
							fixedLabel2 = fixedAdjacencyMap.get(tlinkableId2).get(tlinkableId3);
						
						for (L[][] compositionRule : compositionRules) {
							L firstConjunctLabel = compositionRule[0][0];
							L secondConjunctLabel = compositionRule[0][1];
							L[] consequentLabels = compositionRule[1];
							
							if (fixedLabel1 != null && !firstConjunctLabel.equals(fixedLabel1))
								continue;
							if (fixedLabel2 != null && !secondConjunctLabel.equals(fixedLabel2))
								continue;
							if (consequentLabels[0] == null || (!this.includeDisjunctiveRules && consequentLabels.length > 1))
								continue;
							
							Linear transitivityConstraint = new Linear();
							transitivityConstraint.add(1, tlinkVarPrefix1 + firstConjunctLabel);
							transitivityConstraint.add(1, tlinkVarPrefix2 + secondConjunctLabel);
							for (int i = 0; i < consequentLabels.length; i++)
								transitivityConstraint.add(-1, tlinkVarPrefix3 + consequentLabels[i]);
							problem.add(transitivityConstraint, "<=", 1);
						}
					}
				}
			}

			// Solve the ILP
			Solver solver = factory.get(); 
			Result result = solver.solve(problem);
			Map<TLinkDatum<L>, L> resultLabels = new HashMap<TLinkDatum<L>, L>();
			
			if (result == null) {
				// ILP failed, so just assign labels by maximum scores without
				// constraints
				String document = TLinkGraph.this.iterator().next().getTLink().getTarget().getTokenSpan().getDocument().getName();
				int sentence1 = TLinkGraph.this.iterator().next().getTLink().getSource().getTokenSpan().getSentenceIndex();
				int sentence2 = TLinkGraph.this.iterator().next().getTLink().getTarget().getTokenSpan().getSentenceIndex();
				output.debugWriteln("WARNING: TLinkGraph inference optimizer failed on " + document + "(" + sentence1 + "," + sentence2 + ").  "
								  + "Outputting best unconstrained graph instead...");
			
				for (Entry<TLinkDatum<L>, Map<L, Double>> datumEntry : scoredDatumLabels.entrySet()) {
					Map<L, Double> labelValues = datumEntry.getValue();
					
					double maxValue = Double.MIN_VALUE;
					L maxLabel = null;
					for (Entry<L, Double> labelEntry : labelValues.entrySet()) {
						if (labelEntry.getValue() > maxValue) {
							maxValue = labelEntry.getValue();
							maxLabel = labelEntry.getKey();
						}
					}
					
					resultLabels.put(datumEntry.getKey(), maxLabel);
				}
				
				return resultLabels;
			}
			
			// Get ILP results, and map labels from result to validLabels
			// using the given label mapping
			for (TLinkDatum<L> datum : TLinkGraph.this) {
				String tlinkableId1 = datum.getTLink().getSource().getId();
				String tlinkableId2 = datum.getTLink().getTarget().getId();
				
				String tlinkVarPrefix = "t_" + tlinkableId1 + "_" + tlinkableId2 + "_";
				
				for (L label : allLabels) {
					String tlinkVar = tlinkVarPrefix + label;
					boolean datumHasLabel = result.getBoolean(tlinkVar);
					if (datumHasLabel) {
						resultLabels.put(datum, labelMapping.map(label));
						continue;
					}
				}
			}
			
			return resultLabels;
			
		}
		
		
		private Map<TLinkDatum<L>, L> actuallyUnstructuredSetup(Map<TLinkDatum<L>, Map<L, Double>> scoredDatumLabels) {
			Map<TLinkDatum<L>, L> resultLabels = new HashMap<TLinkDatum<L>, L>();
		
			for (Entry<TLinkDatum<L>, Map<L, Double>> datumEntry : scoredDatumLabels.entrySet()) {
				Map<L, Double> labelValues = datumEntry.getValue();
				
				double maxValue = Double.MIN_VALUE;
				L maxLabel = null;
				for (Entry<L, Double> labelEntry : labelValues.entrySet()) {
					if (labelEntry.getValue() > maxValue) {
						maxValue = labelEntry.getValue();
						maxLabel = labelEntry.getKey();
					}
				}
				
				resultLabels.put(datumEntry.getKey(), maxLabel);
			}
			
			return resultLabels;
		}

		public String getGenericName() {
			StringBuilder name = new StringBuilder();
			
			name = name.append("Inference");
			name = name.append(this.labelInferenceRules.getGenericName());
			
			if (this.includeDisjunctiveRules)
				name = name.append("Disjunctive");
			if (this.includeRuleBasedFixedLabels)
				name = name.append("RuleBased");

			return name.toString();
		}
	}
		
		
	
	public class TLinkIterator implements Iterator<TLinkDatum<L>> {
		private Iterator<Map<String, TLinkDatum<L>>> mapIterator;
		private Iterator<TLinkDatum<L>> datumIterator;
		
		public TLinkIterator() {
			this.mapIterator = adjacencyMap.values().iterator();
			this.datumIterator = this.mapIterator.hasNext() ? this.mapIterator.next().values().iterator() : null;
		}
		
		
		@Override
		public boolean hasNext() {
			if (this.datumIterator == null)
				return false;
			
			while (!this.datumIterator.hasNext() && this.mapIterator.hasNext())
				this.datumIterator = this.mapIterator.next().values().iterator();
			
			return this.datumIterator.hasNext();
		}

		@Override
		public TLinkDatum<L> next() {
			if (this.datumIterator == null)
				return null;
			
			while (!this.datumIterator.hasNext() && this.mapIterator.hasNext())
				this.datumIterator = this.mapIterator.next().values().iterator();
			
			if (this.datumIterator.hasNext())
				return this.datumIterator.next();
			else
				return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
	}
}
