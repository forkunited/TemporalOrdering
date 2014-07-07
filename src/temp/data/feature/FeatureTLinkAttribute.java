package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLink;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;
import ark.util.BidirectionalLookupTable;

/**
 * FeatureTLinkAttribute computes an attribute of a TLink
 * as an indicator vector to be used by a model. The attribute
 * that is computed for a TLink is determined by the 'attribute'
 * parameter.  Valid attributes are enum properties of the 
 * temp.data.annotation.timeml.TLink class.  The computed
 * indicator vector has a component for each value of the
 * enum corresponding to the specified attribute.
 * 
 * @author Bill McDowell
 *
 * @param <L> label type of the TLink
 */
public class FeatureTLinkAttribute<L> extends Feature<TLinkDatum<L>, L>{
	public enum Attribute {
		POSITION,
		TYPE
	}
	
	// Determines which attribute to compute
	private Attribute attribute;
	private String[] parameterNames = { "attribute" };
	
	// Mapping from attribute values to indices of the returned vector
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkAttribute() {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}
	
	/**
	 * @param dataSet
	 * @return true if the feature has been initialized with a mapping
	 * from attribute values to their indices within vectors computed
	 * for datums
	 */
	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		if (this.attribute == Attribute.POSITION) {
			TLink.Position[] positions = TLink.Position.values();
			for (int i = 0; i < positions.length; i++)
				this.vocabulary.put(positions[i].toString(), vocabulary.size());
		} else if (this.attribute == Attribute.TYPE) {
			TLink.Type[] types = TLink.Type.values();
			for (int i = 0; i < types.length; i++)
				this.vocabulary.put(types[i].toString(), vocabulary.size());
		}
		
		return true;
	}

	/**
	 * @param datum
	 * @return sparse mapping from attribute value indices to indicators of
	 * whether or not the attribute takes the value
	 */
	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum) {
		Map<Integer, Double> vector = new HashMap<Integer, Double>();
		
		if (this.attribute == Attribute.POSITION)
			vector.put(this.vocabulary.get(datum.getTLink().getPosition().toString()), 1.0);
		else if (this.attribute == Attribute.TYPE)
			vector.put(this.vocabulary.get(datum.getTLink().getType().toString()), 1.0);
		
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkAttribute";
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
		if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : this.attribute.toString();
		return null;
	}

	@Override
	protected boolean setParameterValue(String parameter,
			String parameterValue, Tools<TLinkDatum<L>, L> datumTools) {
		if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(parameterValue);
		else
			return false;
		return true;
	}

	@Override
	protected Feature<TLinkDatum<L>, L> makeInstance() {
		return new FeatureTLinkAttribute<L>();
	}

}
