package temp.data.feature;

import java.util.List;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import ark.data.feature.Feature;

/**
 * Don't implement.  Delete later. 
 * 
 * @author Bill McDowell
 */
public class FeatureTLinkEventTense<L> extends Feature<TLinkDatum<L>, L>{
	
	public FeatureTLinkEventTense() {
		
	}
	
	@Override
	public Feature<TLinkDatum<L>, L> clone() {
		return new FeatureTLinkEventTense<L>();
	}

	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(List<TLinkDatum<L>> data) {
		return;
	}

	@Override
	public void init(String initStr) {
		return;
	}

	@Override
	public String toString(boolean withInit) {
		// TODO Auto-generated method stub
		return null;
	}

}
