package averroes.testsuite.arrays3.output.rta;

import java.io.PrintStream;

public class A {
  private Object[] f1; // set for field f1
  private Object f2; // set for field f2

  public A() {
    RTA.set = this; // inferred from implicit "this" parameter
  }

  public void m1(Object[] a) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = a; // inferred for parameter "a"
    f1 = (Object[]) RTA.set; // inferred from field write "this.f1 = .."
  }

  public void m2(Object b) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = b; // inferred for parameter "b"
    f2 = RTA.set; // inferred from field write "this.f2 = .."
  }

  public void m3() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred from field-read "this.f1[0]"
    RTA.set = f2; // inferred from field-read "this.f2"
    ((Object[]) RTA.set)[0] = RTA.set; // inferred from aastore "this.f1[0] = ..."
  }

  public void m4() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred from field-read "this.f1[0]"
    RTA.set = System.out;
    RTA.set = ((Object[]) RTA.set)[0]; // inferred for aaload "this.f1[0]"
    RTA.set.toString(); // inferred from call "this.f2.toString()" to Object.toString()
    ((PrintStream) RTA.set)
        .println(
            (String) RTA.set); // inferred from call "System.out.println(this.f1[0].toString())"
  }

  public Object m5() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred from field-read "this.f1[0]"
    RTA.set = ((Object[]) RTA.set)[0]; // inferred for aaload "this.f1[0]"
    return RTA.set; // inferred from method return type
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
