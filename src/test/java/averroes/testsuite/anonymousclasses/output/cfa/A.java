package averroes.testsuite.anonymousclasses.output.cfa;

import java.util.Iterator;

/**
 * This example uses anonymous classes to implement iterators. (this is a variant of the
 * PrivateClasses example)
 */
public class A {

  private Object key;
  private Object value;

  public void put(Object k, Object v) {
    key = k;
    value = v;
  }

  public Iterator keys() {
    return new Iterator() {

      private Object obj = key;
      private boolean done = false;

      @Override
      public boolean hasNext() {
        return !done;
      }

      @Override
      public Object next() {
        done = true;
        return obj;
      }
    };
  }

  public Iterator values() {
    return new Iterator() {

      private Object obj = value;
      private boolean done = false;

      @Override
      public boolean hasNext() {
        return !done;
      }

      @Override
      public Object next() {
        done = true;
        return obj;
      }
    };
  }
}
