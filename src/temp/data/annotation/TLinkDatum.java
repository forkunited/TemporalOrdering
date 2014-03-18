package temp.data.annotation;

import temp.data.annotation.timeml.TLink;
import ark.data.annotation.Datum;
import ark.data.annotation.nlp.TokenSpan;

public class TLinkDatum<L> extends Datum<L> {
	protected TLink tlink;
	
	public TLinkDatum(TLink tlink, L label) {
		this.tlink = tlink;
		this.label = label;
	}
	
	public TLink getTLink() {
		return this.tlink;
	}
	
	public static Tools<TLink.TimeMLRelType> getTimeMLRelTypeTools() {
		return new Tools<TLink.TimeMLRelType>() {
			@Override
			public TLink.TimeMLRelType labelFromString(String str) {
				return TLink.TimeMLRelType.valueOf(str);
			}
		};
	}
	
	private static abstract class Tools<L> extends Datum.Tools<TLinkDatum<L>, L> {
		public Tools() {
			super();
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTokenSpan";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { 
						tlinkDatum.getTLink().getSource().getTokenSpan()
					};
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "TargetTokenSpan";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { 
						tlinkDatum.getTLink().getTarget().getTokenSpan()
					};
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SignalTokenSpan";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSignal() == null)
						return null;
					return new TokenSpan[] { 
						tlinkDatum.getTLink().getSignal().getTokenSpan()
					};
				}
			});
		}
	}
}
