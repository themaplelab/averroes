package averroes.testsuite.ll.output.xta;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/** XTA model */
@SuppressWarnings("rawtypes")
public class LL implements Iterable {

  Node first; // Node first;
  Node last; // Node last;

  @SuppressWarnings("unused")
  private Object set_m0;

  private Object set_m1;
  private Object set_m2;
  private Object set_m3;
  private Object set_m4;
  private Object set_m5;
  private Object set_m6;
  private Object set_m7;
  private Object set_m8;
  private Object set_m9;
  private Object set_m10;
  private Object set_m11;
  private Object set_m12;
  private Object set_m13;
  private Object set_m14;
  private Object set_m15;
  private Object set_m16;

  @SuppressWarnings("unused")
  private Object set_m17;

  private Object set_m18;
  private Object set_m19;
  private Object set_m20;
  private Object set_m21;
  private Object set_m22;
  private Object set_m23;
  private Object set_m24;
  private Object set_m25;
  private Object set_m26;

  @SuppressWarnings("unused")
  private Object set_m27;

  @SuppressWarnings("unused")
  private Object set_m28;

  private Object set_m29;
  private Object set_m30;
  private Object set_m31;
  private Object set_m32;
  private Object set_m33;
  private Object set_m34;
  private Object set_m35;
  private Object set_m36;
  private Object set_m37;
  private Object set_m38;
  private Object set_m39;
  private Object set_m40;
  private Object set_m41;
  private Object set_m42;
  private Object set_m43;
  private Object set_m44;
  private Object set_m45;
  private Object set_m46;
  private Object set_m47;
  private Object set_m48;
  private Object set_m49;
  private Object set_m50;
  private Object set_m51;
  private Object set_m52;
  private Object set_m53;
  private Object set_m54;
  private Object set_m55;
  private Object set_m56;
  private Object set_m57;
  private Object set_m58;
  private Object set_m59;
  private Object set_m60;

  public LL() {
    set_m0 = this;
  }

  public LL(Collection c) {
    this(); // call to overloaded constructor stays the same

    set_m1 = this;
    set_m1 = c;

    // ignore return value because it is primitive type boolean
    addAll((Collection) set_m1);
  }

  public Iterator iterator() {
    set_m2 = this;
    set_m2 = listIterator();
    return (ListIterator) set_m2;
  }

  public ListIterator listIterator() {
    set_m3 = this;
    set_m3 = listIterator(1);
    return (ListIterator) set_m3;
  }

  private void linkFirst(Object e) {
    set_m4 = this;
    set_m4 = e;

    // create Node
    Object set = set_m4;
    Node node = (Node) set;
    set_m4 = new Node(null, set, node);

    set_m4 = first; // read first

    first = node; // write first
    last = node; // write last
    node.prev = node; // write Node.prev
  }

  void linkLast(Object e) {
    set_m5 = this;
    set_m5 = e;

    // create Node
    Object set = set_m5;
    Node node = (Node) set;
    set_m5 = new Node(node, set, null);

    set_m5 = last; // read last
    last = node; // write last
    first = node; // write first
    node.next = node; // write Node.next
  }

  void linkBefore(Object e, Node succ) {
    set_m6 = this;
    set_m6 = e;
    set_m6 = succ;

    // create Node
    Object set = set_m6;
    Node node = (Node) set;
    set_m6 = new Node(node, set, node); // create Node

    set_m6 = node.prev; // read Node.prev
    node.prev = node; // write Node.prev
    first = node; // write LL.first
    node.next = node; // write Node.next
  }

  private Object unlinkFirst(Node f) {
    set_m7 = this;
    set_m7 = f;

    Object set = set_m7;
    Node node = (Node) set;

    set_m7 = node.item; // read Node.item
    set_m7 = node.next; // read Node.next
    node.item = set; // write Node.item
    node.next = node; // write Node.next
    first = node; // write LL.first
    last = node; // write LL.last
    node.prev = node; // write Node.prev

    return set;
  }

