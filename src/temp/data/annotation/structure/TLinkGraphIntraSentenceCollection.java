package temp.data.annotation.structure;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.structure.TLinkGraph.LabelInferenceRules;
import temp.data.annotation.timeml.TLink;
import ark.data.annotation.DataSet;
import ark.data.annotation.structure.DatumStructureCollection;

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
					datumStructures.put(structureKey, new TLinkGraph<L>(structureKey, this.labelInferenceRules));
				} 
				datumStructures.get(structureKey).add(datum);
			}
		}
		
		TLinkGraphIntraSentenceCollection<L> instance = new TLinkGraphIntraSentenceCollection<L>(this.labelInferenceRules);
		instance.datumStructures.addAll(datumStructures.values());
		
		return instance;
	}

}
