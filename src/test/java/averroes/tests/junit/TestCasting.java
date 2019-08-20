package averroes.tests.junit;

import org.junit.Test;

public class TestCasting {
  String testCase = "Casting";
  boolean guard = true;

  @Test
  public void testCastingRta() {
    Tests.runRta(testCase, guard);
  }

  @Test
  public void testCastingXta() {
    Tests.runXta(testCase, guard);
  }
}
