package averroes.tests;

import averroes.tests.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  TestAnonymousClasses.class,
  TestArrays.class,
  TestCasting.class,
  TestExceptions.class,
  TestInheritance.class,
  TestLinkedList.class,
  TestMultipleClasses.class,
  TestNestedClasses.class,
  TestPrivateClasses.class,
  TestSimple.class
})
public class AllTests {}
