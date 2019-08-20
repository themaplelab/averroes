package averroes.testsuite.arrays4.output.rta;

public class A {

  private Object f1; // set for field f1
  private Object[] f2; // set for field f2

  public A() {
    RTA.set = this; // inferred from implicit "this" parameter
  }

  public void m1() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new Object[1]; // inferred for new array
    RTA.set = new int[1]; // inferred for new array
    f1 = RTA.set; // inferred for field write to f1
  }

  public void m2() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new String[1]; // inferred for new array
    f2 = (Object[]) RTA.set; // inferred for field write to f2
  }

  public Object m3() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred for field read from f1
    return RTA.set; // inferred from method return type
  }

  public Object[] m4() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f2; // inferred for field read from f2
    return (Object[]) RTA.set; // inferred from method return type
  }

  public Object m5() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f2; // inferred for field read from f2
    RTA.set = ((Object[]) RTA.set)[0]; // inferred from aaload
    return RTA.set; // inferred from method return type
  }
}
