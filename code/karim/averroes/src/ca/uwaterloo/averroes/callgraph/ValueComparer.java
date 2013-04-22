package ca.uwaterloo.averroes.callgraph;

import java.util.Comparator;
import java.util.Map;

/**
 * A comparator that compares two maps (representing call back frequencies) and orders them in descending order
 * according to the value of the frequency. Otherwise, they are ordered alphabetically.
 * 
 * @author karim
 * 
 */
public class ValueComparer implements Comparator<String> {
	private Map<String, Integer> data = null;

	public ValueComparer(Map<String, Integer> data) {
		super();
		this.data = data;
	}

	public int compare(String o1, String o2) {
		int e1 = data.get(o1);
		int e2 = data.get(o2);
		return e1 > e2 ? -1 : (e1 < e2 ? 1 : o1.compareTo(o2));
	}
}