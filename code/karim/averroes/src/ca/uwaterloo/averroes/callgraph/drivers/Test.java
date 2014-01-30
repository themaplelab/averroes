package ca.uwaterloo.averroes.callgraph.drivers;

import java.util.Arrays;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		List<A> list = Arrays.asList(new A(), new B(), new C(), new D());

		for (A temp : list) {
			A a = (A) temp;
			System.out.println(a.getClass());
			a.foo();
		}

	}
}

class A {
	public void foo() {
		System.out.println("A.foo");
	}
}

class B extends A {
	public void foo() {
		System.out.println("B.foo");
	}
}

class C extends A {
}

class D extends A {
	public void foo() {
		System.out.println("D.foo");
	}
}