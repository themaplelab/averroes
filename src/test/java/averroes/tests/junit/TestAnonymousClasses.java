package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.tests.Main;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestAnonymousClasses {
  String testCase = "AnonymousClasses";
  boolean guard = CommonOptions.guard;
  boolean whole = CommonOptions.whole;

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory(testCase));
  }

  @Test
  public void testAnonymousClassesRta() {
    Tests.runRta(testCase, guard, whole);
  }

  @Test
  public void testAnonymousClassesXta() {
    Tests.runXta(testCase, guard, whole);
  }
}
