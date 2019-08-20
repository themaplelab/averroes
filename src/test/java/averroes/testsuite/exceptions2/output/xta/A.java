package averroes.testsuite.exceptions2.output.xta;

public class A {
  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;

  public A() {
    set_m0 = this; // inferred for implicit "this" parameter
  }

  public void m() {
    set_m1 = this; // inferred for implicit "this" parameter
    set_m1 = new Error(); // inferred from the object creation Error e = new Error()
    set_m1 = new Exception(); // inferred from the checked exception
    // no throw generated for "throw e" because the exception is caught locally
    ((Exception) set_m1).printStackTrace();
  }
}
