package averroes.testsuite.simple.input;

public class A {
  private Object f1;
  private Object f2;
  private Object f3;

  public void m1(Object x) {
    f1 = x;
  }

  public void m2(Object y, Object z) {
    if (y.hashCode()
        > z.hashCode()) { // arbitrary condition so that we can discuss flow-sensitivity
      f2 = y;
    } else {
      f3 = z;
    }
  }

  public Object m3() {
    return f1;
  }

  public Object m4() {
    return f2;
  }

  public Object m5() {
    return f3;
  }
}
