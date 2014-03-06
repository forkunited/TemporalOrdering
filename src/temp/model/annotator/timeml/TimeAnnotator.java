package temp.model.annotator.timeml;

import temp.data.annotation.TempDocument;
import temp.data.annotation.timeml.Time;

public abstract class TimeAnnotator {
	public abstract Time[][] makeTimes(TempDocument document);
	
}
