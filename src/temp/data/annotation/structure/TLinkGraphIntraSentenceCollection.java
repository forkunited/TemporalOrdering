package temp.data.annotation.structure;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.structure.TLinkGraph.LabelInferenceRules;
import temp.data.annotation.timeml.TLink;
import ark.data.annotation.DataSet;
import ark.data.annotation.structure.DatumStructureCollection;

/**
 * TLinkGraphIntraSentenceCollection represents a collection 
 * containing a temp.data.annotation.structure.TLinkGraph
 * for every sentence.  Each TLinkGraph in the
 * collection contains all TLinks within a single sentence.
 * 
 * This collection is used by a structured prediction model
 * to group TLinks by sentence and impose constraints within
 * the groups.
 * 
 * @author Bill McDowell
 *
 * @param <L> TLink label type
 */
public class TLinkGraphIntraSentenceCollection<L> extends DatumStructureCollection<TLinkDatum<L>, L> {
	private LabelInferenceRules<L> labelInferenceRules;
	
	public TLinkGraphIntraSentenceCollection(LabelInferenceRules<L> labelInferenceRules) {
		this.labelInferenceRules = labelInferenceRules;
	}
	
	@Override
	public String getGenericName() {
		return "TLinkGraphIntraSentence";
	}

	@Override
	public DatumStructureCollection<TLinkDatum<L>, L> makeInstance(
			DataSet<TLinkDatum<L>, L> data) {
		Map<String, TLinkGraph<L>> datumStructures = new HashMap<String, TLinkGraph<L>>();
		for (TLinkDatum<L> datum : data) {
			if (datum.getTLink().getPosition() != TLink.Position.BETWEEN_SENTENCE) {
				String documentName = datum.getTLink().getSource().getTokenSpan().getDocument().getName();
				int sourceSentenceIndex = datum.getTLink().getSource().getTokenSpan().getSentenceIndex();
				int targetSentenceIndex = datum.getTLink().getTarget().getTokenSpan().getSentenceIndex();
				int tlinkSentenceIndex = Math.max(sourceSentenceIndex, targetSentenceIndex);
				String structureKey = documentName + "_" + tlinkSentenceIndex;
				if (!datumStructures.containsKey(structureKey)) {
					datumStructures.put(structureKey, new TLinkGraph<L>(structureKey, data.getDatumTools(), this.labelInferenceRules));
				} 
				datumStructures.get(structureKey).add(datum);
			}
		}
		
		TLinkGraphIntraSentenceCollection<L> instance = new TLinkGraphIntraSentenceCollection<L>(this.labelInferenceRules);
		instance.datumStructures.addAll(datumStructures.values());
		
		return instance;
	}

}
