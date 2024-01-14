package taojava.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * A randomized implementation of sorted lists.
 * @author Samuel A. Rebelsky
 * @author Meyssam Rostamzadeh

 */
public class SkipList<T extends Comparable<T>>
    implements SortedList<T>
{
  int maxLevel; //Maximum allowed levels in the list

  int levels; // total levels of current node

  Node<T> first;

  Node<T> last;
  int mods = 0; //Number of modifications made to the SkipList
  Random random = new Random();

  double probability = 0.5; //used for generating random level for new node


  public class Node<T>
  {

    T val; //actual value of node
    Node<T>[] forward; //Array of forward pointers, size determined by level of node.
    @SuppressWarnings("unchecked")
    public Node(T val, int level) //generate new node
    {
      this.val = val;
      this.forward = new Node[level + 1];
      for (int i = 0; i < level; i++)
        {
          forward[i] = null;
        }
    }
  }
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public SkipList(int max) //Creates a SkipList with a specified maxLevel
  {
    //at start, just two node with value == null.
    // first node point to last node in all levels
    this.maxLevel = max;
    this.levels = 0;
    this.first = new Node(null, maxLevel);
    this.last = new Node(null, maxLevel);
    for (int i = 0; i < maxLevel; i++)
      {
        this.last.forward[i] = null;
        this.first.forward[i] = last;
      }
  }

  public SkipList() //Creates a SkipList with a maxLevel of 20.
  {
    this(20);
  }
  private int randomLevel() //generate a random level for new node
  {
    int newLevel = 0;
    while (this.random.nextDouble() < this.probability)
      {
        newLevel = newLevel + 1;
      }
    return Math.min(newLevel, this.maxLevel - 1);
  }
  @SuppressWarnings("unused")
  private boolean hasNext(Node<T> node, int level)
  {
    return node.forward[level].val != null;
  }

  /**
   * Return a read-only iterator (one that does not implement the remove
   * method) that iterates the values of the list from smallest to
   * largest.
   */
  public Iterator<T> iterator()
  {
    return new Iterator<T>()
      {
        /**
         * The node that immediately precedes the value to be returned by
         * next.
         */
        Node<T> cursor = SkipList.this.first;

        int mods = SkipList.this.mods; //The number of modifications at the time this iterator was created or last updated.

        void failFast() //Determine if the list has been updated since this iterator was created or modified.
        {
          if (this.mods != SkipList.this.mods)
            throw new ConcurrentModificationException();
        }

        @Override
        public boolean hasNext()
        {
          failFast();
          return SkipList.this.hasNext(cursor, 0);
        }

        @Override
        public T next()
        {
          failFast();
          if (!this.hasNext())
            throw new NoSuchElementException();
          // Advance to the next node.
          this.cursor = this.cursor.forward[0];
          // Return the data in the now current node.
          return this.cursor.val;
        }

        @Override
        public void remove()
        {
          T val = cursor.val;
          SkipList.this.remove(val);
          this.mods++;
        }
      };
  }

  public void add(T val)
  {
    int newLevel = randomLevel();
    @SuppressWarnings("unchecked")
    Node<T>[] update = new Node[this.maxLevel];
    Node<T> newNode = new Node<T>(val, newLevel);
    Node<T> tmp = this.first;
    for (int i = this.levels; i >= 0; i--)
      {
        while (hasNext(tmp, i) && tmp.forward[i].val.compareTo(val) < 0)
          {
            tmp = tmp.forward[i];
          }
        update[i] = tmp;
      }
    if (hasNext(tmp, 0) && tmp.forward[0].val.compareTo(val) == 0)
      return;
    // if the newLevel is bigger than levels
    if (newLevel > this.levels)
      {
        for (int i = this.levels + 1; i <= newLevel; i++)
          {
            update[i] = this.first;
          } // for
        this.levels = newLevel;
      } // if 
    for (int i = 0; i <= newLevel; i++)
      {
        newNode.forward[i] = update[i].forward[i];
        update[i].forward[i] = newNode;
      }
    this.mods++;
  }
  @SuppressWarnings("unused")
  public boolean contains(T val)
  {
    Node<T> tmp = this.first;
    for (int i = this.levels; i >= 0; i--)
      {
        while (hasNext(tmp, i) && tmp.forward[i].val.compareTo(val) < 0)
          {
            tmp = tmp.forward[i];
          }
      }
    if(!hasNext(tmp, 0))
      {
        return false;
      }
    tmp = tmp.forward[0];
    if (tmp.val.compareTo(val) == 0)
      return true;
    else
      return false;
  }


  @SuppressWarnings({ "unused", "unchecked" })
  public void remove(T val)
  {
    Node<T>[] update = new Node[maxLevel];
    Node<T> tmp = this.first;
    for (int i = this.levels; i >= 0; i--)
      {
        while (hasNext(tmp, i) && tmp.forward[i].val.compareTo(val) < 0)
          {
            tmp = tmp.forward[i];
          }
        update[i] = tmp;
      }
    tmp = tmp.forward[0];
    if (tmp.val != null && tmp.val.compareTo(val) == 0)
      {
        for (int i = 0; i <= this.levels; i++)
          {
            if (!update[i].forward[i].equals(tmp))
              break;
            update[i].forward[i] = tmp.forward[i];
          }
        while (this.levels > 0 && this.first.forward[this.levels].val == null)
          {
            this.levels--;
          }
        this.mods++;
      }
  }
  public T get(int i)
  {
    Node<T> tmp = this.first;
    for (int j = 0; j < i; j++)
      {
        if (!hasNext(tmp, 0))
          {
            throw new IndexOutOfBoundsException();
          }
        tmp = tmp.forward[0];
      }
    if (!hasNext(tmp, 0))
      {
        throw new IndexOutOfBoundsException();
      } // if
    return tmp.forward[0].val;
  }
  public int length()
  {
    int length = 0;
    Node<T> tmp = this.first;
    while (hasNext(tmp, 0))
      {
        tmp = tmp.forward[0];
        length++;
      }
    return length;
  }

}
