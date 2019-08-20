package averroes.testsuite.anonymousclasses.output.rta;

import java.util.Iterator;

/**
 * This example uses anonymous classes to implement iterators. (this is a variant of the
 * PrivateClasses example)
 */
public class A {

  private Object key;
  private Object value;

  public A() {
    RTA.set = this; // inferred for implicit "this" parameter
  }

  public void put(Object k, Object v) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = k; // inferred for parameter "k"
    RTA.set = v; // inferred for parameter "v"

    Object obj = RTA.set;
    A a = (A) obj;
    a.key = obj;
    a.value = obj;
  }

  public Iterator keys() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set =
        new Iterator() { // inferred for allocation "new Iterator(){ ... }"

          private boolean done = false;
          private Object obj;

          {
            RTA.set = this;
            RTA.set = A.this;
            obj = ((A) RTA.set).key;
          }

          @Override
          public boolean hasNext() {
            RTA.set = this; // inferred for implicit "this" parameter
            RTA.set = A.this; // inferred for implicit "this" parameter of outer class A
            return true; // inferred from return type
          }

          @Override
          public Object next() {
            RTA.set = this; // inferred for implicit "this" parameter
            RTA.set = A.this; // inferred for implicit "this" parameter of outer class A
            // nothing generated for "done = true;"

            // field reads
            Object o = RTA.set;
            RTA.set = obj;
            return o; // inferred from return type
          }
        };
    return (Iterator) RTA.set; // inferred from return type
  }

  public Iterator values() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set =
        new Iterator() { // inferred for allocation "new Iterator(){ ... }"

          private boolean done = false;
          private Object obj;

          {
            RTA.set = this;
            RTA.set = A.this;
            obj = ((A) RTA.set).value;
          }

          @Override
          public boolean hasNext() {
            RTA.set = this; // inferred for implicit "this" parameter
            RTA.set = A.this; // inferred for implicit "this" parameter of outer class A
            return true; // inferred from return type
          }

          @Override
          public Object next() {
            RTA.set = this; // inferred for implicit "this" parameter
            RTA.set = A.this; // inferred for implicit "this" parameter of outer class A
            // nothing generated for "done = true;"

            // field reads
            Object o = RTA.set;
            RTA.set = obj;
            return o; // inferred from return type
          }
        };
    return (Iterator) RTA.set; // inferred from return type
  }
}
