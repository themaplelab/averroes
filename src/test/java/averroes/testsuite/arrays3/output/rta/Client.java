package averroes.testsuite.arrays3.output.rta;

class Client {

  Client() {
    RTA.set = this; // inferred from the implicit "this" parameter
  }

  public static void main(String[] args) {
    RTA.set = args; // inferred from the parameter args
    RTA.set = new A(); // inferred from the object creation new (A)
    foo((A) RTA.set); // inferred from the method call foo(new A());
  }

  public static void foo(A a) {
    RTA.set = a; // inferred from the parameter "a"
    RTA.set = new X(); // inferred from creation of X
    RTA.set = new Object[1]; // inferred from creation of array object
    ((A) RTA.set).m1((Object[]) RTA.set); // inferred from method call a.m1()
    ((A) RTA.set).m2(RTA.set); // inferred from method call a.m2()
    ((A) RTA.set).m3(); // inferred from method call a.m3()
    ((A) RTA.set).m4(); // inferred from method call a.m4()
  }
}

class X {
  X() {
    RTA.set = this; // inferred from the implicit "this" parameter
  }

  @Override
  public String toString() {
    RTA.set = this; // inferred from the implicit "this" parameter
    return (String) RTA.set;
  }
}
