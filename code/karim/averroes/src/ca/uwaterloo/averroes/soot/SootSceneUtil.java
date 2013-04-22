package ca.uwaterloo.averroes.soot;

import soot.ClassProvider;
import soot.Scene;

/**
 * A utility class for the {@link Scene} class.
 * 
 * @author karim
 * 
 */
public class SootSceneUtil {

	/**
	 * Add a basic class to the {@link Scene}.
	 * 
	 * @param provider
	 * @param className
	 */
	private static void addCommonDynamicClass(ClassProvider provider, String className) {
		if (provider.find(className) != null) {
			Scene.v().addBasicClass(className);
		}
	}

	/**
	 * Add the common dynamic classes to the {@link Scene} as basic classes.
	 * 
	 * @param provider
	 */
	public static void addCommonDynamicClasses(ClassProvider provider) {
		/*
		 * For simulating the FileSystem class, we need the implementation of the FileSystem, but the classes are not
		 * loaded automatically due to the indirection via native code.
		 */
		addCommonDynamicClass(provider, "java.io.UnixFileSystem");
		addCommonDynamicClass(provider, "java.io.WinNTFileSystem");
		addCommonDynamicClass(provider, "java.io.Win32FileSystem");

		/* java.net.URL loads handlers dynamically */
		addCommonDynamicClass(provider, "sun.net.www.protocol.file.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.ftp.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.http.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.https.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.jar.Handler");
	}
}