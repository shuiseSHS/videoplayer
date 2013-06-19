package jni;

public class kernel_loader {

	public native void start(String dir, int timeOut, String AppName);
	public native void stop();
	public native void stop_play();
	
	
	static
	{
		System.loadLibrary("kernel");
		System.loadLibrary("jnidll");
	}
}
