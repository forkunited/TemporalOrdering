package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.Time;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;
import ark.util.BidirectionalLookupTable;

public class FeatureTLinkTimeAttribute<L> extends Feature<TLinkDatum<L>, L>{
	public enum Attribute {
		TIMEML_TYPE
	}
	
	public enum SourceOrTarget {
		SOURCE,
		TARGET
	}
	
	private SourceOrTarget sourceOrTarget;
	private Attribute attribute;
	private String[] parameterNames = { "sourceOrTarget", "attribute" };
	
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkTimeAttribute() {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}
	
	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		if (this.attribute == Attribute.TIMEML_TYPE) {
			Time.TimeMLType[] tenses = Time.TimeMLType.values();
			for (int i = 0 ; i < tenses.length; i++){
				this.vocabulary.put(tenses[i].toString(), i);
			}
		} 
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum) {
		Map<Integer, Double> vector = new HashMap<Integer, Double>();
		
		TLinkable linkable = null;
		if (this.sourceOrTarget == SourceOrTarget.SOURCE)
			linkable = datum.getTLink().getSource();
		else if (this.sourceOrTarget == SourceOrTarget.TARGET)
			linkable = datum.getTLink().getTarget();
		
		Time t = null;
		// to make sure we're only adding features for events:
		if (linkable.getTLinkableType() == TLinkable.Type.TIME)
			t = (Time) linkable;
		else
			return vector;
		
		if (this.attribute == Attribute.TIMEML_TYPE)
			vector.put(this.vocabulary.get(t.getTimeMLType().toString()), 1.0);
	
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkTimeAttribute";
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
		if (parameter.equals("sourceOrTarget"))
			return (this.sourceOrTarget == null) ? null : this.sourceOrTarget.toString();
		else if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : this.attribute.toString();
		return null;
	}

	@Override
	protected boolean setParameterValue(String parameter,
			String parameterValue, Tools<TLinkDatum<L>, L> datumTools) {
		if (parameter.equals("sourceOrTarget"))
			this.sourceOrTarget = (parameterValue == null) ? null : SourceOrTarget.valueOf(parameterValue);
		else if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(parameterValue);
		else
			return false;
		return true;
	}

	@Override
	protected Feature<TLinkDatum<L>, L> makeInstance() {
		return new FeatureTLinkTimeAttribute<L>();
	}

}
