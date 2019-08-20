package averroes.testsuite.arrays4.output.cfa;

public class A {

  private Object f1;
  private Object[] f2;

  public void m1() {
    f1 = new Object[10];
    f1 = new int[7];
  }

  public void m2() {
    f2 = new String[5];
  }

  public Object m3() {
    return f1;
  }

  public Object[] m4() {
    return f2;
  }

  public Object m5() {
    return f2[0];
  }
}
