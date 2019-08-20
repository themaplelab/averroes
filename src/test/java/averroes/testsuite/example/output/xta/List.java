package averroes.testsuite.example.output.xta;

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
  private int size;
  private String[] elements;
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;

  public List() {
    // parameters
    if (XTA.guard) set_m0 = this;

    // allocs
    if (XTA.guard) set_m0 = new String[1];

    // field writes
    Object obj = set_m0;
    String[] rhs = (String[]) obj;
    List l = (List) obj;
    if (XTA.guard) l.elements = rhs;
  }

  public void add(String o) {
    // parameters
    if (XTA.guard) set_m1 = this;
    if (XTA.guard) set_m1 = o;

    // methods calls
    if (XTA.guard) growList();

    // array writes
    Object obj = set_m1;
    Object[] arr = (Object[]) obj;
    if (XTA.guard) arr[0] = obj;

    // field reads
    List l = (List) obj;
    if (XTA.guard) set_m1 = l.elements;
  }

  public String get(int i) {
    // parameters
    if (XTA.guard) set_m2 = this;

    // array reads
    Object obj = set_m2;
    Object[] arr = (Object[]) obj;
    if (XTA.guard) set_m2 = arr[0];

    // field reads
    List l = (List) obj;
    if (XTA.guard) set_m2 = l.elements;

    // return
    String str = (String) obj;
    return str;
  }

  public boolean contains(String o) {
    // parameters
    if (XTA.guard) set_m3 = this;
    if (XTA.guard) set_m3 = o;

    // method calls
    Object obj = set_m3;
    String str = (String) obj;
    if (XTA.guard) str.equals(str);

    // array reads
    Object[] arr = (Object[]) obj;
    if (XTA.guard) set_m3 = arr[0];

    // field reads
    List l = (List) obj;
    if (XTA.guard) set_m3 = l.elements;

    return true;
  }

  private void growList() {
    // parameters
    if (XTA.guard) set_m4 = this;
    if (XTA.guard) set_m4 = new String[1];

    // method calls
    Object obj = set_m4;
    String[] arr = (String[]) obj;
    if (XTA.guard) System.arraycopy(arr, 1, arr, 1, 1);

    // field reads
    List l = (List) obj;
    if (XTA.guard) set_m4 = l.elements;

    // field writes
    if (XTA.guard) l.elements = arr;
  }

  public int size() {
    // parameters
    if (XTA.guard) set_m5 = this;
    return 1;
  }
}
