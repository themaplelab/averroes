package averroes.testsuite.ll.input;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * LinkedList from the java.util.collections framework. Simplified by eliminating superclasses,
 * removing uses of generics. Copied modCount field and a few iterator methods from abstract
 * superclasses to make this code self-contained.
 */
public class LL implements Iterable {
  private static final long serialVersionUID = 876323262645176354L;
  protected int modCount = 0;
  int size = 0;
  Node first;
  Node last;

  public LL() {}

  public LL(Collection c) {
    this();
    addAll(c);
  }

  public Iterator iterator() {
    return listIterator();
  }

  public ListIterator listIterator() {
    return listIterator(0);
  }

  private void linkFirst(Object e) {
    final Node f = first;
    final Node newNode = new Node(null, e, f);
    first = newNode;
    if (f == null) last = newNode;
    else f.prev = newNode;
    size++;
    modCount++;
  }

  void linkLast(Object e) {
    final Node l = last;
    final Node newNode = new Node(l, e, null);
    last = newNode;
    if (l == null) first = newNode;
    else l.next = newNode;
    size++;
    modCount++;
  }

  void linkBefore(Object e, Node succ) {
    // assert succ != null;
    final Node pred = succ.prev;
    final Node newNode = new Node(pred, e, succ);
    succ.prev = newNode;
    if (pred == null) first = newNode;
    else pred.next = newNode;
    size++;
    modCount++;
  }

  private Object unlinkFirst(Node f) {
    // assert f == first && f != null;
    final Object element = f.item;
    final Node next = f.next;
    f.item = null;
    f.next = null; // help GC
    first = next;
    if (next == null) last = null;
    else next.prev = null;
    size--;
    modCount++;
    return element;
  }

  private Object unlinkLast(Node l) {
    // assert l == last && l != null;
    final Object element = l.item;
    final Node prev = l.prev;
    l.item = null;
    l.prev = null; // help GC
    last = prev;
    if (prev == null) first = null;
    else prev.next = null;
    size--;
    modCount++;
    return element;
  }

  Object unlink(Node x) {
    // assert x != null;
    final Object element = x.item;
    final Node next = x.next;
    final Node prev = x.prev;

    if (prev == null) {
      first = next;
    } else {
      prev.next = next;
      x.prev = null;
    }

    if (next == null) {
      last = prev;
    } else {
      next.prev = prev;
      x.next = null;
    }

    x.item = null;
    size--;
    modCount++;
    return element;
  }

  public Object getFirst() {
    final Node f = first;
    if (f == null) throw new NoSuchElementException();
    return f.item;
  }

  public Object getLast() {
    final Node l = last;
    if (l == null) throw new NoSuchElementException();
    return l.item;
  }

  public Object removeFirst() {
    final Node f = first;
    if (f == null) throw new NoSuchElementException();
    return unlinkFirst(f);
  }

  public Object removeLast() {
    final Node l = last;
    if (l == null) throw new NoSuchElementException();
    return unlinkLast(l);
  }

  public void addFirst(Object e) {
    linkFirst(e);
  }

  public void addLast(Object e) {
    linkLast(e);
  }

  public boolean contains(Object o) {
    return indexOf(o) != -1;
  }

  public int size() {
    return size;
  }

  public boolean add(Object e) {
    linkLast(e);
    return true;
  }

  public boolean remove(Object o) {
    if (o == null) {
      for (Node x = first; x != null; x = x.next) {
        if (x.item == null) {
          unlink(x);
          return true;
        }
      }
    } else {
      for (Node x = first; x != null; x = x.next) {
        if (o.equals(x.item)) {
          unlink(x);
          return true;
        }
      }
    }
    return false;
  }

  public boolean addAll(Collection c) {
    return addAll(size, c);
  }

  public boolean addAll(int index, Collection c) {
    checkPositionIndex(index);

    Object[] a = c.toArray();
    int numNew = a.length;
    if (numNew == 0) return false;

    Node pred, succ;
    if (index == size) {
      succ = null;
      pred = last;
    } else {
      succ = node(index);
      pred = succ.prev;
    }

    for (Object o : a) {
      @SuppressWarnings("unchecked")
      Object e = o;
      Node newNode = new Node(pred, e, null);
      if (pred == null) first = newNode;
      else pred.next = newNode;
      pred = newNode;
    }

    if (succ == null) {
      last = pred;
    } else {
      pred.next = succ;
      succ.prev = pred;
    }

    size += numNew;
    modCount++;
    return true;
  }

  public void clear() {
    // Clearing all of the links between nodes is "unnecessary", but:
    // - helps a generational GC if the discarded nodes inhabit
    //   more than one generation
    // - is sure to free memory even if there is a reachable Iterator
    for (Node x = first; x != null; ) {
      Node next = x.next;
      x.item = null;
      x.next = null;
      x.prev = null;
      x = next;
    }
    first = last = null;
    size = 0;
    modCount++;
  }

