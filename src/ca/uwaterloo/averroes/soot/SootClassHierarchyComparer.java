package ca.uwaterloo.averroes.soot;

import java.util.Comparator;

import soot.SootClass;

/**
 * A comparator that compares two Soot classes based on their position in the class hierarchy (i.e., the superclass
 * relation).
 * 
 * @author karim
 * 
 */
public class SootClassHierarchyComparer implements Comparator<SootClass> {
	private Hierarchy hierarchy;

	/**
	 * Construct a new class hierarchy comparer.
	 * 
	 * @param hierarchy
	 */
	public SootClassHierarchyComparer(Hierarchy hierarchy) {
		super();
		this.hierarchy = hierarchy;
	}

	/**
	 * Compare two Soot classes based on the class hierarchy.
	 * 
	 * @param classA
	 * @param classB
	 */
	public int compare(SootClass classA, SootClass classB) {
		if (hierarchy.isSuperclassOf(classA, classB) || hierarchy.isSuperinterfaceOf(classA, classB)) {
			return -1;
		} else if (hierarchy.isSuperclassOf(classB, classA) || hierarchy.isSuperinterfaceOf(classB, classA)) {
			return 1;
		} else {
			return classA.getName().compareTo(classB.getName());
		}
	}
}