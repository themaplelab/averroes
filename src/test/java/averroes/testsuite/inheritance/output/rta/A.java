package averroes.testsuite.inheritance.output.rta;

/**
 * This class is part of the public API. It invokes callback methods in a library-internal class
 * that are overridden in client code
 */
public class A {

  B f1;

  public A(B b) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = b; // inferred for parameter b
    f1 = (B) RTA.set; // inferred from field write "this.f1 = b"
  }

  public void m1(I i) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = i; // inferred for parameter i
    ((B) RTA.set).m4((I) RTA.set); // inferred for call "this.f1.m4(i)"
    RTA.set = f1; // inferred from field read "this.f1"
  }

  public void m2(Object o) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = o; // inferred for parameter o
    RTA.set = f1; // inferred from field read "this.f1"
    ((B) RTA.set).m5(RTA.set); // inferred for call this.f1.m5(o);
  }

  public Object m3() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = ((B) RTA.set).m6(); // inferred for call f1.m6();
    RTA.set = f1; // inferred from field read "this.f1"
    return RTA.set; // inferred from method return type "Object"
  }
}
