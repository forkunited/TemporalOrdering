package temp.data.annotation.structure.timeline;

import java.util.*;

import ark.util.OutputWriter;

import temp.data.annotation.timeml.TLinkable;
import temp.data.annotation.timeml.Time.TimeMLType;

public class TimelineComparitor {
	// creating max and min values to which we can compare
	Calendar minDate;
	Calendar maxDate;
	double maxDistance;
	int numUnbounded;

	public TimelineComparitor(){
		minDate = Calendar.getInstance();
		minDate.setTime(new Date(Long.MIN_VALUE));
		maxDate = Calendar.getInstance();
		maxDate.setTime(new Date(Long.MAX_VALUE));
		maxDistance = 0;
	}
	
	// first is predicted
	// second is gold
	public double compare(Timeline firstTimeline,
			Timeline secondTimeline, OutputWriter output) {
		//tryingEvaluations();
		//double hausdorfDistance = sumOfHausdorfDistance(firstTimeline, secondTimeline);
		numUnbounded = 0;
		maxDistance = 0;
		double expectedValueDistance = sumOfExpectedValues(firstTimeline, secondTimeline);
		//sumOfUniformDistance(firstTimeline, secondTimeline);
		
		writeOut(output, hausdorfDistance, expectedValueDistance, firstTimeline, secondTimeline);
		
		return hausdorfDistance;
	}
	
	private void writeOut(OutputWriter output, double hausdorfDistance, double expectedValueDistance, Timeline firstTimeline, Timeline secondTimeline){
		output.resultsWriteln("Predicted timeline:" + TimelineBuilder.testTimeline(firstTimeline));
		output.resultsWriteln("Gold timeline: " + TimelineBuilder.testTimeline(secondTimeline));
		output.resultsWriteln("Hausdorf distance: " + hausdorfDistance);
		output.resultsWriteln("Expected Value Distance: " + expectedValueDistance);
	}
	
	private double sumOfExpectedValues(Timeline firstTimeline, Timeline secondTimeline){
		double distance = 0.0;
		for (TLinkable curEntity : firstTimeline.timelineEntities.keySet()){
			TimelineEntity firstEntity = firstTimeline.timelineEntities.get(curEntity);
			TimelineEntity secondEntity = secondTimeline.timelineEntities.get(curEntity);
			distance += calculateDistance(firstEntity, secondEntity);
		}
		//System.out.println("Distance found: " + distance);
		//System.out.println("Num unbounded: " + numUnbounded);
		// to increase the distance by the max for each time we have an unbounded interval matched with a bounded one.
		for (int i = 0; i < numUnbounded; i++){
			distance += maxDistance;
		}
		return distance;
	}
	
	enum LengthOfInterval {UNBOUNDED, LEFT_BOUNDED, RIGHT_BOUNDED, BOTH_SIDES_BOUNDED};
	
	private double calculateDistance(TimelineEntity firstEntity, TimelineEntity secondEntity){
		if (noTimelineRepresentation(firstEntity) || noTimelineRepresentation(secondEntity))
			return 0;
		
		double startDistance = computeOneIntervalDistance(firstEntity.lowerStart, firstEntity.upperStart, secondEntity.lowerStart, secondEntity.upperStart);
		double endDistance = computeOneIntervalDistance(firstEntity.lowerEnd, firstEntity.upperEnd, secondEntity.lowerEnd, secondEntity.upperEnd);
		
		/*
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
		*/
	}
	
