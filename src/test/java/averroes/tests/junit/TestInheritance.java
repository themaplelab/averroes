package averroes.tests.junit;

import org.junit.Test;

public class TestInheritance {
	String testCase = "Inheritance";
	boolean guard = true;

	@Test
	public void testInheritanceRta() {
		Tests.runRta(testCase, guard);
	}

	@Test
	public void testInheritanceXta() {
		Tests.runXta(testCase, guard);
	}
}
