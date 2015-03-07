package intellimate.izou;

import intellimate.izou.identification.Identifiable;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IdentificationManager;

import java.util.*;

/**
 * it has the same Properties as an normal HashSet, but (optionally) keeps an Identification for every object to
 * identify its source.
 * @author Leander Kurscheidt
 * @version 1.0
 */
public class IdentifiableSet<X> extends AbstractSet<X> implements Set<X>, Cloneable, Identifiable {
    private HashMap<X, Identification> map;
    private boolean allowElementsWithoutIdentification;
    private static Identification placeholder = null;

    /**
     * Constructs a new, empty set;
     * <p>
     * the backing HashMap instance has default initial capacity (16) and load factor (0.75).
     * </p>
     */
    public IdentifiableSet() {
        map = new HashMap<>();
        init();
    }

    /**
     * Constructs a new, empty set.
     * <p>the backing HashMap instance has the specified initial capacity and the specified load factor.</p>
     * @param initialCapacity the initial capacity of the hash map
     * @param loadFactor the load factor of the hash map
     * @throws java.lang.IllegalArgumentException if the initial capacity is less than zero, or if the load factor is
     *                                          nonpositive
     */
    public IdentifiableSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
        init();
    }

    /**
     * Constructs a new, empty set.
     * <p>the backing HashMap instance has the specified initial capacity and default load factor (0.75).</p>
     * @param initialCapacity initialCapacity the initial capacity of the hash table
     * @throws java.lang.IllegalArgumentException if the initial capacity is less than zero
     */
    public IdentifiableSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
        init();
    }

    /**
     * Constructs a new, empty set;
     * <p>
     * the backing HashMap instance has default initial capacity (16) and load factor (0.75).
     * </p>
     * @param allow whether it is allowed to put Elements without Identification in this Set
     */
    public IdentifiableSet(boolean allow) {
        map = new HashMap<>();
        allowElementsWithoutIdentification = allow;
        init();
    }

    /**
     * Constructs a new, empty set.
     * <p>the backing HashMap instance has the specified initial capacity and the specified load factor.</p>
     * @param initialCapacity the initial capacity of the hash map
     * @param loadFactor the load factor of the hash map
     * @param allow whether it is allowed to put Elements without Identification in this Set
     * @throws java.lang.IllegalArgumentException if the initial capacity is less than zero, or if the load factor is
     *                                          nonpositive
     */
    public IdentifiableSet(int initialCapacity, float loadFactor, boolean allow) {
        map = new HashMap<>(initialCapacity, loadFactor);
        allowElementsWithoutIdentification = allow;
        init();
    }

    /**
     * Constructs a new, empty set.
     * <p>the backing HashMap instance has the specified initial capacity and default load factor (0.75).</p>
     * @param initialCapacity initialCapacity the initial capacity of the hash table
     * @param allow whether it is allowed to put Elements without Identification in this Set
     * @throws java.lang.IllegalArgumentException if the initial capacity is less than zero
     */
    public IdentifiableSet(int initialCapacity, boolean allow) {
        map = new HashMap<>(initialCapacity);
        allowElementsWithoutIdentification = allow;
        init();
    }

    /**
     * initializes some common fields in the Set
     */
    private void init() {
        if (placeholder == null) {
            IdentificationManager.getInstance().registerIdentification(this);
            Optional<Identification> identification = IdentificationManager.getInstance().getIdentification(this);
            if (!identification.isPresent()) {
                throw new IllegalStateException("Unable to obtain Identification");
            } else {
                placeholder = identification.get();
            }
        }
    }

    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
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
        return map.containsKey(o);
    }

    /**
     * Returns an iterator over the elements in this set. The elements are returned in no particular order.
     * @return an Iterator over the elements in this set
     */
    public Iterator<X> iterator() {
        return map.keySet().iterator();
    }

    /**
     * An ID must always be unique.
     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
     * If you have to implement this interface multiple times, just concatenate unique Strings to
     * .class.getCanonicalName()
     *
     * @return A String containing an ID
     */
    @Override
    public String getID() {
        return IdentifiableSet.class.getCanonicalName();
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
        if (!allowElementsWithoutIdentification)
            throw new IllegalArgumentException("It is not allowed to put Elements without Identification in this Set");

        return map.put(x, placeholder) == null;
    }

    /**
     * Adds an Element to the Set
     * @param x the Element
     * @param identification the identification
     * @return true if this set did not already contain the specified element
     */
    public boolean add(X x, Identification identification) {
        return map.put(x, identification) == null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>This implementation iterates over the collection looking for the
     * specified element.  If it finds the element, it removes the element
     * from the collection using the iterator's remove method.
     * <p>
     *
     * @param o
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>This implementation iterates over this collection, removing each
     * element using the <tt>Iterator.remove</tt> operation.  Most
     * implementations will probably choose to override this method for
     * efficiency.
     * <p>
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns a shallow copy of this HashSet instance: the elements themselves are not cloned.
     *
     * @return a shallow copy of this set
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        try {
            IdentifiableSet<X> newSet = (IdentifiableSet<X>) super.clone();
            newSet.map = (HashMap<X, Identification>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * returns the associated Identification (if it was added with an identification)
     * @param x the Element
     * @return the identification or an Empty Optional
     */
    public Optional<Identification> getIdentificationFor(X x) {
        Identification identification = map.get(x);
        if (identification == null || identification.equals(placeholder)) {
            return Optional.empty();
        } else {
            return Optional.of(identification);
        }
    }
}
