package averroes.testsuite.arrays3.input;

class Client {

  public static void main(String[] args) {
    foo(new A());
  }

  public static void foo(A a) {
    a.m1(new Object[10]);
    a.m2(new X());
    a.m3();
    a.m4();
  }
}

class X {
  @Override
  public String toString() {
    return "X";
  }
}
