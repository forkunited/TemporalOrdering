package temp.data.annotation.optimization;
import java.util.*;

public class JNIInterfaceAD3 {
	
	static {
		System.loadLibrary("AD3"); // Load native library at runtime
										// hello.dll (Windows) or libhello.so
										// (Unixes)
	}
	
	public native void getPosteriors(List<Double> scores, List<List<Integer>> oneHotConstraints, List<List<Integer>> transConstraints, List<Double> posteriors);


}
