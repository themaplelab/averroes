package averroes.tests;

import averroes.tests.junit.TestAnonymousClasses;
import averroes.tests.junit.TestArrays;
import averroes.tests.junit.TestCasting;
import averroes.tests.junit.TestExceptions;
import averroes.tests.junit.TestInheritance;
import averroes.tests.junit.TestLinkedList;
import averroes.tests.junit.TestMultipleClasses;
import averroes.tests.junit.TestNestedClasses;
import averroes.tests.junit.TestPrivateClasses;
import averroes.tests.junit.TestSimple;
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
