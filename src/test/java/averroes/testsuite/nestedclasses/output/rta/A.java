package averroes.testsuite.nestedclasses.output.rta;

public class A {
  private B f3;
  private Object f4;
  private C f5;

  public A() {
    RTA.set = this;
  }

  public void m6() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new B(); // inferred from allocation "new B()"
    RTA.set = ""; // inferred from allocation "new String()"

    // field writes
    Object obj = RTA.set;
    A a = (A) obj;
    a.f3 = (B) obj;
    a.f4 = obj;
  }

  public void m7() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = new C(); // inferred from allocation "new C()"

    // field writes
    Object obj = RTA.set;
    A a = (A) obj;
    a.f5 = (C) obj;
  }

  public B m8() {
    RTA.set = this; // inferred for implicit "this" parameter

    // field reads
    Object obj = RTA.set;
    A a = (A) obj;
    RTA.set = a.f3;

    return (B) obj; // inferred from return type
  }

  /* static nested class */
  static class C {
    private Object f2;

    C() {
      RTA.set = this;
    }

    void m4(Object o) {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = o; // inferred for parameter "o"

      // field writes
      Object obj = RTA.set;
      C c = (C) obj;
      c.f2 = obj;
    }

    Object m5() {
      RTA.set = this; // inferred for implicit "this" parameter

      // field reads
      Object obj = RTA.set;
      C c = (C) obj;
      RTA.set = c.f2;

      return obj; // inferred from return type
    }
  }

  /* non-static nested class */
  class B {
    private A f1;

    B() {
      RTA.set = this;
      RTA.set = A.this;
    }

    void m1() {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = A.this; // inferred for implicit "this" parameter of outer class A

      // field writes
      Object obj = RTA.set;
      A a = (A) obj;
      B b = (B) obj;
      b.f1 = a;
    }

    A m2() {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = A.this; // inferred for implicit "this" parameter of outer class A

      // field reads
      Object obj = RTA.set;
      B b = (B) obj;
      RTA.set = b.f1;

      return (A) obj; // inferred from return type
    }

    Object m3() {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = A.this; // inferred for implicit "this" parameter of outer class A

      // field reads
      Object obj = RTA.set;
      A a = (A) obj;
      RTA.set = a.f5;

      return obj; // inferred from return type
    }
  }
}