  private Object unlinkLast(Node l) {
    set_m8 = this;
    set_m8 = l;

    Object set = set_m8;
    Node node = (Node) set;

    set_m8 = node.item; // read Node.item
    set_m8 = node.prev; // read Node.prev
    node.item = set; // write Node.item
    node.prev = node; // write Node.prev
    last = node; // write LL.last
    first = node; // write LL.first
    node.next = node; // write Node.next

    return set;
  }

  Object unlink(Node x) {
    set_m9 = this;
    set_m9 = x;

    Object set = set_m9;
    Node node = (Node) set;

    set_m9 = node.item; // read Node.item
    set_m9 = node.next; // read Node.next
    set_m9 = node.prev; // read Node.prev
    first = node; // write LL.first
    node.next = node; // write Node.next
    node.prev = node; // write Node.prev
    last = node; // write LL.last
    node.item = set; // write Node.item

    return set;
  }

  public Object getFirst() {
    set_m10 = this;

    set_m10 = new NoSuchElementException();

    set_m10 = first; // read LL.first

    // read Node.item
    Object set = set_m10;
    Node node = (Node) set;
    set_m10 = node.item;

    NoSuchElementException ex = (NoSuchElementException) set;
    if (XTA.guard) throw ex;

    return set;
  }

  public Object getLast() {
    set_m11 = this;

    set_m11 = new NoSuchElementException();

    set_m11 = last; // read LL.last

    // read Node.item
    Object set = set_m11;
    Node node = (Node) set;
    set_m11 = node.item;

    NoSuchElementException ex = (NoSuchElementException) set;
    if (XTA.guard) throw ex;

    return set;
  }

  public Object removeFirst() {
    set_m12 = this;

    set_m12 = new NoSuchElementException();

    // handle call to unlinkFirst(f);
    Object set = set_m12;
    Node param = (Node) set;
    set_m12 = unlinkFirst(param);

    set_m12 = first; // read LL.first

    NoSuchElementException ex = (NoSuchElementException) set;
    if (XTA.guard) throw ex;

    return set;
  }

  public Object removeLast() {
    set_m13 = this;

    set_m13 = new NoSuchElementException();

    // handle call to unlinkLast(f);
    Object set = set_m13;
    Node param = (Node) set;
    set_m13 = unlinkLast(param);

    set_m13 = last; // read LL.last

    NoSuchElementException ex = (NoSuchElementException) set;
    if (XTA.guard) throw ex;

    return set;
  }

  public void addFirst(Object e) {
    set_m14 = this;
    set_m14 = e;
    linkFirst(set_m14); // linkFirst(e);
  }

  public void addLast(Object e) {
    set_m15 = this;
    set_m15 = e;
    linkLast(set_m15); // linkLast(e);
  }

  public boolean contains(Object o) {
    set_m16 = this;
    set_m16 = o;
    indexOf(set_m16); // indexOf(o); ignore boolean return value
    return true;
  }

  public int size() {
    set_m17 = this;
    return 1;
  }

  public boolean add(Object e) {
    set_m18 = this;
    set_m18 = e;
    linkLast(set_m18); // linkLast(e);
    return true;
  }

  public boolean remove(Object o) {
    set_m19 = this;
    set_m19 = o;

    Object set = set_m19;
    Node node = (Node) set;

    unlink(node); // unlink(x);
    unlink(node); // unlink(x);
    set.equals(set); // call equals(); ignore boolean return value

    set_m19 = first; // read LL.first
    set_m19 = node.item; // read Node.item
    set_m19 = node.next; // read Node.next

    return true;
  }

  public boolean addAll(Collection c) {
    set_m20 = this;
    set_m20 = c;
    addAll(1, (Collection) set_m20); // addAll(size, c);
    // ignore boolean return
    // value
    return true;
  }

  public boolean addAll(int index, Collection c) {
    set_m21 = this;
    set_m21 = c;

    // Object creation
    Object set = set_m21;
    Node node = (Node) set;
    set_m21 = new Node(node, set, null); // create Node

    // Method calls
    checkPositionIndex(1); // checkPositionIndex(index);

    Collection coll = (Collection) set;
    set_m21 = coll.toArray(); // Object[] a = c.toArray();
    set_m21 = node(1); // node(index);

    // Array handling
    set_m21 = ((Object[]) set)[0];

    // Field handling
    set_m21 = last; // read LL.last

    set_m21 = node.prev; // read Node.prev

    first = node; // write LL.first
    node.next = node; // write Node.next
    last = node; // write LL.last
    node.prev = node; // write Node.prev
    return true;
  }

