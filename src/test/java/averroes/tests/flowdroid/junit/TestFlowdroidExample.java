package averroes.tests.flowdroid.junit;

import org.junit.Test;

public class TestFlowdroidExample {
  String testCase = "Example";
  String entryPoint = "<example.Client: void main(java.lang.String[])>";

  @Test
  public void testFlowdroidExample() {
    Tests.runFlowDroid(testCase, entryPoint, 1);
  }

  @Test
  public void testFlowdroidExampleXta() {
    Tests.runFlowDroidXta(testCase, entryPoint, 2, false);
  }

  @Test
  public void testFlowdroidExampleRta() {
    Tests.runFlowDroidRta(testCase, entryPoint, 3, false);
  }

  @Test
  public void testFlowdroidExampleAverroes() {
    Tests.runFlowDroidAverroes(testCase, entryPoint, 5, false);
  }

  @Test
  public void testFlowdroidExampleHandWrittenXta() {
    Tests.runFlowDroidXta(testCase, entryPoint, 2, true);
  }

  @Test
  public void testFlowdroidExampleHandWrittenRta() {
    Tests.runFlowDroidRta(testCase, entryPoint, 3, true);
  }

  @Test
  public void testFlowdroidExampleHandWrittenAverroes() {
    Tests.runFlowDroidAverroes(testCase, entryPoint, 5, true);
  }
}
