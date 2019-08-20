package averroes.testsuite.arrays2.output.rta;

public class A {
  private Object f1; // set for field f1
  private Object[] f2; // set for field f2
  private Object[] f3; // set for field f3

  public A() {
    RTA.set = this; // inferred from implicit "this" parameter
  }

  public void m1(Object a) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = a; // inferred for parameter "a"
    f1 = RTA.set; // inferred from field write "this.f1 = a;"
  }

  public void m2(Object b) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = b; // inferred for parameter "b"
    RTA.set = f2; // inferred from field read f2 in "this.f2[17] = b"
    ((Object[]) RTA.set)[0] = RTA.set; // inferred for aawrite "this.f2[17] = b"
  }

  public void m3(Object[] c) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = c; // inferred for parameter "c"
    f3 = (Object[]) RTA.set; // inferred from field write "this.f3 = c;"
  }

  public Object m4() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred from field read "this.f1"
    return RTA.set; // inferred from return type
  }

  public Object m5() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f2; // inferred from field read f2 in "this.f2[17]"
    RTA.set = ((Object[]) RTA.set)[0]; // inferred for aaread "this.f2[17]"
    return RTA.set; // inferred from return type
  }

  public Object[] m6() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f2; // inferred from field read "this.f2"
    return (Object[]) RTA.set; // inferred from return type
  }

  public Object m7() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f3; // inferred from field read f3 in  "this.f3[17]"
    RTA.set = ((Object[]) RTA.set)[0]; // inferred for aaread "this.f3[17]"
    return RTA.set; // inferred from return type
  }

  public Object[] m8() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f3; // inferred from field read "this.f3"
    return (Object[]) RTA.set; // inferred from return type
  }

  public void m9() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f3; // inferred from field read "this.f3" in "this.f2 = this.f3;"
    f2 = (Object[]) RTA.set; // inferred from field write "this.f2" in "this.f2 = this.f3;"
  }

  public void m10() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f2; // inferred from field read f2 in "this.f2[18]" in "this.f3[17] = this.f2[18]"
    RTA.set = f3; // inferred from field read f3 in "this.f3[17]" in "this.f3[17] = this.f2[18]"
    ((Object[]) RTA.set)[0] = RTA.set; // inferred for aawrite "this.f3[17]"
    RTA.set = ((Object[]) RTA.set)[0]; // inferred from aaread " this.f2[18]"
  }
}
