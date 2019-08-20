package averroes.testsuite.exceptions2.output.rta;

public class A {
  public A() {
    RTA.set = this; // inferred for implicit "this" parameter
  }

  public void m() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new Error();
    RTA.set = new Exception();
    if (RTA.guard) throw (Error) RTA.set;
    ((Exception) RTA.set).printStackTrace();
  }
}
