package main;

import parsing.DataCollector;
import parsing.entities.*;
import db.dao.DAO;
import db.dao.DAOException;
import utils.DBUser;

import java.io.IOException;
import java.util.Collection;

public class UploadDataFromYagoMain {

    public static void main(String args[]) throws Exception {

        if (args.length < 0 ) {
            throw new Exception("You must supply first argument as path to folder containing YAGO files.");
        }
        DAO dao = new DAO();
        try {
            dao.connect(DBUser.MODIFIER, null);

            System.out.println("Delete data from DB ...");
            dao.deleteDB();     // NOTE: The main deletes the data that is already on the DB

            System.out.println("Collecting Data ...");
            DataCollector dataCollector = collectData(args[0]);
            printStats(dataCollector);

            System.out.println("Uploading ...");
            dao.uploadDataCollector(dataCollector);
        } catch (DAOException e) {
            throw new Exception("Could not upload data to DB: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("Could not find YAGO data: " + e.getMessage());
        } finally {
            dao.disconnect();
        }
    }

    private static DataCollector collectData(String yagoPath) throws IOException {
        DataCollector dataCollector = new DataCollector(yagoPath);
        dataCollector.collectData();

        return dataCollector;
    }

    private static void printStats(DataCollector dataCollector) {
        Collection<Country> countries = dataCollector.getCountries();
        Collection<City> cities = dataCollector.getCities();

        Collection<University> universities = dataCollector.getUniversities();
        int universityCity = 0;
        int universityCountry = 0;
        for (University u: universities) {
            universityCity += u.cities.size();
            universityCountry += u.countries.size();
        }

        Collection<Artifact> artifacts = dataCollector.getArtifacts();
        int artifactBusiness = 0;
        int artifcatCreator = 0;
        for (Artifact a : artifacts) {
            artifactBusiness += a.businesses.size();
            artifcatCreator += a.creators.size();
        }

        Collection<Business> businesses = dataCollector.getBusinesses();
        int businessCity = 0;
        int businessCountry = 0;
        for(Business b : businesses) {
            businessCity += b.cities.size();
            businessCountry += b.countries.size();
        }

        Collection<Person> persons = dataCollector.getPersons();
        int personCountry = 0;
        int personUniversity = 0;
        int personBusiness = 0;
        for (Person p : persons) {
            personCountry += p.politicianOf.size();
            personUniversity += p.universities.size();
            personBusiness += p.businesses.size();
        }

        System.out.println();
        System.out.println(String.format("Collected %d countries", countries.size()));
        System.out.println(String.format("Collected %d cities", cities.size()));
        System.out.println(String.format("Collected %d universities: %d countries, %d cities", universities.size(), universityCountry, universityCity));
        System.out.println(String.format("Collected %d persons: %d countries, %d universities, %d businesses", persons.size(), personCountry, personUniversity, personBusiness));
        System.out.println(String.format("Collected %d artifacts: %d businesses, %d creators,", artifacts.size(), artifactBusiness, artifcatCreator));
        System.out.println(String.format("Collected %d businesses: %d countries, %d cities", businesses.size(), businessCountry, businessCity));
    }
}