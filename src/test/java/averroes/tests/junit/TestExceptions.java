package averroes.tests.junit;

import averroes.tests.CommonOptions;
import averroes.util.io.Paths;
import org.junit.Before;
import org.junit.Test;

public class TestExceptions {
  boolean guard = CommonOptions.guard;
  boolean whole = CommonOptions.whole;

  @Before
  public void nuke() {
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Exceptions"));
    Paths.deleteDirectory(CommonOptions.getOutputDirectory("Exceptions2"));
  }

  @Test
  public void testExceptionsRta() {
    Tests.runRta("Exceptions", guard, whole);
  }

  @Test
  public void testExceptionsXta() {
    Tests.runXta("Exceptions", guard, whole);
  }

  @Test
  public void testExceptions2Rta() {
    Tests.runRta("Exceptions2", guard, whole);
  }

  @Test
  public void testExceptions2Xta() {
    Tests.runXta("Exceptions2", guard, whole);
  }
}
