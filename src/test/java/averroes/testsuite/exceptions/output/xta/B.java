package averroes.testsuite.exceptions.output.xta;

import java.io.PrintStream;

@SuppressWarnings("serial")
public class B extends Exception {
  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;

  public B() {
    set_m0 = this; // inferred for implicit "this" parameter
  }

  @Override
  public void printStackTrace() {
    set_m1 = this; // inferred for implicit "this" parameter

    super.printStackTrace((PrintStream) set_m1); // inferred
    // for call
    // to
    // printStackTrace()

    set_m1 = System.err; // inferred for field read System.err
  }
}
