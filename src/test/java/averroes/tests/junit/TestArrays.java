package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestArrays {

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays2"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays3"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays4"));
  }

  // RTA Tests
  @Test
  public void testRtaPlain() {
    Tests.runRta("Arrays", false, false);
  }

  @Test
  public void testRtaGuard() {
    Tests.runRta("Arrays", true, false);
  }

  @Test
  public void testRtaWhole() {
    Tests.runRta("Arrays", false, true);
  }

  @Test
  public void testRtaBoth() {
    Tests.runRta("Arrays", true, true);
  }


  // XTA Tests
  @Test
  public void testXtaPlain() {
    Tests.runXta("Arrays", false, false);
  }

  @Test
  public void testXtaGuard() {
    Tests.runXta("Arrays", true, false);
  }

  @Test
  public void testXtaWhole() {
    Tests.runXta("Arrays", false, true);
  }

  @Test
  public void testXtaBoth() {
    Tests.runXta("Arrays", true, true);
  }

  // RTA Tests
  @Test
  public void testRta2Plain() {
    Tests.runRta("Arrays2", false, false);
  }

  @Test
  public void testRta2Guard() {
    Tests.runRta("Arrays2", true, false);
  }

  @Test
  public void testRta2Whole() {
    Tests.runRta("Arrays2", false, true);
  }

  @Test
  public void testRta2Both() {
    Tests.runRta("Arrays2", true, true);
  }


  // XTA Tests
  @Test
  public void testXta2Plain() {
    Tests.runXta("Arrays2", false, false);
  }

  @Test
  public void testXta2Guard() {
    Tests.runXta("Arrays2", true, false);
  }

  @Test
  public void testXta2Whole() {
    Tests.runXta("Arrays2", false, true);
  }

  @Test
  public void testXta2Both() {
    Tests.runXta("Arrays2", true, true);
  }

  // RTA Tests
  @Test
  public void testRta3Plain() {
    Tests.runRta("Arrays3", false, false);
  }

  @Test
  public void testRta3Guard() {
    Tests.runRta("Arrays3", true, false);
  }

  @Test
  public void testRta3Whole() {
    Tests.runRta("Arrays3", false, true);
  }

  @Test
  public void testRta3Both() {
    Tests.runRta("Arrays3", true, true);
  }


  // XTA Tests
  @Test
  public void testXta3Plain() {
    Tests.runXta("Arrays3", false, false);
  }

  @Test
  public void testXta3Guard() {
    Tests.runXta("Arrays3", true, false);
  }

  @Test
  public void testXta3Whole() {
    Tests.runXta("Arrays3", false, true);
  }

  @Test
  public void testXta3Both() {
    Tests.runXta("Arrays3", true, true);
  }

  // RTA Tests
  @Test
  public void testRta4Plain() {
    Tests.runRta("Arrays4", false, false);
  }

  @Test
  public void testRta4Guard() {
    Tests.runRta("Arrays4", true, false);
  }

  @Test
  public void testRta4Whole() {
    Tests.runRta("Arrays4", false, true);
  }

  @Test
  public void testRta4Both() {
    Tests.runRta("Arrays4", true, true);
  }


  // XTA Tests
  @Test
  public void testXta4Plain() {
    Tests.runXta("Arrays4", false, false);
  }

  @Test
  public void testXta4Guard() {
    Tests.runXta("Arrays4", true, false);
  }

  @Test
  public void testXta4Whole() {
    Tests.runXta("Arrays4", false, true);
  }

  @Test
  public void testXta4Both() {
    Tests.runXta("Arrays4", true, true);
  }
}
