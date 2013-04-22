package ca.uwaterloo.averroes.soot;

import soot.SootClass;

/**
 * An enumeration for all possible values of {@value SootClass#resolvingLevel()}.
 * 
 * @author karim
 * 
 */
public enum ResolvingLevel {

	DANGLING(0), HIERARCHY(1), SIGNATURES(2), BODIES(3);

	private final int value;

	private ResolvingLevel(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
