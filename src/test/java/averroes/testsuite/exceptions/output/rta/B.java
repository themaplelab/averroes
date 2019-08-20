package averroes.testsuite.exceptions.output.rta;

import java.io.PrintStream;

@SuppressWarnings("serial")
public class B extends Exception {
  public B() {
    RTA.set = this; // inferred for implicit "this" parameter
  }

  @Override
  public void printStackTrace() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = System.err; // inferred for field read System.err
    super.printStackTrace((PrintStream) RTA.set); // inferred for call to
    // printStackTrace()
  }
}
