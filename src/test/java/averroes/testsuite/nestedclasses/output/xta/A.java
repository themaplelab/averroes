package averroes.testsuite.nestedclasses.output.xta;

public class A {

  private B f3; // set for field f3
  private Object f4; // set for field f4
  private C f5; // set for field f5
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  public A() {
    set_m0 = this;
  }

  public void m6() {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = new B(); // inferred from allocation "new B()"
    set_m1 = ""; // inferred from allocation "new String()"

    // field writes
    Object set = set_m1;
    A a = (A) set;
    a.f3 = (B) set;
    a.f4 = set;
  }

  public void m7() {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = new C(); // inferred from allocation "new C()"

    // field writes
    Object set = set_m2;
    A a = (A) set;
    a.f5 = (C) set;
  }

  public B m8() {
    set_m3 = this; // inferred from implicit "this" parameter

    // field reads
    Object set = set_m3;
    A a = (A) set;
    set_m3 = a.f3;

    return (B) set; // inferred from return type
  }

  /* static nested class */
  static class C {
    private Object f2; // set for field f2
    private Object set_m0;
    private Object set_m1;
    private Object set_m2;
    C() {
      set_m0 = this;
    }

    void m4(Object o) {
      set_m1 = this; // inferred from implicit "this" parameter
      set_m1 = o; // inferred for parameter o

      // field writes
      Object set = set_m1;
      C c = (C) set;
      c.f2 = set;
    }

    Object m5() {
      set_m2 = this; // inferred from implicit "this" parameter

      // field reads
      Object set = set_m2;
      C c = (C) set;
      set_m2 = c.f2;

      return set; // inferred from return type
    }
  }

  /* non-static nested class */
  class B {
    // weirdly, it seems that static fields cannot be used here because the
    // nested class is nonstatic
    private /* static */ A f1; // set for field f1
    private Object set_m0;
    private Object set_m1;
    private Object set_m2;
    private Object set_m3;
    B() {
      set_m0 = this;
      set_m0 = A.this;
    }

    void m1() {
      set_m1 = this; // inferred from implicit "this" parameter
      set_m1 = A.this; // inferred from implicit "this" parameter of outer class A

      // field writes
      Object set = set_m1;
      A a = (A) set;
      B b = (B) set;
      b.f1 = a;
    }

    A m2() {
      set_m2 = this; // inferred from implicit "this" parameter
      set_m2 = A.this; // inferred from implicit "this" parameter of outer class A

      // field reads
      Object set = set_m2;
      B b = (B) set;
      set_m2 = b.f1;

      return (A) set; // inferred from return type
    }

    Object m3() {
      set_m3 = this; // inferred from implicit "this" parameter
      set_m3 = A.this; // inferred from implicit "this" parameter of outer class A

      // field reads
      Object set = set_m3;
      A a = (A) set;
      set_m3 = a.f5;

      return set; // inferred from return type
    }
  }
}
