package averroes.testsuite.exceptions.output.xta;

public class A {
  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;
  private Object set_m6;
  private Object set_m7;
  private Error f1; // set for field f1

  public A() {
    set_m0 = this; // inferred from implicit "this" parameter
  }

  public void m1(Throwable t) throws Throwable {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = t; // inferred for parameter "t"
    throw (Throwable) set_m1; // inferred for "throw t"
  }

  public void m2() {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = new NullPointerException(); // inferred for ctor call "new
    // NullPointerException()"
    throw (NullPointerException) set_m2; // inferred for "throw new
    // NullPointerException()"
  }

  public void m3(String s) {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = s; // inferred for parameter "s"
    set_m3 = new Error(s); // inferred for ctor call "new Error(s)"
    f1 = (Error) set_m3; // inferred for field write to f1
  }

  public void m4() {
    set_m4 = this; // inferred from implicit "this" parameter
    set_m4 = f1; // inferred for field read from f1
    throw (Error) set_m4; // inferred from "throw f1"
  }

  public Throwable m5() {
    set_m5 = this; // inferred from implicit "this" parameter
    set_m5 = f1; // inferred for field read from f1
    return (Throwable) set_m5; // inferred from return statement
  }

  public void m6() throws Exception {
    set_m6 = this; // inferred from implicit "this" parameter
    set_m6 = new B(); // inferred from ctor call "new B()"
    throw (B) set_m6; // inferred from "throw e". Soot optimizes this to B instead of Exception.
  }

  public void m7() {
    set_m7 = this; // inferred from implicit "this" parameter
    set_m7 = new B(); // inferred from ctor call "new B()"
    // no throw generated for "throw e" because the exception is caught locally
    set_m7 = new Exception(); // inferred from the checked exception
    ((Exception) set_m7).printStackTrace(); // generated for
    // "e1.printStackTrace()"
  }
}
