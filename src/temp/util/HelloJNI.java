import java.util.*;

public class HelloJNI {
	static {
		System.loadLibrary("hello"); // Load native library at runtime
										// hello.dll (Windows) or libhello.so
										// (Unixes)
	}

	// Declare a native method sayHello() that receives nothing and returns void
	private native void sayHello(int[] nums);

	// Test Driver
	public static void main(String[] args) {
		int[] n = {3,4};
		new HelloJNI().sayHello(n); // invoke the native method
	}
}