  public void clear() {
    set_m22 = this;

    set_m22 = first; // read LL.first

    Object set = set_m22;
    Node node = (Node) set;

    set_m22 = node.next; // read Node.next
    node.item = set; // write Node.item
    node.next = node; // write Node.next
    node.prev = node; // write Node.prev
    last = node; // write LL.last
    first = node; // write LL.first
  }

  public Object get(int index) {
    set_m23 = this;

    checkElementIndex(1); // checkElementIndex(index);
    set_m23 = node(1); // node(index);

    Object set = set_m23;
    Node node = (Node) set;
    set_m23 = node.item; // read Node.item

    return set;
  }

  public Object set(int index, Object element) {
    set_m24 = this;
    set_m24 = element;

    checkElementIndex(1); // checkElementIndex(index);
    set_m24 = node(1); // node(index);

    Object set = set_m24;
    Node node = (Node) set;

    set_m24 = node.item; // read Node.item
    node.item = set; // write Node.item
    return set;
  }

  public void add(int index, Object element) {
    set_m25 = this;
    set_m25 = element;

    checkPositionIndex(1); // checkPositionIndex(index);

    Object set = set_m25;
    linkLast(set); // linkLast(element);

    Node node = (Node) set;
    linkBefore(set, node); // linkBefore(element, node(index));

    set_m25 = node(1); // node(index);
  }

  public Object remove(int index) {
    set_m26 = this;

    checkElementIndex(1); // checkElementIndex(index);

    set_m26 = node(1); // node(index);

    Object set = set_m26;
    Node node = (Node) set;
    set_m26 = unlink(node); // unlink(node(index))

    return set;
  }

  private boolean isElementIndex(int index) {
    // TODO: should this be optimized away by the optimization phase?
    set_m27 = this;
    return true;
  }

  private boolean isPositionIndex(int index) {
    // TODO: should this be optimized away by the optimization phase?
    set_m28 = this;
    return true;
  }

  private String outOfBoundsMsg(int index) {
    set_m29 = this;
    Object set = set_m29;
    String str = (String) set;

    set_m29 = new StringBuilder(str);

    StringBuilder sb = (StringBuilder) set;
    set_m29 = sb.append(1);
    set_m29 = sb.append(str);
    set_m29 = sb.append(1);
    set_m29 = sb.toString();

    return str;
  }

  private void checkElementIndex(int index) {
    set_m30 = this;

    Object set = set_m30;
    String str = (String) set;
    set_m30 = new IndexOutOfBoundsException(str);

    isElementIndex(1); // isElementIndex(index);
    // ignore
    // boolean
    // return
    // value

    set_m30 = outOfBoundsMsg(1); // outOfBoundsMsg(index);

    IndexOutOfBoundsException ex = (IndexOutOfBoundsException) set;
    throw ex;
  }

  private void checkPositionIndex(int index) {
    set_m31 = this;

    Object set = set_m31;
    String str = (String) set;
    set_m31 = new IndexOutOfBoundsException(str);

    isPositionIndex(1); // isPositionIndex(index);
    // ignore
    // boolean
    // return
    // value

    set_m31 = outOfBoundsMsg(1); // outOfBoundsMsg(index);

    IndexOutOfBoundsException ex = (IndexOutOfBoundsException) set;
    throw ex;
  }

  Node node(int index) {
    set_m32 = this;

    set_m32 = first; // read LL.first

    Node node = (Node) set_m32;
    set_m32 = node.next; // read Node.next

    set_m32 = last; // read LL.last
    set_m32 = node.prev; // read Node.prev

    return node;
  }

  public int indexOf(Object o) {
    set_m33 = this;
    set_m33 = o;

    // o.equals(x.item)); ignore boolean return value
    Object set = set_m33;
    set.equals(set);

    set_m33 = first; // read LL.first

    // read Node.item
    Node node = (Node) set;
    set_m33 = node.item;

    set_m33 = node.next; // read Node.next

    return 1;
  }

