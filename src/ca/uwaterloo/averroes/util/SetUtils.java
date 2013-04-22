package ca.uwaterloo.averroes.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A utility class for set-related operations.
 * 
 * @author karim
 * 
 */
public class SetUtils {

	/**
	 * Return the last element of a linked hashset. The methods returns null if the set is empty.
	 * 
	 * @param set
	 * @return
	 */
	public static <T> T getLastElement(LinkedHashSet<T> set) {
		T element = null;

		if (!set.isEmpty()) {
			Iterator<T> iterator = set.iterator();
			while (iterator.hasNext()) {
				element = iterator.next();
			}
		}

		return element;
	}

	/**
	 * Calculate the value of A - B.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> Set<T> minus(Set<T> a, Set<T> b) {
		Set<T> result = new HashSet<T>(a);
		result.addAll(a);
		result.removeAll(b);
		return result;
	}

	/**
	 * Calculate the value of A intersect B.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> Set<T> intersect(Set<T> a, Set<T> b) {
		Set<T> result = new HashSet<T>(a);
		result.retainAll(b);
		return result;
	}

	/**
	 * Calculate the value of A union B.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> Set<T> union(Set<T> a, Set<T> b) {
		Set<T> result = new HashSet<T>(a);
		result.addAll(b);
		return result;
	}

	/**
	 * Calculate the value of (A union B) - (A intersect B).
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> Set<T> diff(Set<T> a, Set<T> b) {
		Set<T> intersection = intersect(a, b);
		Set<T> union = union(a, b);
		Set<T> result = new HashSet<T>(union);
		result.removeAll(intersection);
		return result;
	}

}
