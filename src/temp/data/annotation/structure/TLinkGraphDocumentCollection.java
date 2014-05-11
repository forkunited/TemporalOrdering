package temp.data.annotation.structure;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.structure.TLinkGraph.LabelInferenceRules;
import ark.data.annotation.DataSet;
import ark.data.annotation.structure.DatumStructureCollection;

public class TLinkGraphDocumentCollection<L> extends DatumStructureCollection<TLinkDatum<L>, L> {
	private LabelInferenceRules<L> labelInferenceRules;
	
	public TLinkGraphDocumentCollection(LabelInferenceRules<L> labelInferenceRules) {
		super();
		this.labelInferenceRules = labelInferenceRules;
	}
	
	@Override
	public String getGenericName() {
		return "TLinkGraphDocument";
	}

	@Override
	public DatumStructureCollection<TLinkDatum<L>, L> makeInstance(
			DataSet<TLinkDatum<L>, L> data) {
		Map<String, TLinkGraph<L>> datumStructures = new HashMap<String, TLinkGraph<L>>();
		for (TLinkDatum<L> datum : data) {
			String documentName = datum.getTLink().getSource().getTokenSpan().getDocument().getName();
			String structureKey = documentName;
			if (!datumStructures.containsKey(structureKey)) {
				datumStructures.put(structureKey, new TLinkGraph<L>(structureKey, data.getDatumTools(), this.labelInferenceRules));
			} 
			datumStructures.get(structureKey).add(datum);
		}
		
		TLinkGraphDocumentCollection<L> instance = new TLinkGraphDocumentCollection<L>(this.labelInferenceRules);
		instance.datumStructures.addAll(datumStructures.values());
		
		return instance;
	}

}
