package temp.data.feature;

import java.util.HashMap;
import java.util.Map;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.Event;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLinkable;
import temp.data.feature.FeatureTLinkAttribute.Attribute;
import ark.data.annotation.Datum;
import ark.data.annotation.Datum.Tools;
import ark.data.annotation.nlp.TokenSpan;
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
 * 				field of the event specified in the config file. 
 * 			 	e.g. will add a feature for event aspect.
 */
public class FeatureEventField<L> extends Feature<TLinkDatum<L>, L>{
	public enum Field {
		TENSE,
		ASPECT,
		POLARITY,
		CLASS,
		PoS,
		MOOD,
		VERBFORM,
		MODALITY
	}
	
	private String sourceOrTarget;
	private Field field;
	private String[] parameterNames = { "sourceOrTarget", "field" };
	
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureEventField() {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}
	
	@Override
	public boolean init(FeaturizedDataSet<TLinkDatum<L>, L> dataSet) {
		if (this.field == Field.TENSE) {
			Event.TimeMLTense[] tenses = Event.TimeMLTense.values();
			for (int i = 0 ; i < tenses.length; i++){
				vocabulary.put(tenses[i].toString(), i);
			}
		} else if (this.field == Field.ASPECT) {
			Event.TimeMLAspect[] aspects = Event.TimeMLAspect.values();
			for (int i = 0 ; i < aspects.length; i++){
				vocabulary.put(aspects[i].toString(), i);
			}
		} else if (this.field == Field.POLARITY) {
			Event.TimeMLPolarity[] polarities = Event.TimeMLPolarity.values();
			for (int i = 0 ; i < polarities.length; i++){
				vocabulary.put(polarities[i].toString(), i);
			}
		} else if (this.field == Field.CLASS) {
			Event.TimeMLClass[] classes = Event.TimeMLClass.values();
			for (int i = 0 ; i < classes.length; i++){
				vocabulary.put(classes[i].toString(), i);
			}
		} else if (this.field == Field.PoS) {
			Event.TimeMLPoS[] poss = Event.TimeMLPoS.values();
			for (int i = 0 ; i < poss.length; i++){
				vocabulary.put(poss[i].toString(), i);
			}
		} else if (this.field == Field.MOOD) {
			Event.TimeMLMood[] moods = Event.TimeMLMood.values();
			for (int i = 0 ; i < moods.length; i++){
				vocabulary.put(moods[i].toString(), i);
			}
		} else if (this.field == Field.PoS) {
			Event.TimeMLPoS[] poss = Event.TimeMLPoS.values();
			for (int i = 0 ; i < poss.length; i++){
				vocabulary.put(poss[i].toString(), i);
			}
		} else if (this.field == Field.VERBFORM) {
			Event.TimeMLVerbForm[] verbForms = Event.TimeMLVerbForm.values();
			for (int i = 0 ; i < verbForms.length; i++){
				vocabulary.put(verbForms[i].toString(), i);
			}
		} else if (this.field == Field.MODALITY) {
			//TODO: This may be an issue. I believe the set of modalities should be captured as an enum, but since it's just a string
			//		we have to compute the possible enums from the whole dataset. 
			throw new IllegalArgumentException("This isn't implemented yet!");
		}		
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum) {
		Map<Integer, Double> vector = new HashMap<Integer, Double>();
		
		TLinkable linkable;
		if (sourceOrTarget.equals("Source"))
			linkable = datum.getTLink().getSource();
		else if (sourceOrTarget.equals("Target"))
			linkable = datum.getTLink().getTarget();
		else
			throw new IllegalArgumentException("bad param from conif file!");
		
		Event e;
		// to make sure we're only adding features for events:
		if (linkable.getTLinkableType() == TLinkable.Type.EVENT)
			e = (Event) linkable;
		else
			return vector;
		
		if (this.field == Field.TENSE)
			vector.put(this.vocabulary.get(e.getTimeMLTense().toString()), 1.0);
		else if (this.field == Field.ASPECT)
			vector.put(this.vocabulary.get(e.getTimeMLAspect().toString()), 1.0);
		else if (this.field == Field.POLARITY)
			vector.put(this.vocabulary.get(e.getTimeMLPolarity().toString()), 1.0);
		else if (this.field == Field.CLASS)
			vector.put(this.vocabulary.get(e.getTimeMLClass().toString()), 1.0);
		else if (this.field == Field.PoS)
			vector.put(this.vocabulary.get(e.getTimeMLPoS().toString()), 1.0);
		else if (this.field == Field.MOOD)
			vector.put(this.vocabulary.get(e.getTimeMLMood().toString()), 1.0);
		else if (this.field == Field.VERBFORM)
			vector.put(this.vocabulary.get(e.getTimeMLVerbForm().toString()), 1.0);
		else if (this.field == Field.MODALITY)
			vector.put(this.vocabulary.get(e.getModality()), 1.0);
	
		return vector;
	}

	@Override
	public String getGenericName() {
		return "EventField";
	}

	@Override
	public int getVocabularySize() {
		return this.vocabulary.size();
	}

	@Override
	protected String getVocabularyTerm(int index) {
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
			return this.sourceOrTarget;
		else if (parameter.equals("field"))
			return (this.field == null) ? null : this.field.toString();
		return null;
	}

	@Override
	protected boolean setParameterValue(String parameter,
			String parameterValue, Tools<TLinkDatum<L>, L> datumTools) {
		if (parameter.equals("sourceOrTarget"))
			this.sourceOrTarget = parameterValue;
		else if (parameter.equals("field"))
			this.field = (parameterValue == null) ? null : Field.valueOf(parameterValue);
		else
			return false;
		return true;
	}

	@Override
	protected Feature<TLinkDatum<L>, L> makeInstance() {
		return new FeatureTLinkAttribute<L>();
	}

}
