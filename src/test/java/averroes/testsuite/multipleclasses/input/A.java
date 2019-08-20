package averroes.testsuite.multipleclasses.input;

/**
 * Public library class A that refers to auxiliary classes B and C that are not exposed to the
 * client.
 */
public class A {
  private Object f1;
  private B f2;

  public void m1(Object x) {
    this.f1 = x;
    this.f2 = new B(x);
  }

  public boolean m2(Object y, Object z) {
    C c = new C(y, z);
    return c.m6();
  }

  public Object m3() {
    return f1;
  }

  public Object m4() {
    return f2.m5();
  }
}
