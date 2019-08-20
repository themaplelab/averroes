package averroes.testsuite.privateclasses.output.rta;

import java.util.Iterator;

/** This example uses a private inner class to implement an iterator. */
public class A {

  private Object key;
  private Object value;

  public A() {
    RTA.set = this;
  }

  public void put(Object k, Object v) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = k; // inferred for parameter "k"
    RTA.set = v; // inferred for parameter "v"

    // field writes
    Object obj = RTA.set;
    A a = (A) obj;
    a.key = obj;
    a.value = obj;
  }

  public Iterator keys() {
    RTA.set = this; // inferred for implicit "this" parameter

    // method calls
    Object obj = RTA.set;
    RTA.set = new It(obj); // inferred for allocation "new It(key)"

    // field reads
    RTA.set = ((A) obj).key;

    return (Iterator) obj; // inferred from return type
  }

  public Iterator values() {
    RTA.set = this; // inferred for implicit "this" parameter

    // method calls
    Object obj = RTA.set;
    RTA.set = new It(obj); // inferred for allocation "new It(key)"

    // field reads
    RTA.set = ((A) obj).value;

    return (Iterator) obj; // inferred from return type
  }

  private static class It implements Iterator {

    private Object obj;
    private boolean done;

    It(Object o) {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = o; // inferred for parameter "o"
      // no need to generate anything for "done = false;"

      // field writes
      Object obj = RTA.set;
      ((It) obj).obj = obj;
    }

    @Override
    public boolean hasNext() {
      RTA.set = this; // inferred for implicit "this" parameter
      return true; // inferred from return type
    }

    @Override
    public Object next() {
      RTA.set = this; // inferred for implicit "this" parameter
      // no need to generate anything for "done = true;"

      // field reads
      Object obj = RTA.set;
      RTA.set = ((It) obj).obj;

      return obj; // inferred from return type
    }
  }
}
