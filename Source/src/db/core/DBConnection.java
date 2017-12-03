package db.core;

import parsing.entities.*;
import parsing.util.Utils;
import utils.DBUser;
import utils.IDName;
import utils.UserIDScoreDate;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DBConnection {

    private Connection conn;

    /**
     * Opens new connection to the db and initialize conn with it.
     * Throws DBException if something bad happened.
     * */
    public void connect(DBUser user, String host) throws DBException {

        // loading the driver
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new DBException("Unable to load the MySQL JDBC driver..");
        }
        System.out.println("Driver loaded successfully");

        // creating the connection
        System.out.print("Trying to connect... ");
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader("db.conf"));
            String hostname, schema, username, password;
            hostname = br.readLine();
            schema = br.readLine();
            username = br.readLine();
            password = br.readLine();

            String format = String.format(CONNECTION_STRING, hostname, schema);
            conn = DriverManager.getConnection(format, username, password);
            br.close();
        } catch (SQLException e) {
            conn = null;
            throw new DBException("Unable to connect: " + e.getMessage());
        } catch (IOException e) {
            throw new DBException("Unable to read config: " + e.getMessage());
        }
        System.out.println("Connected!");
    }

    /**
     * Disconnect, ignoring errors.
     */
    public void disconnect() {
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
            System.out.println("Connection closed!");
        } catch (SQLException e) {
        }
    }

    public boolean isConnected() throws DBException {
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            throw new DBException("Unable to check connection status: " + e.getMessage());
        }
    }

    public void uploadCountries(List<Country> countries) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Country(name, creation_date, economic_growth, poverty, population, unemployment, gini, inflation, population_density) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            List<Entity> batchingList = new LinkedList<>();
            for (Country Country : countries) {
                pstmt.setString(1, Country.getName());
                pstmt.setDate(2, Utils.localDateToDate(Country.getCreationDate()));
                pstmt.setFloat(3, Country.getEconomicGrowth());
                pstmt.setFloat(4, Country.getPoverty());
                pstmt.setLong(5, Country.getPopulation());
                pstmt.setFloat(6, Country.getUnemployment());
                pstmt.setFloat(7, Country.getGini());
                pstmt.setFloat(8, Country.getInflation());
                pstmt.setFloat(9, Country.getPopulationDensity());

                addBatchAndExecuteIfNeeded(pstmt, batchingList, Country, true);
            }
            executeBatch(pstmt, batchingList);

            conn.commit();
        } catch (SQLException e) {
            throw new DBException("Error while uploading countries: " + e.getMessage());
        } finally {
            safelySetAutoCommit();
        }
    }

    public void uploadCities(List<City> cities) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO City(name, Country_id, creation_date, economic_growth, poverty, population, unemployment, gini, inflation, population_density) " +
                                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            List<Entity> batchingList = new LinkedList<>();
            for (City City : cities) {
                pstmt.setString(1, City.getName());
                pstmt.setInt(2, City.getCountry().getId());
                pstmt.setDate(3, Utils.localDateToDate(City.getCreationDate()));
                pstmt.setFloat(4, City.getEconomicGrowth());
                pstmt.setFloat(5, City.getPoverty());
                pstmt.setLong(6, City.getPopulation());
                pstmt.setFloat(7, City.getUnemployment());
                pstmt.setFloat(8, City.getGini());
                pstmt.setFloat(9, City.getInflation());
                pstmt.setFloat(10, City.getPopulationDensity());

                addBatchAndExecuteIfNeeded(pstmt, batchingList, City, true);
            }
            executeBatch(pstmt, batchingList);

            conn.commit();
        } catch (SQLException e) {
            throw new DBException("Error while uploading countries: " + e.getMessage());
        } finally {
            safelySetAutoCommit();
        }
    }

    public void uploadUniversities(List<University> universities) throws DBException {
        try {
            conn.setAutoCommit(false);

            uploadUniversitiesEntities(universities);
            uploadUniversityCountryRelation(universities);
            uploadUniversityCityRelation(universities);

            conn.commit();
        } catch (SQLException e) {
            throw new DBException("Error while uploading universities: " + e.getMessage());
        } finally {
            safelySetAutoCommit();
        }
    }

    private void uploadUniversitiesEntities(List<University> universities) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO University(name, creation_date) " +
                                "VALUES(?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            List<Entity> batchingList = new LinkedList<>();
            for (University University : universities) {
                pstmt.setString(1, University.getName());
                pstmt.setDate(2, Utils.localDateToDate(University.getCreationDate()));

                addBatchAndExecuteIfNeeded(pstmt, batchingList, University, true);
            }
            executeBatch(pstmt, batchingList);
        } catch (SQLException e) {
            throw new DBException("Error while uploading universities: " + e.getMessage());
        }
    }

    private void uploadUniversityCountryRelation(List<University> universities) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO University_Country_Relation(University_id, Country_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (University University : universities) {
                for (Country Country : University.getCountries()) {
                    createRelation(pstmt, University, Country);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, Country, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading politicians: " + e.getMessage());
        }
    }

    private void uploadUniversityCityRelation(List<University> universities) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO University_City_Relation(University_id, City_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (University University : universities) {
                for (City City : University.getCities()) {
                    createRelation(pstmt, University, City);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, University, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading politicians: " + e.getMessage());
        }
    }

    public void uploadPersons(List<Person> Persons) throws DBException {
        try {
            conn.setAutoCommit(false);

            uploadPersonsEntities(Persons);
            uploadPoliticianUniversityRelation(Persons);
            uploadPersonsPoliticianOfCountryRelation(Persons);
            uploadBusinessCreatorRelation(Persons);

            conn.commit();
        } catch (SQLException e) {
            throw new DBException("Error while uploading Persons: " + e.getMessage());
        } finally {
            safelySetAutoCommit();
        }
    }

    private void uploadPersonsEntities(List<Person> Persons) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Person(name, birth_City_id, birth_date, death_City_id, death_date) " +
                                "VALUES(?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            List<Entity> batchingList = new LinkedList<>();
            for (Person Person : Persons) {
                pstmt.setString(1, Person.getName());

                if (Person.getBirthCity() != null) {
                    pstmt.setInt(2, Person.getBirthCity().getId());
                } else {
                    pstmt.setNull(2, java.sql.Types.INTEGER);
                }
                pstmt.setDate(3, Utils.localDateToDate(Person.getBirthDate()));

                if (Person.getDeathCity() != null) {
                    pstmt.setInt(4, Person.getDeathCity().getId());
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                }
                pstmt.setDate(5, Utils.localDateToDate(Person.getDeathDate()));

                addBatchAndExecuteIfNeeded(pstmt, batchingList, Person, true);
            }
            executeBatch(pstmt, batchingList);
        } catch (SQLException e) {
            throw new DBException("Error while uploading Persons: " + e.getMessage());
        }
    }

    private void uploadPersonsPoliticianOfCountryRelation(List<Person> Persons) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Person_Politician_Of_Country_Relation(Country_id, politician_id) " +
                                "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (Person Person : Persons) {
                for (Country Country : Person.getPoliticianOf()) {
                    createRelation(pstmt, Country, Person);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, Country, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading Persons-Politician-Of-Relation: " + e.getMessage());
        }
    }

    private void uploadPoliticianUniversityRelation(List<Person> Persons) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO University_Person_Relation(Person_id, University_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (Person Person : Persons) {
                for (University University : Person.getUniversities()) {
                    createRelation(pstmt, Person, University);
                     addBatchAndExecuteIfNeeded(pstmt, batchingList, University, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading Person-University-Relation: " + e.getMessage());
        }
    }

    private void uploadBusinessCreatorRelation(List<Person> Persons) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Business_Creator_Relation(creator_id, Business_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (Person Person : Persons) {
                for (Business Business : Person.getBusinesses()) {
                    createRelation(pstmt, Person, Business);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, Business, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading Business-creator-relation: " + e.getMessage());
        }
    }

    private void uploadArtifactPersonRelation(List<Artifact> Artifacts) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Artifact_Creator_Relation(creator_id, Artifact_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (Artifact Artifact : Artifacts) {
                for (Person creator : Artifact.getCreators()) {
                    createRelation(pstmt, creator, Artifact);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, creator, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading creators: " + e.getMessage());
        }
    }

    public void uploadBusinesses(List<Business> Businesses) throws DBException {
        try {
            conn.setAutoCommit(false);

            uploadBusinessesEntity(Businesses);
            uploadBusinessCityRelation(Businesses);
            uploadBusinessCountryRelation(Businesses);

            conn.commit();
        } catch(SQLException e) {
            throw new DBException("Error while uploading Businesses: " + e.getMessage());
        } finally {
            safelySetAutoCommit();
        }
    }

    private void uploadBusinessesEntity(List<Business> Businesses) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Business(name, creation_date, number_of_employees) " +
                                "VALUES(?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            List<Entity> batchingList = new LinkedList<>();
            for (Business Business : Businesses) {
                pstmt.setString(1, Business.getName());
                pstmt.setDate(2, Utils.localDateToDate(Business.getCreationDate()));
                pstmt.setLong(3, Business.getNumberOfEmployees());

                addBatchAndExecuteIfNeeded(pstmt, batchingList, Business, true);
            }
            executeBatch(pstmt, batchingList);
        } catch (SQLException e) {
            throw new DBException("Error while uploading creators: " + e.getMessage());
        }
    }

    private void uploadBusinessCityRelation(List<Business> Businesses) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Business_city_relation(Business_id, City_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (Business Business : Businesses) {
                for (City City : Business.getCities()) {
                    createRelation(pstmt, Business, City);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, City, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading Businesses: " + e.getMessage());
        }
    }

    private void uploadBusinessCountryRelation(List<Business> Businesses) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Business_Country_Relation(Country_id, Business_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (Business Business : Businesses) {
                for (Country Country : Business.getCountries()) {
                    createRelation(pstmt, Country, Business);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, Country, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading creators: " + e.getMessage());
        }
    }

    public void uploadArtifacts(List<Artifact> Artifacts) throws DBException {
        try {
            conn.setAutoCommit(false);

            uploadArtifactsEntity(Artifacts);
            uploadArtifactPersonRelation(Artifacts);
            uploadBusinessArtifactRelation(Artifacts);

            conn.commit();
        } catch(SQLException e) {
            throw new DBException("Error while uploading Artifacts: " + e.getMessage());
        } finally {
            safelySetAutoCommit();
        }
    }

    private void uploadBusinessArtifactRelation(List<Artifact> Artifacts) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Business_Artifact_Relation(Business_id, Artifact_id) " +
                        "VALUES(?, ?)")) {

            List<Entity> batchingList = new LinkedList<>();
            for (Artifact Artifact : Artifacts) {
                for (Business Business : Artifact.getBusinesses()) {
                    createRelation(pstmt, Business, Artifact);
                    addBatchAndExecuteIfNeeded(pstmt, batchingList, Artifact, false);
                }
            }
            executeBatch(pstmt, null);
        } catch (SQLException e) {
            throw new DBException("Error while uploading Businesses: " + e.getMessage());
        }
    }

    private void uploadArtifactsEntity(List<Artifact> Artifacts) throws DBException {
        try (PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO Artifact(name, creation_date) " +
                                "VALUES(?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            List<Entity> batchingList = new LinkedList<>();
            for (Artifact Artifact : Artifacts) {
                pstmt.setString(1, Artifact.getName());
                pstmt.setDate(2, Utils.localDateToDate(Artifact.getCreationDate()));
                addBatchAndExecuteIfNeeded(pstmt, batchingList, Artifact, true);
            }
            executeBatch(pstmt, batchingList);
        } catch (SQLException e) {
            throw new DBException("Error while uploading Artifacts: " + e.getMessage());
        }
    }

    public int createUser(String user, String password) throws DBException {
        ResultSet rs = null;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format("INSERT INTO User(name, password) VALUES ('%s', '%s')", user, password), new String[] { "ID" });

            rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DBException("Could not create user: " + e.getMessage());
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException e) {
                throw new DBException("Something really bad happend while creating user: " + e.getMessage());
            }
        }
    }

    public Collection<String> getUserID(String name) throws DBException {
        return genericStringCollectionFetcher(String.format("SELECT ID FROM User WHERE NAME='%s'", name));
    }

    public int checkPassword(int userID, String givenPassword) throws DBException {
        return genericIntFetcher(String.format("SELECT '%s' = password FROM User WHERE ID=%s", givenPassword, userID));
    }

    public Integer getUserAnsweredCorrectly(int userID) throws DBException {
        return genericIntFetcher(
                String.format("SELECT number_of_correct_answers FROM User WHERE ID=%s", userID));
    }

    public void setUserAnsweredCorrectly(int userID, int number) throws DBException {
        genericUpdater(
                String.format("UPDATE User SET number_of_correct_answers=%s WHERE ID=%s", number, userID)
        );
    }

    public Integer getUserAnsweredWrong(int userID) throws DBException {
        return genericIntFetcher(
                String.format("SELECT number_of_wrong_answers FROM User WHERE ID=%s", userID)
        );
    }

    public void setUserAnsweredWrong(int userID, int number) throws DBException {
        genericUpdater(
                String.format("UPDATE User SET number_of_wrong_answers=%s WHERE ID=%s", number, userID)
        );
    }

    public Integer setUserStartedNewGame(int userID) throws DBException {
        return genericIntFetcher(
                String.format("SELECT number_of_games_played FROM User WHERE ID=%s", userID)
        );
    }

    public void setUserStartedNewGame(int userID, int number) throws DBException {
        genericUpdater(
                String.format("UPDATE User SET number_of_games_played=%s WHERE ID=%s", number, userID)
        );
    }

    /**
     * @return How many countries there are in the DB
     */
    public int getCountOfCountries() throws DBException {
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Country")) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DBException("Error while counting countries: " + e.getMessage());
        }
    }

    public int getCountOfCities() throws DBException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM City")) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DBException("Error while counting countries: " + e.getMessage());
        }
    }

    public Collection<IDName> getAllCountries() throws DBException {
        return genericIntStringCollectionFetcher("SELECT ID, NAME FROM Country");
    }

    public Collection<IDName> getRandomCountries(int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery("SELECT ID, NAME FROM Country", count)
        );
    }

    public Integer getEntityID(String entity_type, String name) throws DBException {
        return genericIntFetcher(String.format("SELECT ID FROM %s WHERE NAME='%s'", entity_type, name));
    }

    public Collection<IDName> getCities(int Country, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT ID, NAME FROM City WHERE City.Country_ID='%s'", Country), count)
        );
    }

    private String addRandomLimitToQuery(String select, int count) {
        return select + String.format(" ORDER BY RAND() LIMIT %s", count);
    }

    private Collection<String> genericStringCollectionFetcher(String select) throws DBException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {

            Collection<String> stringCollection = new HashSet<>();
            while (rs.next()) {
                stringCollection.add(rs.getString(1));
            }
            return stringCollection;
        } catch (SQLException e) {
            throw new DBException("Error while fetching countries: " + e.getMessage());
        }
    }

    private Collection<IDName> genericIntStringCollectionFetcher(String select) throws DBException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {

            Collection<IDName> idNameCollection = new HashSet<>();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                idNameCollection.add(new IDName(id, name));
            }
            return idNameCollection;
        } catch (SQLException e) {
            throw new DBException("Error while fetching countries: " + e.getMessage());
        }
    }

    private List<IDName> genericIntStringListFetcher(String select) throws DBException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {

            List<IDName> idNameCollection = new LinkedList<>();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                idNameCollection.add(new IDName(id, name));
            }
            return idNameCollection;
        } catch (SQLException e) {
            throw new DBException("Error while fetching countries: " + e.getMessage());
        }
    }

    private IDName genericIntStringFetcher(String select) throws DBException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {
            if (rs.next()) {
                return new IDName(rs.getInt(1), rs.getString(2));
            }
            return null;
        } catch (SQLException e) {
            throw new DBException("Error while fetching countries: " + e.getMessage());
        }
    }

    private Integer genericIntFetcher(String select) throws DBException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        } catch (SQLException e) {
            throw new DBException("Could not fetch data: " + e.getMessage());
        }
    }

    private List<Integer> genericListIntFetcher(String select) throws DBException {
        List<Integer> result = new LinkedList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {
            while(rs.next()) {
                result.add(rs.getInt(1));
            }
            return result;
        } catch (SQLException e) {
            throw new DBException("Could not fetch data: " + e.getMessage());
        }
    }

    private List<Long> genericListLongFetcher(String select) throws DBException {
        List<Long> result = new LinkedList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {
            while(rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (SQLException e) {
            throw new DBException("Could not fetch data: " + e.getMessage());
        }
    }

    private Date genericDateFetcher(String select) throws DBException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {
            if (rs.next())
                return rs.getDate(1);
            return null;
        } catch (SQLException e) {
            throw new DBException("Could not fetch data: " + e.getMessage());
        }
    }

    private void genericUpdater(String update) throws DBException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(update);
        } catch (SQLException e) {
            throw new DBException("Could not set data: " + e.getMessage());
        }
    }

