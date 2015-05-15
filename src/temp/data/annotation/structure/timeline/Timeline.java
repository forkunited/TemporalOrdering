package temp.data.annotation.structure.timeline;

import temp.data.annotation.timeml.*;

import java.util.*;


public class Timeline {
	
	// TODO: FOR TESTING DELETEEE
	//private Set<Time> thingsWithoutRange;
	
	
	public Map<TLinkable, TimelineEntity> timelineEntities;
	
	public Timeline(){
		timelineEntities = new HashMap<TLinkable, TimelineEntity>();
	}

	private void addTime(Time newTime){
		//if (newTime.getValue().getRange() == null){
		//	thingsWithoutRange.add(newTime);
		//}
		timelineEntities.put(newTime, new TimelineEntity(newTime));
	}
	
	private void addEvent(Event newEvent){
		timelineEntities.put(newEvent, new TimelineEntity(newEvent));
	}

	// to create one timelne entity for each TLinkable in the dataset
	public void addAll(Set<TLinkable> entities) {
		for (TLinkable entity : entities){
			if (entity.getTLinkableType() == TLinkable.Type.TIME)
				addTime((Time)entity);
			else if (entity.getTLinkableType() == TLinkable.Type.EVENT)
				addEvent((Event)entity);
			else{
				throw new IllegalArgumentException("Problem! Encountered something other than time or event.");
			}
		}
	}

	public boolean updateBefore(TLinkable curVertex, TLinkable curNeighbor) {
		TimelineEntity curTimelineEntity = timelineEntities.get(curVertex);
		TimelineEntity neighborTimelineEntity = timelineEntities.get(curNeighbor);
		boolean foundUpdate = false;
		if (curTimelineEntity.lowerEnd.compareTo(neighborTimelineEntity.lowerStart) > 0){
			neighborTimelineEntity.lowerStart = (Calendar)curTimelineEntity.lowerEnd.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.lowerEnd.compareTo(neighborTimelineEntity.lowerEnd) > 0){
			neighborTimelineEntity.lowerEnd = (Calendar)curTimelineEntity.lowerEnd.clone();
			foundUpdate = true;
		}
		return foundUpdate;
	}

	public boolean updateAfter(TLinkable curVertex, TLinkable curNeighbor) {
		TimelineEntity curTimelineEntity = timelineEntities.get(curVertex);
		TimelineEntity neighborTimelineEntity = timelineEntities.get(curNeighbor);
		boolean foundUpdate = false;
		if (curTimelineEntity.upperStart.compareTo(neighborTimelineEntity.upperStart) < 0){
			neighborTimelineEntity.upperStart = (Calendar)curTimelineEntity.upperStart.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.upperStart.compareTo(neighborTimelineEntity.upperEnd) < 0){
			neighborTimelineEntity.upperEnd = (Calendar)curTimelineEntity.upperStart.clone();
			foundUpdate = true;
		}
		return foundUpdate;
	}

	public boolean updateSimultaneous(TLinkable curVertex, TLinkable curNeighbor) {
		TimelineEntity curTimelineEntity = timelineEntities.get(curVertex);
		TimelineEntity neighborTimelineEntity = timelineEntities.get(curNeighbor);
		boolean foundUpdate = false;
		if (curTimelineEntity.lowerStart.compareTo(neighborTimelineEntity.lowerStart) > 0){
			neighborTimelineEntity.lowerStart = (Calendar)curTimelineEntity.lowerStart.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.upperStart.compareTo(neighborTimelineEntity.upperStart) < 0){
			neighborTimelineEntity.upperStart = (Calendar)curTimelineEntity.upperStart.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.lowerEnd.compareTo(neighborTimelineEntity.lowerEnd) > 0){
			neighborTimelineEntity.lowerEnd = (Calendar)curTimelineEntity.lowerEnd.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.upperEnd.compareTo(neighborTimelineEntity.upperEnd) < 0){
			neighborTimelineEntity.upperEnd = (Calendar)curTimelineEntity.upperEnd.clone();
			foundUpdate = true;
		}
		return foundUpdate;
	}

	public boolean updateIncludes(TLinkable curVertex, TLinkable curNeighbor) {
		TimelineEntity curTimelineEntity = timelineEntities.get(curVertex);
		TimelineEntity neighborTimelineEntity = timelineEntities.get(curNeighbor);
		boolean foundUpdate = false;
		if (curTimelineEntity.lowerStart.compareTo(neighborTimelineEntity.lowerStart) > 0){
			neighborTimelineEntity.lowerStart = (Calendar)curTimelineEntity.lowerStart.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.upperEnd.compareTo(neighborTimelineEntity.upperStart) < 0){
			neighborTimelineEntity.upperStart = (Calendar)curTimelineEntity.upperEnd.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.lowerStart.compareTo(neighborTimelineEntity.lowerEnd) > 0){
			neighborTimelineEntity.lowerEnd = (Calendar)curTimelineEntity.lowerStart.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.upperEnd.compareTo(neighborTimelineEntity.upperEnd) < 0){
			neighborTimelineEntity.upperEnd = (Calendar)curTimelineEntity.upperEnd.clone();
			foundUpdate = true;
		}
		return foundUpdate;
	}

	public boolean updateIsIncluded(TLinkable curVertex, TLinkable curNeighbor) {
		TimelineEntity curTimelineEntity = timelineEntities.get(curVertex);
		TimelineEntity neighborTimelineEntity = timelineEntities.get(curNeighbor);
		boolean foundUpdate = false;
		if (curTimelineEntity.upperStart.compareTo(neighborTimelineEntity.upperStart) < 0){
			neighborTimelineEntity.upperStart = (Calendar)curTimelineEntity.upperStart.clone();
			foundUpdate = true;
		}
		if (curTimelineEntity.lowerEnd.compareTo(neighborTimelineEntity.lowerEnd) > 0){
			neighborTimelineEntity.lowerEnd = (Calendar)curTimelineEntity.lowerEnd.clone();
			foundUpdate = true;
		}
		return foundUpdate;
	}
}
