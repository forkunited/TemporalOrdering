package temp.data.annotation.structure.timeline;

import java.util.*;

import ark.data.annotation.DataSet;

import temp.data.annotation.TLinkDatum;
import temp.data.annotation.timeml.TLink.TimeMLRelType;
import temp.data.annotation.timeml.TLink;
import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.TLinkable.Type;
import temp.data.annotation.timeml.Time;
import temp.data.annotation.timeml.Time.TimeMLType;

/*
 * This class takes a set of tlinks and generates a timeline.
 * @author: Jesse Dodge
 */
public class TimelineBuilder {
	private Map<TLinkDatum<TimeMLRelType>, TimeMLRelType> tLinks;
	private Map<TLinkable, Map<TLinkable, TimeMLRelType>> vertices;
	
	// usually using predicted links
	public TimelineBuilder(Map<TLinkDatum<TimeMLRelType>, TimeMLRelType> tLinks){
		this.tLinks = tLinks;
		//buildTimeline();
	}
	
	// usually using human-annotated links
	public TimelineBuilder(DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> dataset) {		
		Map<TLinkDatum<TimeMLRelType>, TimeMLRelType> datasetAsMap = new HashMap<TLinkDatum<TimeMLRelType>, TimeMLRelType>();
		for (TLinkDatum<TimeMLRelType> curTLink : dataset){
			curTLink.getTLink().getSource().getTokenSpan().getDocument();
			datasetAsMap.put(curTLink, curTLink.getLabel());
		}
		tLinks = datasetAsMap;
		//buildTimeline();
	}

	// iterate over the dates in the dataset
	// for each date: 
	//  look at its relation with all of its neighbors, and propigate info using the rules defined in the doc
	public Timeline buildTimeline() {
		// we need to represent this as a graph, not a list of edges.
		vertices = makeTLinksGraph();
		Timeline timeline = new Timeline();
		// loop over the tlinkables and if it's a date, pass the info through
		// create one timelineentity for each vertex
		timeline.addAll(vertices.keySet());
		for (TLinkable curVertex : vertices.keySet()){
			if (curVertex.getTLinkableType() == TLinkable.Type.TIME){
				Time curTime = (Time)curVertex;
				if (curTime.getTimeMLType() == TimeMLType.DATE || curTime.getTimeMLType() == TimeMLType.TIME){
					Set<TLinkable> visitedNodes = new HashSet<TLinkable>();
					updateNeighbors(curVertex, timeline, visitedNodes);
				}
			}
		}
		testTimeline(timeline);
		return timeline;
	}
	
	public static String testTimeline(Timeline timeline){
		Calendar minDate = Calendar.getInstance();
		minDate.setTime(new Date(Long.MIN_VALUE));
		Calendar maxDate = Calendar.getInstance();
		maxDate.setTime(new Date(Long.MAX_VALUE));
		
		int numBounded = 0;
		int total = 0;

		for (TLinkable curEntity : timeline.timelineEntities.keySet()){
			if (curEntity.getTLinkableType() == Type.EVENT){
				TimelineEntity curTimelineEntity = timeline.timelineEntities.get(curEntity);
				if (curTimelineEntity.lowerStart != null && curTimelineEntity.lowerStart.compareTo(minDate) > 0 ||
						curTimelineEntity.upperStart != null && curTimelineEntity.upperStart.compareTo(maxDate) < 0 ||
								curTimelineEntity.lowerEnd != null && curTimelineEntity.lowerEnd.compareTo(maxDate) > 0 ||
										curTimelineEntity.upperEnd != null && curTimelineEntity.upperEnd.compareTo(maxDate) < 0){
					numBounded++;
				}
				total++;
			}
		}
		return "num bounded intervals: " + numBounded + ", total: " + total;
		//System.exit(0);
	}

	// propigating the info in a date 
	private void updateNeighbors(TLinkable curVertex, Timeline timeline, Set<TLinkable> visitedNodes){
		// this is just for testing
		visitedNodes.add(curVertex);
		// for each neighbor in the graph
		//  apply update rule from document
		//  add that neighbor to a set so we don't visit it twice
		for (TLinkable curNeighbor : vertices.get(curVertex).keySet()){
			if (curNeighbor.getTLinkableType() == TLinkable.Type.EVENT){
				boolean updateFound = updateNeighbor(curVertex, curNeighbor, vertices.get(curVertex).get(curNeighbor), timeline);
				if (updateFound)
					updateNeighbors(curNeighbor, timeline, visitedNodes);
			}
		}
	}
	
	private boolean updateNeighbor(TLinkable curVertex, TLinkable curNeighbor, TimeMLRelType relation, Timeline timeline){
		if (neighborIsNotTime(curNeighbor)){
			if (relation == TimeMLRelType.BEFORE)
				return timeline.updateBefore(curVertex, curNeighbor);
			else if (relation == TimeMLRelType.AFTER)
				return timeline.updateAfter(curVertex, curNeighbor);
			else if (relation == TimeMLRelType.SIMULTANEOUS)
				return timeline.updateSimultaneous(curVertex, curNeighbor);
			else if (relation == TimeMLRelType.INCLUDES)
				return timeline.updateIncludes(curVertex, curNeighbor);
			else if (relation == TimeMLRelType.IS_INCLUDED)
				return timeline.updateIsIncluded(curVertex, curNeighbor);
			else if (relation == TimeMLRelType.VAGUE)
				return false;
		}
		
		throw new IllegalArgumentException("Problem! We don't have a proper relation type.");
	}
	
	// it's a little unclear if we should be propigating this info into times. 
	// we defniitely shouldn't be for sets, this representation doesn't make sense for sets. 
	// maybe we should for durations? they are durations of an event, so it may make sense to have these
	private boolean neighborIsNotTime(TLinkable curNeighbor){
		if (curNeighbor.getTLinkableType() == Type.TIME){
			Time curNeighborTime = (Time) curNeighbor;
			return curNeighborTime.getTimeMLType() == TimeMLType.DURATION;
		}
			
		return true;
	}
		
	private Map<TLinkable, Map<TLinkable, TimeMLRelType>> makeTLinksGraph(){
		Map<TLinkable, Map<TLinkable, TimeMLRelType>> vertices = 
				new HashMap<TLinkable, Map<TLinkable, TimeMLRelType>>();
		for (TLinkDatum<TimeMLRelType> datum : tLinks.keySet()){
			TLinkable source = datum.getTLink().getSource();
			TLinkable target = datum.getTLink().getTarget();
			TimeMLRelType relation = datum.getLabel();
			
			addTLinkableToVertices(source, target, relation, vertices);
			addTLinkableToVertices(target, source, TLink.getConverseTimeMLRelType(relation), vertices);
		}
		
		return vertices;
	}
	
	// helper method to create vertices
	private void addTLinkableToVertices(TLinkable source, TLinkable target, 
			TimeMLRelType relation, Map<TLinkable, Map<TLinkable, TimeMLRelType>> vertices){

		if (!vertices.containsKey(source))
			vertices.put(source, new HashMap<TLinkable, TimeMLRelType>());
		vertices.get(source).put(target, relation);	
	}
}
