  public int lastIndexOf(Object o) {
    set_m34 = this;
    set_m34 = o;

    // o.equals(x.item)); ignore boolean return value
    Object set = set_m34;
    set.equals(set);

    set_m34 = last; // read LL.last

    Node node = (Node) set;
    set_m34 = node.item; // read Node.item

    set_m34 = node.prev; // read Node.prev

    return 1;
  }

  public Object peek() {
    set_m35 = this;
    set_m35 = first; // read LL.first

    Object set = set_m35;
    Node node = (Node) set;
    set_m35 = node.item; // read Node.item

    return set;
  }

  public Object element() {
    set_m36 = this;
    set_m36 = getFirst();
    return set_m36;
  }

  public Object poll() {
    set_m37 = this;

    Object set = set_m37;
    Node node = (Node) set;
    set_m37 = unlinkFirst(node); // unlinkFirst(f);

    set_m37 = first; // read LL.first

    return set;
  }

  public Object remove() {
    set_m38 = this;
    set_m38 = removeFirst();
    return set_m38;
  }

  public boolean offer(Object e) {
    set_m39 = this;
    set_m39 = e;
    add(set_m39); // add(e); ignore boolean return value
    return true;
  }

  public boolean offerFirst(Object e) {
    set_m40 = this;
    set_m40 = e;
    addFirst(set_m40); // addFirst(e);
    return true;
  }

  public boolean offerLast(Object e) {
    set_m41 = this;
    set_m41 = e;
    addLast(set_m41); // addLast(e);
    return true;
  }

  public Object peekFirst() {
    set_m42 = this;

    set_m42 = first; // read LL.first

    Object set = set_m42;
    Node node = (Node) set;
    set_m42 = node.item; // read Node.item

    return set;
  }

  public Object peekLast() {
    set_m43 = this;

    set_m43 = last; // read LL.last

    Object set = set_m43;
    Node node = (Node) set;
    set_m43 = node.item; // read Node.item

    return set;
  }

  public Object pollFirst() {
    set_m44 = this;

    Object set = set_m44;
    Node node = (Node) set;
    set_m44 = unlinkFirst(node); // unlinkFirst(f);

    set_m44 = first; // read LL.first

    return set;
  }

  public Object pollLast() {
    set_m45 = this;

    Object set = set_m45;
    Node node = (Node) set;
    set_m45 = unlinkLast(node); // unlinkLast(f);

    set_m45 = last; // read LL.ladt

    return set;
  }

  public void push(Object e) {
    set_m46 = this;
    set_m46 = e;
    addFirst(set_m46); // addFirst(e);
  }

  public Object pop() {
    set_m47 = this;
    set_m47 = removeFirst();
    return set_m47;
  }

  public boolean removeFirstOccurrence(Object o) {
    set_m48 = this;
    set_m48 = o;
    remove(set_m48); // ignore boolean return val
    return true;
  }

  public boolean removeLastOccurrence(Object o) {
    set_m49 = this;
    set_m49 = o;

    Object set = set_m49;
    Node node = (Node) set;
    unlink(node);
    unlink(node);
    set.equals(set); // o.equals(x.item)

    set_m49 = last; // read LL.last

    set_m49 = node.item; // read

    set_m49 = node.prev; // read Node.prev

    return true;
  }

  public ListIterator listIterator(int index) {
    set_m50 = this;
    set_m50 = new ListItr(1); // new ListItr(index);
    checkPositionIndex(1); // checkPositionIndex(index);
    return (ListIterator) set_m50; // infer cast from return type
  }

  public Iterator descendingIterator() {
    set_m51 = this;
    set_m51 = new DescendingIterator();
    return (Iterator) set_m51;
  }

  private LL superClone() {
    set_m52 = this;

    Object set = set_m52;
    CloneNotSupportedException ex = (CloneNotSupportedException) set;
    set_m52 = new InternalError(ex);

    set_m52 = new CloneNotSupportedException();

    try {
      set_m52 = super.clone();
    } catch (CloneNotSupportedException e) {
    }

    if (XTA.guard) throw (InternalError) set;

    return (LL) set;
  }

