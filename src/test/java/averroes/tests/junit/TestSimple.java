package averroes.tests.junit;

import org.junit.Test;

public class TestSimple {
  String testCase = "Simple";
  boolean guard = true;
  boolean whole = true;

  @Test
  public void testSimpleRta() {
    Tests.runRta(testCase, guard, whole);
  }

  @Test
  public void testSimpleXta() {
    Tests.runXta(testCase, guard, whole);
  }
}
