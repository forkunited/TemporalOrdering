package temp.model.annotator.timeml;

import temp.data.annotation.TempDocument;
import temp.data.annotation.timeml.Event;

public abstract class EventAnnotator {
	public abstract Event[][] makeEvents(TempDocument document);
}
