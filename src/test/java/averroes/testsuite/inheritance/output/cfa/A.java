package averroes.testsuite.inheritance.output.cfa;

/**
 * This class is part of the public API. It invokes callback methods in a library-internal class
 * that are overridden in client code
 */
public class A {

  B f1;

  public A(B b) {
    this.f1 = b;
  }

  public void m1(I i) {
    this.f1.m4(i);
  }

  public void m2(Object o) {
    this.f1.m5(o);
  }

  public Object m3() {
    return f1.m6();
  }
}
