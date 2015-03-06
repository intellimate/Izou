package intellimate.izou;

import intellimate.izou.identification.Identifiable;
import intellimate.izou.identification.IdentificationManager;

import java.util.HashSet;
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
public class AddOnsBla<X extends Identifiable> {
    private IdentificationManager identificationManager = IdentificationManager.getInstance();
    private Set<X> set = new HashSet<>();

    public AddOnsBla(Set<X> set) {
        this.set = set;
    }

    public AddOnsBla() {
    }

    public boolean add(X x) {
        return set.add(x);
    }

    public boolean remove(X x) {
        return  set.remove(x);
    }

    public Stream<X> stream() {
        return set.stream();
    }
}

