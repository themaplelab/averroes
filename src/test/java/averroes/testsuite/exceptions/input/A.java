package averroes.testsuite.exceptions.input;

public class A {
  private Error f1;

  public void m1(Throwable t) throws Throwable {
    throw t;
  }

  public void m2() {
    throw new NullPointerException();
  }

  public void m3(String s) {
    f1 = new Error(s);
  }

  public void m4() {
    throw f1;
  }

  public Throwable m5() {
    return f1;
  }

  public void m6() throws Exception {
    Exception e = new B();
    throw e;
  }

  public void m7() {
    Exception e = new B();
    try {
      throw e;
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }
}
