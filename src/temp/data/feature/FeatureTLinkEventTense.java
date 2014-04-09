package temp.data.feature;

import java.util.Map;

import temp.data.annotation.TLinkDatum;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;

/**
 * Don't implement.  Delete later. 
 * 
 * @author Bill McDowell
 */
public class FeatureTLinkEventTense<L> extends Feature<TLinkDatum<L>, L>{

	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGenericName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getParameterValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVocabularySize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getVocabularyTerm(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Feature<TLinkDatum<L>, L> makeInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean setParameterValue(String arg0, String arg1,
			Tools<TLinkDatum<L>, L> arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean setVocabularyTerm(int arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}


}
