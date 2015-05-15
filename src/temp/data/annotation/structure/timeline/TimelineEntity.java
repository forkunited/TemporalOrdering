package temp.data.annotation.structure.timeline;

import java.text.SimpleDateFormat;
import java.util.*;
import ark.util.Pair;

import temp.data.annotation.timeml.*;
import temp.data.annotation.timeml.NormalizedTimeValue.Reference;
import temp.data.annotation.timeml.TLinkable.Type;
import temp.data.annotation.timeml.Time.TimeMLType;


public class TimelineEntity {
	private Type type;
	private TimeMLType timeType;
	private Time time;
	public Calendar lowerStart;
	public Calendar upperStart;
	public Calendar lowerEnd;
	public Calendar upperEnd;
	
	public TimelineEntity(Time time) {
		type = Type.TIME;
		timeType = time.getTimeMLType();
		this.time = time;
		if (timeType != TimeMLType.DURATION && timeType != TimeMLType.SET){
			
			if (time.getValue().getReference() == Reference.NONE){
				lowerStart = time.getValue().getRange().getFirst();
				upperStart = (Calendar)lowerStart.clone();
				lowerEnd = time.getValue().getRange().getSecond();
				upperEnd = (Calendar)lowerEnd.clone();
			} else {
				addTimeWithReference(time);
			}
		}
	}
	
	// TODO: DELETE THIS IS JUST FOR TESTING
	private void checkAnchorTime(Time time){
		Time anchorTime = time.getAnchorTime();
		Calendar aStart = anchorTime.getValue().getRange().getFirst();
		Calendar aEnd = anchorTime.getValue().getRange().getSecond();
		double comparison = aStart.compareTo(aEnd);
		if (comparison > 0){
			System.out.println("PROBLEM! We have an anchor time which has a large interval.");
		}
	}
	
	public TimelineEntity(Event event){
		type = Type.EVENT;
		lowerStart = Calendar.getInstance();
		lowerStart.setTime(new Date(Long.MIN_VALUE));
		upperStart = Calendar.getInstance();
		upperStart.setTime(new Date(Long.MAX_VALUE));
		lowerEnd = Calendar.getInstance();
		lowerEnd.setTime(new Date(Long.MIN_VALUE));
		upperEnd = Calendar.getInstance();
		upperEnd.setTime(new Date(Long.MAX_VALUE));
	}

	private void addTimeWithReference(Time time){
		//checkAnchorTime(time);
		Time docTime = time.getAnchorTime();
		if (time.getValue().getReference() == Reference.PAST){
			pastRef(docTime);
		} else if (time.getValue().getReference() == Reference.PRESENT){
			presentRef(docTime);
		} else //if (time.getValue().getReference() == Reference.FUTURE){
			futureRef(docTime);
		
	}
	
	private void pastRef(Time docTime){
		lowerStart = Calendar.getInstance();
		lowerStart.setTime(new Date(Long.MIN_VALUE));
		upperStart = docTime.getValue().getRange().getFirst();
		lowerEnd= Calendar.getInstance();
		lowerEnd.setTime(new Date(Long.MIN_VALUE));
		upperEnd = docTime.getValue().getRange().getFirst();
	}
	
	private void presentRef(Time docTime){
		lowerStart = docTime.getValue().getRange().getFirst();
		upperStart = docTime.getValue().getRange().getFirst();
		lowerEnd = docTime.getValue().getRange().getSecond();
		upperEnd = docTime.getValue().getRange().getSecond();
	}
	
	private void futureRef(Time docTime){
		lowerStart = docTime.getValue().getRange().getSecond();
		upperStart = Calendar.getInstance();
		upperStart.setTime(new Date(Long.MAX_VALUE));
		lowerEnd = docTime.getValue().getRange().getSecond();
		upperEnd = Calendar.getInstance();
		upperEnd.setTime(new Date(Long.MAX_VALUE));
	}
	
	public String toString(){
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		
		String lowerStartString = (lowerStart == null) ? null : sdf.format(lowerStart.getTime());
		String upperStartString = (upperStart == null) ? null : sdf.format(upperStart.getTime());
		String lowerEndString = (lowerEnd == null) ? null : sdf.format(lowerEnd.getTime());
		String upperEndString = (upperEnd == null) ? null : sdf.format(upperEnd.getTime());
		
		return "[[" + lowerStartString + ", " + upperStartString + "], [" + lowerEndString + ", " + upperEndString + "]]";
	}
		
}
