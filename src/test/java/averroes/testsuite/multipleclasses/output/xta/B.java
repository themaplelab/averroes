package averroes.testsuite.multipleclasses.output.xta;

class B {

  private Object f3; // set for field f3
  private Object set_m0;
  private Object set_m1;

  B(Object o) {
    set_m0 = this; // inferred from implicit "this" parameter
    set_m0 = o; // inferred from parameter o
    f3 = set_m0; // inferred from write to field f3
  }

  public Object m5() {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = f3; // inferred from read of field f3
    return set_m1;
  }
}
