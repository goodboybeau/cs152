package a6;

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

/**
    Instances of this class represent nodes of ordered trees where
    the data in each node is of a class specified by the user.

    Parse trees, syntax trees, and other ordered trees may be represented
    by their roots.

    There is no upper bound on the number of children that an ordered tree
    may have.

    Null children will be removed by the constructor.

    Instances of this class are immutable.

    The cast in the equals method will give a type safety warning.
    This warning may be ignored for CS 152.

    @author Jeff Smith
    @version for CS 152, Spring 2014, SJSU

 */


public class OrderedTree<E>
{

   // the data in the node
   private E data;

   // a list of the node's children
   private LinkedList<OrderedTree<E>> children;


   /**
      Builds an ordered tree with null data and an empty
        list of children.
   */

   public OrderedTree()                            {
     this(null, null);                             }


   /**
       Builds an ordered tree from given data and children.
       A shallow copy is made of the list of children.
       @param data the data for the node.  Null values
          are permitted
       @param children the list of children of the node.
          Null lists are converted to empty lists.
          Null list members are ignored
    */

   public OrderedTree(E data, List<OrderedTree<E>> children)        {
     this.data = data;
     if (children == null)
       this.children = new LinkedList<OrderedTree<E>>();
     else                                                         {
       this.children = new LinkedList<OrderedTree<E>>();
       for (OrderedTree<E> child : children)
         if (child != null) this.children.add(child);             } }


   /**
       Builds an ordered tree with no children from a given data item..
       @param data the data for the node.  Null values
          are permitted
   */

   public OrderedTree(E data)                                        {
     this(data, null);                                               }



    /**
       Finds the value of the data in the root
       @return the value of the data in the root (which may be null)
    */

   public E getRootData()                                            {
     return data;                                                    }


   /**
       Returns the number of children of the tree
       @return the number of children
   */

   public int getNumberOfChildren()                                  {
     return children.size();                                         }


   /**
       Returns the kth child of the tree.
       No copy is made of this child
       @param k the index of the child to be
         returned, starting with 1 for the first child
       @return the kth child
   */

   public OrderedTree<E> getKthChild(int k)                          {
     return children.get(k-1);                                       }


   /**
        Determines whether the ordered tree equals another ordered
          tree.  The comparison is a deep comparison that uses
          <code>equals</code> to compare each node.  In particular,
          two trees without children are equal iff their roots are.
        @param o the other ordered tree
        @return <code>true</code> iff the two trees are equal
    */

   public boolean equals(Object o)                                  {
     if (o == null)
       return false;
     if (getClass() != o.getClass())
       return false;
     OrderedTree<E> otherTree = (OrderedTree<E>) o;
     if (data == null && otherTree.data != null)
       return false;
     if (data != null && !data.equals(otherTree.data))
       return false;
     for (int k = 0; k<children.size(); k++)                      {
       OrderedTree<E> kthChild = children.get(k);
       if (!kthChild.equals(otherTree.children.get(k)))
           return false;                                          }
     return true;                                                   }


   /**
       Traverses the tree rooted at the node, in preorder.
       A null data value is treated as an empty string, and
       a null list of children is treated as an empty list.
       @return a string representing the result of preorder
         traversal, with the contents of every node appearing
         on a separate line, indented an amount proportional to
         its distance from the root
    */

   public String toString()                                       {
     return toString("", System.getProperty("line.separator"));   }


   /**
        A function that behaves as toString, but takes the amount
        of indentation of the first line as a parameter.
        @param indentation a string of spaces representing
          the indentation of each child from its parent
        @param newline the string to be used to separate lines of output
        @return a printed representation of the traversal, suitable
           as a return value for toString()
    */

   private String toString(String indentation, String newline)      {
     StringBuffer resultSoFar =
       new StringBuffer(indentation);  // won't be null

     // print the data, appropriately indented

     if (data == null)
       resultSoFar.append("null");
     else
       resultSoFar.append(data.toString());
     resultSoFar.append(newline);

     // recursively print the children,
     //   indented an additional two spaces

     if (children == null)
       return new String(resultSoFar);
     ListIterator<OrderedTree<E>> lit = children.listIterator();
     while (lit.hasNext())                                  {
       OrderedTree<E> nextChild = lit.next();
       String childString =
         nextChild.toString(indentation + "..", newline);
       resultSoFar.append(childString);                     }
     return new String(resultSoFar);                             }


   /**
        A test method, mainly to check null components
    */

   public static void main(String[] args)                        {
     OrderedTree<String> a = new OrderedTree<String>("a",null);
     OrderedTree<String> b = new OrderedTree<String>("b",null);
     LinkedList<OrderedTree<String>> c1 =
       new LinkedList<OrderedTree<String>>();
     LinkedList<OrderedTree<String>> c2 =
       new LinkedList<OrderedTree<String>>();

     // t1 and t2 should be equal, since null children
     //   are ignored by the constructor

     // the method shouldn't crash when comparing null root data

     c1.add(a);
     c1.add(null);
     c1.add(b);
     c2.add(a);
     c2.add(b);
     c2.add(null);
     OrderedTree<String> t1 = new OrderedTree<String>("r", c1);
     OrderedTree<String> t2 = new OrderedTree<String>("r", c2);
     OrderedTree<String> t3 = new OrderedTree<String>(null, c1);

     System.out.println(t1);
     System.out.println(t2);
     System.out.println(t3);
     System.out.println(t1.equals(t1));
     System.out.println(t2.equals(t2));
     System.out.println(t1.equals(t2));
     System.out.println(t2.equals(t1));
     System.out.println(t3.equals(t3));
     System.out.println();

     System.out.println(t1.equals(t3));
     System.out.println(t2.equals(t3));
     System.out.println(t3.equals(t1));
     System.out.println(t3.equals(t2));
     System.out.println(t1.equals(null));
     System.out.println(t2.equals(null));
     System.out.println(t3.equals(null));
                                                                 }

}