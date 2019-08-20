package averroes.tests.junit;

import org.junit.Test;

public class TestArrays {
  boolean guard = true;

  @Test
  public void testArraysRta() {
    Tests.runRta("Arrays", guard);
  }

  @Test
  public void testArraysXta() {
    Tests.runXta("Arrays", guard);
  }

  @Test
  public void testArrays2Rta() {
    Tests.runRta("Arrays2", guard);
  }

  @Test
  public void testArrays2Xta() {
    Tests.runXta("Arrays2", guard);
  }

  @Test
  public void testArrays3Rta() {
    Tests.runRta("Arrays3", guard);
  }

  @Test
  public void testArrays3Xta() {
    Tests.runXta("Arrays3", guard);
  }

  @Test
  public void testArrays4Rta() {
    Tests.runRta("Arrays4", guard);
  }

  @Test
  public void testArrays4Xta() {
    Tests.runXta("Arrays4", guard);
  }
}
