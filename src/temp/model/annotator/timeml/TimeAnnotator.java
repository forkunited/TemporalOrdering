package temp.model.annotator.timeml;

import temp.data.annotation.TempDocument;
import temp.data.annotation.timeml.Time;

/**
 * 
 * TimeAnnotator is an abstract class for annotating the time
 * expressions in a temporal ordering document.
 * 
 * @author Bill McDowell
 */
public abstract class TimeAnnotator {
	/**
	 * @param document
	 * @return two-dimensional array of time expressions in the 
	 * document, where inner array with index i contains times for 
	 * sentence i.
	 */
	public abstract Time[][] makeTimes(TempDocument document);
}
