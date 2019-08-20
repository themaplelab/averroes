package averroes.testsuite.arrays4.output.xta;

public class A {

  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;
  private Object f1; // set for field f1
  private Object[] f2; // set for field f2
  public A() {
    set_m0 = this; // inferred from implicit "this" parameter
  }

  public void m1() {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = new Object[1]; // inferred for array creation
    set_m1 = new int[1]; // inferred for array creation
    f1 = set_m1; // inferred for field write to f1
  }

  public void m2() {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = new String[1]; // inferred for array creation
    f2 = (Object[]) set_m2; // inferred for field write to f2
  }

  public Object m3() {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = f1; // inferred for field read from f1
    return set_m3; // inferred from return type
  }

  public Object[] m4() {
    set_m4 = this; // inferred from implicit "this" parameter
    set_m4 = f2; // inferred for field read from f2
    return (Object[]) set_m4; // inferred from return type
  }

  public Object m5() {
    set_m5 = this; // inferred from implicit "this" parameter
    set_m5 = f2; // inferred for field read from f2
    set_m5 = ((Object[]) set_m5)[0]; // inferred from aaload
    return set_m5; // inferred from return type
  }
}
