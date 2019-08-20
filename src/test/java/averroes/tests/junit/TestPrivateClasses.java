package averroes.tests.junit;

import org.junit.Test;

public class TestPrivateClasses {
  String testCase = "PrivateClasses";
  boolean guard = true;

  @Test
  public void testPrivateClassesRta() {
    Tests.runRta(testCase, guard);
  }

  @Test
  public void testPrivateClassesXta() {
    Tests.runXta(testCase, guard);
  }
}
