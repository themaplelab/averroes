package averroes.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Main test suite runner.
 *
 * @author Karim Ali
 */
public class Main {

  public static void main(String[] args) {
    Result result = JUnitCore.runClasses(AllTests.class);
    result.getFailures().forEach(System.out::println);
    System.out.println(result.wasSuccessful());
  }
}
