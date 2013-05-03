package ca.uwaterloo.averroes.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtils {
	
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	public static <T> List<T> concat(List<T> first, List<T> second) {
		List<T> result = new ArrayList<T>(first);
		result.addAll(second);
		return result;
	}

}
