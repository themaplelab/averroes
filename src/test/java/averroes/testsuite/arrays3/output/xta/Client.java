package averroes.testsuite.arrays3.output.xta;

class Client {

  private static Object set_m1;
  private static Object set_m2;
  @SuppressWarnings("unused")
  private Object set_m0;

  Client() {
    set_m0 = this; // inferred from the implicit "this" parameter
  }

  public static void main(String[] args) {
    set_m1 = args; // inferred from the parameter args
    set_m1 = new A(); // inferred from the object creation new (A)
    foo((A) set_m1); // inferred from the method call foo(new A());
  }

  public static void foo(A a) {
    set_m2 = a; // inferred from the parameter "a"
    set_m2 = new X(); // inferred from creation of X
    set_m2 = new Object[1]; // inferred from creation of array object
    ((A) set_m2).m1((Object[]) set_m2); // inferred from method call a.m1()
    ((A) set_m2).m2((X) set_m2); // inferred from method call a.m2()
    ((A) set_m2).m3(); // inferred from method call a.m3()
    ((A) set_m2).m4(); // inferred from method call a.m4()
  }
}

class X {
  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;

  X() {
    set_m0 = this; // inferred from the implicit "this" parameter
  }

  @Override
  public String toString() {
    set_m1 = this; // inferred from the implicit "this" parameter
    return (String) set_m1;
  }
}
