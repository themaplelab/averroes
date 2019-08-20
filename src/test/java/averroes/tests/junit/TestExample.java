package averroes.tests.junit;

import org.junit.Test;

public class TestExample {
  String testCase = "Example";
  boolean guard = true;

  @Test
  public void testExampleRta() {
    Tests.runRta(testCase, guard);
  }

  @Test
  public void testExampleXta() {
    Tests.runXta(testCase, guard);
  }
}
