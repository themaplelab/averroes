package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestArrays {
  boolean guard = CommonOptions.guard;
  boolean whole = CommonOptions.whole;

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays2"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays3"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Arrays4"));
  }

  @Test
  public void testArraysRta() {
    Tests.runRta("Arrays", guard, whole);
  }

  @Test
  public void testArraysXta() {
    Tests.runXta("Arrays", guard, whole);
  }

  @Test
  public void testArrays2Rta() {
    Tests.runRta("Arrays2", guard, whole);
  }

  @Test
  public void testArrays2Xta() {
    Tests.runXta("Arrays2", guard, whole);
  }

  @Test
  public void testArrays3Rta() {
    Tests.runRta("Arrays3", guard, whole);
  }

  @Test
  public void testArrays3Xta() {
    Tests.runXta("Arrays3", guard, whole);
  }

  @Test
  public void testArrays4Rta() {
    Tests.runRta("Arrays4", guard, whole);
  }

  @Test
  public void testArrays4Xta() {
    Tests.runXta("Arrays4", guard, whole);
  }
}
