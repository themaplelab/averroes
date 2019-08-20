package averroes.testsuite.example;

import averroes.testsuite.example.input.List;
import averroes.testsuite.example.input.Map;
import averroes.testsuite.example.input.Observer;

/**
 * This example is intended to illustrate how approximating the behavior of the above library
 * classes with different levels of precision affects a client analysis. In the example below, it is
 * assumed that the client is a static taint analysis that tracks simple data flows. Here, the
 * function "secret()" is assumed to be the source of tainted values, and the print statements
 * labeled out1-out5 are the sinks for the taint analysis.
 */
public class Client {
  public static String secret() {
    return "secret";
  }

  public static void main(String[] args) {
    List list = new ObservedList(new PrintObserver());
    list.add("not_secret");

    /* (1) orig/XTA/RTA: untainted, Averroes: tainted */
    /* Averroes is just flow-insensitive, despite adding the guards */
    System.out.println(list.get(0));

    Map m = new Map();
    m.put("aaa", secret());

    /* (2) orig/XTA/RTA/Averroes: tainted */
    // This is the only true positive in the code.
    System.out.println(m.get("aaa"));

    /* (3) orig: untainted, XTA/RTA/Averroes: tainted */
    /*
     * Here the returned value is untainted, but the XTA/RTA/Averroes models
     * are too imprecise to determine that. This case illustrates a
     * situation where XTA is less precise than the original code.
     */
    System.out.println(m.findKeyFor("not_secret"));

    /* (4) orig/XTA: untainted, RTA/Averroes: tainted */
    /*
     * Averroes and RTA use a single location that represents both List and
     * Map, and hence report that the value printed here is tainted. XTA
     * uses separate locations for these classes so that it knows that the
     * printed value is untainted.
     */
    System.out.println(list.get(0));
  }
}

class ObservedList extends List {
  private Observer observer;

  public ObservedList(Observer observer) {
    this.observer = observer;
  }

  public void add(String s) {
    if (observer != null) {
      observer.update(s);
    }
    super.add(s);
  }
}

class PrintObserver implements Observer {
  public void update(Object o) {
    /* (5) Averroes: tainted, orig/XTA/RTA: untainted */
    /* Averroes inserts additional call(s) to Client.update() with tainted value. */
    System.out.println(o);
  }
}
