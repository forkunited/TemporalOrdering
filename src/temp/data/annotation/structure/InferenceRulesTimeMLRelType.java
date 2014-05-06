package temp.data.annotation.structure;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.annotation.timeml.Time;

public class InferenceRulesTimeMLRelType implements TLinkGraph.LabelInferenceRules<TLink.TimeMLRelType> {

	@Override
	public TimeMLRelType[][][] getCompositionRules() {
		return TLink.getTimeMLRelTypeCompositionRules();
	}

	@Override
	public TimeMLRelType getConverse(TimeMLRelType label) {
		return TLink.getConverseTimeMLRelType(label);
	}

	@Override
	public TimeMLRelType getRuleBasedFixedLabel(TLinkDatum<TimeMLRelType> datum) {
		TLink tlink = datum.getTLink();
		
		if (tlink.getType() != TLink.Type.TIME_TIME)
			return null;
		
		Time time1 = (Time)tlink.getSource();
		Time time2 = (Time)tlink.getTarget();
		
		return time1.getRelationToTime(time2);
	}

	@Override
	public String getGenericName() {
		return "TimeMLRelType";
	}

}
