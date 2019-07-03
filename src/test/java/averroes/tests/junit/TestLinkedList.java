package averroes.tests.junit;

import org.junit.Test;

public class TestLinkedList {
	String testCase = "LL";
	boolean guard = true;

	@Test
	public void testLinkedListRta() {
		Tests.runRta(testCase, guard);
	}

	@Test
	public void testLinkedListXta() {
		Tests.runXta(testCase, guard);
	}
}
