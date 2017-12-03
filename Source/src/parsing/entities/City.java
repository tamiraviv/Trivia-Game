package parsing.entities;

public class City extends PopulatedRegion {

    /** --- Ctors --- */

    public City(String entity) {
        super(entity);
    }


    /** --- Getters and Setters --- */

    public Country getCountry() {
        return country;
    }


    /** --- Data Members --- */

    /** NOTE: The members are PUBLIC because we use reflection to set them */

    public Country country;
}