  public Object get(int index) {
    checkElementIndex(index);
    return node(index).item;
  }

  public Object set(int index, Object element) {
    checkElementIndex(index);
    Node x = node(index);
    Object oldVal = x.item;
    x.item = element;
    return oldVal;
  }

  public void add(int index, Object element) {
    checkPositionIndex(index);

    if (index == size) linkLast(element);
    else linkBefore(element, node(index));
  }

  public Object remove(int index) {
    checkElementIndex(index);
    return unlink(node(index));
  }

  private boolean isElementIndex(int index) {
    return index >= 0 && index < size;
  }

  private boolean isPositionIndex(int index) {
    return index >= 0 && index <= size;
  }

  private String outOfBoundsMsg(int index) {
    return "Index: " + index + ", Size: " + size;
  }

  private void checkElementIndex(int index) {
    if (!isElementIndex(index)) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
  }

  private void checkPositionIndex(int index) {
    if (!isPositionIndex(index)) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
  }

  Node node(int index) {
    // assert isElementIndex(index);

    if (index < (size >> 1)) {
      Node x = first;
      for (int i = 0; i < index; i++) x = x.next;
      return x;
    } else {
      Node x = last;
      for (int i = size - 1; i > index; i--) x = x.prev;
      return x;
    }
  }

  public int indexOf(Object o) {
    int index = 0;
    if (o == null) {
      for (Node x = first; x != null; x = x.next) {
        if (x.item == null) return index;
        index++;
      }
    } else {
      for (Node x = first; x != null; x = x.next) {
        if (o.equals(x.item)) return index;
        index++;
      }
    }
    return -1;
  }

  public int lastIndexOf(Object o) {
    int index = size;
    if (o == null) {
      for (Node x = last; x != null; x = x.prev) {
        index--;
        if (x.item == null) return index;
      }
    } else {
      for (Node x = last; x != null; x = x.prev) {
        index--;
        if (o.equals(x.item)) return index;
      }
    }
    return -1;
  }

  public Object peek() {
    final Node f = first;
    return (f == null) ? null : f.item;
  }

  public Object element() {
    return getFirst();
  }

  public Object poll() {
    final Node f = first;
    return (f == null) ? null : unlinkFirst(f);
  }

  public Object remove() {
    return removeFirst();
  }

  public boolean offer(Object e) {
    return add(e);
  }

  public boolean offerFirst(Object e) {
    addFirst(e);
    return true;
  }

  public boolean offerLast(Object e) {
    addLast(e);
    return true;
  }

  public Object peekFirst() {
    final Node f = first;
    return (f == null) ? null : f.item;
  }

  public Object peekLast() {
    final Node l = last;
    return (l == null) ? null : l.item;
  }

  public Object pollFirst() {
    final Node f = first;
    return (f == null) ? null : unlinkFirst(f);
  }

  public Object pollLast() {
    final Node l = last;
    return (l == null) ? null : unlinkLast(l);
  }

  public void push(Object e) {
    addFirst(e);
  }

  public Object pop() {
    return removeFirst();
  }

  public boolean removeFirstOccurrence(Object o) {
    return remove(o);
  }

  public boolean removeLastOccurrence(Object o) {
    if (o == null) {
      for (Node x = last; x != null; x = x.prev) {
        if (x.item == null) {
          unlink(x);
          return true;
        }
      }
    } else {
      for (Node x = last; x != null; x = x.prev) {
        if (o.equals(x.item)) {
          unlink(x);
          return true;
        }
      }
    }
    return false;
  }

  public ListIterator listIterator(int index) {
    checkPositionIndex(index);
    return new ListItr(index);
  }

  public Iterator descendingIterator() {
    return new DescendingIterator();
  }

