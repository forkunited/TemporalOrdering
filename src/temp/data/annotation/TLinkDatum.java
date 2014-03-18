package temp.data.annotation;

import temp.data.annotation.timeml.TLink;
import ark.data.annotation.Datum;

public class TLinkDatum<L> extends Datum<L> {
	protected TLink tlink;
	
	public TLinkDatum(TLink tlink, L label) {
		this.tlink = tlink;
		this.label = label;
	}
	
	public TLink getTLink() {
		return this.tlink;
	}
}
