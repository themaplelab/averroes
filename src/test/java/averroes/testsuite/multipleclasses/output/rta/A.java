package averroes.testsuite.multipleclasses.output.rta;

public class A {

  private Object f1;
  private B f2;

  public A() {
    RTA.set = this;
  }

  public void m1(Object x) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = x; // inferred for parameter x

    Object obj = RTA.set;
    RTA.set = new B(obj); // represents "new B(x)" allocation site in the original code

    // field writes
    A a = (A) obj;
    a.f1 = obj;
    a.f2 = (B) obj;
  }

  public boolean m2(Object y, Object z) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = y; // inferred for parameter y
    RTA.set = z; // inferred for parameter z
    RTA.set =
        new C(RTA.set, RTA.set); // inferred for "new C(y,z)" allocation site in the original code
    ((C) RTA.set).m6(); // inferred for call to C.m6()
    return RTA.booleanVal; // inferred for return of primitive boolean value
  }

  public Object m3() {
    RTA.set = this; // inferred for implicit "this" parameter

    // field read
    Object obj = RTA.set;
    A a = (A) obj;
    RTA.set = a.f1;

    return obj; // inferred for return of Object value
  }

  public Object m4() {
    RTA.set = this; // inferred for implicit "this" parameter
    // field read
    Object obj = RTA.set;
    A a = (A) obj;
    RTA.set = a.f2;

    RTA.set = ((B) obj).m5(); // inferred for call to B.m5()

    return obj; // inferred for return of Object value
  }
}