	// strategy:
	// UNBOUNDED, LEFT_BOUNDED should call the same method as LEFT_BOUNDED, UNBOUNDED, just passing params in a diff order
	private double computeOneIntervalDistance(Calendar firstLower, Calendar firstUpper, Calendar secondLower, Calendar secondUpper){
		LengthOfInterval first = getIntervalLength(firstLower, firstUpper);
		LengthOfInterval second = getIntervalLength(secondLower, secondUpper);
		
		if (first == LengthOfInterval.UNBOUNDED && second == LengthOfInterval.UNBOUNDED)
			return 0;
		else if (first == LengthOfInterval.UNBOUNDED && second == LengthOfInterval.LEFT_BOUNDED)
			return exponentialGaussian(secondLower, secondUpper, second, firstLower, firstUpper);
		else if (first == LengthOfInterval.UNBOUNDED && second == LengthOfInterval.RIGHT_BOUNDED)
			return exponentialGaussian(secondLower, secondUpper, second, firstLower, firstUpper);
		else if (first == LengthOfInterval.UNBOUNDED && second == LengthOfInterval.BOTH_SIDES_BOUNDED)
			return uniformGaussian(secondLower, secondUpper, firstLower, firstUpper);

		else if (first == LengthOfInterval.LEFT_BOUNDED && second == LengthOfInterval.UNBOUNDED)
			return exponentialGaussian(firstLower, firstUpper, first, secondLower, secondUpper);
		else if (first == LengthOfInterval.LEFT_BOUNDED && second == LengthOfInterval.LEFT_BOUNDED)
			return exponentialExponential(firstLower, firstUpper, first, secondLower, secondUpper, second);
		else if (first == LengthOfInterval.LEFT_BOUNDED && second == LengthOfInterval.RIGHT_BOUNDED)
			return exponentialExponential(firstLower, firstUpper, first, secondLower, secondUpper, second);
		else if (first == LengthOfInterval.LEFT_BOUNDED && second == LengthOfInterval.BOTH_SIDES_BOUNDED)
			return uniformExponential(secondLower, secondUpper, firstLower, firstUpper, first);
		
		else if (first == LengthOfInterval.RIGHT_BOUNDED && second == LengthOfInterval.UNBOUNDED)
			return exponentialGaussian(firstLower, firstUpper, first, secondLower, secondUpper);
		else if (first == LengthOfInterval.RIGHT_BOUNDED && second == LengthOfInterval.LEFT_BOUNDED)
			return exponentialExponential(firstLower, firstUpper, first, secondLower, secondUpper, second);
		else if (first == LengthOfInterval.RIGHT_BOUNDED && second == LengthOfInterval.RIGHT_BOUNDED)
			return exponentialExponential(firstLower, firstUpper, first, secondLower, secondUpper, second);
		else if (first == LengthOfInterval.RIGHT_BOUNDED && second == LengthOfInterval.BOTH_SIDES_BOUNDED)
			return uniformExponential(secondLower, secondUpper, firstLower, firstUpper, first);
		
		else if (first == LengthOfInterval.BOTH_SIDES_BOUNDED && second == LengthOfInterval.UNBOUNDED)
			return uniformGaussian(firstLower, firstUpper,secondLower, secondUpper);
		else if (first == LengthOfInterval.BOTH_SIDES_BOUNDED && second == LengthOfInterval.LEFT_BOUNDED)
			return uniformExponential(firstLower, firstUpper, secondLower, secondUpper, second);
		else if (first == LengthOfInterval.BOTH_SIDES_BOUNDED && second == LengthOfInterval.RIGHT_BOUNDED)
			return uniformExponential(firstLower, firstUpper, secondLower, secondUpper, second);
		else if (first == LengthOfInterval.BOTH_SIDES_BOUNDED && second == LengthOfInterval.BOTH_SIDES_BOUNDED)
			return uniformUniform(firstLower, firstUpper, secondLower, secondUpper);
		else 
			throw new IllegalArgumentException("Problem when comparing two intervals!");
	}
	
	private Map<String, Double> getIntervalNums(Calendar firstLower, Calendar firstUpper, Calendar secondLower, Calendar secondUpper){
		Map<String, Double> nameToNum = new HashMap<String, Double>();
		nameToNum.put("firstLower", (double)firstLower.getTimeInMillis());
		nameToNum.put("firstUpper", (double)firstUpper.getTimeInMillis());
		nameToNum.put("secondLower", (double)secondLower.getTimeInMillis());
		nameToNum.put("secondUpper", (double)secondUpper.getTimeInMillis());
		return nameToNum;
	}
	
	private double uniformUniform(Calendar firstLower, Calendar firstUpper, Calendar secondLower, Calendar secondUpper){
		Map<String, Double> intervalNums = getIntervalNums(firstLower, firstUpper, secondLower, secondUpper);
		double a = intervalNums.get("firstLower");
		double b = intervalNums.get("firstUpper");
		double c = intervalNums.get("secondLower");
		double d = intervalNums.get("secondUpper");
		double numerator = (1.0/3)*(a*a*a - b*b*b)*(c-d)+(1.0/3)*(c*c*c-d*d*d)*(a-b)-(1.0/2)*(a*a-b*b)*(c*c-d*d);
		
		// for whe trying to divide by the length of each interval.
		//double denomFirst = (1.0/6)*(a-b)*(a-b);
		//double denomSecond = (1.0/6)*(c-d)*(c-d);
		
		//System.out.println(a + ", " + b + ", " + c + ", " + d + ", " + numerator + ", " + denomFirst + ", " + denomSecond + ", " + 
			//	(numerator / (denomFirst * denomSecond)));
		
		//return numerator / Math.sqrt(denomFirst * denomSecond);
		return numerator;
		
	}
	
