package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestInheritance {
  String testCase = "Inheritance";
  boolean guard = CommonOptions.guard;
  boolean whole = CommonOptions.whole;

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory(testCase));
  }

  @Test
  public void testInheritanceRta() {
    Tests.runRta(testCase, guard, whole);
  }

  @Test
  public void testInheritanceXta() {
    Tests.runXta(testCase, guard, whole);
  }
}
