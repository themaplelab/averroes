package averroes.testsuite.inheritance.output.cfa;

/**
 * This class is not part of the library's public API. However, it invokes callback methods that
 * dispatch to client code.
 */
class B {
  private I f2;

  void m4(I i) {
    this.f2 = i;
  }

  void m5(Object o) {
    this.f2.f(o);
  }

  Object m6() {
    return f2.g();
  }
}
