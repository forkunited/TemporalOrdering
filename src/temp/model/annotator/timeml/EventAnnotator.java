package temp.model.annotator.timeml;

import temp.data.annotation.TempDocument;
import temp.data.annotation.timeml.Event;

/**
 * EventAnnotator is an abstract class for annotating the event
 * mentions in a temporal ordering document.
 * 
 * @author Bill McDowell
 *
 */
public abstract class EventAnnotator {
	/**
	 * @param document
	 * @return two-dimensional array of events in the document, where
	 * inner array with index i contains events for sentence i.
	 */
	public abstract Event[][] makeEvents(TempDocument document);
}