  public Object clone() {
    set_m53 = this;

    // clone.add(x.item);
    Object set = set_m53;
    add(set);

    set_m53 = superClone();

    set_m53 = first; // read LL.first

    // read Node.item and Node.next
    Node node = (Node) set;
    set_m53 = node.item;
    set_m53 = node.next;

    last = node; // write LL.last
    first = node; // write LL.first

    return set;
  }

  public Object[] toArray() {
    set_m54 = this;

    set_m54 = new Object[1]; // new Object[size];

    // array-write result[i++] = x.item;
    Object set = set_m54;
    Object[] arr = (Object[]) set;
    arr[0] = set;

    set_m54 = first; // read LL.first

    Node node = (Node) set;
    set_m54 = node.item; // read Node.item
    set_m54 = node.next; // read Node.next

    return arr;
  }

  @SuppressWarnings("rawtypes")
  public Object[] toArray(Object[] a) {
    set_m55 = this;
    set_m55 = a;

    // a.getClass()
    Object set = set_m55;
    Object[] arr = (Object[]) set;
    set_m55 = arr.getClass();

    // a.getClass().getComponentType()
    Class cls = (Class) set;
    set_m55 = cls.getComponentType();

    set_m55 = java.lang.reflect.Array.newInstance(cls, 1);

    // array-write result[i++] = x.item;
    arr[0] = set;

    set_m55 = first; // read LL.first

    Node node = (Node) set;
    set_m55 = node.item; // read Node.item
    set_m55 = node.next; // read Node.next

    return arr;
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    set_m56 = this;
    set_m56 = s;

    Object set = set_m56;
    java.io.ObjectOutputStream out = (java.io.ObjectOutputStream) set;
    out.defaultWriteObject(); // s.defaultWriteObject();
    out.writeInt(1); // s.writeInt(size);
    out.writeObject(set); // s.writeObject(x.item);

    set_m56 = first;

    Node node = (Node) set;
    set_m56 = node.item; // read Node.item
    set_m56 = node.next; // read Node.next
  }

  @SuppressWarnings("resource")
  private void readObject(java.io.ObjectInputStream s)
      throws java.io.IOException, ClassNotFoundException {
    set_m57 = this;
    set_m57 = s;

    Object set = set_m57;
    java.io.ObjectInputStream in = (java.io.ObjectInputStream) set;
    in.defaultReadObject(); // s.defaultReadObject();
    linkLast(set); // linkLast(s.readObject());
    in.readInt(); // s.readInt();
    set_m57 = in.readObject(); // s.readObject()
  }

  public Spliterator spliterator() {
    set_m58 = this;

    // new LLSpliterator(this, -1, 0);
    set_m58 = new LLSpliterator(this, 1, 1);

    return (Spliterator) set_m58;
  }

  private static class Node {
    Object item;
    Node next;
    Node prev;

    private Object set_m0;

    Node(Node prev, Object element, Node next) {
      set_m0 = this;
      set_m0 = prev;
      set_m0 = element;
      set_m0 = next;

      Object set = set_m0;
      item = set;

      this.next = this;
      this.prev = this;
    }
  }

  /** A customized variant of Spliterators.IteratorSpliterator */
  static final class LLSpliterator implements Spliterator {
    final LL list;
    Node current;

    private Object set_m0;
    private Object set_m1;
    private Object set_m2;
    private Object set_m3;
    private Object set_m4;
    private Object set_m5;
    private Object set_m6;

    LLSpliterator(LL list, int est, int expectedModCount) {
      set_m0 = this;
      set_m0 = list;
      this.list = (LL) set_m0;
    }

    final int getEst() {
      set_m1 = this;
      set_m1 = list; // read LLSplitIterator.list

      Object set = set_m1;
      set_m1 = ((LL) set).first; // read LL.first
      current = (Node) set;

      return 1;
    }

    public long estimateSize() {
      set_m2 = this;
      getEst(); // ignore int return value
      return 1;
    }

