package averroes.testsuite.casting.output.rta;

public class A {
  private Object f1;
  private String f2;

  public A() {
    RTA.set = this; // inferred from implicit "this" parameter
  }

  public void m1(Object x) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = x; // inferred for parameter "x"

    // field write
    Object obj = RTA.set;
    A a = (A) obj;
    a.f1 = obj;
  }

  public void m2(String y) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = y; // inferred for parameter "y"

    // field write
    Object obj = RTA.set;
    A a = (A) obj;
    a.f1 = obj;
  }

  public Object m3() {
    RTA.set = this; // inferred for implicit "this" parameter

    // field read
    Object obj = RTA.set;
    A a = (A) obj;
    RTA.set = a.f2;

    return obj; // inferred from return type
  }

  public String m4() {
    RTA.set = this; // inferred for implicit "this" parameter

    // field read
    Object obj = RTA.set;
    A a = (A) obj;
    RTA.set = a.f2;

    return (String) obj; // inferred from return type
  }

  public Object m5() {
    RTA.set = this; // inferred for implicit "this" parameter
    m3(); // inferred from call m3()
    return RTA.set; // inferred from return type
  }

  public Object m6() {
    RTA.set = this; // inferred for implicit "this" parameter
    m4(); // inferred from call m4()
    return RTA.set; // inferred from return type
  }

  public void m7() {
    RTA.set = this; // inferred for implicit "this" parameter
    m1(RTA.set); // inferred from call to m1()
  }

  public void m8() {
    RTA.set = this; // inferred for implicit "this" parameter

    // field read
    Object obj = RTA.set;
    A a = (A) obj;
    RTA.set = a.f1;

    m2((String) obj); // inferred from call to m2()
  }
}
