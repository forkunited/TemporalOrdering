package temp.data.annotation.structure;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.annotation.timeml.Time;

/**
 * InferenceRulesTimeMLRelType represents a set of rules for 
 * drawing inferences about the TimeML relationship types assigned 
 * to TLinks given the relationship types assigned to other
 * TLinks.  These inference rules are used by the
 * temp.data.annotation.structure.TLinkGraph datum structure as
 * constraints on a graph of TLinks.
 * 
 * The rules are generally specified by Section 1.1 of the 
 * papers/TemporalOrderingNotes.pdf document.
 * 
 * @author Bill McDowell
 *
 */
public class InferenceRulesTimeMLRelType implements TLinkGraph.LabelInferenceRules<TLink.TimeMLRelType> {

	/**
	 * Represents composition rules of the form: 
	 * 
	 * ((r(l')=t') and (r(l'')=t'')) implies or_i(r(l''')=t_i)
	 * 
	 * Where r(l)=t means that TLink l has relation t and
	 * 'or_i' is a disjunction over propositions indexed by i.
	 * These are the rules referred to as 'Transitivity' and
	 * 'Disjunctive Transitivity' in the 
	 * papers/TemporalOrderingNotes.pdf document.  They were 
	 * derived from Allen's interval algebra
	 * (see http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
	 * 
	 * Each rule is stored in a two-dimensional array of length 2.
	 * The first element of this array contains t' and t'', and
	 * the second element of the array contains t_i for each i.  
	 * 
	 * @return array representing composition rules
	 * 
	 */
	@Override
	public TimeMLRelType[][][] getCompositionRules() {
		return TLink.getTimeMLRelTypeCompositionRules();
	}

	/**
	 * Represents converse rules of the form:
	 * 
	 * (r(l')=t') implies (r(l'')=t'')
	 * 
	 * Where l' is a link in the reverse direction of l'' but
	 * between the same events/times, and 
	 * r(l)=t means that TLink l has relation t.
	 * These are the rules referred to as 'Converse' in the
	 * papers/TemporalOrderingNotes.pdf document.  They were 
	 * derived from Allen's interval algebra
	 * (see http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
	 * 
	 * @param label (t')
	 * @return converse of label (t'')
	 *
	 */
	@Override
	public TimeMLRelType getConverse(TimeMLRelType label) {
		return TLink.getConverseTimeMLRelType(label);
	}

	/**
	 * Represents rules about relation types based on properties
	 * of the TLink.  Currently, these are just rules about the 
	 * relations between time expressions given the time intervals
	 * referred to by those expressions.  They are referred to
	 * as 'Grounded Time-Time' in the 
	 * papers/TemporalOrderingNotes.pdf document.
	 * 
	 * @param datum
	 * @return TLink label type for datum based on the rules
	 * 
	 */
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
