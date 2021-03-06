package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLinkable;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;
import ark.util.BidirectionalLookupTable;

/**
 * FeatureTLinkableType computes the TLinkable-type 
 * (either EVENT or TIME) of one of the entities linked
 * by the TLink as an indicator vector to be used by a
 * model.  Whether the TLinkable-type is computed
 * for the source or target entity of the TLink is 
 * determined by the 'sourceOrTarget' parameter.
 * 
 * @author Bill McDowell
 *
 * @param <L> label type for the TLinks
 */
public class FeatureTLinkableType<L> extends Feature<TLinkDatum<L>, L> {
	public enum SourceOrTarget {
		SOURCE,
		TARGET
	}
	
	// Indicates whether to compute the TLinkable type for the
	// source or target entity of the TLink
	private SourceOrTarget sourceOrTarget; 
	
	// Vector mapping EVENT and TIME to indices
	private BidirectionalLookupTable<String, Integer> vocabulary;
	private String[] parameterNames = { "sourceOrTarget"};


	public FeatureTLinkableType(){
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}

	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		vocabulary.put(TLinkable.Type.EVENT.toString(), vocabulary.size()); // Map EVENT to 0
		vocabulary.put(TLinkable.Type.TIME.toString(), vocabulary.size()); // Map TIME to 1
		return true;
	}

	/**
	 * @param datum
	 * @return sparse indicator vector representing whether the specified
	 * entity (source or target) of the TLink is an EVENT or a TIME.
	 */
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
	public String getVocabularyTerm(int index) {
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
			return (sourceOrTarget == null) ? null : sourceOrTarget.toString();
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
