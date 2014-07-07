package temp.data.annotation.cost;



/**
 * TimeMLRelTypeSplit enumerates TLink relationship
 * types for the TLink split-relation-type classification
 * task.  This task is the same as the TLink relation-
 * type task except that every relation-type R 
 * is split into relation-types R_0 and R_1, 
 * and R_0 and R_1 are randomly assigned to relations that 
 * actually have type R.  The 
 * purpose of this is to test whether the cost learning 
 * models can recover from random label splitting.  
 * You may need to import
 * cost.jar (compiled from the repository at 
 * https://github.com/forkunited/CostFunctionLearning) to get 
 * the cost learning 
 * models to work, since these were recently removed from 
 * ARKWater and placed in their own library.
 * 
 * @author Bill McDowell
 * 
 */
public enum TimeMLRelTypeSplit {
	BEFORE_0,
	BEFORE_1,
	AFTER_0,
	AFTER_1,
	INCLUDES_0,
	INCLUDES_1,
	IS_INCLUDED_0,
	IS_INCLUDED_1,
	SIMULTANEOUS_0,
	SIMULTANEOUS_1,
	VAGUE_0,
	VAGUE_1
}
