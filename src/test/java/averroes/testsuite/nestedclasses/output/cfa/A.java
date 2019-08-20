package averroes.testsuite.nestedclasses.output.cfa;

public class A {

	/* non-static nested class */
	class B {
		void m1(){
			f1 = A.this; /* read this from outer class */
		}
		A m2(){
			return f1;
		}
		Object m3(){
			return f5; /* read field from outer class */
		}
		 
		private A f1; 
	}
	
	/* static nested class */
	static class C {
		void m4(Object o){
			f2 = o;
		}
		Object m5(){
			return f2;
		}
		
		private Object f2;
	}
	
	public void m6(){
		f3 = new B(); 
		f4 = new String();
	}
	public void m7(){
		f5 = new C();
	}
	public B m8(){
		return f3;
	} 
	
	private B f3;
	private Object f4;
	private C f5;
}
