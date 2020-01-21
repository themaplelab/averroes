package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestLinkedList {
  String testCase = "LL";
  boolean guard = CommonOptions.guard;
  boolean whole = CommonOptions.whole;

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory(testCase));
  }

  @Test
  public void testLinkedListRta() {
    Tests.runRta(testCase, guard, whole);
  }

  @Test
  public void testLinkedListXta() {
    Tests.runXta(testCase, guard, whole);
  }
}
