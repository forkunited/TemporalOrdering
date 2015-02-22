package temp.util;

import java.util.ArrayList;

public class TemporalDecoder {
	static {
		System.loadLibrary("ad3temporal"); // Load native library at runtime
		// ad3temporal.dll (Windows) or libad3temporal.so (Unixes)
	}

	private native void decode_graph(ArrayList<Double> scores,
			ArrayList<ArrayList<Integer>> oneHotConstraints,
			ArrayList<ArrayList<Integer>> transConstraints,
			ArrayList<Double> posteriors, boolean exact);

	public static void main(String[] args) {
		int num_arcs = 18;

		  double[] scores_arr = new double[]{1.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

		  ArrayList<ArrayList<Integer>> one_hot_constraints = new ArrayList<ArrayList<Integer>>();
		  one_hot_constraints.add(new ArrayList<Integer>());
		  one_hot_constraints.add(new ArrayList<Integer>());
		  one_hot_constraints.add(new ArrayList<Integer>());

		  one_hot_constraints.get(0).add(1);
		  one_hot_constraints.get(0).add(0);
		  one_hot_constraints.get(0).add(17);
		  one_hot_constraints.get(0).add(14);
		  one_hot_constraints.get(0).add(3);
		  one_hot_constraints.get(0).add(5);

		  one_hot_constraints.get(1).add(10);
		  one_hot_constraints.get(1).add(9);
		  one_hot_constraints.get(1).add(16);
		  one_hot_constraints.get(1).add(11);
		  one_hot_constraints.get(1).add(15);
		  one_hot_constraints.get(1).add(13);

		  one_hot_constraints.get(2).add(12);
		  one_hot_constraints.get(2).add(8);
		  one_hot_constraints.get(2).add(6);
		  one_hot_constraints.get(2).add(2);
		  one_hot_constraints.get(2).add(7);
		  one_hot_constraints.get(2).add(4);

		  ArrayList<Double> scores = new ArrayList<Double>();
		  for (double d : scores_arr){
			  scores.add(d);
		  }
		  ArrayList<ArrayList<Integer>> transitivity_constraints = new ArrayList<ArrayList<Integer>>(); 
		  for (int i = 0; i < 17; i++){
			  ArrayList<Integer> g = new ArrayList<Integer>();
			  g.add(-1);
			  g.add(-1);
			  g.add(-1);
			  transitivity_constraints.add(g);
		  }
		  
		  transitivity_constraints.get(0).set(0, 15);
		  transitivity_constraints.get(0).set(1, 5);
		  transitivity_constraints.get(0).set(2, 4);

		  transitivity_constraints.get(1).set(0, 15);
		  transitivity_constraints.get(1).set(1, 3);
		  transitivity_constraints.get(1).set(2, 7);

		  transitivity_constraints.get(2).set(0, 16);
		  transitivity_constraints.get(2).set(1, 0);
		  transitivity_constraints.get(2).set(2, 8);

		  transitivity_constraints.get(3).set(0, 13);
		  transitivity_constraints.get(3).set(1, 5);
		  transitivity_constraints.get(3).set(2, 4);

		  transitivity_constraints.get(4).set(0, 9);
		  transitivity_constraints.get(4).set(1, 3);
		  transitivity_constraints.get(4).set(2, 8);

		  transitivity_constraints.get(5).set(0, 15);
		  transitivity_constraints.get(5).set(1, 1);
		  transitivity_constraints.get(5).set(2, 12);

		  transitivity_constraints.get(6).set(0, 16);
		  transitivity_constraints.get(6).set(1, 17);
		  transitivity_constraints.get(6).set(2, 6);

		  transitivity_constraints.get(7).set(0, 9);
		  transitivity_constraints.get(7).set(1, 0);
		  transitivity_constraints.get(7).set(2, 8);

		  transitivity_constraints.get(8).set(0, 10);
		  transitivity_constraints.get(8).set(1, 3);
		  transitivity_constraints.get(8).set(2, 12);

		  transitivity_constraints.get(9).set(0, 10);
		  transitivity_constraints.get(9).set(1, 1);
		  transitivity_constraints.get(9).set(2, 12);

		  transitivity_constraints.get(10).set(0, 15);
		  transitivity_constraints.get(10).set(1, 17);
		  transitivity_constraints.get(10).set(2, 6);

		  transitivity_constraints.get(11).set(0, 13);
		  transitivity_constraints.get(11).set(1, 1);
		  transitivity_constraints.get(11).set(2, 4);

		  transitivity_constraints.get(12).set(0, 9);
		  transitivity_constraints.get(12).set(1, 1);
		  transitivity_constraints.get(12).set(2, 8);

		  transitivity_constraints.get(13).set(0, 13);
		  transitivity_constraints.get(13).set(1, 3);
		  transitivity_constraints.get(13).set(2, 4);

		  transitivity_constraints.get(14).set(0, 16);
		  transitivity_constraints.get(14).set(1, 3);
		  transitivity_constraints.get(14).set(2, 6);

		  transitivity_constraints.get(15).set(0, 16);
		  transitivity_constraints.get(15).set(1, 5);
		  transitivity_constraints.get(15).set(2, 4);

		  transitivity_constraints.get(16).set(0, 15);
		  transitivity_constraints.get(16).set(1, 0);
		  transitivity_constraints.get(16).set(2, 8);
		  
//		  for(int i = 0; i < transitivity_constraints.size(); i++){
//			  for(int j = 0; j < transitivity_constraints.get(i).size(); j++){
//				  System.out.println(transitivity_constraints.get(i).get(j));
//			  }
//		  }
		  ArrayList<Double> posteriors = new ArrayList<Double>();
		  for(int i = 0; i < scores.size(); i++){
			  posteriors.add(0.0);
		  }
		  
		  new TemporalDecoder().decode_graph(scores, one_hot_constraints,transitivity_constraints,posteriors, false);
		  
		  for(int i = 0; i < posteriors.size(); i++){
			  System.out.println(posteriors.get(i));
		  }
	}
}