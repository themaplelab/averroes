package averroes.testsuite.example.output.rta;

/* That's the order that Averroes generates the code. */
// parameters
// allocs
// method calls
// array reads
// array writes
// field reads
// field writes
// exceptions
// return

class List {
  public List() {
    // parameters
    if (RTA.guard) RTA.set = this;

    // allocs
    if (RTA.guard) RTA.set = new String[1];
  }

  public void add(String o) {
    // parameters
    if (RTA.guard) RTA.set = this;
    if (RTA.guard) RTA.set = o;

    // methods calls
    if (RTA.guard) growList();

    // array writes
    Object obj = RTA.set;
    Object[] arr = (Object[]) obj;
    if (RTA.guard) arr[0] = obj;
  }

  public String get(int i) {
    // parameters
    if (RTA.guard) RTA.set = this;

    // array reads
    Object obj = RTA.set;
    if (RTA.guard) RTA.set = ((Object[]) obj)[0];

    return (String) obj;
  }

  public boolean contains(String o) {
    // parameters
    if (RTA.guard) RTA.set = this;
    if (RTA.guard) RTA.set = o;

    // method calls
    Object obj = RTA.set;
    String str = (String) obj;
    if (RTA.guard) str.equals(str);

    // array reads
    if (RTA.guard) RTA.set = ((Object[]) obj)[0];

    return true;
  }

  private void growList() {
    // parameters
    if (RTA.guard) RTA.set = this;
    if (RTA.guard) RTA.set = new String[1];

    // method calls
    Object obj = RTA.set;
    String[] arr = (String[]) obj;
    if (RTA.guard) System.arraycopy(arr, 1, arr, 1, 1);
  }

  public int size() {
    // parameters
    if (RTA.guard) RTA.set = this;
    return 1;
  }
}
