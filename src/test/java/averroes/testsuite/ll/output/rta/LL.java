package averroes.testsuite.ll.output.rta;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/** RTA model */
public class LL implements Iterable {

  Node first; // Node first;
  Node last; // Node last;

  public LL() {
    RTA.set = this;
  }

  public LL(Collection c) {
    this();
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = c; // unable to put this before the super-call
    addAll((Collection) RTA.set);
  }

  public Iterator iterator() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = listIterator();
    return (ListIterator) RTA.set;
  }

  public ListIterator listIterator() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = listIterator(1);
    return (ListIterator) RTA.set;
  }

  private void linkFirst(Object e) {
    RTA.set = this;
    RTA.set = e;

    // create Node
    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = new Node(null, set, node);

    RTA.set = first; // read first

    first = node; // write first
    last = node; // write last
    node.prev = node; // write Node.prev
  }

  void linkLast(Object e) {
    RTA.set = this;
    RTA.set = e;

    // create Node
    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = new Node(node, set, null);

    RTA.set = last; // read last
    last = node; // write last
    first = node; // write first
    node.next = node; // write Node.next
  }

  void linkBefore(Object e, Node succ) {
    RTA.set = this;
    RTA.set = e;
    RTA.set = succ;

    // create Node
    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = new Node(node, set, node); // create Node

    RTA.set = node.prev; // read Node.prev
    node.prev = node; // write Node.prev
    first = node; // write LL.first
    node.next = node; // write Node.next
  }

  private Object unlinkFirst(Node f) {
    RTA.set = this;
    RTA.set = f;

    Object set = RTA.set;
    Node node = (Node) set;

    RTA.set = node.item; // read Node.item
    RTA.set = node.next; // read Node.next
    node.item = set; // write Node.item
    node.next = node; // write Node.next
    first = node; // write LL.first
    last = node; // write LL.last
    node.prev = node; // write Node.prev

    return set;
  }

  private Object unlinkLast(Node l) {
    RTA.set = this;
    RTA.set = l;

    Object set = RTA.set;
    Node node = (Node) set;

    RTA.set = node.item; // read Node.item
    RTA.set = node.prev; // read Node.prev
    node.item = set; // write Node.item
    node.prev = node; // write Node.prev
    last = node; // write LL.last
    first = node; // write LL.first
    node.next = node; // write Node.next

    return set;
  }

  Object unlink(Node x) {
    RTA.set = this;
    RTA.set = x;

    Object set = RTA.set;
    Node node = (Node) set;

    RTA.set = node.item; // read Node.item
    RTA.set = node.next; // read Node.next
    RTA.set = node.prev; // read Node.prev
    first = node; // write LL.first
    node.next = node; // write Node.next
    node.prev = node; // write Node.prev
    last = node; // write LL.last
    node.item = set; // write Node.item

    return set;
  }

  public Object getFirst() {
    RTA.set = this;

    RTA.set = new NoSuchElementException();

    RTA.set = first; // read LL.first

    // read Node.item
    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = node.item;

    NoSuchElementException ex = (NoSuchElementException) set;
    if (RTA.guard) throw ex;

    return set;
  }

  public Object getLast() {
    RTA.set = this;

    RTA.set = new NoSuchElementException();

    RTA.set = last; // read LL.last

    // read Node.item
    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = node.item;

    NoSuchElementException ex = (NoSuchElementException) set;
    if (RTA.guard) throw ex;

    return set;
  }

  public Object removeFirst() {
    RTA.set = this;

    RTA.set = new NoSuchElementException();

    // handle call to unlinkFirst(f);
    Object set = RTA.set;
    Node param = (Node) set;
    RTA.set = unlinkFirst(param);

    RTA.set = first; // read LL.first

    NoSuchElementException ex = (NoSuchElementException) set;
    if (RTA.guard) throw ex;

    return set;
  }

  public Object removeLast() {
    RTA.set = this;

    RTA.set = new NoSuchElementException();

    // handle call to unlinkLast(f);
    Object set = RTA.set;
    Node param = (Node) set;
    RTA.set = unlinkLast(param);

    RTA.set = last; // read LL.last

    NoSuchElementException ex = (NoSuchElementException) set;
    if (RTA.guard) throw ex;

    return set;
  }

  public void addFirst(Object e) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = e;
    linkFirst(RTA.set);
  }

  public void addLast(Object e) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = e;
    linkLast(RTA.set);
  }

  public boolean contains(Object o) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = o;
    indexOf(RTA.set);
    return true;
  }

  public int size() {
    RTA.set = this; // inferred for implicit "this" parameter
    return 1;
  }

  public boolean add(Object e) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = e;
    linkLast(RTA.set);
    return true;
  }

  public boolean remove(Object o) {
    RTA.set = this;
    RTA.set = o;

    Object set = RTA.set;
    Node node = (Node) set;

    unlink(node); // unlink(x);
    unlink(node); // unlink(x);
    set.equals(set); // call equals(); ignore boolean return value

    RTA.set = first; // read LL.first
    RTA.set = node.item; // read Node.item
    RTA.set = node.next; // read Node.next

    return true;
  }

  public boolean addAll(Collection c) {
    RTA.set = this;
    RTA.set = c;
    addAll(1, (Collection) RTA.set); // addAll(size, c);
    // ignore boolean return
    // value
    return true;
  }

  public boolean addAll(int index, Collection c) {
    RTA.set = this;
    RTA.set = c;

    // Object creation
    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = new Node(node, set, null); // create Node

    // Method calls
    checkPositionIndex(1); // checkPositionIndex(index);

    Collection coll = (Collection) set;
    RTA.set = coll.toArray(); // Object[] a = c.toArray();
    RTA.set = node(1); // node(index);

    // Array handling
    RTA.set = ((Object[]) set)[0];

    // Field handling
    RTA.set = last; // read LL.last

    RTA.set = node.prev; // read Node.prev

    first = node; // write LL.first
    node.next = node; // write Node.next
    last = node; // write LL.last
    node.prev = node; // write Node.prev
    return true;
  }

  public void clear() {
    RTA.set = this;

    RTA.set = first; // read LL.first

    Object set = RTA.set;
    Node node = (Node) set;

    RTA.set = node.next; // read Node.next
    node.item = set; // write Node.item
    node.next = node; // write Node.next
    node.prev = node; // write Node.prev
    last = node; // write LL.last
    first = node; // write LL.first
  }

  public Object get(int index) {
    RTA.set = this;

    checkElementIndex(1); // checkElementIndex(index);
    RTA.set = node(1); // node(index);

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = node.item; // read Node.item

    return set;
  }

  public Object set(int index, Object element) {
    RTA.set = this;
    RTA.set = element;

    checkElementIndex(1); // checkElementIndex(index);
    RTA.set = node(1); // node(index);

    Object set = RTA.set;
    Node node = (Node) set;

    RTA.set = node.item; // read Node.item
    node.item = set; // write Node.item
    return set;
  }

  public void add(int index, Object element) {
    RTA.set = this;
    RTA.set = element;

    checkPositionIndex(1); // checkPositionIndex(index);

    Object set = RTA.set;
    linkLast(set); // linkLast(element);

    Node node = (Node) set;
    linkBefore(set, node); // linkBefore(element, node(index));

    RTA.set = node(1); // node(index);
  }

  public Object remove(int index) {
    RTA.set = this;

    checkElementIndex(1); // checkElementIndex(index);

    RTA.set = node(1); // node(index);

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = unlink(node); // unlink(node(index))

    return set;
  }

  private boolean isElementIndex(int index) {
    RTA.set = this; // inferred for implicit "this" parameter
    return true;
  }

  private boolean isPositionIndex(int index) {
    RTA.set = this; // inferred for implicit "this" parameter
    return true;
  }

  private String outOfBoundsMsg(int index) {
    RTA.set = this;
    Object set = RTA.set;
    String str = (String) set;

    RTA.set = new StringBuilder(str);

    StringBuilder sb = (StringBuilder) set;
    RTA.set = sb.append(1);
    RTA.set = sb.append(str);
    RTA.set = sb.append(1);
    RTA.set = sb.toString();

    return str;
  }

  private void checkElementIndex(int index) {
    RTA.set = this;

    Object set = RTA.set;
    String str = (String) set;
    RTA.set = new IndexOutOfBoundsException(str);

    isElementIndex(1); // isElementIndex(index);
    // ignore
    // boolean
    // return
    // value

    RTA.set = outOfBoundsMsg(1); // outOfBoundsMsg(index);

    IndexOutOfBoundsException ex = (IndexOutOfBoundsException) set;
    throw ex;
  }

  private void checkPositionIndex(int index) {
    RTA.set = this;

    Object set = RTA.set;
    String str = (String) set;
    RTA.set = new IndexOutOfBoundsException(str);

    isPositionIndex(1); // isPositionIndex(index);
    // ignore
    // boolean
    // return
    // value

    RTA.set = outOfBoundsMsg(1); // outOfBoundsMsg(index);

    IndexOutOfBoundsException ex = (IndexOutOfBoundsException) set;
    throw ex;
  }

  Node node(int index) {
    RTA.set = this;

    RTA.set = first; // read LL.first

    Node node = (Node) RTA.set;
    RTA.set = node.next; // read Node.next

    RTA.set = last; // read LL.last
    RTA.set = node.prev; // read Node.prev

    return node;
  }

  public int indexOf(Object o) {
    RTA.set = this;
    RTA.set = o;

    // o.equals(x.item)); ignore boolean return value
    Object set = RTA.set;
    set.equals(set);

    RTA.set = first; // read LL.first

    // read Node.item
    Node node = (Node) set;
    RTA.set = node.item;

    RTA.set = node.next; // read Node.next

    return 1;
  }

  public int lastIndexOf(Object o) {
    RTA.set = this;
    RTA.set = o;

    // o.equals(x.item)); ignore boolean return value
    Object set = RTA.set;
    set.equals(set);

    RTA.set = last; // read LL.last

    Node node = (Node) set;
    RTA.set = node.item; // read Node.item

    RTA.set = node.prev; // read Node.prev

    return 1;
  }

  public Object peek() {
    RTA.set = this;
    RTA.set = first; // read LL.first

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = node.item; // read Node.item

    return set;
  }

  public Object element() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = getFirst();
    return RTA.set;
  }

  public Object poll() {
    RTA.set = this;

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = unlinkFirst(node); // unlinkFirst(f);

    RTA.set = first; // read LL.first

    return set;
  }

  public Object remove() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = removeFirst();
    return RTA.set;
  }

  public boolean offer(Object e) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = e;
    Object set = RTA.set;
    add(set);
    return true;
  }

  public boolean offerFirst(Object e) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = e;
    addFirst(RTA.set);
    return true;
  }

  public boolean offerLast(Object e) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = e;
    addLast(RTA.set);
    return true;
  }

  public Object peekFirst() {
    RTA.set = this;

    RTA.set = first; // read LL.first

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = node.item; // read Node.item

    return set;
  }

  public Object peekLast() {
    RTA.set = this;

    RTA.set = last; // read LL.last

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = node.item; // read Node.item

    return set;
  }

  public Object pollFirst() {
    RTA.set = this;

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = unlinkFirst(node); // unlinkFirst(f);

    RTA.set = first; // read LL.first

    return set;
  }

  public Object pollLast() {
    RTA.set = this;

    Object set = RTA.set;
    Node node = (Node) set;
    RTA.set = unlinkLast(node); // unlinkLast(f);

    RTA.set = last; // read LL.ladt

    return set;
  }

  public void push(Object e) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = e;
    addFirst(RTA.set);
  }

  public Object pop() {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = removeFirst();
    return RTA.set;
  }

  public boolean removeFirstOccurrence(Object o) {
    RTA.set = this; // inferred for implicit "this" parameter
    RTA.set = o;
    remove(RTA.set);
    return true;
  }

  public boolean removeLastOccurrence(Object o) {
    RTA.set = this;
    RTA.set = o;

    Object set = RTA.set;
    Node node = (Node) set;
    unlink(node);
    unlink(node);
    set.equals(set); // o.equals(x.item)

    RTA.set = last; // read LL.last

    RTA.set = node.item; // read

    RTA.set = node.prev; // read Node.prev

    return true;
  }

  public ListIterator listIterator(int index) {
    RTA.set = this;
    RTA.set = new ListItr(1); // new ListItr(index);
    checkPositionIndex(1); // checkPositionIndex(index);
    return (ListIterator) RTA.set; // infer cast from return type
  }

  public Iterator descendingIterator() {
    RTA.set = this;
    RTA.set = new DescendingIterator();
    return (Iterator) RTA.set;
  }

  private LL superClone() {
    RTA.set = this;

    Object set = RTA.set;
    CloneNotSupportedException ex = (CloneNotSupportedException) set;
    RTA.set = new InternalError(ex);

    RTA.set = new CloneNotSupportedException();

    try {
      RTA.set = super.clone();
    } catch (CloneNotSupportedException e) {
    }

    if (RTA.guard) throw (InternalError) set;

    return (LL) set;
  }

  public Object clone() {
    RTA.set = this;

    // clone.add(x.item);
    Object set = RTA.set;
    add(set);

    RTA.set = superClone();

    RTA.set = first; // read LL.first

    // read Node.item and Node.next
    Node node = (Node) set;
    RTA.set = node.item;
    RTA.set = node.next;

    last = node; // write LL.last
    first = node; // write LL.first

    return set;
  }

  public Object[] toArray() {
    RTA.set = this;

    RTA.set = new Object[1]; // new Object[size];

    // array-write result[i++] = x.item;
    Object set = RTA.set;
    Object[] arr = (Object[]) set;
    arr[0] = set;

    RTA.set = first; // read LL.first

    Node node = (Node) set;
    RTA.set = node.item; // read Node.item
    RTA.set = node.next; // read Node.next

    return arr;
  }

  public Object[] toArray(Object[] a) {
    RTA.set = this;
    RTA.set = a;

    // a.getClass()
    Object set = RTA.set;
    Object[] arr = (Object[]) set;
    RTA.set = arr.getClass();

    // a.getClass().getComponentType()
    Class cls = (Class) set;
    RTA.set = cls.getComponentType();

    RTA.set = java.lang.reflect.Array.newInstance(cls, 1);

    // array-write result[i++] = x.item;
    arr[0] = set;

    RTA.set = first; // read LL.first

    Node node = (Node) set;
    RTA.set = node.item; // read Node.item
    RTA.set = node.next; // read Node.next

    return arr;
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    RTA.set = this;
    RTA.set = s;

    Object set = RTA.set;
    java.io.ObjectOutputStream out = (java.io.ObjectOutputStream) set;
    out.defaultWriteObject(); // s.defaultWriteObject();
    out.writeInt(1); // s.writeInt(size);
    out.writeObject(set); // s.writeObject(x.item);

    RTA.set = first;

    Node node = (Node) set;
    RTA.set = node.item; // read Node.item
    RTA.set = node.next; // read Node.next
  }

  private void readObject(java.io.ObjectInputStream s)
      throws java.io.IOException, ClassNotFoundException {
    RTA.set = this;
    RTA.set = s;

    Object set = RTA.set;
    java.io.ObjectInputStream in = (java.io.ObjectInputStream) set;
    in.defaultReadObject(); // s.defaultReadObject();
    linkLast(set); // linkLast(s.readObject());
    in.readInt(); // s.readInt();
    RTA.set = in.readObject(); // s.readObject()
  }

  public Spliterator spliterator() {
    RTA.set = this;

    // new LLSpliterator(this, -1, 0);
    RTA.set = new LLSpliterator(this, 1, 1);

    return (Spliterator) RTA.set;
  }

  private static class Node {
    Object item;
    Node next;
    Node prev;

    Node(Node prev, Object element, Node next) {
      RTA.set = this;
      RTA.set = prev;
      RTA.set = element;
      RTA.set = next;

      Object set = RTA.set;
      item = set;

      this.next = this;
      this.prev = this;
    }
  }

  static final class LLSpliterator implements Spliterator {
    final LL list;
    Node current;

    LLSpliterator(LL list, int est, int expectedModCount) {
      RTA.set = this;
      RTA.set = list;
      this.list = (LL) RTA.set;
    }

    final int getEst() {
      RTA.set = this;
      RTA.set = list; // read LLSplitIterator.list

      Object set = RTA.set;
      RTA.set = ((LL) set).first; // read LL.first
      current = (Node) set;

      return 1;
    }

    public long estimateSize() {
      RTA.set = this;
      getEst(); // ignore int return value
      return 1;
    }

    public Spliterator trySplit() {
      RTA.set = this;

      RTA.set = new Object[1]; // new Object[n];

      getEst();
      Object set = RTA.set;
      Object[] arr = (Object[]) set;
      RTA.set = Spliterators.spliterator(arr, 1, 1, 1);

      arr[0] = set; // array write a[j++] = p.item;

      RTA.set = current; // read LLSpliterator.current

      Node node = (Node) set;
      RTA.set = node.item; // read Node.item
      RTA.set = node.next; // read Node.next
      current = node; // write LLSpliterator.current

      return (Spliterator) set;
    }

    public void forEachRemaining(Consumer action) {
      RTA.set = this;
      RTA.set = action;

      RTA.set = new NullPointerException();
      RTA.set = new ConcurrentModificationException();

      Object set = RTA.set;
      Consumer consumer = (Consumer) set;
      consumer.accept(set); // action.accept(e);

      getEst(); // ignore int return value

      RTA.set = current; // read LLSpliterator.current

      Node node = (Node) set;
      RTA.set = node.item; // read Node.item
      RTA.set = node.next; // read Node.next
      RTA.set = list; // read LLSpliterator.list
      current = node; // write LLSpliterator.current

      NullPointerException ne = (NullPointerException) set;
      boolean guard = RTA.guard;
      if (guard) throw ne;

      ConcurrentModificationException cme = (ConcurrentModificationException) set;
      if (guard) throw cme;
    }

    public boolean tryAdvance(Consumer action) {
      RTA.set = this;
      RTA.set = action;

      RTA.set = new NullPointerException();
      RTA.set = new ConcurrentModificationException();

      Object set = RTA.set;
      Consumer consumer = (Consumer) set;
      consumer.accept(set); // action.accept(e);

      getEst(); // ignore int return value

      RTA.set = current; // read LLSpliterator.current

      Node node = (Node) set;
      RTA.set = node.item; // read Node.item
      RTA.set = node.next; // read Node.next
      RTA.set = list; // read LLSpliterator.list
      current = node; // write LLSpliterator.current

      NullPointerException ne = (NullPointerException) set;
      boolean guard = RTA.guard;
      if (guard) throw ne;

      ConcurrentModificationException cme = (ConcurrentModificationException) set;
      if (guard) throw cme;

      return true;
    }

    public int characteristics() {
      RTA.set = this;
      return 1;
    }
  }

  private class ListItr implements ListIterator {

    private Node lastReturned;
    private Node next;

    // Force java to read field this$0 in constructor
    {
      RTA.set = this;
      RTA.set = LL.this;
    }

    ListItr(int index) {

      Object set = RTA.set;
      LL base = (LL) set;
      RTA.set = base.node(1); // node(index);

      Node node = (Node) set;
      lastReturned = node; // write ListItr.lastReturned = null
      next = node; // write ListItr.next
    }

    public boolean hasNext() {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = (ListItr) RTA.set;
      RTA.set = LL.this;
      return true;
    }

    public Object next() {
      RTA.set = this; // note: cannot use name "next" because of conflict with field name
      RTA.set = LL.this;

      RTA.set = new NoSuchElementException();

      checkForComodification();
      hasNext();

      // field reads
      Object set = RTA.set;
      ListItr itr = (ListItr) set;
      RTA.set = itr.next;
      RTA.set = itr.lastReturned;

      Node node = (Node) set;
      RTA.set = node.next;
      RTA.set = node.item;

      // field writes
      itr.lastReturned = node;
      itr.next = node;

      NoSuchElementException ex = (NoSuchElementException) set;
      if (RTA.guard) throw ex;

      return set;
    }

    public boolean hasPrevious() {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = LL.this;
      return RTA.booleanVal;
    }

    public Object previous() {
      RTA.set = this;
      RTA.set = LL.this;

      RTA.set = new NoSuchElementException();

      checkForComodification();
      hasPrevious(); // ignore boolean return value

      RTA.set = next; // read ListItr.next

      Object set = RTA.set;
      RTA.set = ((LL) set).last; // read LL.last

      Node node = (Node) set;

      RTA.set = node.prev; // read Node.prev
      RTA.set = lastReturned; // read ListItr.lastReturned
      RTA.set = node.item; // read Node.item
      next = node; // write ListItr.next;
      lastReturned = node; // write ListItr.lastReturned

      NoSuchElementException ex = (NoSuchElementException) set;
      if (RTA.guard) throw ex;

      return set;
    }

    public int nextIndex() {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = LL.this;
      return 1;
    }

    public int previousIndex() {
      RTA.set = this; // inferred for implicit "this" parameter
      RTA.set = LL.this;
      return 1;
    }

    public void remove() {
      RTA.set = this;
      RTA.set = LL.this;

      RTA.set = new IllegalStateException();

      checkForComodification();

      Object set = RTA.set;
      Node node = (Node) set;
      unlink(node); // unlink(lastReturned);

      RTA.set = lastReturned; // read ListItr.lastReturned

      RTA.set = (node).next; // read Node.next

      RTA.set = next; // read ListItr.next
      next = node; // write ListItr.next
      lastReturned = node; // write ListItr.lastReturned

      throw (IllegalStateException) set;
    }

    public void set(Object e) {
      RTA.set = this;
      RTA.set = LL.this;
      RTA.set = e;

      RTA.set = new IllegalStateException();

      checkForComodification();

      RTA.set = lastReturned; // read ListItr.lastReturned

      Object set = RTA.set;
      Node node = (Node) set;
      node.item = set; // write Node.item

      throw (IllegalStateException) set;
    }

    public void add(Object e) {
      RTA.set = this;
      RTA.set = LL.this;
      RTA.set = e;

      checkForComodification();

      Object set = RTA.set;
      LL ll = (LL) set;
      ll.linkLast(set); // linkLast(e);

      Node node = (Node) set;
      ll.linkBefore(set, node); // linkBefore(e, next);

      RTA.set = next; // read ListItr.next
      lastReturned = node; // write ListItr.lastReturned
    }

    public void forEachRemaining(Consumer action) {
      RTA.set = this;
      RTA.set = LL.this;
      RTA.set = action;

      Object set = RTA.set;
      Consumer consumer = (Consumer) set;
      Objects.requireNonNull(consumer); // Objects.requireNonNull(action);
      consumer.accept(set); // action.accept(next.item);

      checkForComodification();

      RTA.set = next; // read ListItr.next

      Node node = (Node) set;
      RTA.set = node.item; // read Node.item
      RTA.set = node.next; // read Node.next

      lastReturned = node; // write ListItr.lastReturned
      next = node; // write ListItr.next
    }

    final void checkForComodification() {
      RTA.set = this;
      RTA.set = LL.this;
      RTA.set = new ConcurrentModificationException();
      throw (ConcurrentModificationException) RTA.set;
    }
  }

  // ----------------------------------------------------- //

  private class DescendingIterator implements Iterator {
    private final ListItr itr;
    // Force java to use a default constructor
    {
      RTA.set = this;
      RTA.set = LL.this;

      RTA.set = new ListItr(1); // new ListItr(size());

      LL.this.size(); // ignore int return value

      itr = (ListItr) RTA.set; // ListItr itr = new
    }

    public boolean hasNext() {
      RTA.set = this;
      RTA.set = LL.this;

      ((ListItr) RTA.set).hasPrevious();

      RTA.set = itr; // read DescendingIterator.itr

      return true;
    }

    public Object next() {
      RTA.set = this;
      RTA.set = LL.this;

      Object set = RTA.set;
      ListItr itrObj = (ListItr) set;
      RTA.set = itrObj.previous(); // itr.previous();

      RTA.set = itr; // read DescendingIterator.itr

      return set;
    }

    public void remove() {
      RTA.set = this;
      RTA.set = LL.this;

      ((ListItr) RTA.set).remove(); // itr.remove();
      RTA.set = itr; // read DescendingIterator.itr
    }
  }
}
