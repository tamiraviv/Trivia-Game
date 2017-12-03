package parsing.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Business extends Entity {

    /** ---  Ctors--- */

    public Business(String entity) {
        super(entity);
        this.cities = new HashSet<>();
        this.countries = new HashSet<>();
    }


    /** --- Getters and Setters --- */

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public long getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public Set<City> getCities() {
        return cities;
    }

    public Set<Country> getCountries() {
        return countries;
    }


    /** --- Data Members --- */

    /** NOTE: The members are PUBLIC because we use reflection to set them */

    public LocalDate creationDate;

    public long numberOfEmployees;

    public Set<City> cities;

    public Set<Country> countries;

}
