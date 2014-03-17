package jesse.util;
import java.util.*;
 
public class CounterMap <S>{
	public Map<S,Double> map;
	private double totalCount = 0;
	
	public CounterMap() {
		map = new HashMap<>();
	}
	
	public void increment(S term, double value) { 
		ensure0(term);
		map.put(term, map.get(term) + value);
		totalCount += value;
	}
	public void increment(S term) {
		increment(term, 1.0);
	}
 
	public void addInPlace(CounterMap<S> other) {
		for (S k : other.map.keySet()) {
			ensure0(k);
			increment(k, other.value(k));
		}
	}
	public double value(S term) {
		if (!map.containsKey(term)) return 0;
		return map.get(term);
	}
	public Set<S> support() {
		// tricky. it would be safer to check for zero-ness
		return map.keySet();
	}
	
	public CounterMap<S> copy() {
		CounterMap<S> ret = new CounterMap<S>();
		ret.map = new HashMap<S,Double>(this.map);
		ret.totalCount = this.totalCount;
		return ret;
	}
	
	public double getTotal(){
		return totalCount;
	}
	
	/** helper: ensure that 'term' exists in the map */
	void ensure0(S term) {
		if (!map.containsKey(term)) {
			map.put(term, 0.0);
		}
	}
	
	public Map<S, Double> sortByValue(){
		Map<S, Double> sorted = new TreeMap<S, Double>(new ValueComparator(map));
		sorted.putAll(map);
		return sorted;
	}
	
	// a comparator for sorting by value
	class ValueComparator implements Comparator<S> {

	    Map<S, Double> base;
	    public ValueComparator(Map<S, Double> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(S a, S b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
}