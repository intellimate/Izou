package org.intellimate.izou.internal.util;

import org.intellimate.izou.identification.Identifiable;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Use this class if you want to store a Set of AddOnModules permanently.
 * <p>
 * The reason for this class is that in the future it might be needed to unregister an AddOn and with this class it is
 * easy to introduce this feature.
 * </p>
 * @author Leander Kurscheidt
 * @version 1.0
 */
public class IdentifiableSet<X extends Identifiable> extends AbstractSet<X> implements Set<X>, Cloneable {
    private Set<X> set = new HashSet<>();

    public IdentifiableSet(Set<X> set) {
        this.set = set;
    }

    public IdentifiableSet() {}

    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
        return set.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              set does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set. The elements are returned in no particular order.
     * @return an Iterator over the elements in this set
     */
    public Iterator<X> iterator() {
        return set.iterator();
    }

    /**
     * Adds an Element to the Set
     * @param x the Element
     * @return true if this set did not already contain the specified element
     * @throws java.lang.IllegalArgumentException if it is not allowed to put Elements without Identification in this
     *                                            Set
     */
    @Override
    public boolean add(X x) {
        return set.add(x);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation iterates over the collection looking for the
     * specified element.  If it finds the element, it removes the element
     * from the collection using the iterator's remove method.
     * </p>
     *
     * @param o the object to remove
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation iterates over this collection, removing each
     * element using the <tt>Iterator.remove</tt> operation.  Most
     * implementations will probably choose to override this method for
     * efficiency.
     * </p>
     */
    @Override
    public void clear() {
        set.clear();
    }

    /**
     * Returns a shallow copy of this HashSet instance: the elements themselves are not cloned.
     *
     * @return a shallow copy of this set
     */
    @Override
    public Object clone() {
        try {
            IdentifiableSet<X> newSet = (IdentifiableSet<X>) super.clone();
            newSet.set = (Set<X>) ((HashSet<X>) set).clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}


