package averroes.testsuite.casting.input;

public class A {
  private Object f1;
  private String f2;

  public void m1(Object x) {
    f1 = x;
  }

  public void m2(String y) {
    f1 = y;
  }

  public Object m3() {
    return f2;
  }

  public String m4() {
    return f2;
  }

  public Object m5() {
    return m3();
  }

  public Object m6() {
    return m4();
  }

  public void m7() {
    m1("foo");
  }

  public void m8() {
    m2((String) f1);
  }
}
