package temp.data.annotation.structure.timeline;

import java.util.Calendar;
import java.util.Date;

import ark.util.OutputWriter;

import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.Time.TimeMLType;

public class TimelineComparitor {
	// creating max and min values to which we can compare
	Calendar lowerStart;
	Calendar upperStart;
	Calendar lowerEnd;
	Calendar upperEnd;
	double maxDistance;
	int numUnbounded;
	
	public TimelineComparitor(){
		lowerStart = Calendar.getInstance();
		lowerStart.setTime(new Date(Long.MIN_VALUE));
		upperStart = Calendar.getInstance();
		upperStart.setTime(new Date(Long.MAX_VALUE));
		lowerEnd = Calendar.getInstance();
		lowerEnd.setTime(new Date(Long.MIN_VALUE));
		upperEnd = Calendar.getInstance();
		upperEnd.setTime(new Date(Long.MAX_VALUE));
		maxDistance = 0;
	}
	
	// first is predicted
	// second is gold
	public double compare(Timeline firstTimeline,
			Timeline secondTimeline, OutputWriter output) {
		//tryingEvaluations();
		double hausdorfDistance = sumOfHausdorfDistance(firstTimeline, secondTimeline);
		numUnbounded = 0;
		maxDistance = 0;
		double expectedValueDistance = sumOfUniformDistance(firstTimeline, secondTimeline);
		
		writeOut(output, hausdorfDistance, expectedValueDistance, firstTimeline, secondTimeline);
		
		return hausdorfDistance;

	}
	
	private void writeOut(OutputWriter output, double hausdorfDistance, double expectedValueDistance, Timeline firstTimeline, Timeline secondTimeline){
		output.resultsWriteln("Predicted timeline:" + TimelineBuilder.testTimeline(firstTimeline));
		output.resultsWriteln("Gold timeline: " + TimelineBuilder.testTimeline(secondTimeline));
		output.resultsWriteln("Hausdorf distance: " + hausdorfDistance);
		output.resultsWriteln("Expected Value Distance: " + expectedValueDistance);
	}
	
	private double sumOfUniformDistance(Timeline firstTimeline, Timeline secondTimeline){
		double distance = 0.0;
		for (TLinkable curEntity : firstTimeline.timelineEntities.keySet()){
			TimelineEntity firstEntity = firstTimeline.timelineEntities.get(curEntity);
			TimelineEntity secondEntity = secondTimeline.timelineEntities.get(curEntity);
			distance += calculateUniformDistance(firstEntity, secondEntity);
		}
		//System.out.println("Distance found: " + distance);
		//System.out.println("Num unbounded: " + numUnbounded);
		// to increase the distance by the max for each time we have an unbounded interval matched with a bounded one.
		for (int i = 0; i < numUnbounded; i++){
			distance += maxDistance;
		}
		return distance;
	}
	
	private double calculateUniformDistance(TimelineEntity firstEntity, TimelineEntity secondEntity){
		if (firstEntity.lowerStart == null)
			return 0.0;
		if (oneEntityNotAllFinite(firstEntity) != oneEntityNotAllFinite(secondEntity)){
			numUnbounded++;
			return 0.0;
		}
		if (oneEntityNotAllFinite(firstEntity))
			return 0.0;
		
		double startDistance = uniformUniform(firstEntity.lowerStart.getTimeInMillis(), firstEntity.upperStart.getTimeInMillis(), secondEntity.lowerStart.getTimeInMillis(), 
								secondEntity.upperStart.getTimeInMillis());
		double endDistance = uniformUniform(firstEntity.lowerEnd.getTimeInMillis(), firstEntity.upperEnd.getTimeInMillis(), secondEntity.lowerEnd.getTimeInMillis(), 
				secondEntity.upperEnd.getTimeInMillis());
		if (maxDistance < startDistance || maxDistance < endDistance)
			maxDistance = Math.max(startDistance, endDistance);
		return startDistance + endDistance;

		
		
	}
	
