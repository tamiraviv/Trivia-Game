package parsing.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class University extends Entity {

    /** --- Ctors --- */

    public University(String entity) {
        super(entity);
    }


    /** --- Getters and Setters --- */

    public Set<Country> getCountries() {
        return countries;
    }

    public Set<City> getCities() {
        return cities;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }


    /** --- Data Members --- */

    /** NOTE: The members are PUBLIC because we use reflection to set them */

    public Set<Country> countries = new HashSet<>();

    public Set<City> cities = new HashSet<>();

    public LocalDate creationDate;

}
