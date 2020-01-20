package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestPrivateClasses {
  String testCase = "PrivateClasses";
  boolean guard = CommonOptions.guard;
  boolean whole = CommonOptions.whole;

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory(testCase));
  }

  @Test
  public void testPrivateClassesRta() {
    Tests.runRta(testCase, guard, whole);
  }

  @Test
  public void testPrivateClassesXta() {
    Tests.runXta(testCase, guard, whole);
  }
}