    public Spliterator trySplit() {
      set_m3 = this;

      set_m3 = new Object[1]; // new Object[n];

      getEst();
      Object set = set_m3;
      Object[] arr = (Object[]) set;
      set_m3 = Spliterators.spliterator(arr, 1, 1, 1);

      arr[0] = set; // array write a[j++] = p.item;

      set_m3 = current; // read LLSpliterator.current

      Node node = (Node) set;
      set_m3 = node.item; // read Node.item
      set_m3 = node.next; // read Node.next
      current = node; // write LLSpliterator.current

      return (Spliterator) set;
    }

    public void forEachRemaining(Consumer action) {
      set_m4 = this;
      set_m4 = action;

      set_m4 = new NullPointerException();
      set_m4 = new ConcurrentModificationException();

      Object set = set_m4;
      Consumer consumer = (Consumer) set;
      consumer.accept(set); // action.accept(e);

      getEst(); // ignore int return value

      set_m4 = current; // read LLSpliterator.current

      Node node = (Node) set;
      set_m4 = node.item; // read Node.item
      set_m4 = node.next; // read Node.next
      set_m4 = list; // read LLSpliterator.list
      current = node; // write LLSpliterator.current

      NullPointerException ne = (NullPointerException) set;
      boolean guard = XTA.guard;
      if (guard) throw ne;

      ConcurrentModificationException cme = (ConcurrentModificationException) set;
      if (guard) throw cme;
    }

    public boolean tryAdvance(Consumer action) {
      set_m5 = this;
      set_m5 = action;

      set_m5 = new NullPointerException();
      set_m5 = new ConcurrentModificationException();

      Object set = set_m5;
      Consumer consumer = (Consumer) set;
      consumer.accept(set); // action.accept(e);

      getEst(); // ignore int return value

      set_m5 = current; // read LLSpliterator.current

      Node node = (Node) set;
      set_m5 = node.item; // read Node.item
      set_m5 = node.next; // read Node.next
      set_m5 = list; // read LLSpliterator.list
      current = node; // write LLSpliterator.current

      NullPointerException ne = (NullPointerException) set;
      boolean guard = XTA.guard;
      if (guard) throw ne;

      ConcurrentModificationException cme = (ConcurrentModificationException) set;
      if (guard) throw cme;

      return true;
    }

    public int characteristics() {
      set_m6 = this;
      return 1;
    }
  }

  private class ListItr implements ListIterator {

    private Node lastReturned;
    private Node next;

    private Object set_m0;
    private Object set_m1;
    private Object set_m2;
    private Object set_m3;
    private Object set_m4;
    private Object set_m5;
    private Object set_m6;
    private Object set_m7;
    private Object set_m8;
    private Object set_m9;
    private Object set_m10;
    private Object set_m11;

    // Force java to read field this$0 in constructor
    {
      set_m0 = this;
      set_m0 = LL.this;
    }

    ListItr(int index) {

      Object set = set_m0;
      LL base = (LL) set;
      set_m0 = base.node(1); // node(index);

      Node node = (Node) set;
      lastReturned = node; // write ListItr.lastReturned = null
      next = node; // write ListItr.next
    }

    public boolean hasNext() {
      // TODO: should this be optimized away?
      set_m1 = this;
      set_m1 = (ListItr) set_m1;
      set_m1 = LL.this;
      return true;
    }

    public Object next() {
      set_m2 = this; // note: cannot use name "next" because of conflict with field name
      set_m2 = LL.this;

      set_m2 = new NoSuchElementException();

      checkForComodification();
      hasNext();

      // field reads
      Object set = set_m2;
      ListItr itr = (ListItr) set;
      set_m2 = itr.next;
      set_m2 = itr.lastReturned;

      Node node = (Node) set;
      set_m2 = node.next;
      set_m2 = node.item;

      // field writes
      itr.lastReturned = node;
      itr.next = node;

      NoSuchElementException ex = (NoSuchElementException) set;
      if (XTA.guard) throw ex;

      return set;
    }

    public boolean hasPrevious() {
      set_m3 = this;
      set_m3 = LL.this;
      return true;
    }

