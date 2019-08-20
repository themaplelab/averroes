package averroes.testsuite.exceptions.input;

public class B extends Exception {
  @Override
  public void printStackTrace() {
    super.printStackTrace(System.err);
  }
}
