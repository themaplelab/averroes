package averroes.testsuite.inheritance.output.xta;

/**
 * This class is not part of the library's public API. However, it invokes callback methods that
 * dispatch to client code.
 */
class B {
  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private I f2;

  B() {
    set_m0 = this; // inferred from implicit "this" parameter
  }

  void m4(I i) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = i; // inferred for parameter "i"
    f2 = (I) set_m1; // inferred from field write "this.f2 = i"
  }

  void m5(Object o) {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = o; // inferred for parameter "o"
    set_m2 = f2; // inferred from field read "this.f2"
    ((I) set_m2).f(set_m2); // inferred from method call "this.f2.f(o)"
  }

  Object m6() {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = ((I) set_m3).g(); // inferred from call "f2.g()"
    set_m3 = f2; // inferred from field read in f2.g()
    return set_m3; // inferred from return type Object
  }
}
