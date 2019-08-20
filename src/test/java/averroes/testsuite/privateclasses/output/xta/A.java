package averroes.testsuite.privateclasses.output.xta;

import java.util.Iterator;

/** This example uses a private inner class to implement an iterator. */
public class A {

  private Object key; // set for field key
  private Object value; // set for field value
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  public A() {
    set_m0 = this;
  }

  public void put(Object k, Object v) {
    set_m1 = this; // inferred from implicit "this" parameter
    set_m1 = k; // inferred for parameter k
    set_m1 = v; // inferred for parameter v

    Object set = set_m1;
    key = set; // inferred for field write to "put"
    value = set; // inferred for field write to "value"
  }

  public Iterator keys() {
    set_m2 = this; // inferred from implicit "this" parameter

    Object set = set_m2;
    set_m2 = new It(set); // inferred from allocation "new It(key)"

    set_m2 = key; // inferred for field read from "key"

    return (Iterator) set; // inferred from return type
  }

  public Iterator values() {
    set_m3 = this; // inferred from implicit "this" parameter

    Object set = set_m3;
    set_m3 = new It(set); // inferred from allocation "new It(value)"

    set_m3 = value; // inferred for field read from "value"

    return (Iterator) set; // inferred from return type
  }

  private static class It implements Iterator {

    private Object obj; // set for field obj
    private Object set_m0;
    private Object set_m1;
    private Object set_m2;
    // no field generated for primitive-typed field "private boolean done;"

    It(Object o) {
      set_m0 = this; // inferred from implicit "this" parameter
      set_m0 = o; // inferred for parameter o
      obj = set_m0; // inferred for field write "obj = o"
      // nothing generated for "done = false;" assuming we don't track
      // primitive values
    }

    @Override
    public boolean hasNext() {
      set_m1 = this; // inferred from implicit "this"
      // parameter
      return true; // inferred from return type
      // nothing generated for field read "!done;"
    }

    @Override
    public Object next() {
      set_m2 = this; // inferred from implicit "this" parameter
      set_m2 = obj; // inferred for field read from "obj"
      // nothing generated for field write "done = true;"
      return set_m2;
    }
  }
}
