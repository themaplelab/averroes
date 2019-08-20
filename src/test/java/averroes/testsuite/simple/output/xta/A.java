package averroes.testsuite.simple.output.xta;

public class A {
  @SuppressWarnings("unused")
  private Object set_m0; // objects in m0
  private Object set_m1; // objects in m1
  private Object set_m2; // objects in m2
  private Object set_m3; // objects in m3
  private Object set_m4; // objects in m4
  private Object set_m5; // objects in m5
  private Object f1; // objects in f1
  private Object f2; // objects in f1
  private Object f3; // objects in f1
  public A() {
    set_m0 = this; // for any instance method, assign "this" into the defining method's set
  }

  public void m1(Object x) {
    set_m1 = this; // for any instance method, assign "this" into the defining method's set
    set_m1 = x; // assume any parameter flows into set_m1
    f1 = set_m1; // the original m1 method writes field f1, so propagate types from set_m1 to f1
  }

  public void m2(Object y, Object z) {
    set_m2 = this; // for any instance method, assign "this" into the defining method's set
    set_m2 = y; // assume any parameter flows into set_m2
    set_m2 = z; // assume any parameter flows into set_m2

    set_m2.hashCode(); // call to hashCode in conditional
    set_m2.hashCode(); // call to hashCode in conditional

    f2 = set_m2; // the original m2 method writes field f2, so propagate types from set_m2 to f2
    f3 = set_m2; // the original m2 method writes field f3, so propagate types from set_m2 to f3
  }

  public Object m3() {
    set_m3 = this; // for any instance method, assign "this" into the defining method's set
    set_m3 = f1; // original method reads f1, so propagate from f1 to set_m3
    return set_m3;
  }

  public Object m4() {
    set_m4 = this; // for any instance method, assign "this" into the defining method's set
    set_m4 = f2; // original method reads f2, so propagate from f2 to set_m4
    return set_m4;
  }

  public Object m5() {
    set_m5 = this; // for any instance method, assign "this" into the defining method's set
    set_m5 = f3; // original method reads f3, so propagate from f3 to set_m5
    return set_m5;
  }
}
