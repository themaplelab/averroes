package averroes.testsuite.example.output.cfa;

class List {
  private int size;
  private String[] elements;

  public List() {
    // all arrays will be of size 1
    elements = new String[1];
  }

  public void add(String o) {
    // size is read and written
    // elements.length is only read

    // Extract all expressions from the conditional (already done in Jimple)
    // replace all conditions with CFA.guard

    if (CFA.guard()) {
      growList();
    }

    size++;

    if (CFA.guard()) {
      elements[0] = o;
    }
  }

  public String get(int i) {
    // all array reads are changed to reads of arr[0]
    return elements[0];
  }

  public boolean contains(String o) {
    // remove loops all together
    // extract expressions in if-statements

    // Would this call be optimized away in the bytecode/Jimple?
    elements[0].equals(o);

    // replace if-condition with CFA.guard
    // this conditional can also be optimized away
    return CFA.guard();
  }

  // Discuss with Julian and Ondrej
  private void growList() {
    // allocs
    String[] newElements = new String[1];

    // method calls
    System.arraycopy(elements, 1, newElements, 1, 1);

    // array writes
    elements = newElements;
  }
}
