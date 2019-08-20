package averroes.testsuite.casting.output.xta;

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
  private Object set_m8;
  private Object f1; // objects in f1
  private String f2; // objects in f2
  public A() {
    set_m0 = this; // inferred from implicit "this" parameter
  }

  public void m1(Object x) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = x; // inferred from parameter x
    f1 = set_m1; // inferred from write to f1
  }

  public void m2(String y) {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = y; // inferred from parameter y
    f1 = set_m2; // inferred from write to f1
  }

  public Object m3() {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = f2; // inferred from read of f2
    return set_m3;
  }

  public String m4() {
    set_m4 = this; // inferred from implicit "this" parameter
    set_m4 = f2; // inferred from read of f2
    return (String) set_m4; // Note: cast needed because return type is not Object
  }

  public Object m5() {
    set_m5 = this; // inferred from implicit "this" parameter
    set_m5 = m3(); // inferred from call to m3()
    return set_m5;
  }

  public Object m6() {
    set_m6 = this; // inferred from implicit "this" parameter
    set_m6 = m4(); // inferred from call to m4()
    return set_m6;
  }

  public void m7() {
    set_m7 = this; // inferred from implicit "this" parameter
    m1(set_m7); // inferred from call to m1()
  }

  public void m8() {
    set_m8 = this; // inferred from implicit "this" parameter
    m2(
        (String)
            set_m8); // inferred from call to m2. Note: cast needed because arg type is not Object
    set_m8 = f1; // inferred from read of f1
  }
}
