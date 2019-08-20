package averroes.testsuite.arrays.output.cfa;

public class A {
  private Object[] f1 = new Object[1];
  private Object[] f2 = new Object[1];
  private Object[] f3 = new Object[1];

  public void m1(Object a, Object b) {
    f1[0] = a;
    f2[0] = b;
  }

  public void m2(Object a) {
    f3[0] = a;
  }

  public Object m3() {
    return f1[0];
  }

  public Object m4() {
    return f2[0];
  }

  public Object m5() {
    return f3[0];
  }
}
