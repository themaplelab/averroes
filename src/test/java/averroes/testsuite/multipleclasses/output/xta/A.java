package averroes.testsuite.multipleclasses.output.xta;

/**
 * Public library class A that refers to auxiliary classes B and C that are not exposed to the
 * client.
 */
public class A {
  private Object f1; // objects in f1
  private B f2; // objects in f2
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;

  public A() {
    set_m0 = this;
  }

  public void m1(Object x) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = x; // inferred from parameter x

    Object set = set_m1;
    set_m1 = new B(set); // inferred from allocation of B inside m1
    f1 = set; // inferred from write to f1
    f2 = (B) set; // inferred from write to f2
  }

  public boolean m2(Object y, Object z) {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = y; // inferred from parameter y
    set_m2 = z; // inferred from parameter z

    Object set = set_m2;
    set_m2 = new C(set, set); // inferred from allocation of C inside m2

    ((C) set).m6(); // inferred from the call to C.m6()
    return true;
  }

  public Object m3() {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = f1; // inferred from read of f1
    return set_m3;
  }

  public Object m4() {
    set_m4 = this; // inferred from implicit "this" parameter

    Object set = set_m4;
    set_m4 = ((B) set).m5(); // inferred from call to method B.m5()

    set_m4 = f2; // inferred from read of f2

    return set;
  }
}
