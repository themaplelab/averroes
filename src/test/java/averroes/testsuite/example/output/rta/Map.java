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

class Map {
  private String[] keys;
  private String[] values;

  public Map() {
    // parameters
    if (RTA.guard) RTA.set = this;

    // allocs
    RTA.set = (String) RTA.set;
    if (RTA.guard) RTA.set = new String[1];
  }

  public void put(String k, String v) {
    // parameters
    if (RTA.guard) RTA.set = this;
    if (RTA.guard) RTA.set = k;
    if (RTA.guard) RTA.set = v;

    RTA.set = keys; // Inferred from read of field keys in "size == keys.length"

    // method calls
    if (RTA.guard) growMap();
    Object obj = RTA.set;
    String str = (String) obj;
    if (RTA.guard) getIndex(str);

    RTA.set = keys; // Inferred from read of field keys in "keys[size] = k"
    RTA.set = values; // Inferred from read of field values in "values[size] = v"

    // array writes
    Object[] arr = (Object[]) obj;
    if (RTA.guard) arr[0] = obj;
  }

  private void growMap() {
    // parameters
    if (RTA.guard) RTA.set = this;

    // allocs
    if (RTA.guard) RTA.set = new String[1];

    RTA.set = keys; // Inferred from read of field keys in "System.arraycopy(keys, 0, newKeys, 0, size)"
    RTA.set = values; // Inferred from read of field values in "System.arraycopy(values, 0, newValues, 0, size)"

    // method calls
    Object obj = RTA.set;
    String[] arr = (String[]) obj;
    if (RTA.guard) System.arraycopy(arr, 1, arr, 1, 1);
    if (RTA.guard) System.arraycopy(arr, 1, arr, 1, 1);
  }

  public Object get(String k) {
    // parameters
    if (RTA.guard) RTA.set = this;
    if (RTA.guard) RTA.set = k;

    // method calls
    Object obj = RTA.set;
    String str = (String) obj;
    if (RTA.guard) getIndex(str);

    RTA.set = values; // Inferred from read of field values in "values[index]"

    // array reads
    Object[] arr = (Object[]) obj;
    if (RTA.guard) RTA.set = arr[0];

    // return
    return obj;
  }

  private int getIndex(String k) {
    // parameters
    if (RTA.guard) RTA.set = this;
    if (RTA.guard) RTA.set = k;

    RTA.set = keys; // Inferred from read of field keys in "keys[i].equals(k)"

    // method calls
    Object obj = RTA.set;
    String str = (String) obj;
    if (RTA.guard) str.equals(str);

    // array reads
    Object[] arr = (Object[]) obj;
    if (RTA.guard) RTA.set = arr[0];

    // return
    return 1;
  }

  public String findKeyFor(String v) {
    // parameters
    if (RTA.guard) RTA.set = this;
    if (RTA.guard) RTA.set = v;

    RTA.set = values; // Inferred from read of field values in "values[i].equals(v)"

    // method calls
    Object obj = RTA.set;
    String str = (String) obj;
    if (RTA.guard) str.equals(str);

    RTA.set = keys; // Inferred from read of field keys in "keys[i]"

    // array reads
    Object[] arr = (Object[]) obj;
    if (RTA.guard) RTA.set = arr[0];

    // return
    return str;
  }
}
