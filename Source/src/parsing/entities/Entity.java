package parsing.entities;

public class Entity {

    /* --- Ctors --- */

    public Entity(String entity) {
        this.entity = entity;
    }


    /** --- Getters and Setters --- */

    public String getName() {
        return name;
    }

    public String getEntity() {
        return entity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }


    /** --- Date Members --- */

    /** NOTE: The members are PUBLIC because we use reflection to set them */

    /** YAGO's entity */
    public String       entity;

    /** Entity's name */
    public String       name;

    /** Entity's ID in the DB */
    public int         id;
}
