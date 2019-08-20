package averroes.testsuite.arrays.output.xta;

public class A {
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;
  private Object[] f1; // objects in f1
  private Object[] f2; // objects in f2
  private Object[] f3; // objects in f3
  public A() {
    set_m0 = this; // inferred from implicit "this" parameter

    // inferred from the creation of array objects for the fields f1, f2, f3
    set_m0 = new Object[1];

    // now assign that array object to the fields
    f1 = (Object[]) set_m0;
    f2 = (Object[]) set_m0;
    f3 = (Object[]) set_m0;
  }

  public void m1(Object a, Object b) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = a; // inferred from parameter a
    set_m1 = b; // inferred from parameter b
    set_m1 = f1; // inferred from read of field f1 in "f1[0] = a"
    set_m1 = f2; // inferred from read of field f2 in "f2[0] = b"
    ((Object[]) set_m1)[0] = set_m1; // inferred from aastores "f1[0] = a" and "f2[0] = b"
  }

  public void m2(Object a) {
    set_m2 = this; // inferred from implicit "this" parameter
    set_m2 = a; // inferred from parameter a
    set_m2 = f3; // inferred from read of field f3 in "f3[0] = a"
    ((Object[]) set_m2)[0] = set_m2; // inferred from aastore "f3[0] = a"
  }

  public Object m3() {
    set_m3 = this; // inferred from implicit "this" parameter
    set_m3 = f1; // inferred from read of field f1 in "f1[0]"
    set_m3 = ((Object[]) set_m3)[0]; // inferred from aaload "f1[0]"
    return set_m3;
  }

  public Object m4() {
    set_m4 = this; // inferred from implicit "this" parameter
    set_m4 = f2; // inferred from read of field f2 in "f2[0]"
    set_m4 = ((Object[]) set_m4)[0]; // inferred from aaload "f2[0]"
    return set_m4;
  }

  public Object m5() {
    set_m5 = this; // inferred from implicit "this" parameter
    set_m5 = f3; // inferred from read of field f3 in "f3[0]"
    set_m5 = ((Object[]) set_m5)[0]; // inferred from aaload "f3[0]"
    return set_m5;
  }
}
