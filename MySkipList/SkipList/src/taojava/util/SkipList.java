
package taojava.util;
import java.util.Iterator;
import java.util.Random;

/**
 * A randomized implementation of sorted lists.
 * @author Samuel A. Rebelsky
 * @author Meyssam Rostamzadeh
 */
public class SkipList<T extends Comparable<T>>
    implements SortedList<T>
{
  Node head; //start of skiplist
  int maxLevel; //max vertical level for each node
  double probability;
  int size; //size of skiplist

  public class Node<T>
  {

    T val;
    Node<T>[] next; // it should be array because each node have multiple level...
    public Node(T value, int setLevel) //adding new node
    {
      this.val = value;
      next = new Node[setLevel + 1];

      for (int i = 0; i <= setLevel; i++)
      {
        next[i] = null; // for each level, set next to null
      }
    }
  }

  public SkipList(int level, double prob)
  {
    this.probability = prob;
    this.maxLevel = level;
    this.head = new Node(null, maxLevel);
    this.size = 0;
  }

  public int randomLevel()
  {
    int newLevel = 1;
    Random value = new Random();
    while (value.nextDouble() < this.probability)
    {
      newLevel++;
    }// while random is less than the probability
    return Math.min(newLevel, this.maxLevel);
  }


  public class SkipListIterator implements Iterator
  {
    Node current;
    public SkipListIterator()
    {
      current = head;
    }

    public boolean hasNext() // Check if the iterator has a next element.
    {
      return current.next[0] != null;
    }

    @SuppressWarnings("unchecked")
    public T next() //Return the next element.
    {
      current = current.next[0];
      return (T) current.val;
    }


    @SuppressWarnings("unchecked")
    public void remove() //Remove the current element.
    {
      SkipList.this.remove((T) this.current.val);
    }
  }

  @SuppressWarnings("unchecked")
  public Iterator<T> iterator()
  {
    return new SkipListIterator();
  }

  public void add(T val)
  {
    if (val == null)
    {
      return;
    } // if value is null, don't add anything

    Node current = this.head;
    Node[] update = new Node[this.maxLevel + 1];

    for (int level = this.maxLevel; level >= 0; level--)
    {
      while ((current.next[level] != null)
              && val.compareTo((T) current.next[level].val) > 0)
      {
        current = current.next[level];
      } //search for the preceding node
      update[level] = current;
    } // for all levels

    current = current.next[0];

    if (current != null && val.compareTo((T) current.val) == 0)
    {
      return;
    } // if already exists don't add
    else
    {
      //create a new node with a random level
      int newLevel = randomLevel();

      if (newLevel > this.maxLevel)
      {
        for (int level = this.maxLevel + 1; level <= newLevel; level--)
        {
          update[level] = this.head;
        } // for
        this.maxLevel = newLevel;
      } // if, new level is greater than maxlevel

      current = new Node(val, newLevel);

      for (int level = 0; level <= newLevel; level++)
      {
        current.next[level] = update[level].next[level];
        update[level].next[level] = current;
      } // update the pointers
    } // else
    this.size++;
  } // add(T val)


  @SuppressWarnings("unchecked")
  public boolean contains(T val)
  {
    Node<T> current = this.head;

    if (val == null)
    {
      return false;
    } // if searching for null return false
    else
    {
      for (int level = this.maxLevel; level >= 0; level--)
      {
        while ((current.next[level] != null)
                && val.compareTo((T) current.next[level].val) > 0)
        {
          current = current.next[level];
        } // while
      } // for all  levels
      current = current.next[0];
      // return val.equals(current.val);
    } // else if val is not null

    if (current == null) // if, not found
      return false;
    else
      return val.equals(current.val);
  } // contains(T)


  public void remove(T val)
  {
    if (val == null)
    {
      return;
    } // if value is null don't remove anything

    Node current = this.head;
    Node[] update = new Node[this.maxLevel + 1];

    for (int level = this.maxLevel; level >= 0; level--)
    {
      while ((current.next[level] != null) && val.compareTo((T) current.next[level].val) > 0)
      {
        current = current.next[level];
      } // while, finding the preceding nodes
      update[level] = current;
    } // for all levels

    current = current.next[0];

    if (current != null && val.compareTo((T) current.val) == 0)
    {
      for (int i = 0; i <= this.maxLevel; i++)
      {
        if (update[i].next[i] != current)
          break;
        update[i].next[i] = current.next[i];
      } //for, all the levels update the pointers
      current = null;
      while ((this.maxLevel > 1) && (this.head.next[this.maxLevel] == null))
      {
        this.maxLevel--;
      } // while

      this.size--;
      return;
    } // if, the val exists, then remove it
    else
    {
      return;
    } // else, if not found
  }// remove(T)


  public T get(int i) //Get the element at index i.
  {
    if ((i < 0) || (i > this.size))
    {
      return null; //index out of bounds
    }

    Node current = this.head.next[0];
    if (current == null)
    {
      return null;
    } // if list is empty

    for (int pos = 0; pos < i; pos++)
    {
      current = current.next[0];
    } // loop through to the index
    return (T) current.val;
  }

  public int length() //Determine the number of elements in the collection.
  {
    return this.size;
  }
}
