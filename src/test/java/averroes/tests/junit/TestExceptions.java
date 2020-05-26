package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestExceptions {

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Exceptions"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Exceptions2"));
  }

  // RTA Tests
  @Test
  public void testRtaPlain() {
    Tests.runRta("Exceptions", false, false);
  }

  @Test
  public void testRtaGuard() {
    Tests.runRta("Exceptions", true, false);
  }

  @Test
  public void testRtaWhole() {
    Tests.runRta("Exceptions", false, true);
  }

  @Test
  public void testRtaBoth() {
    Tests.runRta("Exceptions", true, true);
  }


  // XTA Tests
  @Test
  public void testXtaPlain() {
    Tests.runXta("Exceptions", false, false);
  }

  @Test
  public void testXtaGuard() {
    Tests.runXta("Exceptions", true, false);
  }

  @Test
  public void testXtaWhole() {
    Tests.runXta("Exceptions", false, true);
  }

  @Test
  public void testXtaBoth() {
    Tests.runXta("Exceptions", true, true);
  }

  // RTA Tests
  @Test
  public void testRta2Plain() {
    Tests.runRta("Exceptions2", false, false);
  }

  @Test
  public void testRta2Guard() {
    Tests.runRta("Exceptions2", true, false);
  }

  @Test
  public void testRta2Whole() {
    Tests.runRta("Exceptions2", false, true);
  }

  @Test
  public void testRta2Both() {
    Tests.runRta("Exceptions2", true, true);
  }


  // XTA Tests
  @Test
  public void testXta2Plain() {
    Tests.runXta("Exceptions2", false, false);
  }

  @Test
  public void testXta2Guard() {
    Tests.runXta("Exceptions2", true, false);
  }

  @Test
  public void testXta2Whole() {
    Tests.runXta("Exceptions2", false, true);
  }

  @Test
  public void testXta2Both() {
    Tests.runXta("Exceptions2", true, true);
  }
}
