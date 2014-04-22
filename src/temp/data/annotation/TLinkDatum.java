package temp.data.annotation;

import temp.data.annotation.cost.TimeMLRelTypeSplit;
import temp.data.annotation.timeml.TLink;
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
	
	public static Tools<TLink.TimeMLRelType> getTimeMLRelTypeTools(DataTools dataTools) {
		return new Tools<TLink.TimeMLRelType>(dataTools) {
			@Override
			public TLink.TimeMLRelType labelFromString(String str) {
				return TLink.TimeMLRelType.valueOf(str);
			}
		};
	}
	
	public static Tools<TimeMLRelTypeSplit> getTimeMLRelTypeSplitTools(DataTools dataTools) {
		Tools<TimeMLRelTypeSplit> tools =  new Tools<TimeMLRelTypeSplit>(dataTools) {
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
					String labelPrefix = label.toString().split("_")[0];
					return TimeMLRelTypeSplit.valueOf(labelPrefix + "_0");
				}
			}
		);
		
		
		return tools;
	}
	
	private static abstract class Tools<L> extends Datum.Tools<TLinkDatum<L>, L> {
		public Tools(DataTools dataTools) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureTLinkAttribute<L>());
			this.addGenericFeature(new FeatureTLinkEventAttribute<L>());
			this.addGenericFeature(new FeatureTLinkTimeAttribute<L>());
			this.addGenericFeature(new FeatureTLinkTimeRelation<L>());
			this.addGenericFeature(new FeatureTLinkableType<L>());
			
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