	private double sumOfHausdorfDistance(Timeline firstTimeline,
			Timeline secondTimeline) {
		double distance = 0.0;
		for (TLinkable curEntity : firstTimeline.timelineEntities.keySet()){
			TimelineEntity firstEntity = firstTimeline.timelineEntities.get(curEntity);
			TimelineEntity secondEntity = secondTimeline.timelineEntities.get(curEntity);
			distance += calculateHausdorfDistance(firstEntity, secondEntity);
		}
		//System.out.println("Distance found: " + distance);
		//System.out.println("Num unbounded: " + numUnbounded);
		// to increase the distance by the max for each time we have an unbounded interval matched with a bounded one.
		for (int i = 0; i < numUnbounded; i++){
			distance += maxDistance;
		}
		return distance;
	}

	private double calculateHausdorfDistance(TimelineEntity firstEntity, TimelineEntity secondEntity){
		if (firstEntity.lowerStart == null)
			return 0.0;
		if (oneEntityNotAllFinite(firstEntity) != oneEntityNotAllFinite(secondEntity)){
			numUnbounded++;
			return 0.0;
		}
		if (oneEntityNotAllFinite(firstEntity) && oneEntityNotAllFinite(secondEntity)){
			return 0.0;
		}
		
		double startDistance = Math.max(Math.abs(firstEntity.lowerStart.getTimeInMillis() - secondEntity.lowerStart.getTimeInMillis()), 
				Math.abs(firstEntity.upperStart.getTimeInMillis() - secondEntity.upperStart.getTimeInMillis()));
		double endDistance = Math.max(Math.abs(firstEntity.lowerEnd.getTimeInMillis() - secondEntity.lowerEnd.getTimeInMillis()), 
				Math.abs(firstEntity.upperEnd.getTimeInMillis() - secondEntity.upperEnd.getTimeInMillis()));
		if (maxDistance < startDistance || maxDistance < endDistance)
			maxDistance = Math.max(startDistance, endDistance);
		return startDistance + endDistance;
	}
		
	private boolean oneEntityNotAllFinite(TimelineEntity entity){
		return (entity.lowerStart.compareTo(this.lowerStart) == 0 || 
				entity.upperStart.compareTo(this.upperStart) == 0 ||
				entity.lowerEnd.compareTo(this.lowerEnd) == 0 ||
				entity.upperEnd.compareTo(this.upperEnd) == 0);
		}

	private void tryingEvaluations(){
		System.out.println();
		System.out.println("Testing small interval...");
		double a = 10.0;
		double b = 11.0;
		double c = 10.0;
		double d = 11.0;

		testUniform(a,b,c,d);
		
		System.out.println();
		System.out.println("Testing larger interval...");
		// to figure out the denom necessary for the size of the interval to not make a difference
		double e = 10.0;
		double f = 20.0;
		double g = 10.0;
		double h = 20.0;
		
		testUniform(e,f,g,h);
	}
	
	private void testUniform(double a, double b, double c, double d){
		uniformUniform(a, b, c, d);
		uniformUniform(a, b, c-1, d);
		uniformUniform(a, b, c, d+1);
		uniformUniform(a, b, c-1, d+1);
		uniformUniform(a, b, c-1, d+2);
		uniformUniform(a, b, c-2, d+2);
	}
	
	
	// this compares tow intervals. For example, this could be 
	// a = lower_start, b = lower_end for the first interval
	// c = lower_start, d = lower_end for the second interval
	private double uniformUniform(double a,double b,double c,double d){
		double numerator = (1.0/3)*(a*a*a - b*b*b)*(c-d)+(1.0/3)*(c*c*c-d*d*d)*(a-b)-(1.0/2)*(a*a-b*b)*(c*c-d*d);
		double denomFirst = (1.0/6)*(a-b)*(a-b);
		double denomSecond = (1.0/6)*(c-d)*(c-d);
		//System.out.println(a + ", " + b + ", " + c + ", " + d + ", " + numerator + ", " + denomFirst + ", " + denomSecond + ", " + 
			//	(numerator / (denomFirst * denomSecond)));
		
		return numerator / Math.sqrt(denomFirst * denomSecond);
		
		
	}
}
