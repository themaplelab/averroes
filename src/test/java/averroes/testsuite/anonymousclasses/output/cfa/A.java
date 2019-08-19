package averroes.testsuite.anonymousclasses.output.cfa;
 

import java.util.Iterator;

/**
 * This example uses anonymous classes to implement iterators.
 * (this is a variant of the PrivateClasses example)  
 *
 */
public class A {
	 
	public void put(Object k, Object v){
		key = k;
		value = v;
	}
	
	public Iterator keys() {
		return new Iterator(){

			@Override
			public boolean hasNext() {
				return !done;
			}

			@Override
			public Object next() {
				done = true;
				return obj;
			}
			
			private Object obj = key;
			private boolean done = false;
		};
	}
	
	public Iterator values() {
		return new Iterator(){

			@Override
			public boolean hasNext() {
				return !done;
			}

			@Override
			public Object next() {
				done = true;
				return obj;
			}
			
			private Object obj = value;
			private boolean done = false;
		};
	}
	
	private Object key;
	private Object value;
}

