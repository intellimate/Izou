package intellimate.izou;

import intellimate.izou.identification.Identifiable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Use this class if you want to store a Set of AddOnModules permanently.
 * <p>
 * The reason for this class is that in the future it might be needed to de-register an AddOn and with this class it is
 * easy to introduce this Feature.
 * </p>
 * @author Leander Kurscheidt
 * @version 1.0
 */
public class IdentifiableCollection<X extends Identifiable> implements Iterable<X> {
    private Set<X> set = new HashSet<>();

    public IdentifiableCollection(Set<X> set) {
        this.set = set;
    }

    public IdentifiableCollection() {}

    public boolean add(X x) {
        return set.add(x);
    }

    public boolean addAll(Collection<? extends X> x) {
        return set.addAll(x);
    }

    public boolean remove(X x) {
        return  set.remove(x);
    }
    
    public boolean contains(X x) {
        return set.contains(x);
    }

    public Stream<X> stream() {
        return set.stream();
    }
    
    public Set<X> getCopy() {
        return new HashSet<>(set);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<X> iterator() {
        return set.iterator();
    }
}


