package averroes.testsuite.arrays3.input;

public class A {
  private Object[] f1;
  private Object f2;

  public void m1(Object[] a) {
    this.f1 = a;
  }

  public void m2(Object b) {
    this.f2 = b;
  }

  public void m3() {
    this.f1[0] = this.f2;
  }

  public void m4() {
    System.out.println(this.f1[0].toString());
  }

  public Object m5() {
    return this.f1[0];
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
