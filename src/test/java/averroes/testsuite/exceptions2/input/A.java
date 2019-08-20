package averroes.testsuite.exceptions2.input;

public class A {
  public void m() {
    Error e = new Error();
    try {
      throw e;
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }
}
