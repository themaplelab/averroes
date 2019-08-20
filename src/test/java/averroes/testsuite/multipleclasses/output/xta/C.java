package averroes.testsuite.multipleclasses.output.xta;

class C {
  private Object f4; // set for field f4
  private Object f5; // set for field f5
  private Object set_m0;
  private Object set_m1;

  C(Object y, Object z) {
    set_m0 = this; // inferred from implicit "this" parameter
    set_m0 = y; // inferred for parameter y
    set_m0 = z; // inferred for parameter z

    Object set = set_m0;
    f4 = set; // inferred for write to field f4
    f5 = set; // inferred for write to field f5
  }

  public boolean m6() {
    set_m1 = this; // inferred from implicit "this" parameter

    Object set = set_m1;
    set.equals(set); // inferred for call "f4.equals(f5)"

    set_m1 = f4; // inferred for read from field f4
    set_m1 = f5; // inferred for read from field f5

    return true; // inferred from boolean return
  }
}
