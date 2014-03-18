package temp.data.annotation;

import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLink.TimeMLRelType;

public class TLinkTypeAnnotationTools extends TLinkAnnotationTools<TLink.TimeMLRelType> {
	@Override
	public TimeMLRelType labelFromString(String str) {
		return TLink.TimeMLRelType.valueOf(str);
	}
}
