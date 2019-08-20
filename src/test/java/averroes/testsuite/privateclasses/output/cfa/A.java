package averroes.testsuite.privateclasses.output.cfa;

import java.util.Iterator;

/**
 * This example uses a private inner class to implement
 * an iterator.  
 *
 */
public class A {
	
	private static class It implements Iterator {

		It(Object o){
			obj = o;
			done = false;
		}
		
		@Override
		public boolean hasNext() { 
			return !done;
		}

		@Override
		public Object next() { 
			done = true;
			return obj;
		}
		
		private Object obj;
		private boolean done;
		
	}
	
	public void put(Object k, Object v){
		key = k;
		value = v;
	}
	
	public Iterator keys() {
		return new It(key);
	}
	
	public Iterator values() {
		return new It(value);
	}
	
	private Object key;
	private Object value;
}
