package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLink;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;
import ark.util.BidirectionalLookupTable;

public class FeatureTLinkAttribute<L> extends Feature<TLinkDatum<L>, L>{
	public enum Attribute {
		POSITION,
		TYPE
	}
	
	private Attribute attribute;
	private String[] parameterNames = { "attribute" };
	
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkAttribute() {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}
	
	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		if (this.attribute == Attribute.POSITION) {
			TLink.Position[] positions = TLink.Position.values();
			for (int i = 0; i < positions.length; i++)
				this.vocabulary.put(positions[i].toString(), i);
		} else if (this.attribute == Attribute.TYPE) {
			TLink.Type[] types = TLink.Type.values();
			for (int i = 0; i < types.length; i++)
				this.vocabulary.put(types[i].toString(), i);
		}
		
		return true;
	}

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
