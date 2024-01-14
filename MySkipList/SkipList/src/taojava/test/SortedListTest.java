package taojava.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

import org.junit.Test;

import taojava.util.SortedList;

/**
 * Generic tests of sorted lists.
 *
 * To test a particular implementation of sorted lists, subclass this
 * class and add an appropriate @Before clause to fill in strings and
 * ints.
 *
 * @author Samuel A. Rebelsky
 * @author Ameer Shujjah
 * @author Yazan Kittaneh
 */
public class SortedListTest
{
  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * A sorted list of strings for tests.  (Gets set by the subclasses.)
   */
  SortedList<String> strings;

  /**
   * A sorted list of integers for tests.  (Gets set by the subclasses.)
   */
  SortedList<Integer> ints;

  /**
   * A random number generator for the randomized tests.
   */
  Random random = new Random();

  // +---------+---------------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Dump a SortedList to stderr.
   */
  static <T extends Comparable<T>> void dump(SortedList<T> slist)
  {
    System.err.print("[");
    for (T val : slist)
    {
      System.err.print(val + " ");
    } // for
    System.err.println("]");
  } // dump

  /**
   * Determine if an iterator only returns values in non-decreasing
   * order.
   */
  static <T extends Comparable<T>> boolean inOrder(Iterator<T> it)
  {
    // Simple case: The empty iterator is in order.
    if (!it.hasNext())
      return true;
    // Otherwise, we need to compare neighboring elements, so
    // grab the first element.
    T current = it.next();
    // Step through the remaining elements
    while (it.hasNext())
    {
      // Get the next element
      T next = it.next();
      // Verify that the current node <= next
      if (current.compareTo(next) > 0)
      {
        return false;
      } // if (current > next)
      // Update the current node
      current = next;
    } // while
    // If we've made it this far, everything is in order
    return true;
  } // inOrder(Iterator<T> it)

  // +-------------+-----------------------------------------------------
  // | Basic Tests |
  // +-------------+

  /**
   * A really simple test.  Add an element and make sure that it's there.
   */
  @Test
  public void simpleTest()
  {
    strings.add("hello");
    //dump(strings);
    assertTrue(strings.contains("hello"));
    assertFalse(strings.contains("goodbye"));
  } // simpleTest()

  /**
   * Another simple test.  The list should not contain anything when
   * we start out.
   */
  @Test
  public void emptyTest()
  {
    assertFalse(strings.contains("hello"));
  } // emptyTest()

  // +-----------------+-------------------------------------------------
  // | RandomizedTests |
  // +-----------------+

  /**
   * Verify that a randomly created list is sorted.
   */
  @Test
  public void testOrdered()
  {
    // For reporting errors: an array of operations
    ArrayList<String> operations = new ArrayList<String>();
    // Add a bunch of values
    for (int i = 0; i < 100; i++)
    {
      int rand = random.nextInt(1000);
      ints.add(rand);
      operations.add("ints.add(" + rand + ")");
    } // for
    if (!inOrder(ints.iterator()))
    {
      System.err.println("inOrder() failed");
      for (String op : operations)
        System.err.println(op + ";");
      dump(ints);
      fail("The instructions did not produce a sorted list.");
    } // if the elements are not in order.
  } // testOrdered()

  /**
   * Verify that a randomly created list contains all the values
   * we added to the list.
   */
  @Test
  public void testContainsOnlyAdd()
  {
    ArrayList<String> operations = new ArrayList<String>();
    ArrayList<Integer> vals = new ArrayList<Integer>();
    // Add a bunch of values
    for (int i = 0; i < 100; i++)
    {
      int rand = random.nextInt(200);
      vals.add(rand);
      operations.add("ints.add(" + rand + ")");
      ints.add(rand);
    } // for i
    // Make sure that they are all there.
    for (Integer val : vals)
    {
      if (!ints.contains(val))
      {
        System.err.println("contains(" + val + ") failed");
        for (String op : operations)
          System.err.println(op + ";");
        dump(ints);
        fail(val + " is not in the sortedlist");
      } // if (!ints.contains(val))
    } // for val
  } // testContainsOnlyAdd()

