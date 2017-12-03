package parsing.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Artifact extends Entity {

    /** --- Ctors --- */

    public Artifact(String entity) {
        super(entity);
        this.businesses = new HashSet<>();
        this.creators = new HashSet<>();
    }


    /** --- Getter and Setter --- */

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public Set<Business> getBusinesses() {
        return businesses;
    }

    public Set<Person> getCreators() {
        return creators;
    }


    /** --- Data Members --- */

    /** NOTE: The members are PUBLIC because we use reflection to set them */

    public LocalDate creationDate;

    public Set<Business> businesses;

    public Set<Person> creators;

}
