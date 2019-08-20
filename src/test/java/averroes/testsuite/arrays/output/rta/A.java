package averroes.testsuite.arrays.output.rta;

public class A {
  private Object[] f1; // objects in f1
  private Object[] f2; // objects in f2
  private Object[] f3; // objects in f3

  public A() {
    RTA.set = this; // inferred from implicit "this" parameter

    // inferred from the creation of array objects for the fields f1, f2, f3
    RTA.set = new Object[1];

    // now assign that array object to the fields
    f1 = (Object[]) RTA.set;
    f2 = (Object[]) RTA.set;
    f3 = (Object[]) RTA.set;
  }

  public void m1(Object a, Object b) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = a; // inferred for parameter "a"
    RTA.set = b; // inferred for parameter "b"
    RTA.set = f1; // inferred from read of field f1 in "f1[0] = a"
    RTA.set = f2; // inferred from read of field f2 in "f2[0] = b"
    ((Object[]) RTA.set)[0] = RTA.set; // inferred from aastores "f1[0] = a"
    // and "f2[0] = b"
  }

  public void m2(Object a) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = a; // inferred for parameter "a"
    RTA.set = f3; // inferred from read of field f3 in "f3[0] = a"
    ((Object[]) RTA.set)[0] = RTA.set; // inferred from aastore "f3[0] = a"
  }

  public Object m3() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f1; // inferred from read of field f1 in "f1[0]"
    RTA.set = ((Object[]) RTA.set)[0]; // inferred from aaload "f1[0]"
    return RTA.set; // inferred from return type
  }

  public Object m4() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f2; // inferred from read of field f2 in "f2[0]"
    RTA.set = ((Object[]) RTA.set)[0]; // inferred from aaload "f2[0]"
    return RTA.set; // inferred from return type
  }

  public Object m5() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = f3; // inferred from read of field f3 in "f3[0]"
    RTA.set = ((Object[]) RTA.set)[0]; // inferred from aaload "f3[0]"
    return RTA.set; // inferred from return type
  }
}