  /**
   * An extensive randomized test.
   */
  @Test
  public void randomTest()
  {
    // Set up a list of all the operations we performed.  (That way,
    // we can replay a failed test.)
    ArrayList<String> operations = new ArrayList<String>();
    // Keep track of the values that are currently in the sorted list.
    ArrayList<Integer> vals = new ArrayList<Integer>();

    // Add a bunch of values
    for (int i = 0; i < 1000; i++)
    {
      boolean ok = true;
      int rand = random.nextInt(2000);
      // Half the time we add
      if (random.nextBoolean())
      {
        if (!ints.contains(rand))
          vals.add(rand);
        operations.add("ints.add(" + rand + ")");
        ints.add(rand);
        if (!ints.contains(rand))
        {
          System.err.println("After adding " + rand + " contains fails");
          ok = false;
        } // if (!ints.contains(rand))
      } // if we add
      // Half the time we remove
      else
      {
        operations.add("ints.remove(" + rand + ")");
        ints.remove(rand);
        vals.remove((Integer) rand);
        if (ints.contains(rand))
        {
          System.err.println("After removing " + rand
                  + " contains succeeds");
          ok = false;
        } // if ints.contains(rand)
      } // if we remove
      // See if all of the appropriate elements are still there
      for (Integer val : vals)
      {
        if (!ints.contains(val))
        {
          System.err.println("ints no longer contains " + val);
          ok = false;
          break;
        } // if the value is no longer contained
      } // for each value
      // Dump the instructions if we've encountered an error
      if (!ok)
      {
        for (String op : operations)
          System.err.println(op + ";");
        dump(ints);
        fail("Operations failed");
      } // if (!ok)
    } // for i
  } // randomTest()


  /**
   * Tests if repeated values are not added
   */
  @Test
  public void simpleRepeat()
  {
    for (int i = 0; i < 5; i++)
      ints.add(5);

    //remove 5
    ints.remove(5);
    assertFalse(ints.contains(5));
  } // simpleRepeat()

  /**
   * Tests if the remove method works
   */
  @Test
  public void simpleRemove()
  {
    for (int i = 0; i < 10; i++)  //Add values
      ints.add(i);
    for (int i = 0; i < 10; i++) //Remove values
      ints.remove(i);

    assertFalse(ints.contains(5));
  } // simpleRemove()

  /**
   * Checks if add() inserts values from a unordered pair
   * in the correct order
   */
  @Test
  public void orderedStrings()
  {
    String[] strs = new String[] { "aaac", "aaaab", "aaaaa", "aaaaad" };

    // Add a bunch of values
    for (int i = 0; i < 4; i++)
    {
      strings.add(strs[i]);
    } // for
    if (!inOrder(strings.iterator()))
    {
      System.err.println("inOrder() failed");
      fail("String set is not in order");
    } // if the elements are not in order.
  } // orderedStrings()

  /**
   * Tests if two different permutations of the same array
   * result in the same order within the SkipList
   */
  @Test
  public void stringPermutation()
  {
    //declarations
    String[] per1 = new String[] { "d", "e", "f", "g", "a", "b", "c" };
    String[] per2 = new String[] { "b", "d", "c", "e", "f", "a", "g" };
    boolean ok = true;

    // Add a bunch of values
    for (int i = 0; i < 7; i++)
    {
      strings.add(per1[i]);
    } // for
    if (!inOrder(strings.iterator()))
    {
      System.err.println("inOrder() failed");
      ok = false;
    } // if the elements are not in order.
    // empty SkipList
    for (int i = 0; i < 7; i++)
    {
      strings.remove(per1[i]);
    } // for

    // Add values from per2
    for (int i = 0; i < 7; i++)
    {
      strings.add(per2[i]);
    } // for
    if (!inOrder(strings.iterator()))
    {
      System.err.println("inOrder() failed");
      ok = false;
    } // if the elements are not in order.

    if (!ok)
      fail("Permutation test failure");
  } // stringPermutation

  /**
   * Tests to see if remove works in succession
   * (As appose to other implementations of lists which need to be
   * every time iterated)
   */
  @Test
  public void removeTwice()
  {
    String[] strs = new String[] { "aaa", "bbb", "ccc", "ddd", "eee" };

    for (int i = 0; i < 5; i++)
    {
      strings.add(strs[i]);
    } // for

    strings.remove("ccc");
    strings.remove("ddd");
    if (!inOrder(strings.iterator()))
    {
      System.err.println("inOrder() failed");
      fail("Removing consecutive elements does not work");
    } // if the elements are not in order.
  } // removeTwice()

  /**
   * Checks if a deleted node is actually deleted
   */
  @Test
  public void missingNodeTest()
  {
    for (int i = 0; i < 5; i++)
      ints.add(i);
    // remove an element and check
    ints.remove(3);
    System.out.println("get : " + ints.get(3));

    if (ints.get(3) != 4)
      fail("Node is still present; fail");
    // remove another element and check
    ints.remove(4);
    assertFalse(ints.contains(4));
    if (ints.get(2) == null)
      fail("Node is still present; fail");
  } // missingNodeTest()
} // class SortedListTest
