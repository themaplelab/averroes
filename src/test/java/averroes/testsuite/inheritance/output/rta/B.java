package averroes.testsuite.inheritance.output.rta;

/**
 * This class is not part of the library's public API. However, it invokes callback methods that
 * dispatch to client code.
 */
class B {
  private I f2;

  B() {
    RTA.set = this; // inferred from implicit "this" parameter
  }

  void m4(I i) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = i; // inferred for parameter "i"
    f2 = (I) RTA.set; // inferred from field write "this.f2 = i"
  }

  void m5(Object o) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = o; // inferred for parameter "o"
    RTA.set = f2; // inferred from field read "this.f2"
    ((I) RTA.set).f(RTA.set); // inferred for call "this.f2.f(o)"
  }

  Object m6() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = ((I) RTA.set).g(); // inferred for call "f2.g()"
    RTA.set = f2; // inferred from field read in f2.g()
    return RTA.set;
  }
}
