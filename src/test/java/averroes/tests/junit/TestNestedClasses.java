package averroes.tests.junit;

import org.junit.Test;

public class TestNestedClasses {
  String testCase = "NestedClasses";
  boolean guard = true;

  @Test
  public void testNestedClassesRta() {
    Tests.runRta(testCase, guard);
  }

  @Test
  public void testNestedClassesXta() {
    Tests.runXta(testCase, guard);
  }
}
