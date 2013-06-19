package ca.uwaterloo.averroes.util;

/**
 * A utility class to time operations.
 * 
 * @author karim
 * 
 */
public class TimeUtils {

	private static long start = System.currentTimeMillis();
	private static long splitStart = System.currentTimeMillis();

	/**
	 * Calculate the elapsed time in seconds.
	 * 
	 * @param start
	 * @return
	 */
	public static double elapsedTime(long start) {
		return MathUtils.round((System.currentTimeMillis() - start) / 1000.0);
	}

	/**
	 * Calculate the elapsed time in seconds.
	 * 
	 * @return
	 */
	public static double elapsedTime() {
		return MathUtils.round((System.currentTimeMillis() - start) / 1000.0);
	}

	/**
	 * Calculate the elapsed time in seconds starting at the split start.
	 * 
	 * @return
	 */
	public static double elapsedSplitTime() {
		return MathUtils.round((System.currentTimeMillis() - splitStart) / 1000.0);
	}

	/**
	 * Split the timer.
	 */
	public static void splitStart() {
		splitStart = System.currentTimeMillis();
	}

	/**
	 * Reset the start time used to calculate the elapsed time.
	 */
	public static void reset() {
		start = System.currentTimeMillis();
	}
}
