package averroes.testsuite.example.input;

public class List {
  private int size;
  private String[] elements;
  private String[] bla;

  public List() {
    elements = new String[10];
    bla = new String[10];
  }

  public void add(String o) {
    if (size == elements.length) {
      growList();
    }
    elements[size++] = o;
  }

  public String get(int i) {
    return elements[i];
  }

  public boolean contains(String o) {
    for (int i = 0; i < size; i++) {
      if (elements[i].equals(o)) {
        return true;
      }
    }
    return false;
  }

  private void growList() {
    String[] newElements = new String[size * 2];
    System.arraycopy(elements, 0, newElements, 0, size);
    elements = newElements;
  }

  public int size() {
    return size;
  }
}