    public Object previous() {
      set_m4 = this;
      set_m4 = LL.this;

      set_m4 = new NoSuchElementException();

      checkForComodification();
      hasPrevious(); // ignore boolean return value

      set_m4 = next; // read ListItr.next

      Object set = set_m4;
      set_m4 = ((LL) set).last; // read LL.last

      Node node = (Node) set;

      set_m4 = node.prev; // read Node.prev
      set_m4 = lastReturned; // read ListItr.lastReturned
      set_m4 = node.item; // read Node.item
      next = node; // write ListItr.next;
      lastReturned = node; // write ListItr.lastReturned

      NoSuchElementException ex = (NoSuchElementException) set;
      if (XTA.guard) throw ex;

      return set;
    }

    public int nextIndex() {
      set_m5 = this;
      set_m5 = LL.this;
      return 1;
    }

    public int previousIndex() {
      set_m6 = this;
      set_m6 = LL.this;
      return 1;
    }

    public void remove() {
      set_m7 = this;
      set_m7 = LL.this;

      set_m7 = new IllegalStateException();

      checkForComodification();

      Object set = set_m7;
      Node node = (Node) set;
      unlink(node); // unlink(lastReturned);

      set_m7 = lastReturned; // read ListItr.lastReturned

      set_m7 = (node).next; // read Node.next

      set_m7 = next; // read ListItr.next
      next = node; // write ListItr.next
      lastReturned = node; // write ListItr.lastReturned

      throw (IllegalStateException) set;
    }

    public void set(Object e) {
      set_m8 = this;
      set_m8 = LL.this;
      set_m8 = e;

      set_m8 = new IllegalStateException();

      checkForComodification();

      set_m8 = lastReturned; // read ListItr.lastReturned

      Object set = set_m8;
      Node node = (Node) set;
      node.item = set; // write Node.item

      throw (IllegalStateException) set;
    }

    public void add(Object e) {
      set_m9 = this;
      set_m9 = LL.this;
      set_m9 = e;

      checkForComodification();

      Object set = set_m9;
      LL ll = (LL) set;
      ll.linkLast(set); // linkLast(e);

      Node node = (Node) set;
      ll.linkBefore(set, node); // linkBefore(e, next);

      set_m9 = next; // read ListItr.next
      lastReturned = node; // write ListItr.lastReturned
    }

    public void forEachRemaining(Consumer action) {
      set_m10 = this;
      set_m10 = LL.this;
      set_m10 = action;

      Object set = set_m10;
      Consumer consumer = (Consumer) set;
      Objects.requireNonNull(consumer); // Objects.requireNonNull(action);
      consumer.accept(set); // action.accept(next.item);

      checkForComodification();

      set_m10 = next; // read ListItr.next

      Node node = (Node) set;
      set_m10 = node.item; // read Node.item
      set_m10 = node.next; // read Node.next

      lastReturned = node; // write ListItr.lastReturned
      next = node; // write ListItr.next
    }

    final void checkForComodification() {
      set_m11 = this;
      set_m11 = LL.this;
      set_m11 = new ConcurrentModificationException();
      throw (ConcurrentModificationException) set_m11;
    }
  }

  private class DescendingIterator implements Iterator {

    private final ListItr itr;

    private Object set_m0;
    private Object set_m1;
    private Object set_m2;
    private Object set_m3;

    // Force Java to use a default constructor
    {
      set_m0 = this;
      set_m0 = LL.this;

      set_m0 = new ListItr(1); // new ListItr(size());

      LL.this.size(); // ignore int return value

      itr = (ListItr) set_m0; // ListItr itr = new ListItr(size());
    }

    public boolean hasNext() {
      set_m1 = this;
      set_m1 = LL.this;

      ((ListItr) set_m1).hasPrevious();

      set_m1 = itr; // read DescendingIterator.itr

      return true;
    }

    public Object next() {
      set_m2 = this;
      set_m2 = LL.this;

      Object set = set_m2;
      ListItr itrObj = (ListItr) set;
      set_m2 = itrObj.previous(); // itr.previous();

      set_m2 = itr; // read DescendingIterator.itr

      return set;
    }

    public void remove() {
      set_m3 = this;
      set_m3 = LL.this;

      ((ListItr) set_m3).remove(); // itr.remove();
      set_m3 = itr; // read DescendingIterator.itr
    }
  }
}