  private LL superClone() {
    try {
      return (LL) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public Object clone() {
    LL clone = superClone();

    // Put clone into "virgin" state
    clone.first = clone.last = null;
    clone.size = 0;
    clone.modCount = 0;

    // Initialize clone with our elements
    for (Node x = first; x != null; x = x.next) clone.add(x.item);

    return clone;
  }

  public Object[] toArray() {
    Object[] result = new Object[size];
    int i = 0;
    for (Node x = first; x != null; x = x.next) result[i++] = x.item;
    return result;
  }

  public Object[] toArray(Object[] a) {
    if (a.length < size)
      a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
    int i = 0;
    Object[] result = a;
    for (Node x = first; x != null; x = x.next) result[i++] = x.item;

    if (a.length > size) a[size] = null;

    return a;
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    // Write out any hidden serialization magic
    s.defaultWriteObject();

    // Write out size
    s.writeInt(size);

    // Write out all elements in the proper order.
    for (Node x = first; x != null; x = x.next) s.writeObject(x.item);
  }

  private void readObject(java.io.ObjectInputStream s)
      throws java.io.IOException, ClassNotFoundException {
    // Read in any hidden serialization magic
    s.defaultReadObject();

    // Read in size
    int size = s.readInt();

    // Read in all elements in the proper order.
    for (int i = 0; i < size; i++) linkLast(s.readObject());
  }

  public Spliterator spliterator() {
    return new LLSpliterator(this, -1, 0);
  }

  private static class Node {
    Object item;
    Node next;
    Node prev;

    Node(Node prev, Object element, Node next) {
      this.item = element;
      this.next = next;
      this.prev = prev;
    }
  }

  static final class LLSpliterator implements Spliterator {
    static final int BATCH_UNIT = 1 << 10; // batch array size increment
    static final int MAX_BATCH = 1 << 25; // max batch array size;
    final LL list; // null OK unless traversed
    Node current; // current node; null until initialized
    int est; // size estimate; -1 until first needed
    int expectedModCount; // initialized when est set
    int batch; // batch size for splits

    LLSpliterator(LL list, int est, int expectedModCount) {
      this.list = list;
      this.est = est;
      this.expectedModCount = expectedModCount;
    }

    final int getEst() {
      int s; // force initialization
      final LL lst;
      if ((s = est) < 0) {
        if ((lst = list) == null) s = est = 0;
        else {
          expectedModCount = lst.modCount;
          current = lst.first;
          s = est = lst.size;
        }
      }
      return s;
    }

    public long estimateSize() {
      return getEst();
    }

    public Spliterator trySplit() {
      Node p;
      int s = getEst();
      if (s > 1 && (p = current) != null) {
        int n = batch + BATCH_UNIT;
        if (n > s) n = s;
        if (n > MAX_BATCH) n = MAX_BATCH;
        Object[] a = new Object[n];
        int j = 0;
        do {
          a[j++] = p.item;
        } while ((p = p.next) != null && j < n);
        current = p;
        batch = j;
        est = s - j;
        return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
      }
      return null;
    }

    public void forEachRemaining(Consumer action) {
      Node p;
      int n;
      if (action == null) throw new NullPointerException();
      if ((n = getEst()) > 0 && (p = current) != null) {
        current = null;
        est = 0;
        do {
          Object e = p.item;
          p = p.next;
          action.accept(e);
        } while (p != null && --n > 0);
      }
      if (list.modCount != expectedModCount) throw new ConcurrentModificationException();
    }

    public boolean tryAdvance(Consumer action) {
      Node p;
      if (action == null) throw new NullPointerException();
      if (getEst() > 0 && (p = current) != null) {
        --est;
        Object e = p.item;
        current = p.next;
        action.accept(e);
        if (list.modCount != expectedModCount) throw new ConcurrentModificationException();
        return true;
      }
      return false;
    }

    public int characteristics() {
      return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }
  }

  private class ListItr implements ListIterator {
    private Node lastReturned = null;
    private Node next;
    private int nextIndex;
    private int expectedModCount = modCount;

    ListItr(int index) {
      // assert isPositionIndex(index);
      next = (index == size) ? null : node(index);
      nextIndex = index;
    }

    public boolean hasNext() {
      return nextIndex < size;
    }

    public Object next() {
      checkForComodification();
      if (!hasNext()) throw new NoSuchElementException();

      lastReturned = next;
      next = next.next;
      nextIndex++;
      return lastReturned.item;
    }

    public boolean hasPrevious() {
      return nextIndex > 0;
    }

    public Object previous() {
      checkForComodification();
      if (!hasPrevious()) throw new NoSuchElementException();

      lastReturned = next = (next == null) ? last : next.prev;
      nextIndex--;
      return lastReturned.item;
    }

    public int nextIndex() {
      return nextIndex;
    }

    public int previousIndex() {
      return nextIndex - 1;
    }

    public void remove() {
      checkForComodification();
      if (lastReturned == null) throw new IllegalStateException();

      Node lastNext = lastReturned.next;
      unlink(lastReturned);
      if (next == lastReturned) next = lastNext;
      else nextIndex--;
      lastReturned = null;
      expectedModCount++;
    }

    public void set(Object e) {
      if (lastReturned == null) throw new IllegalStateException();
      checkForComodification();
      lastReturned.item = e;
    }

    public void add(Object e) {
      checkForComodification();
      lastReturned = null;
      if (next == null) linkLast(e);
      else linkBefore(e, next);
      nextIndex++;
      expectedModCount++;
    }

    public void forEachRemaining(Consumer action) {
      Objects.requireNonNull(action);
      while (modCount == expectedModCount && nextIndex < size) {
        action.accept(next.item);
        lastReturned = next;
        next = next.next;
        nextIndex++;
      }
      checkForComodification();
    }

    final void checkForComodification() {
      if (modCount != expectedModCount) throw new ConcurrentModificationException();
    }
  }

  private class DescendingIterator implements Iterator {
    private final ListItr itr = new ListItr(size());

    public boolean hasNext() {
      return itr.hasPrevious();
    }

    public Object next() {
      return itr.previous();
    }

    public void remove() {
      itr.remove();
    }
  }
}
