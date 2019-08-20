package averroes.testsuite.exceptions.output.rta;

public class A {
  private Error f1; // set for field f1

  public A() {
    RTA.set = this; // inferred from implicit "this" parameter
  }

  public void m1(Throwable t) throws Throwable {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = t; // inferred for parameter t
    throw (Throwable) RTA.set; // inferred for "throw"
  }

  public void m2() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new NullPointerException(); // inferred for "new NullPointerException()
    throw (NullPointerException) RTA.set; // inferred for "throw new NullPointerException()"
  }

  public void m3(String s) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = s; // inferred for parameter s
    f1 = new Error((String) RTA.set); // inferred for "new Error(s)"
  }

  public void m4() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred for field read from f1
    throw (Error) RTA.set; // inferred for "throw f1"
  }

  public Throwable m5() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred for field read from f1
    return (Throwable) RTA.set; // inferred from return type
  }

  public void m6() throws Exception {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new B(); // inferred from "new B()"
    throw (Exception) RTA.set;
  }

  public void m7() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new B(); // inferred from "new B()"
    // note: no code generated for "throw e" that is handled by a local catch block
    RTA.set = new Exception(); // inferred from the checked exception
    ((Exception) RTA.set)
        .printStackTrace(); // generated for call to printStackTrace() inside local catch block
  }
}
