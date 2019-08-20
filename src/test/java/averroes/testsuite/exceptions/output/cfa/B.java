package averroes.testsuite.exceptions.output.cfa;

public class B extends Exception {
  @Override
  public void printStackTrace() {
    super.printStackTrace(System.err);
  }
}
