package averroes.testsuite.arrays3.output.xta;

import java.io.PrintStream;

public class A {
  @SuppressWarnings("unused")
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;
  private Object[] f1; // set for field f1
  private Object f2; // set for field f2
  public A() {
    set_m0 = this; // inferred from implicit "this" parameter
  }

  public void m1(Object[] a) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = a; // inferred from parameter "a"
    f1 = (Object[]) set_m1; // inferred from field write "this.f1 = .."
  }

  public void m2(Object b) {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = b; // inferred from parameter "b"
    f2 = set_m2; // inferred from field write "this.f2 = .."
  }

  public void m3() {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = f1; // inferred from field-read "this.f1[0]"
    set_m3 = f2; // inferred from field-read "this.f2"
    ((Object[]) set_m3)[0] = set_m3; // inferred from aastore "this.f1[0] = .."
  }

  public void m4() {
    set_m4 = this; // inferred from implicit "this" parameter
    set_m4 = f1; // inferred from field-read "this.f1[0]"
    set_m4 = System.out;
    set_m4 = ((Object[]) set_m4)[0]; // inferred from aaload "this.f1[0]"
    set_m4.toString(); // inferred from call "this.f1[0].toString()"
    ((PrintStream) set_m4)
        .println((String) set_m4); // inferred from call "System.out.println(this.f1[0].toString())"
  }

  public Object m5() {
    set_m5 = this; // inferred from implicit "this" parameter
    set_m5 = f1; // inferred from field-read "this.f1[0]"
    set_m5 = ((Object[]) set_m5)[0]; // inferred from aaload "this.f1[0]"
    return set_m5; // inferred from return type
  }
}

/*
class Client {
	public void foo(A a){
		a.m1(new Object[10]);
		a.m2(new X());
		a.m3();
		a.m4();
	}
}

class X {
	@Override
	public String toString(){ return "X"; }
}
*/
