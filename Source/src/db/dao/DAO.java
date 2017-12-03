package db.dao;

import parsing.DataCollector;
import db.core.DBConnection;
import db.core.DBException;
import utils.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DAO {

    private DBConnection connection;

    public DAO() {
        connection = new DBConnection();
    }

    public void connect(DBUser user, String host) throws DAOException {
        try {
            connection.connect(user, host);
        } catch (DBException e) {
            throw new DAOException("Could not connect to DB: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection.isConnected()) {
                connection.disconnect();
            }
        } catch (DBException e) {
        }
    }

    public void deleteDB() throws DAOException {
        try {
            connection.deleteData();
        } catch (DBException e) {
            throw new DAOException("Could not delete data from DB: " + e.getMessage());
        }
    }

    /* --- Entity existence validation --- */

    public Boolean checkIfEntityExists(String entityType, int id) throws DAOException {
        try {
            int answer = connection.getCountOf(entityType, id);
            return answer != 0;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    public Boolean checkIfEntityExists(String entityType, Collection<Integer> id) throws DAOException {
        try {
            Collection<Integer> answer = connection.getCountOf(entityType, new ArrayList<Integer>(id));
            return (answer.size() == id.size());
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    private void validateUserExists(int userID) throws EntityNotFound, DAOException {
        if (!checkIfEntityExists("User", userID))
            throw new EntityNotFound(String.format("User ID %s does not exists", userID));
    }

    private void validateCountryExists(int countryID) throws EntityNotFound, DAOException {
        if (!checkIfEntityExists("Country", countryID)) {
            throw new EntityNotFound(String.format("Country ID %s does not exists", countryID));
        }
    }
    private void validateCountryExists(Collection<Integer> countryIDCollection) throws EntityNotFound, DAOException {
        if (!checkIfEntityExists("Country", countryIDCollection)) {
            throw new EntityNotFound("One of the countries does not exists");
        }
    }

    private void validateCityExists(int cityID) throws EntityNotFound, DAOException {
        if (!checkIfEntityExists("City", cityID)) {
            throw new EntityNotFound(String.format("City ID %s does not exists", cityID));
        }
    }

    private void validatePersonExists(int personID) throws EntityNotFound, DAOException {
        if (!checkIfEntityExists("Person", personID)) {
            throw new EntityNotFound(String.format("Person ID %s does not exists", personID));
        }
    }

    /* --- Upload data from Yago --- */

    public void uploadDataCollector(DataCollector dataCollector) throws DAOException {
        try {
            connection.uploadCountries(new LinkedList<>(dataCollector.getCountries()));
            connection.uploadCities(new LinkedList<>(dataCollector.getCities()));
            connection.uploadUniversities(new LinkedList<>(dataCollector.getUniversities()));
            connection.uploadBusinesses(new LinkedList<>(dataCollector.getBusinesses()));
            connection.uploadPersons(new LinkedList<>(dataCollector.getPersons()));
            connection.uploadArtifacts(new LinkedList<>(dataCollector.getArtifacts()));
        } catch (DBException e) {
            throw new DAOException("Could not upload data to DB: " + e.getMessage());
        }
    }

    /* --- Users --- */

    /**
     * @return user id
     */
    public int createUser(String name, String password) throws DAOException {
        try {
            return connection.createUser(name, password);
        } catch (DBException e) {
            throw new DAOException("Could not create user: " + e.getMessage());
        }
    }

    public boolean checkPassword(int userID, String givenPassword) throws DAOException, EntityNotFound {
        try {
            validateUserExists(userID);
            if (1 == connection.checkPassword(userID, givenPassword)) {
                return true;
            }
            return false;
        } catch (DBException e) {
            throw new DAOException("Could not check password: " + e.getMessage());
        }
    }

    public int getUserID(String name) throws DAOException, DataNotFoundException {
        try {
            Collection<String> result = connection.getUserID(name);
            if (result.isEmpty()) {
                throw new DataNotFoundException("Could not find user " + name);
            }
            return Integer.parseInt(result.iterator().next());
        } catch (DBException e) {
            throw new DAOException("Could not fetch user ID: " + e.getMessage());
        }
    }

    public String getUserName(int id) throws DAOException, EntityNotFound {
        try {
            validateUserExists(id);
            return connection.getUserName(id).iterator().next();
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }


    public void setUserAnsweredCorrectly(int userID) throws DAOException, EntityNotFound {
        try {
            validateUserExists(userID);
            connection.setUserAnsweredCorrectly(userID,
                    connection.getUserAnsweredCorrectly(userID) + 1);
        } catch (DBException e) {
            throw new DAOException("Could not add answer to user");
        }
    }

    public void setUserAnsweredWrong(int userID) throws DAOException, EntityNotFound {
        try {
            validateUserExists(userID);
            connection.setUserAnsweredWrong(userID,
                    connection.getUserAnsweredWrong(userID) + 1);
        } catch (DBException e) {
            throw new DAOException("Could not add answer to user");
        }
    }

    public void setUserStartedNewGame(int userID) throws DAOException, EntityNotFound {
        try {
            validateUserExists(userID);
            connection.setUserStartedNewGame(userID,
                    connection.setUserStartedNewGame(userID) + 1);
        } catch (DBException e) {
            throw new DAOException("Could not add answer to user");
        }
    }

    public void setScore(int userID, int score, java.util.Date date) throws DAOException, EntityNotFound {
        try {
            validateUserExists(userID);
            connection.setScore(userID, score, date);
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    public List<UserIDScoreDate> getTopScore(int howMany) throws DAOException {
        try {

            return connection.getTopScore(howMany);
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    public List<UserIDScoreDate> getTopScoreByUser(int userID, int howMany) throws DAOException, EntityNotFound {
        try {
            validateUserExists(userID);
            return connection.getTopScoreByUser(userID, howMany);
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* --- General Entities --- */

    /**
     * Returns entity's ID.
     * Example: getID("country", "Israel")
     */
    public int getID(String entityType, String name) throws DAOException, DataNotFoundException {
        try {
            if (connection.getCountOf(entityType, name) > 0) {
                return connection.getEntityID(entityType, name);
            }
            throw new DataNotFoundException(String.format("%s with name %s does not Exists", entityType, name));
        } catch (DBException e) {
            throw new DAOException("Could not fetch country ID: " + e.getMessage());
        }
    }

    /* --- Countries --- */

    public Collection<IDName> getAllCountries() throws DAOException {
        try {
            return connection.getAllCountries();
        } catch (DBException e) {
            throw new DAOException("Could not get countries: " + e.getMessage());
        }
    }

    /**
     * Return a set of size count with random countries
     */
    public Collection<IDName> getRandomCountries(int count) throws DAOException {
        try {
            return connection.getRandomCountries(count);
        } catch (DBException e) {
            throw new DAOException("Could not get random countries: " + e.getMessage());
        }
    }

    /* How many people lives in X? */
    public int getNumberOfPeopleInCountry(int countryID) throws DAOException, DataNotFoundException, EntityNotFound {
        try {
            validateCountryExists(countryID);
            int numberOfPeople = connection.getNumberOfPeopleInCountryOrderDescResult(countryID);
            if (numberOfPeople > 0)
                return numberOfPeople;
            throw new DataNotFoundException("Data is not found in DB");
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which country is the most populated */
    /**
     * Returns the id of the most populated country among the list.
     *
     * NOTE: You should check first that the countries has a populated data. do this with "getNumberOfPeopleInCountry".
     */
    public int getMostPopulatedCountry(List<Integer> countryIDList) throws DAOException, DataNotFoundException, EntityNotFound {
        try {
            validateCountryExists(countryIDList);
            return connection.getMostPopulatedCountry(countryIDList);
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which country is the least populated */
    /**
     * Returns the id of the least populated country among the list.
     *
     * NOTE: You should check first that the country has a populated data. do this with "getNumberOfPeopleInCountry".
     */
    public int getLeastPopulatedCountry(List<Integer> countryIDList) throws DAOException, DataNotFoundException, EntityNotFound {
        try {
            validateCountryExists(countryIDList);
            return connection.getLeastPopulatedCountry(countryIDList);
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which country is more populated than X? */
    /* Which country is less populated than X? */
    /**
     * NOTE: you should check first that the country has a population data.
     */
    public Collection<IDName> getCountryThatIsMorePopulatedThan(int countryID, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);
        try {
            Collection<IDName> answer = connection.getCountryThatIsMorePopulatedThan(countryID, count);
            if(answer.size() == count) {
                return answer;
            }
            throw new DataNotFoundException(String.format("Can not find %s countries that are more populated than %s", count, countryID));
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /**
     * NOTE: you should check first that the country has a population data.
     */
    public Collection<IDName> getCountryThatIsLessPopulatedThan(int countryID, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);
        try {
            Collection<IDName> answer = connection.getCountryThatIsLessPopulatedThan(countryID, count);
            if(answer.size() == count) {
                return answer;
            }
            throw new DataNotFoundException(String.format("Can not find %s countries that are mote populated than %s", count, countryID));
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* When does X created? */
    public Date getCreationDate(int countryID) throws DAOException, DataNotFoundException, EntityNotFound {
        validateCountryExists(countryID);

        try {
            Date creation_date = connection.getCountryCreationDate(countryID);
            if (creation_date != null) {
                return creation_date;
            }
            throw new DataNotFoundException(String.format("Creation date of %s does not in DB", countryID));
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which country is the oldest? */
    /** Note: You should validate that the country has a creation date */
    public int getOldestCountry(Collection<Integer> integerCollection) throws DAOException, EntityNotFound {
        validateCountryExists(integerCollection);

        try {
            return connection.getTheOldestCountry(new ArrayList<>(integerCollection));
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which country Created before X but after Y? */
    public IDName getCountryCreatedBetween(int afterCountry, int beforeCountry) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(afterCountry);
        validateCountryExists(beforeCountry);

        try {
            IDName result = connection.getCountryCreatedBetween(afterCountry, beforeCountry);
            if (result == null) {
                throw new DataNotFoundException("Can not find entity between countries");
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }
    public IDName getCountryNotCreatedBetween(int afterCountry, int beforeCountry) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(afterCountry);
        validateCountryExists(beforeCountry);

        try {
            IDName result = connection.getCountryNotCreatedBetween(afterCountry, beforeCountry);
            if (result == null) {
                throw new DataNotFoundException("Can not find entity between countries");
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* --- Cities --- */

    /* Which city is in X? */
    /* Which city is not in X? */
    /* Which city is different? */
    public Collection<IDName> getRandomCitiesByCountry(int countryId, int count) throws DAOException, DataNotFoundException, EntityNotFound {
        validateCountryExists(countryId);
        try {
            Collection<IDName> answer = connection.getCities(countryId, count);
            if(answer.size() != count) {
                throw new DataNotFoundException(String.format("Can not find %s cities in %s", count, countryId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not get random cities: " + e.getMessage());
        }
    }
    public Collection<IDName> getRansomCitiesNotInCountry(int countryId, int count) throws DAOException, DataNotFoundException, EntityNotFound {
        validateCountryExists(countryId);
        try {
            Collection<IDName> answer = connection.getCitiesNotIn(countryId, count);
            if (answer.size() != count) {
                throw new DataNotFoundException(String.format("Can not find %s cities that are not in %s", count, countryId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    public Collection<IDName> getRandomCitiesByCountryWithCreationDate(int countryId, int count) throws DAOException, DataNotFoundException, EntityNotFound {
        validateCountryExists(countryId);
        try {
            Collection<IDName> answer = connection.getCitiesWithCreationDate(countryId, count);
            if(answer.size() != count) {
                throw new DataNotFoundException(String.format("Can not find %s cities in %s", count, countryId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not get random cities: " + e.getMessage());
        }
    }

    /* What is the oldest city in X? */
    public IDName getOldestCity(int countryId) throws DAOException, DataNotFoundException, EntityNotFound {
        validateCountryExists(countryId);
        try {
            IDName answer = connection.getOldestCity(countryId);
            if (answer == null) {
                throw new DataNotFoundException("Can not find the oldest city in " + countryId);
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which city is older then X? */
    /* Which city is newer then X? */
    public Collection<IDName> getOlderCityThan(int cityId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCityExists(cityId);
        try {
            Collection<IDName> answer = connection.getOlderCityThan(cityId, count);
            if (answer.size() != count) {
                throw new DataNotFoundException(String.format("Can not find %s cities that are older than %s", count, cityId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }
    public Collection<IDName> getNewerCityThan(int cityId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCityExists(cityId);
        try {
            Collection<IDName> answer = connection.getNewerCityThan(cityId, count);
            if (answer.size() != count) {
                throw new DataNotFoundException(String.format("Can not find %s cities that are older than %s", count, cityId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which city is older then X but in the same country? */
    public Collection<IDName> getOlderCityThanInTheSameCountry(int cityId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCityExists(cityId);
        try {
            Collection<IDName> answer = connection.getOlderCityThanInTheSameCountry(cityId, count);
            if (answer.size() != count) {
                throw new DataNotFoundException(String.format("Can not find %s cities that are older than %s", count, cityId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }


    /* --- Persons --- */

    /* Which person born in COUNTRY_ID? */
    /* Which person lives in other country than the other three? */
    public Collection<IDName> getRandomPersonsBornInCountry(int countryId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryId);
        try {
            Collection<IDName> result = connection.getPersonsByBirthCountry(countryId, count);
            if (result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s persons that lives in %s", count, countryId));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not get random persons: " + e.getMessage());
        }
    }
    public Collection<IDName> getRandomPersonsNotBornInCountry(int countryId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryId);
        try {
            Collection<IDName> result = connection.getPersonsByNotBirthCountry(countryId, count);
            if (result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s persons that lives in %s", count, countryId));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not get random persons: " + e.getMessage());
        }
    }

    /* Where does X born? */
    public IDName getBirthCity(int personId) throws DAOException, DataNotFoundException, EntityNotFound {
        validatePersonExists(personId);
        try {
            IDName result = connection.getBirthCity(personId);
            if (result == null) {
                throw new DataNotFoundException(String.format("%s does not have birth place", personId));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch birth place: " + e.getMessage());
        }
    }

    /* Which person was born in the same country as X? */
    /* Which person was not born in the same country as X? */
    public Collection<IDName> getPersonsBornInSameCountry(int personId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validatePersonExists(personId);
        try {
            Collection<IDName> answer = connection.getPersonsBornInSameCountry(personId, count);
            if (answer.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s persons that was born in the same country as %s", count, personId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }
    public Collection<IDName> getPersonsNotBornInSameCountry(int personId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validatePersonExists(personId);
        try {
            Collection<IDName> answer = connection.getPersonsNotBornInSameCountry(personId, count);
            if (answer.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s persons that was not born in the same country as %s", count, personId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* When X was born? */
    public Date getPersonBirthDate(int personId) throws DAOException, EntityNotFound, DataNotFoundException {
        validatePersonExists(personId);
        try {
            Date answer = connection.getBirthDate(personId);
            if (answer == null) {
                throw new DataNotFoundException(String.format("%s does not have birth date in DB", personId));
            }
            return answer;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Who was born first/last? */
    /**
     * Returns a ordered list with person IDS. The oldest person is the first one.
     */
    public List<IDName> getPersonsOrderByBirthDate(int countryId, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryId);
        try {
            List<IDName> result = connection.getPersonsOrderByBirthDate(countryId, count);
            if (result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s persons with birth date in %s", count, countryId));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }


    /* --- University --- */

    /* In Which city there is university? */
    public Collection<IDName> getCitiesThatHasUniversities(int countryID, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);
        try {
            Collection<IDName> result = connection.getCitiesThatHasUniversities(countryID, count);
            if(result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s cities", count));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }
    public Collection<IDName> getCitiesThatHasNtUniversities(int countryID, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);
        try {
            Collection<IDName> result = connection.getCitiesThatHasntUniversities(countryID, count);
            if(result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s cities", count));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }


    /* --- Business & Artifact --- */

    public Collection<IDName> getRandomCountriesForArtifactBusinessConnection(int count) throws DAOException, DataNotFoundException {
        try {
            Collection<IDName> result = connection.getRandomCountriesForArtifactBusinessConnection(count);
            if(result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s countries", count));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }

    /* Which artifact invented by company that today has branch in COUNTRY_ID */
    public Collection<IDName> getArtifactThatInventedByComapnyInCountry(int countryID, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);

        try {
            Collection<IDName> result = connection.getArtifactThatInventedByComapnyInCountry(countryID, count);
            if(result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s cities", count));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }
    public Collection<IDName> getArtifactThatNotInventedByComapnyInCountry(int countryID, int count) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);

        try {
            Collection<IDName> result = connection.getArtifactThatNotInventedByComapnyInCountry(countryID, count);
            if(result.size() != count) {
                throw new DataNotFoundException(String.format("Could not find %s cities", count));
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Could not fetch data: " + e.getMessage());
        }
    }


    /* --- Complex Questions --- */

    /**
     * This method returns a list of size 2.
     * It consists two business where one of the creators of each business learned in the same country of a creator in the other business
     *
     * Use the method after this one to get more 3 lists that does not.
     */
    // NOTE: There are results only for the US.
    public List<IDName> getTwoBusinessesThatOneOfEachOneCreatorLearnedInTheSameCountryAsTheOtherOne(int countryID) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);

        try {
            List<IDName> result = connection.getTwoBusinessesThatOneOfEachOneCreatorLearnedInTheSameCountryAsTheOtherOne(countryID);
            if (result == null) {
                throw new DataNotFoundException("Could not find data");
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Error while fetching data" + e.getMessage());
        }
    }
    public List<IDName> getTwoBusinessesThatThereIsNotOneOfEachOneCreatorLearnedInTheSameCountryAsTheOtherOne(int countryID) throws DAOException, EntityNotFound, DataNotFoundException {
        validateCountryExists(countryID);

        try {
            List<IDName> result = connection.getTwoBusinessesThatThereIsNotOneOfEachOneCreatorLearnedInTheSameCountryAsTheOtherOne(countryID);
            if (result == null) {
                throw new DataNotFoundException("Could not find data");
            }
            return result;
        } catch (DBException e) {
            throw new DAOException("Error while fetching data" + e.getMessage());
        }
    }


}
