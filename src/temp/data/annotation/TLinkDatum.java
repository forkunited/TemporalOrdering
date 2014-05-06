package temp.data.annotation;

import temp.data.annotation.cost.TimeMLRelTypeSplit;
import temp.data.annotation.structure.TLinkGraph;
import temp.data.annotation.structure.TLinkGraphDocumentCollection;
import temp.data.annotation.structure.TLinkGraphInterSentenceCollection;
import temp.data.annotation.structure.TLinkGraphIntraSentenceCollection;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.feature.FeatureTLinkAttribute;
import temp.data.feature.FeatureTLinkEventAttribute;
import temp.data.feature.FeatureTLinkTimeAttribute;
import temp.data.feature.FeatureTLinkTimeRelation;
import temp.data.feature.FeatureTLinkableType;
import ark.data.DataTools;
import ark.data.annotation.Datum;
import ark.data.annotation.Datum.Tools.LabelMapping;
import ark.data.annotation.nlp.TokenSpan;

public class TLinkDatum<L> extends Datum<L> {
	protected TLink tlink;
	
	public TLinkDatum(int id, TLink tlink, L label) {
		this.id = id;
		this.tlink = tlink;
		this.label = label;
	}
	
	public TLink getTLink() {
		return this.tlink;
	}
	
	public static Tools<TLink.TimeMLRelType> getTimeMLRelTypeTools(DataTools dataTools, TLinkGraph.LabelInferenceRules<TimeMLRelType> inferenceRules) {
		Tools<TLink.TimeMLRelType> tools = new Tools<TLink.TimeMLRelType>(dataTools, inferenceRules) {
			@Override
			public TLink.TimeMLRelType labelFromString(String str) {
				return TLink.TimeMLRelType.valueOf(str);
			}
		};
		
		tools.addLabelMapping(new LabelMapping<TLink.TimeMLRelType>() {
			@Override
			public String toString() {
				return "TE3ToDense";
			}
			
			@Override
			public TLink.TimeMLRelType map(TLink.TimeMLRelType label) {
				if (label == TLink.TimeMLRelType.BEFORE_OR_OVERLAP)
					return TLink.TimeMLRelType.VAGUE;
				else if (label == TLink.TimeMLRelType.BEGINS)
					return TLink.TimeMLRelType.BEFORE;
				else if (label == TLink.TimeMLRelType.BEGUN_BY)
					return TLink.TimeMLRelType.AFTER;
				else if (label == TLink.TimeMLRelType.DURING)
					return TLink.TimeMLRelType.SIMULTANEOUS;
				else if (label == TLink.TimeMLRelType.DURING_INV)
					return TLink.TimeMLRelType.SIMULTANEOUS;
				else if (label == TLink.TimeMLRelType.ENDED_BY)
					return TLink.TimeMLRelType.BEFORE;
				else if (label == TLink.TimeMLRelType.ENDS)
					return TLink.TimeMLRelType.AFTER;
				else if (label == TLink.TimeMLRelType.IAFTER)
					return TLink.TimeMLRelType.AFTER;
				else if (label == TLink.TimeMLRelType.IBEFORE)
					return TLink.TimeMLRelType.BEFORE;
				else if (label == TLink.TimeMLRelType.IDENTITY)
					return TLink.TimeMLRelType.SIMULTANEOUS;
				else if (label == TLink.TimeMLRelType.OVERLAP)
					return TLink.TimeMLRelType.VAGUE;
				else if (label == TLink.TimeMLRelType.OVERLAP_OR_AFTER)
					return TLink.TimeMLRelType.VAGUE;
				else if (label == TLink.TimeMLRelType.OVERLAPPED_BY)
					return TLink.TimeMLRelType.VAGUE;
				else if (label == TLink.TimeMLRelType.OVERLAPS)
					return TLink.TimeMLRelType.VAGUE;
				else 
					return label;
			}
		});
		
		tools.addLabelMapping(new LabelMapping<TLink.TimeMLRelType>() {
			@Override
			public String toString() {
				return "OnlyTimeBankDense";
			}
			
			@Override
			public TLink.TimeMLRelType map(TLink.TimeMLRelType label) {
				if (label == TLink.TimeMLRelType.BEFORE
						|| label == TLink.TimeMLRelType.AFTER
						|| label == TLink.TimeMLRelType.INCLUDES
						|| label == TLink.TimeMLRelType.IS_INCLUDED
						|| label == TLink.TimeMLRelType.SIMULTANEOUS
						|| label == TLink.TimeMLRelType.VAGUE)
					return label;
				else
					return TLink.TimeMLRelType.VAGUE;
			}
		});
		
		return tools;
	}
	
	public static Tools<TimeMLRelTypeSplit> getTimeMLRelTypeSplitTools(DataTools dataTools) {
		Tools<TimeMLRelTypeSplit> tools =  new Tools<TimeMLRelTypeSplit>(dataTools, null) {
			@Override
			public TimeMLRelTypeSplit labelFromString(String str) {
				return TimeMLRelTypeSplit.valueOf(str);
			}
		};
		
		tools.addLabelMapping(
			new LabelMapping<TimeMLRelTypeSplit>() {
				@Override
				public String toString() {
					return "NoSplit";
				}
				
				@Override
				public TimeMLRelTypeSplit map(TimeMLRelTypeSplit label) {
					String labelPrefix = label.toString().substring(0, label.toString().lastIndexOf("_"));
					return TimeMLRelTypeSplit.valueOf(labelPrefix + "_0");
				}
			}
		);
		
		return tools;
	}
	
	private static abstract class Tools<L> extends Datum.Tools<TLinkDatum<L>, L> {
		public Tools(DataTools dataTools, TLinkGraph.LabelInferenceRules<L> inferenceRules) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureTLinkAttribute<L>());
			this.addGenericFeature(new FeatureTLinkEventAttribute<L>());
			this.addGenericFeature(new FeatureTLinkTimeAttribute<L>());
			this.addGenericFeature(new FeatureTLinkTimeRelation<L>());
			this.addGenericFeature(new FeatureTLinkableType<L>());
			
			this.addGenericDatumStructureCollection(new TLinkGraphInterSentenceCollection<L>(inferenceRules));
			this.addGenericDatumStructureCollection(new TLinkGraphIntraSentenceCollection<L>(inferenceRules));
			this.addGenericDatumStructureCollection(new TLinkGraphDocumentCollection<L>(inferenceRules));
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTokenSpan";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSource().getTokenSpan() == null)
						return new TokenSpan[0];
					else 
						return new TokenSpan[] { tlinkDatum.getTLink().getSource().getTokenSpan() };
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "TargetTokenSpan";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getTarget().getTokenSpan() == null)
						return new TokenSpan[0];
					else
						return new TokenSpan[] { tlinkDatum.getTLink().getTarget().getTokenSpan() };
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SignalTokenSpan";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSignal() == null || tlinkDatum.getTLink().getSignal().getTokenSpan() == null)
						return new TokenSpan[0];
					else
						return new TokenSpan[] { tlinkDatum.getTLink().getSignal().getTokenSpan() };
				}
			});
		}
	}
}
