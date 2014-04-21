package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLinkable;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;
import ark.util.BidirectionalLookupTable;

public class FeatureTLinkableType<L> extends Feature<TLinkDatum<L>, L> {
	public enum SourceOrTarget {
		SOURCE,
		TARGET
	}
	private SourceOrTarget sourceOrTarget;
	private BidirectionalLookupTable<String, Integer> vocabulary;
	private String[] parameterNames = { "sourceOrTarget"};


	public FeatureTLinkableType(){
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}

	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		vocabulary.put(TLinkable.Type.EVENT.toString(), vocabulary.size());
		vocabulary.put(TLinkable.Type.TIME.toString(), vocabulary.size());
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum) {
		Map<Integer, Double> vect = new HashMap<Integer, Double>();
		if (sourceOrTarget == SourceOrTarget.SOURCE)
			vect.put(vocabulary.get(datum.getTLink().getSource().getTLinkableType().toString()), 1.0);
		else if (sourceOrTarget == SourceOrTarget.TARGET)
			vect.put(vocabulary.get(datum.getTLink().getTarget().getTLinkableType().toString()), 1.0);
		else
			return null;
		return vect;
	}

	@Override
	public String getGenericName() {
		return "TLinkableType";
	}

	@Override
	public int getVocabularySize() {
		return vocabulary.size();
	}

	@Override
	protected String getVocabularyTerm(int index) {
		return vocabulary.reverseGet(index);
	}

	@Override
	protected boolean setVocabularyTerm(int index, String term) {
		vocabulary.put(term, index);
		return true;
	}

	@Override
	protected String[] getParameterNames() {
		return parameterNames;
	}

	@Override
	protected String getParameterValue(String parameter) {
		if (parameter.equals("sourceOrTarget"))
			return sourceOrTarget.toString();
		else
			return null;
	}

	@Override
	protected boolean setParameterValue(String parameter,
			String parameterValue, Tools<TLinkDatum<L>, L> datumTools) {
		if (parameter.equals("sourceOrTarget"))
			this.sourceOrTarget = (parameterValue == null) ? null : SourceOrTarget.valueOf(parameterValue);
		else
			return false;
		return true;
	}

	@Override
	protected Feature<TLinkDatum<L>, L> makeInstance() {
		return new FeatureTLinkableType<L>();
	}

}
