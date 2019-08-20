package averroes.testsuite.inheritance.output.xta;

/**
 * This class is part of the public API. It invokes callback methods in a library-internal class
 * that are overridden in client code
 */
public class A {
  B f1;
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  public A(B b) {
    set_m0 = this; // inferred from implicit "this" parameter
    set_m0 = b; // inferred from parameter "b"
    f1 = (B) set_m0; // inferred from field write "this.f1 = b"
  }

  public void m1(I i) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = i; // inferred from parameter "i"
    set_m1 = f1; // inferred from field read "this.f1"
    ((B) set_m1).m4((I) set_m1); // inferred from call "this.f1.m4(i)"
  }

  public void m2(Object o) {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = o; // inferred from parameter "o"
    set_m2 = f1; // inferred from field read "this.f1"
    ((B) set_m2).m5(set_m2); // inferred from call "this.f1.m5(o)"
  }

  public Object m3() {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = f1; // inferred from field read "this.f1"
    set_m3 = ((B) set_m3).m6(); // inferred from call "f1.m6()
    return set_m3; // inferred from return type Object
  }
}
