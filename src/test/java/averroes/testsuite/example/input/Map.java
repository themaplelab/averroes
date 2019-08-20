package averroes.testsuite.example.input;

public class Map {
  private int size = 0;
  private String[] keys;
  private String[] values;

  public Map() {
    keys = new String[10];
    values = new String[10];
  }

  public void put(String k, String v) {
    int index = getIndex(k);
    if (index != -1) {
      values[index] = v;
    } else {
      if (size == keys.length) {
        growMap();
      }
      keys[size] = k;
      values[size] = v;
      size++;
    }
  }

  private void growMap() {
    String[] newKeys = new String[size * 2];
    String[] newValues = new String[size * 2];
    System.arraycopy(keys, 0, newKeys, 0, size);
    System.arraycopy(values, 0, newValues, 0, size);
    keys = newKeys;
    values = newValues;
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

  public String findKeyFor(String v) {
    for (int i = 0; i < size; i++) {
      if (values[i].equals(v)) {
        return keys[i];
      }
    }
    return null;
  }
}
