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

class Map {
  private String[] keys;
  private String[] values;
  private Object set_m0;
  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;

  public Map() {
    // parameters
    if (XTA.guard) set_m0 = this;

    // allocs
    if (XTA.guard) set_m0 = new String[1];

    // field writes
    Object obj = set_m0;
    String[] arr = (String[]) obj;
    Map map = (Map) obj;
    if (XTA.guard) map.keys = arr;
    if (XTA.guard) map.values = arr;
  }

  public void put(String k, String v) {
    // parameters
    if (XTA.guard) set_m1 = this;
    if (XTA.guard) set_m1 = k;
    if (XTA.guard) set_m1 = v;

    // method calls
    if (XTA.guard) growMap();
    Object obj = set_m1;
    String str = (String) obj;
    if (XTA.guard) getIndex(str);

    // array writes
    Object[] arr = (Object[]) obj;
    if (XTA.guard) arr[0] = obj;

    // field reads
    Map map = (Map) obj;
    if (XTA.guard) set_m1 = map.values;
    if (XTA.guard) set_m1 = map.keys;
  }

  private void growMap() {
    // parameters
    if (XTA.guard) set_m2 = this;

    // allocs
    if (XTA.guard) set_m2 = new String[1];

    // method calls
    Object obj = set_m2;
    String[] arr = (String[]) obj;
    if (XTA.guard) System.arraycopy(arr, 1, arr, 1, 1);
    if (XTA.guard) System.arraycopy(arr, 1, arr, 1, 1);

    // field reads
    Map map = (Map) obj;
    if (XTA.guard) set_m2 = map.keys;
    if (XTA.guard) set_m2 = map.values;

    // field writes
    if (XTA.guard) map.keys = arr;
    if (XTA.guard) map.values = arr;
  }

  public Object get(String k) {
    // parameters
    if (XTA.guard) set_m3 = this;
    if (XTA.guard) set_m3 = k;

    // method calls
    Object obj = set_m3;
    if (XTA.guard) getIndex((String) obj);

    // array reads
    Object[] arr = (Object[]) obj;
    if (XTA.guard) set_m3 = arr[0];

    // field reads
    Map map = (Map) obj;
    if (XTA.guard) set_m3 = map.values;

    // return
    return obj;
  }

  private int getIndex(String k) {
    // parameters
    if (XTA.guard) set_m4 = this;
    if (XTA.guard) set_m4 = k;

    // method calls
    Object obj = set_m4;
    String str = (String) obj;
    if (XTA.guard) str.equals(str);

    // array reads
    Object[] arr = (Object[]) obj;
    if (XTA.guard) set_m4 = arr[0];

    // field reads
    Map map = (Map) obj;
    if (XTA.guard) set_m4 = map.keys;

    // return
    return 1;
  }

  public String findKeyFor(String v) {
    // parameters
    if (XTA.guard) set_m5 = this;
    if (XTA.guard) set_m5 = v;

    // method calls
    Object obj = set_m5;
    String str = (String) obj;
    if (XTA.guard) str.equals(str);

    // array reads
    Object[] arr = (Object[]) obj;
    if (XTA.guard) set_m5 = arr[0];

    // field reads
    Map map = (Map) obj;
    if (XTA.guard) set_m5 = map.keys;
    if (XTA.guard) set_m5 = map.values;

    // return
    return str;
  }
}
