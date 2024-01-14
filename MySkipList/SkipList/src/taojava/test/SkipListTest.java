package taojava.test;

import org.junit.Before;

import taojava.util.SkipList;

/**
 * Tests of sorted array lists.
 *
 * @author Samuel A. Rebelsky
 */
public class SkipListTest
extends SortedListTest
{
  @Before
  public void setup()
  {
    this.ints = new SkipList<Integer>(20, 0.5);
    this.strings = new SkipList<String>(20, 0.5);
  } // setup
} // SkipListTest
