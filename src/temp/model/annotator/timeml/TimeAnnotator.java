package temp.model.annotator.timeml;

import java.util.List;

import temp.data.annotation.TempDocument;
import temp.data.annotation.timeml.Time;

public abstract class TimeAnnotator {
	public abstract Time[][] makeTimes(TempDocument document);
	public abstract List<Time> makeTimesForSentence(TempDocument document, int sentenceIndex);
}
