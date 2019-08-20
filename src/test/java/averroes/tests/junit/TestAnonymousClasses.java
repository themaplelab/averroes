package averroes.tests.junit;

import org.junit.Test;

public class TestAnonymousClasses {
  String testCase = "AnonymousClasses";
  boolean guard = true;

  @Test
  public void testAnonymousClassesRta() {
    Tests.runRta(testCase, guard);
  }

  @Test
  public void testAnonymousClassesXta() {
    Tests.runXta(testCase, guard);
  }
}
