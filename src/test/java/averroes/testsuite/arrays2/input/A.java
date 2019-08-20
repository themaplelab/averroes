package averroes.testsuite.arrays2.input;

public class A {
  private Object f1;
  private Object[] f2;
  private Object[] f3;

  public void m1(Object a) {
    this.f1 = a;
  }

  public void m2(Object b) {
    this.f2[17] = b;
  }

  public void m3(Object[] c) {
    this.f3 = c;
  }

  public Object m4() {
    return this.f1;
  }

  public Object m5() {
    return this.f2[17];
  }

  public Object[] m6() {
    return this.f2;
  }

  public Object m7() {
    return this.f3[17];
  }

  public Object[] m8() {
    return this.f3;
  }

  public void m9() {
    this.f2 = this.f3;
  }

  public void m10() {
    this.f3[17] = this.f2[18];
  }
}
