package parsing.entities;

import java.time.LocalDate;

public class PopulatedRegion extends Entity {

    /** --- Ctors ---*/

    public PopulatedRegion(String entity) {
        super(entity);
    }


    /** --- Gettes and Setters --- */

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public float getEconomicGrowth() {
        return economicGrowth;
    }

    public float getPoverty() {
        return poverty;
    }

    public long getPopulation() {
        return population;
    }

    public float getUnemployment() {
        return unemployment;
    }

    public float getGini() {
        return gini;
    }

    public float getInflation() {
        return inflation;
    }

    public float getPopulationDensity() {
        return populationDensity;
    }


    /** --- Data Members --- */

    /** NOTE: The members are PUBLIC because we use reflection to set them */

    public LocalDate    creationDate;
    public float        economicGrowth;                 // %
    public float        poverty;                        // %
    public long         population;
    public float        unemployment;                   // %
    public float        gini;
    public float        inflation;                      // %
    public float        populationDensity;              // 1/km^2
}
