package averroes.testsuite.anonymousclasses.output.xta;

import java.util.Iterator;

/**
 * This example uses anonymous classes to implement iterators. (this is a variant of the
 * PrivateClasses example)
 */
public class A {
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object key; // set for field key
  private Object value; // set for field value
  public A() {
    set_m0 = this; // for any instance method, assign "this" into the defining method's set
  }

  public void put(Object k, Object v) {
    set_m1 = this; // for any instance method, assign "this" into the defining method's set
    set_m1 = k; // inferred for parameter k
    set_m1 = v; // inferred for parameter v

    Object set = set_m1;
    A a = (A) set;
    a.key = set; // inferred for field write to "put"
    a.value = set; // inferred for field write to "value"
  }

  public Iterator keys() {
    set_m2 = this; // inferred from implicit "this" parameter

    set_m2 =
        new Iterator() { // inferred from allocation
          private Object set_m0;
          private Object set_m1;
          private Object set_m2;
          private boolean done = false;
          // weirdly, static fields are not allowed in non-static nested classes (and hence
          // anonymous classes)
          private /*static*/ Object obj; // set for field obj

          {
            set_m0 = this;
            set_m0 = A.this;
            obj = ((A) set_m0).key;
          }

          @Override
          public boolean hasNext() {
            set_m1 = this; // inferred from implicit "this" parameter
            set_m1 = A.this; // inferred from implicit "this" of outer class A
            return true; // inferred from return type
            // nothing generated for read of primitive field "done" in "return !done;"
          }

          @Override
          public Object next() {
            set_m2 = this; // inferred from implicit "this" parameter
            set_m2 = A.this; // inferred from implicit "this" of outer class A
            // nothing generated for write to primitive field "done" in "done = true;"

            // field reads
            Object o = set_m2;
            set_m2 = obj;
            return o; // inferred from return type
          }
        };

    return (Iterator) set_m2; // inferred from return type
  }

  public Iterator values() {
    set_m3 = this; // inferred from implicit "this" parameter

    set_m3 =
        new Iterator() { // inferred from allocation

          private Object set_m0;
          private Object set_m1;
          private Object set_m2;
          private boolean done = false;
          // weirdly, static fields are not allowed in non-static nested classes (and hence
          // anonymous classes)
          private /*static*/ Object obj; // set for field obj

          {
            set_m0 = this;
            set_m0 = A.this;
            obj = ((A) set_m0).value;
          }

          @Override
          public boolean hasNext() {
            set_m1 = this; // inferred from implicit "this" parameter
            set_m1 = A.this; // inferred from implicit "this" of outer class A
            return true; // inferred from return type
            // nothing generated for read of primitive field "done" in "return !done;"
          }

          @Override
          public Object next() {
            set_m2 = this; // inferred from implicit "this" parameter
            set_m2 = A.this; // inferred from implicit "this" of outer class A
            // nothing generated for write to primitive field "done" in "done = true;"

            // field reads
            Object o = set_m2;
            set_m2 = obj;
            return o; // inferred from return type
          }
        };
    return (Iterator) set_m3; // inferred from return type
  }
}
