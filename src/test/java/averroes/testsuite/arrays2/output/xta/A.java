package averroes.testsuite.arrays2.output.xta;

public class A {
  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;
  private Object set_m6;
  private Object set_m7;
  private Object set_m8;
  private Object set_m9;
  private Object set_m10;
  private Object f1; // set for field f1
  private Object[] f2; // set for field f2
  private Object[] f3; // set for field f3
  public A() {
    set_m0 = this; // inferred from implicit "this" parameter
  }

  public void m1(Object a) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = a; // inferred from parameter a
    f1 = set_m1; // inferred from field write "this.f1 = a;"
  }

  public void m2(Object b) {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = b; // inferred from parameter "b"
    set_m2 = f2; // inferred from field read f2 in "this.f2[17] = b"
    ((Object[]) set_m2)[0] = set_m2; // inferred from aawrite in "this.f2[17] = b"
  }

  public void m3(Object[] c) {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = c; // inferred from parameter "c"
    f3 = (Object[]) set_m3; // inferred from field write "this.f3 = c;"
  }

  public Object m4() {
    set_m4 = this; // inferred from implicit "this" parameter
    set_m4 = f1; // inferred from field read "this.f1"
    return set_m4; // inferred from return type
  }

  public Object m5() {
    set_m5 = this; // inferred from implicit "this" parameter
    set_m5 = f2; // inferred from field read f2 in "this.f2[17]"
    set_m5 = ((Object[]) set_m5)[0]; // inferred from aaload in "this.f2[17]"
    return set_m5;
  }

  public Object[] m6() {
    set_m6 = this; // inferred from implicit "this" parameter
    set_m6 = f2; // inferred from field read "this.f2"
    return (Object[]) set_m6;
  }

  public Object m7() {
    set_m7 = this; // inferred from implicit "this" parameter
    set_m7 = f3; // inferred from field read f3 in  "this.f3[17]"
    set_m7 = ((Object[]) set_m7)[0]; // inferred from aaload in "this.f3[17]"
    return set_m7; // inferred from return type
  }

  public Object[] m8() {
    set_m8 = this; // inferred from implicit "this" parameter
    set_m8 = f3; // inferred from field read "this.f3"
    return (Object[]) set_m8;
  }

  public void m9() {
    set_m9 = this; // inferred from implicit "this" parameter
    set_m9 = f3; // inferred from field read "this.f3" in "this.f2 = this.f3;"
    f2 = (Object[]) set_m9; // inferred from field write "this.f2" in "this.f2 = this.f3;"
  }

  public void m10() {
    set_m10 = this; // inferred from implicit "this" parameter
    set_m10 = f2; // inferred from field read f2 in "this.f2[18]" in "this.f3[17] = this.f2[18]"
    set_m10 = f3; // inferred from field read f3 in "this.f3[17]" in "this.f3[17] = this.f2[18]"
    set_m10 = ((Object[]) set_m10)[0]; // inferred from aaload in "this.f2[18]"
    ((Object[]) set_m10)[0] = set_m10; // inferred from aastore in "this.f3[17]"
  }
}
