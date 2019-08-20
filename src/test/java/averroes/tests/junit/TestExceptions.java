package averroes.tests.junit;

import org.junit.Test;

public class TestExceptions {
  boolean guard = true;

  @Test
  public void testExceptionsRta() {
    Tests.runRta("Exceptions", guard);
  }

  @Test
  public void testExceptionsXta() {
    Tests.runXta("Exceptions", guard);
  }

  @Test
  public void testExceptions2Rta() {
    Tests.runRta("Exceptions2", guard);
  }

  @Test
  public void testExceptions2Xta() {
    Tests.runXta("Exceptions2", guard);
  }
}
