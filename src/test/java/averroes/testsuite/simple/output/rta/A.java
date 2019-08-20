package averroes.testsuite.simple.output.rta;

public class A {
  private Object f1; // objects in f1
  private Object f2; // objects in f1
  private Object f3; // objects in f1

  public A() {
    RTA.set = this;
  }

  public void m1(Object x) {
    RTA.set = x; // all parameters flow into set
    RTA.set = this; // for any instance method, "this" flows into the set
    f1 = RTA.set; // the original m1 method writes field f1, so propagate types from RTA.set to f1
  }

  public void m2(Object y, Object z) {
    RTA.set = y; // all parameters flow into set
    RTA.set = z; // all parameters flow into set
    RTA.set = this; // for any instance method, "this" flows into the set
    RTA.set.hashCode();
    RTA.set.hashCode();
    f2 = RTA.set; // the original m2 method writes field f2, so propagate types from RTA.set to f2
    f3 = RTA.set; // the original m2 method writes field f3, so propagate types from RTA.set to f3
  }

  public Object m3() {
    RTA.set = this;
    RTA.set = f1; // original method reads f1, so propagate from f1 to RTA.set
    return RTA.set; // any object in the set may be returned
  }

  public Object m4() {
    RTA.set = this;
    RTA.set = f2; // original method reads f2, so propagate from f2 to RTA.set
    return RTA.set; // any object in the set may be returned
  }

  public Object m5() {
    RTA.set = this;
    RTA.set = f3; // original method reads f3, so propagate from f3 to RTA.set
    return RTA.set; // any object in the set may be returned
  }
}
