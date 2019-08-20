package averroes.testsuite.multipleclasses.output.rta;

/**
 * To discuss: can we remove this class C entirely from the RTA output? I think so because there is
 * no way a client could invoke the m6() method on the object, given that C does not inherit from
 * any publicly accessible interface. C would have to be preserved if client code could invoke or
 * override m6().
 *
 * <p>Related question: under which circumstances can we avoid storing classes such as C in RTA.set?
 */
class C {
  private Object f4;
  private Object f5;

  C(Object y, Object z) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = y; // inferred for parameter y
    RTA.set = z; // inferred for parameter z

    // field writes
    Object obj = RTA.set;
    C c = (C) obj;
    c.f4 = obj;
    c.f5 = obj;
  }

  public boolean m6() {
    RTA.set = this; // inferred for implicit "this" parameter

    // field read
    Object obj = RTA.set;
    C c = (C) obj;
    RTA.set = c.f4;
    RTA.set = c.f5;

    obj.equals(obj); // inferred for cal to "equals"
    return true; // inferred for return of primitive boolean value
  }
}
