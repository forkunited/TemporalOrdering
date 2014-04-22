package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.TLinkable;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.data.feature.FeaturizedDataSet;
import ark.util.BidirectionalLookupTable;

/**
 * 
 * @author jesse
 *
 * @param Takes a set of parameters from the config file. 
 * 		  One param is whether to compute features based on the source or target.
 * @whatitdoes if whichever of {source, target} is specified in the conif file is an event, will add a feature for the 
 * 				attribute of the event specified in the config file. 
 * 			 	e.g. will add a feature for event aspect.
 */
public class FeatureTLinkEventAttribute<L> extends Feature<TLinkDatum<L>, L>{
	public enum Attribute {
		TIMEML_TENSE,
		TIMEML_ASPECT,
		TIMEML_POLARITY,
		TIMEML_CLASS,
		TIMEML_POS,
		TIMEML_MOOD,
		TIMEML_VERB_FORM,
		MODALITY
	}
	
	public enum SourceOrTarget {
		SOURCE,
		TARGET
	}
	
	private SourceOrTarget sourceOrTarget;
	private Attribute attribute;
	private String[] parameterNames = { "sourceOrTarget", "attribute" };
	
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkEventAttribute() {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}
	
	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		if (this.attribute == Attribute.TIMEML_TENSE) {
			Event.TimeMLTense[] tenses = Event.TimeMLTense.values();
			for (int i = 0 ; i < tenses.length; i++){
				vocabulary.put(tenses[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_ASPECT) {
			Event.TimeMLAspect[] aspects = Event.TimeMLAspect.values();
			for (int i = 0 ; i < aspects.length; i++){
				vocabulary.put(aspects[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_POLARITY) {
			Event.TimeMLPolarity[] polarities = Event.TimeMLPolarity.values();
			for (int i = 0 ; i < polarities.length; i++){
				vocabulary.put(polarities[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_CLASS) {
			Event.TimeMLClass[] classes = Event.TimeMLClass.values();
			for (int i = 0 ; i < classes.length; i++){
				vocabulary.put(classes[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_POS) {
			Event.TimeMLPoS[] poss = Event.TimeMLPoS.values();
			for (int i = 0 ; i < poss.length; i++){
				vocabulary.put(poss[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_MOOD) {
			Event.TimeMLMood[] moods = Event.TimeMLMood.values();
			for (int i = 0 ; i < moods.length; i++){
				vocabulary.put(moods[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_VERB_FORM) {
			Event.TimeMLVerbForm[] verbForms = Event.TimeMLVerbForm.values();
			for (int i = 0 ; i < verbForms.length; i++){
				vocabulary.put(verbForms[i].toString(), i);
			}
		} else if (this.attribute == Attribute.MODALITY) {
			//TODO: This may be an issue. I believe the set of modalities should be captured as an enum, but since it's just a string
			//		we have to compute the possible enums from the whole dataset. 
			throw new UnsupportedOperationException();
		}		
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum) {
		Map<Integer, Double> vector = new HashMap<Integer, Double>();
		
		TLinkable linkable = null;
		if (this.sourceOrTarget == SourceOrTarget.SOURCE)
			linkable = datum.getTLink().getSource();
		else if (this.sourceOrTarget == SourceOrTarget.TARGET)
			linkable = datum.getTLink().getTarget();
		
		Event e = null;
		// to make sure we're only adding features for events:
		if (linkable.getTLinkableType() == TLinkable.Type.EVENT)
			e = (Event) linkable;
		else
			return vector;
		
		if (this.attribute == Attribute.TIMEML_TENSE)
			vector.put(this.vocabulary.get(e.getTimeMLTense().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_ASPECT)
			vector.put(this.vocabulary.get(e.getTimeMLAspect().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_POLARITY)
			vector.put(this.vocabulary.get(e.getTimeMLPolarity().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_CLASS)
			vector.put(this.vocabulary.get(e.getTimeMLClass().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_POS)
			vector.put(this.vocabulary.get(e.getTimeMLPoS().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_MOOD)
			vector.put(this.vocabulary.get(e.getTimeMLMood().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_VERB_FORM)
			vector.put(this.vocabulary.get(e.getTimeMLVerbForm().toString()), 1.0);
		else if (this.attribute == Attribute.MODALITY)
			vector.put(this.vocabulary.get(e.getModality()), 1.0);
	
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkEventAttribute";
	}

	@Override
	public int getVocabularySize() {
		return this.vocabulary.size();
	}

	@Override
	public String getVocabularyTerm(int index) {
		return this.vocabulary.reverseGet(index);
	}

	@Override
	protected boolean setVocabularyTerm(int index, String term) {
		this.vocabulary.put(term, index);
		return true;
	}

	@Override
	protected String[] getParameterNames() {
		return this.parameterNames;
	}

	@Override
	protected String getParameterValue(String parameter) {
		if (parameter.equals("sourceOrTarget"))
			return (this.sourceOrTarget == null) ? null : this.sourceOrTarget.toString();
		else if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : this.attribute.toString();
		return null;
	}

	@Override
	protected boolean setParameterValue(String parameter,
			String parameterValue, Tools<TLinkDatum<L>, L> datumTools) {
		if (parameter.equals("sourceOrTarget"))
			this.sourceOrTarget = (parameterValue == null) ? null : SourceOrTarget.valueOf(parameterValue);
		else if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(parameterValue);
		else
			return false;
		return true;
	}

	@Override
	protected Feature<TLinkDatum<L>, L> makeInstance() {
		return new FeatureTLinkEventAttribute<L>();
	}

}