//    public Map<Integer, Country> getAllCountriesData() throws DBException {
//        Map<Integer, Country> countries = new HashMap<>();
//
//        try (Statement stmt = conn.createStatement()) {
//            ResultSet rs = stmt.executeQuery("SELECT * FROM Country");
//
//            while (rs.next()) {
//                Country Country = new Country();
//
//                Country.id = rs.getInt("id");
//                Country.name = rs.getString("name");
//
//                Date creationDate = rs.getDate("creation_date");
//                if (creationDate != null) {
//                    Country.creationDate = creationDate.toLocalDate();
//                }
//                Country.economicGrowth = rs.getFloat("economic_growth");
//                Country.poverty = rs.getFloat("poverty");
//                Country.population = rs.getLong("population");
//                Country.unemployment = rs.getFloat("unemployment");
//                Country.gini = rs.getFloat("gini");
//                Country.inflation = rs.getFloat("inflation");
//                Country.populationDensity = rs.getFloat("population_density");
//
//                countries.put(Country.id, Country);
//            }
//
//        } catch (SQLException e) {
//            throw new DBException("Error while fetching countries collect_data : " + e.getMessage());
//        }
//        return countries;
//    }

//    public Map<Integer, City> getAllCitiesData(Map<Integer, Country> countries) throws DBException {
//        Map<Integer, City> cities = new HashMap<>();
//
//        try (Statement stmt = conn.createStatement()) {
//            ResultSet rs = stmt.executeQuery("SELECT * FROM City");
//
//            while (rs.next()) {
//                City City = new City();
//
//                City.id = rs.getInt("id");
//                City.name = rs.getString("name");
//                City.Country = countries.get(rs.getInt("Country_id"));
//                Date creationDate = rs.getDate("creation_date");
//                if (creationDate != null) {
//                    City.creationDate = creationDate.toLocalDate();
//                }
//                City.economicGrowth = rs.getFloat("economic_growth");
//                City.poverty = rs.getFloat("poverty");
//                City.population = rs.getLong("population");
//                City.unemployment = rs.getFloat("unemployment");
//                City.gini = rs.getFloat("gini");
//                City.inflation = rs.getFloat("inflation");
//                City.populationDensity = rs.getFloat("population_density");
//
//                cities.put(City.id, City);
//            }
//
//        } catch (SQLException e) {
//            throw new DBException("Error while fetching cities collect_data : " + e.getMessage());
//        }
//        return cities;
//    }

    /**
     * Clears collect_data from DB.
     */
    public void deleteData() throws DBException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM University");
            stmt.executeUpdate("DELETE FROM City");
            stmt.executeUpdate("DELETE FROM Business");
            stmt.executeUpdate("DELETE FROM Artifact");
            stmt.executeUpdate("DELETE FROM Person");
            stmt.executeUpdate("DELETE FROM Country");
        } catch (SQLException e) {
            throw new DBException("Error while deleting collect_data from Country: " + e.getMessage());
        }
    }

    private void createRelation(PreparedStatement pstmt, Entity entity1, Entity entity2) throws SQLException {
        pstmt.setInt(1, entity1.getId());
        pstmt.setInt(2, entity2.getId());
    }

    /**
     * Reads the generated keys and set the entity accordingly.
     */
    private void setIDsToEntities(PreparedStatement pstmt, List<? extends Entity> entities) throws DBException {
        try(ResultSet rs = pstmt.getGeneratedKeys()) {
            Iterator<? extends Entity> entityIterator = entities.iterator();
            while (rs.next()) {
                entityIterator.next().setId(rs.getInt(1));
            }
        } catch(SQLException e) {
            throw new DBException("Error while getting generated keys: " + e.getMessage());
        }
    }

    private void addBatchAndExecuteIfNeeded(PreparedStatement pstmt, List<Entity> batchingList, Entity entity, boolean setIDs) throws DBException {
        try {
            pstmt.addBatch();

            batchingList.add(entity);

            if (batchingList.size() % 10000 == 0) { // Execute once in 100000 adds
                if (setIDs) {
                    executeBatch(pstmt, batchingList);
                } else {
                    executeBatch(pstmt, null);
                }

                batchingList.clear();
            }
        } catch (SQLException e) {
            throw new DBException("Error while adding to batch: " + e.getMessage());
        }
    }

    private void executeBatch(PreparedStatement pstmt, List<? extends  Entity> entities) throws DBException {
        try {
            pstmt.executeBatch();

            if (entities != null) {
                setIDsToEntities(pstmt, entities);
            }
        } catch (SQLException e) {
            throw new DBException("Error while executing batch: " + e.getMessage());
        }
    }

    /**
     * Attempts to set the connection back to auto-commit, ignoring errors.
     */
    private void safelySetAutoCommit() {
        try {
            conn.setAutoCommit(true);
        } catch (Exception e) {
        }
    }

    private static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static String CONNECTION_STRING = "jdbc:mysql://%s/%s";
    //private static String DEFAULT_HOST = "localhost:3305";
    //private static String DEFAULT_SCHEMA = "DbMysql19";


    public Integer getNumberOfPeopleInCountryOrderDescResult(int CountryID) throws DBException {
        return genericIntFetcher("SELECT POPULATION FROM Country WHERE ID=" + CountryID);
    }

    public List<Long> getNumberOfPeopleInCountryOrderDescResult(List<Integer> CountryID) throws DBException {
        return genericListLongFetcher("SELECT POPULATION FROM Country WHERE ID in " + listToStringForQuery(CountryID));
    }

    public Collection<String> getUserName(int id) throws DBException {
        return genericStringCollectionFetcher("SELECT NAME FROM User WHERE ID=" + id);
    }

    public Date getCountryCreationDate(int CountryID) throws DBException {
        return genericDateFetcher("SELECT CREATION_DATE FROM Country WHERE ID=" + CountryID);
    }

    public Integer getCountOf(String entityType, int id) throws DBException {
        return genericIntFetcher(String.format("SELECT COUNT(*) FROM %s WHERE ID=%s", entityType, id));
    }

    public List<Integer> getCountOf(String entityType, List<Integer> id) throws DBException {
        return genericListIntFetcher(String.format("SELECT COUNT(*) FROM %s WHERE ID in %s GROUP BY ID", entityType, listToStringForQuery(id)));
    }

    public Integer getCountOf(String entityType, String name) throws DBException {
        return genericIntFetcher(String.format("SELECT COUNT(*) FROM %s WHERE NAME='%s'", entityType, name));
    }

    private String listToStringForQuery(List<?> l) {
        StringBuilder s = new StringBuilder();
        s.append("(");
        for (Object str : l) {
            s.append(str);
            s.append(",");
        }
        s.deleteCharAt(s.length()-1);
        s.append(")");
        return s.toString();
    }

    public Integer getMostPopulatedCountry(List<Integer> CountryIDList) throws DBException {
        return genericIntFetcher(String.format("SELECT ID FROM Country WHERE ID IN %s ORDER BY POPULATION DESC",
                listToStringForQuery(CountryIDList)));
    }

    public Integer getLeastPopulatedCountry(List<Integer> CountryIDList) throws DBException {
        return genericIntFetcher(String.format("SELECT ID FROM Country WHERE ID IN %s ORDER BY POPULATION ASC",
                listToStringForQuery(CountryIDList)));
    }

    public Collection<IDName> getCountryThatIsMorePopulatedThan(int CountryID, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format(
                        "SELECT ID, NAME FROM Country WHERE POPULATION > (SELECT POPULATION FROM Country WHERE ID=%s)",
                        CountryID), count)
        );
    }

    public Collection<IDName> getCountryThatIsLessPopulatedThan(int CountryID, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(
                        String.format("SELECT ID, NAME FROM Country WHERE POPULATION < (SELECT POPULATION FROM Country WHERE ID=%s) AND POPULATION > 0", CountryID), count
                )
        );
    }

    public Integer getTheOldestCountry(List<Integer> countriesList) throws DBException {
        return genericIntFetcher(
                String.format("SELECT ID FROM Country WHERE ID IN %s ORDER BY CREATION_DATE ASC", listToStringForQuery(countriesList))
        );
    }

    public IDName getCountryCreatedBetween(int afterCountry, int beforeCountry) throws DBException {
        return genericIntStringFetcher(
                addRandomLimitToQuery(
                        String.format(
                                "SELECT ID, NAME FROM Country WHERE CREATION_DATE > (SELECT CREATION_DATE FROM Country WHERE ID=%s) AND CREATION_DATE < (SELECT CREATION_DATE FROM Country WHERE ID=%s)", afterCountry, beforeCountry
                        ), 1)
        );
    }

    public IDName getCountryNotCreatedBetween(int afterCountry, int beforeCountry) throws DBException {
        return genericIntStringFetcher(
                addRandomLimitToQuery(
                        String.format(
                                "SELECT ID, NAME FROM Country WHERE NOT(CREATION_DATE > (SELECT CREATION_DATE FROM Country WHERE ID=%s) AND CREATION_DATE < (SELECT CREATION_DATE FROM Country WHERE ID=%s))", afterCountry, beforeCountry
                        ), 1)
        );
    }


    public Collection<IDName> getCitiesNotIn(int countryId, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(
                        String.format("SELECT ID, NAME FROM City WHERE Country_ID != %s", countryId),
                        count)
        );
    }

    public IDName getOldestCity(int countryId) throws DBException {
        return genericIntStringFetcher(
                String.format("SELECT ID, NAME FROM City WHERE Country_ID=%s and CREATION_DATE is not null ORDER BY CREATION_DATE ASC LIMIT 1", countryId)
        );
    }

    public Collection<IDName> getOlderCityThan(int cityId, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT ID, NAME FROM City WHERE CREATION_DATE > (SELECT CREATION_DATE FROM City WHERE ID=%s)", cityId), count)
        );
    }

    public Collection<IDName> getNewerCityThan(int cityID, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT ID, NAME FROM City WHERE CREATION_DATE > (SELECT CREATION_DATE FROM City WHERE ID=%s)", cityID), count)
        );
    }

    public Collection<IDName> getOlderCityThanInTheSameCountry(int cityId, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT ID, NAME FROM City WHERE Country_ID=(SELECT Country_ID FROM City WHERE ID=%s) AND CREATION_DATE < (SELECT CREATION_DATE FROM City WHERE ID=%s)", cityId, cityId), count)
        );
    }

    public Collection<IDName> getPersonsByBirthCountry(int countryId, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(
                        String.format("SELECT ID, NAME FROM Person WHERE BIRTH_City_ID IN (SELECT ID FROM City WHERE Country_ID=%s)", countryId), count
                )
        );
    }

    public Collection<IDName> getPersonsByNotBirthCountry(int countryId, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(
                        String.format("SELECT ID, NAME FROM Person WHERE BIRTH_City_ID NOT IN (SELECT ID FROM City WHERE Country_ID=%s)", countryId), count
                )
        );
    }

    public IDName getBirthCity(int Person_id) throws DBException {
        return genericIntStringFetcher(
                String.format("SELECT ID, NAME FROM City WHERE ID in (SELECT BIRTH_City_ID FROM Person WHERE ID=%s)", Person_id)
        );
    }

    public Collection<IDName> getPersonsBornInSameCountry(int Person_id, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(
                        String.format("SELECT ID, NAME FROM Person WHERE birth_City_id in (SELECT ID from City where " +
                                        "Country_id in (SELECT Country.ID FROM Country, Person, City WHERE " +
                                        "City.Country_id=Country.ID AND Person.birth_City_id=City.ID AND Person.ID=%s))", Person_id
                        )
                        , count));
    }

    public Collection<IDName> getPersonsNotBornInSameCountry(int Person_id, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(
                        String.format("SELECT ID, NAME FROM Person WHERE birth_City_id not in (SELECT ID from City where " +
                                        "Country_id in (SELECT Country.ID FROM Country, Person, City WHERE " +
                                        "City.Country_id=Country.ID AND Person.birth_City_id=City.ID AND Person.ID=%s))", Person_id
                        )
                        , count));
    }

    public Date getBirthDate(int Person_id) throws DBException {
        return genericDateFetcher(
                String.format("SELECT BIRTH_DATE FROM Person WHERE ID=%s", Person_id)
        );
    }

    public List<IDName> getPersonsOrderByBirthDate(int Country_id, int count) throws DBException {
        return genericIntStringListFetcher(
                String.format(
                        "SELECT ID, NAME FROM (SELECT ID, NAME, birth_date FROM Person WHERE birth_City_id in (SELECT ID FROM City WHERE Country_ID=%s) ORDER BY RAND() LIMIT %s) as TMP ORDER BY birth_date", Country_id, count
                )
        );
    }

    public List<IDName> getTwoBusinessesThatOneOfEachOneCreatorLearnedInTheSameCountryAsTheOtherOne(int CountryID) throws DBException {
        String query = String.format("SELECT B1.id, B1.name, B2.id, B2.name FROM Business B1, Business B2, Business_Creator_Relation bcr1, University_Person_Relation upr1, University_Country_Relation ucr1, University_Country_Relation ucr2, Business_Creator_Relation bcr2, University_Person_Relation upr2 WHERE B1.id != B2.id AND B1.id=bcr1.Business_id and bcr1.creator_id=upr1.Person_id and upr1.University_id = ucr1.University_id AND B2.id=bcr2.Business_id and bcr2.creator_id=upr2.Person_id and upr2.University_id = ucr2.University_id and ucr1.Country_id=ucr2.Country_id and ucr1.Country_id=254 ORDER BY RAND() LIMIT 1", CountryID);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                List<IDName> result = new LinkedList<>();
                result.add(new IDName(rs.getInt(1), rs.getString(2)));
                result.add(new IDName(rs.getInt(3), rs.getString(4)));
                return result;
            }
            return null;
        } catch (SQLException e) {
            throw new DBException("Error while fetching data: " + e.getMessage());
        }
    }

    public List<IDName> getTwoBusinessesThatThereIsNotOneOfEachOneCreatorLearnedInTheSameCountryAsTheOtherOne(int CountryID) throws DBException {
        String query = String.format("SELECT B1.id, B1.name, B2.id, B2.name FROM Business B1, Business B2, Business_Creator_Relation bcr1, University_Person_Relation upr1, University_Country_Relation ucr1, University_Country_Relation ucr2, Business_Creator_Relation bcr2, University_Person_Relation upr2 WHERE B1.id != B2.id AND B1.id=bcr1.Business_id and bcr1.creator_id=upr1.Person_id and upr1.University_id = ucr1.University_id AND B2.id=bcr2.Business_id and bcr2.creator_id=upr2.Person_id and upr2.University_id = ucr2.University_id and ucr1.Country_id!=ucr2.Country_id and ucr1.Country_id=%s ORDER BY RAND() LIMIT 1", CountryID);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                List<IDName> result = new LinkedList<>();
                result.add(new IDName(rs.getInt(1), rs.getString(2)));
                result.add(new IDName(rs.getInt(3), rs.getString(4)));
                return result;
            }
            return null;
        } catch (SQLException e) {
            throw new DBException("Error while fetching data: " + e.getMessage());
        }
    }


    public List<UserIDScoreDate> getTopScore(int howMany) throws DBException {
        return genericIDScoreDateFetcher("SELECT USER_ID, SCORE, DATE FROM Score ORDER BY SCORE DESC LIMIT " + howMany);

    }

    public List<UserIDScoreDate> getTopScoreByUser(int userID, int howMany) throws DBException {
        return genericIDScoreDateFetcher(String.format("SELECT USER_ID, SCORE, DATE  FROM Score WHERE USER_ID='%s' ORDER BY SCORE DESC LIMIT %s", userID, howMany));
    }

    private List<UserIDScoreDate> genericIDScoreDateFetcher(String select) throws DBException {
        List<UserIDScoreDate> result = new LinkedList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(select)) {
            while(rs.next()) {
                result.add(new UserIDScoreDate(rs.getInt(1), rs.getInt(2), rs.getTimestamp(3)));
            }
            return result;
        } catch (SQLException e) {
            throw new DBException("Could not fetch data: " + e.getMessage());
        }
    }

    public void setScore(int userID, int score, java.util.Date date) throws DBException {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Score(user_id, score, date) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, score);
            pstmt.setTimestamp(3, new Timestamp(date.getTime()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("Could not insert score: " + e.getMessage());
        }
    }

    public Collection<IDName> getCitiesThatHasUniversities(int countryID, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT ID, NAME FROM City WHERE COUNTRY_ID=%s and ID IN (SELECT CITY_ID FROM University_City_Relation)", countryID), count)
        );
    }

    public Collection<IDName> getCitiesThatHasntUniversities(int countryID, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT ID, NAME FROM City WHERE COUNTRY_ID=%s and ID NOT IN (SELECT CITY_ID FROM University_City_Relation)", countryID), count)
        );
    }

    public Collection<IDName> getArtifactThatInventedByComapnyInCountry(int countryID, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT a.ID, a.NAME FROM Artifact a, Business_Country_Relation bcr, Business_Artifact_Relation bar WHERE a.id=bar.artifact_id and bar.business_id=bcr.business_id and bcr.country_id=%s", countryID), count)
        );
    }

    public Collection<IDName> getArtifactThatNotInventedByComapnyInCountry(int countryID, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT a.ID, a.NAME FROM Artifact a, Business_Country_Relation bcr, Business_Artifact_Relation bar WHERE a.id=bar.artifact_id and bar.business_id=bcr.business_id and bcr.country_id!=%s", countryID), count)
        );
    }

    public Collection<IDName> getCitiesWithCreationDate(int countryId, int count) throws DBException {
        return genericIntStringCollectionFetcher(
                addRandomLimitToQuery(String.format("SELECT ID, NAME FROM City WHERE City.Country_ID='%s' and creation_date is not null", countryId), count)
        );
    }

    public Collection<IDName> getRandomCountriesForArtifactBusinessConnection(int count) throws DBException {
        return genericIntStringCollectionFetcher(
                "SELECT distinct Country.id, Country.name FROM Country, Business_Country_Relation r1, Business_Artifact_Relation r2 WHERE Country.ID = r1.country_id and r1.business_id=r2.business_id ORDER BY RAND() LIMIT " + count
        );
    }
}