	private double uniformExponential(Calendar uniformLower, Calendar uniformUpper, Calendar expLower, Calendar expUpper, LengthOfInterval leftOrRightBounded){
		Map<String, Double> intervalNums = getIntervalNums(uniformLower, uniformUpper, expLower, expUpper);
		double a = intervalNums.get("firstLower");
		double b = intervalNums.get("firstUpper");
		if (leftOrRightBounded == LengthOfInterval.LEFT_BOUNDED){
			double delta = intervalNums.get("secondLower");
			return (1.0/3)*(a*a+a*b+b*b)-delta*(a+b)-(a+b)+delta*delta+2*delta+2;
		} else { 
			double delta = intervalNums.get("secondUpper");
			return (1.0/3)*(a*a+a*b+b*b)-delta*(a+b)-(a+b)+delta*delta-2*delta+2;
		}
	}
	
	private double uniformGaussian(Calendar unifLower, Calendar unifUpper, Calendar gausLower, Calendar gausUpper){
		Map<String, Double> intervalNums = getIntervalNums(unifLower, unifUpper, gausLower, gausUpper);
		double a = intervalNums.get("firstLower");
		double b = intervalNums.get("secondLower");
		
		return (1.0/12)*(a-b)*(a-b);
	}
	
	private double exponentialExponential(Calendar firstLower, Calendar firstUpper, LengthOfInterval firstLeftOrRightBounded, 
											Calendar secondLower, Calendar secondUpper, LengthOfInterval secondLeftOrRightBounded){
		Map<String, Double> intervalNums = getIntervalNums(firstLower, firstUpper, secondLower, secondUpper);
		if (firstLeftOrRightBounded == LengthOfInterval.LEFT_BOUNDED && secondLeftOrRightBounded == LengthOfInterval.LEFT_BOUNDED){
			double delta = intervalNums.get("firstLower");
			double eta = intervalNums.get("secondLower");
			return delta*delta+eta*eta-2*delta*eta+2;
		}else if (firstLeftOrRightBounded == LengthOfInterval.LEFT_BOUNDED && secondLeftOrRightBounded == LengthOfInterval.RIGHT_BOUNDED){
			double eta = intervalNums.get("firstLower");
			double delta = intervalNums.get("secondUpper");
			return delta*delta+eta*eta-2*delta*eta+6-4*delta+4*eta;
		}else if (firstLeftOrRightBounded == LengthOfInterval.RIGHT_BOUNDED && secondLeftOrRightBounded == LengthOfInterval.LEFT_BOUNDED){
			double delta = intervalNums.get("firstUpper");
			double eta = intervalNums.get("secondLower");
			return delta*delta+eta*eta-2*delta*eta+6-4*delta+4*eta;
		}else if (firstLeftOrRightBounded == LengthOfInterval.RIGHT_BOUNDED && secondLeftOrRightBounded == LengthOfInterval.RIGHT_BOUNDED){
			double delta = intervalNums.get("firstUpper");
			double eta = intervalNums.get("secondUpper");
			return delta*delta+eta*eta-2*delta*eta+2;
		}else
			throw new IllegalArgumentException("Problem in exponentialExponential!");	
		
	}
	
	private double exponentialGaussian(Calendar firstLower, Calendar firstUpper, LengthOfInterval leftOrRightBounded, Calendar secondLower, Calendar secondUpper){
		return 3;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	// returns true for those entities for which there isn't a timeline representation, like time expressions of type DURATION.
	private boolean noTimelineRepresentation(TimelineEntity entity){
		return (entity.lowerStart == null || entity.upperStart == null || entity.lowerEnd == null || entity.upperEnd == null);
	}
	
	private LengthOfInterval getIntervalLength(Calendar lower, Calendar upper){
		boolean leftBounded = lower.compareTo(this.minDate) != 0;
		boolean rightBounded = upper.compareTo(this.maxDate) != 0;
		if (leftBounded && rightBounded)
			return LengthOfInterval.BOTH_SIDES_BOUNDED;
		else if (leftBounded && !rightBounded)
			return LengthOfInterval.LEFT_BOUNDED;
		else if (!leftBounded && rightBounded)
			return LengthOfInterval.RIGHT_BOUNDED;
		else
			return LengthOfInterval.UNBOUNDED;
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
	

	// testing code
	// to evaluate how the distance metrics do
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
	
	
	/*
	 * Hausdorf distance
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
*/
}
