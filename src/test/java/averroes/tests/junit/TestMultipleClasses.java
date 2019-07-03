package averroes.tests.junit;

import org.junit.Test;

public class TestMultipleClasses {
	String testCase = "MultipleClasses";
	boolean guard = true;

	@Test
	public void testMultipleClassesRta() {		
		Tests.runRta(testCase, guard);
	}

	@Test
	public void testMultipleClassesXta() {
		Tests.runXta(testCase, guard);
	}
}
