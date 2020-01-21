package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestSimple {
  String testCase = "Simple";
  boolean guard = CommonOptions.guard;
  boolean whole = CommonOptions.whole;

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory(testCase));
  }

  @Test
  public void testSimpleRta() {
    Tests.runRta(testCase, guard, whole);
  }

  @Test
  public void testSimpleXta() {
    Tests.runXta(testCase, guard, whole);
  }
}
