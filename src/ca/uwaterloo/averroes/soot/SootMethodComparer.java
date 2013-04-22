package ca.uwaterloo.averroes.soot;

import java.util.Comparator;

import soot.SootMethod;

/**
 * A comparator that compares two Soot methods based on the name of their declaring class.
 * 
 * @author karim
 * 
 */
public class SootMethodComparer implements Comparator<SootMethod> {

	/**
	 * Construct a new Soot method comparator.
	 */
	public SootMethodComparer() {
		super();
	}

	/**
	 * Compare two Soot methods based on the name of their declaring class.
	 * 
	 * @param methodA
	 * @param methodB
	 * @return
	 */
	public int compare(SootMethod methodA, SootMethod methodB) {
		return methodA.getDeclaringClass().getName().compareTo(methodB.getDeclaringClass().getName());
	}
}