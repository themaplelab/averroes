package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestLinkedList {
  String testCase = "LL";

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory(testCase));
  }

  // RTA Tests
  @Test
  public void testRtaPlain() {
    Tests.runRta(testCase, false, false);
  }

  @Test
  public void testRtaGuard() {
    Tests.runRta(testCase, true, false);
  }

  @Test
  public void testRtaWhole() {
    Tests.runRta(testCase, false, true);
  }

  @Test
  public void testRtaBoth() {
    Tests.runRta(testCase, true, true);
  }


  // XTA Tests
  @Test
  public void testXtaPlain() {
    Tests.runXta(testCase, false, false);
  }

  @Test
  public void testXtaGuard() {
    Tests.runXta(testCase, true, false);
  }

  @Test
  public void testXtaWhole() {
    Tests.runXta(testCase, false, true);
  }

  @Test
  public void testXtaBoth() {
    Tests.runXta(testCase, true, true);
  }
}
