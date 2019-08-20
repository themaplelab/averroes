package averroes.testsuite.multipleclasses.output.rta;

/**
 * To discuss: can we remove this class B entirely from the RTA output? I think so because there is
 * no way a client could invoke the m5() method on the object, given that B does not inherit from
 * any publicly accessible interface. B would have to be preserved if client code could invoke or
 * override m5().
 *
 * <p>Related question: under which circumstances can we avoid storing classes such as B in RTA.set?
 */
class B {
  private Object f3;

  B(Object o) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = o; // inferred for parameter o

    // field write
    Object obj = RTA.set;
    B b = (B) obj;
    b.f3 = RTA.set;
  }

  public Object m5() {
    RTA.set = this; // inferred for implicit "this" parameter

    // field read
    Object obj = RTA.set;
    B b = (B) obj;
    RTA.set = b.f3;

    return obj; // inferred for return of Object value
  }
}
