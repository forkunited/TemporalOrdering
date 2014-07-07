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

/**
 * TLinkDatum represents a single TLink datum for
 * a task involving the classification of TLinks. The datum consists 
 * of a text document and a label (e.g. a relationship type) assigned 
 * to the document.
 *
 * @author Bill McDowell
 *
 * @param <L> datum label type
 */
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
	
	
	/**
	 * 
	 * @param dataTools
	 * @param inferenceRules Transitivity rules for inferring the type of one TLink from others
	 * @return tools for manipulating TLinkDatums with TimeMLRelType 
	 * (relationship type) labels.  
	 */
	public static Tools<TLink.TimeMLRelType> getTimeMLRelTypeTools(DataTools dataTools, TLinkGraph.LabelInferenceRules<TimeMLRelType> inferenceRules) {
		Tools<TLink.TimeMLRelType> tools = new Tools<TLink.TimeMLRelType>(dataTools, inferenceRules) {
			@Override
			public TLink.TimeMLRelType labelFromString(String str) {
				return TLink.TimeMLRelType.valueOf(str);
			}
		};
		
		/**
		 * Map TempEval3 relationship types to TimeBank-Dense relationship
		 * types
		 */
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
		
		/**
		 * Map all labels to corresponding labels in TimeBank-Dense
		 */
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
	
	/**
	 * @param dataTools
	 * @return tools for manipulating TLink datums with split relationship types
	 * (for evaluating cost learning models).  See temp.data.annotation.cost.TimeMLRelTypeSplit
	 * for more detail about the purpose of this.
	 */
	public static Tools<TimeMLRelTypeSplit> getTimeMLRelTypeSplitTools(DataTools dataTools) {
		Tools<TimeMLRelTypeSplit> tools =  new Tools<TimeMLRelTypeSplit>(dataTools, null) {
			@Override
			public TimeMLRelTypeSplit labelFromString(String str) {
				return TimeMLRelTypeSplit.valueOf(str);
			}
		};
		
		/**
		 * Map split labels to single label
		 */
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
	
	/**
	 * Tools such as generic features, datum structures, tokenspan extractors,
	 * etc for manipulating TLink datums.  An instantiation of the Tools can 
	 * be used generate generic instances of features, models, etc that can be 
	 * used by ARKWater during deserialization of experiment configuration files.
	 * See ARKWater for more detailed documentation on the deserialization 
	 * process.
	 * 
	 * @author Bill McDowell
	 *
	 * @param <L> TLinkDatum label type
	 */
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
