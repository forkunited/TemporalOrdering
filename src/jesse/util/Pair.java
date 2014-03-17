package jesse.util;

public class Pair<F, S> {
    private final F first; //first member of pair
    private final S second; //second member of pair

    private Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
    
    public static <F,S> Pair<F,S> of (F first, S second){
    	return new Pair<F,S>(first, second);
    }
}