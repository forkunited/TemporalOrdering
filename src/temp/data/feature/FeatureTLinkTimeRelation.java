package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.TempDocument;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.Time;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;
import ark.util.BidirectionalLookupTable;

/**
 * FeatureTLinkTimeRelation computes the relation type 
 * of a Time-Time link according to the grounding of the times
 * to intervals.  The 'relation' property determines whether
 * to compute the relation type between the source and target
 * of a TLink, between the source time of the TLink and the DCT, or
 * between the target time of the TLink and the DCT.  The 
 * computed relation type is represented as an indicator vector
 * to be used by a model.
 * 
 * @author Bill McDowell
 *
 * @param <L> label type of the TLink
 */
public class FeatureTLinkTimeRelation<L> extends Feature<TLinkDatum<L>, L>{
	public enum Relation {
		TARGET_DCT,
		SOURCE_DCT,
		SOURCE_TARGET
	}
	
	private Relation relation;
	private String[] parameterNames = { "relation" };
	
	// Maps relationship types to vector component indices
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkTimeRelation() {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}
	
	/**
	 * @param dataSet
	 * @return true if the feature has been initialized with a mapping
	 * from relationship types to vector component indices 
	 */
	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		TLink.TimeMLRelType[] relations = TLink.TimeMLRelType.values();
		for (int i = 0 ; i < relations.length; i++){
			this.vocabulary.put(relations[i].toString(), i);
		}
	
		return true;
	}

	/**
	 * @param datum
	 * @return a sparse indicator vector representing the relationship
	 * type of either the datum's Time-Time TLink or the TLink between 
	 * one of the
	 * datum's incident timexes and the DCT according to the groundings
	 * of the times to intervals.
	 */
	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum) {
		Map<Integer, Double> vector = new HashMap<Integer, Double>();
		
		Time time1 = null;
		Time time2 = null;
		if (this.relation == Relation.SOURCE_DCT) {
			if (datum.getTLink().getSource().getTLinkableType() != TLinkable.Type.TIME ||
					datum.getTLink().getTarget().getTLinkableType() != TLinkable.Type.EVENT)
				return vector;
			time1 = (Time)datum.getTLink().getSource();
			time2 = ((TempDocument)time1.getTokenSpan().getDocument()).getCreationTime();
		} else if (this.relation == Relation.TARGET_DCT) {
			if (datum.getTLink().getSource().getTLinkableType() != TLinkable.Type.EVENT ||
					datum.getTLink().getTarget().getTLinkableType() != TLinkable.Type.TIME)
				return vector;
			
			time1 = (Time)datum.getTLink().getTarget();
			time2 = ((TempDocument)time1.getTokenSpan().getDocument()).getCreationTime();
		} else if (this.relation == Relation.SOURCE_TARGET) {
			if (datum.getTLink().getSource().getTLinkableType() != TLinkable.Type.TIME ||
					datum.getTLink().getTarget().getTLinkableType() != TLinkable.Type.TIME)
				return vector;
			
			time1 = (Time)datum.getTLink().getSource();
			time2 = (Time)datum.getTLink().getTarget();
		}
		
		if (time1 == null || time2 == null)
			return vector;
		
		TLink.TimeMLRelType relType = time1.getRelationToTime(time2);
		vector.put(this.vocabulary.get(relType.toString()), 1.0);
	
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkTimeRelation";
	}

	@Override
	public int getVocabularySize() {
		return this.vocabulary.size();
	}

	@Override
	public String getVocabularyTerm(int index) {
		return this.vocabulary.reverseGet(index);
	}

	@Override
	protected boolean setVocabularyTerm(int index, String term) {
		this.vocabulary.put(term, index);
		return true;
	}

	@Override
	protected String[] getParameterNames() {
		return this.parameterNames;
	}

	@Override
	protected String getParameterValue(String parameter) {
		if (parameter.equals("relation"))
			return (this.relation == null) ? null : this.relation.toString();
		return null;
	}

	@Override
	protected boolean setParameterValue(String parameter,
			String parameterValue, Tools<TLinkDatum<L>, L> datumTools) {
		if (parameter.equals("relation"))
			this.relation = (parameterValue == null) ? null : Relation.valueOf(parameterValue);
		else
			return false;
		return true;
	}

	@Override
	protected Feature<TLinkDatum<L>, L> makeInstance() {
		return new FeatureTLinkTimeRelation<L>();
	}
}
