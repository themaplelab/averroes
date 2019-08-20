package averroes.testsuite.simple.output.cfa;

public class A {
  private Object f1;
  private Object f2;
  private Object f3;

  public void m1(Object x) {
    this.f1 = x;
  }

  public void m2(Object y, Object z) {
    this.f2 =
        y; // note that the model is flow-insensitive, so some precision is lost for flow-sensitive
           // clients
    this.f3 = z;
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
