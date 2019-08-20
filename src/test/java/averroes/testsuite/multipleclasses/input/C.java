package averroes.testsuite.multipleclasses.input;

class C {
  private Object f4;
  private Object f5;

  C(Object y, Object z) {
    this.f4 = y;
    this.f5 = z;
  }

  public boolean m6() {
    return f4.equals(f5);
  }
}
