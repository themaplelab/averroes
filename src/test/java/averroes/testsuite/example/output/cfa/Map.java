package averroes.testsuite.example.output.cfa;

class Map {
  private int size = 0;
  private String[] keys;
  private String[] values;

  public Map() {
    keys = new String[1];
    values = new String[1];
  }

  public void put(String k, String v) {
    getIndex(k);

    if (CFA.guard()) {
      values[0] = v;
    }

    if (CFA.guard()) {
      growMap();
    }

    if (CFA.guard()) {
      keys[0] = k;
      values[0] = v;
      size++;
    }
  }

  private void growMap() {
    if (CFA.guard()) {
      String[] newKeys = new String[1];
      String[] newValues = new String[1];
      System.arraycopy(keys, 1, newKeys, 1, 1);
      System.arraycopy(values, 1, newValues, 1, 1);
      keys = newKeys;
      values = newValues;
    }
  }

  public Object get(String k) {
    int index = getIndex(k);
    if (index != -1) {
      return values[index];
    } else {
      return null;
    }
  }

  private int getIndex(String k) {
    for (int i = 0; i < size; i++) {
      if (keys[i].equals(k)) {
        return i;
      }
    }
    return -1;
  }

  public boolean containsKey(String k) {
    return getIndex(k) != -1;
  }

  public String[] keys() {
    return keys;
  }

  public String[] values() {
    return values;
  }
}
