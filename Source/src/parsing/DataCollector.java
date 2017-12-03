package parsing;

import parsing.entities.*;
import parsing.generic.Callback;
import parsing.generic.GenericCallback;
import parsing.generic.GenericEntityCallback;
import parsing.generic.GenericObjectLinkCallback;
import parsing.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DataCollector {

    private Map<String, Country> countries = new HashMap<>();
    private Map<String, City> cities = new HashMap<>();
    private Map<String, University> universities = new HashMap<>();
    private Map<String, Business> businesses = new HashMap<>();
    private Map<String, Artifact> artifacts = new HashMap<>();
    private Map<String, Person> persons = new HashMap<>();

    private String yagoPath;

    public DataCollector(String yagoPath) {
        this.yagoPath = yagoPath;
    }

    public void collectData() throws IOException {
        getIDs();
        //noinspection unchecked
        getNames(persons, artifacts, businesses, countries, cities, universities);
        //noinspection unchecked
        getFacts(countries, cities);
        postProcessData();
    }

    public static String combine (String path1, String path2)
    {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    /* -------------------- Get Methods -------------------- */

    public Collection<Country> getCountries() {
        return countries.values();
    }

    public Collection<City> getCities() {
        return cities.values();
    }

    public Collection<University> getUniversities() {
        return universities.values();
    }

    public Collection<Business> getBusinesses() {
        return businesses.values();
    }

    public Collection<Artifact> getArtifacts() {
        return artifacts.values();
    }

    public Collection<Person> getPersons() { return persons.values(); }

    /* -------------------- Data Collection Callbacks -------------------- */

    private void getIDs() throws IOException {
        List<Callback> callbacks = new LinkedList<>();
        Callback[] c = new Callback[]{
                new GenericEntityCallback<>(artifacts,      Artifact.class,     "<wordnet_artifact_100021939>"),
                new GenericEntityCallback<>(persons,        Person.class,       "<wordnet_creator_109614315>"),
                new GenericEntityCallback<>(businesses,     Business.class,     "<wordnet_business_108061042>"),
                new GenericEntityCallback<>(countries,      Country.class,      "<wikicat_Countries>"),
                new GenericEntityCallback<>(cities,         City.class,         "<wordnet_city_108524735>"),
                new GenericEntityCallback<>(persons,        Person.class,       "<wordnet_politician_110450303>"),
                new GenericEntityCallback<>(universities,   University.class,   "<wordnet_university_108286569>"),
        };
        Collections.addAll(callbacks, c);
        Utils.reduceEntitiesByAttributeFromCollectionWithMatcher(combine(yagoPath, YAGOFilesLocation.YAGO_TYPES_FILE), callbacks);
    }


    @SafeVarargs
    private final void getNames(final Map<String, ? extends Entity>... entities_maps) throws IOException {
        List<Callback> callbacks = new LinkedList<>();
        for (final Map<String, ? extends Entity> entities : entities_maps) {
            callbacks.add(new GenericCallback(entities, ValueType.NAME, "skos:prefLabel", "name"));
        }
        Utils.reduceEntitiesByAttributeFromCollectionWithMatcher(combine(yagoPath, YAGOFilesLocation.YAGO_LABELS_FILE), callbacks);
    }

    @SafeVarargs
    private final void getFacts(final Map<String, ? extends PopulatedRegion>... place_maps) throws IOException {
        String factFiles[] = new String[]{combine(yagoPath, YAGOFilesLocation.YAGO_DATE_FACTS_FILE)
                , combine(yagoPath, YAGOFilesLocation.YAGO_FACTS_FILE)
                , combine(yagoPath, YAGOFilesLocation.YAGO_LITERAL_FACTS_FILE)
                ,};

        List<Callback> callbacks = new LinkedList<>();

        //Populated place callbacks
        for (Map<String, ? extends PopulatedRegion> places : place_maps) {
            callbacks.add(new GenericCallback(places, ValueType.DATE,   "<wasCreatedOnDate>",       "creationDate"));
            callbacks.add(new GenericCallback(places, ValueType.FLOAT,  "<hasEconomicGrowth>",      "economicGrowth"));
            callbacks.add(new GenericCallback(places, ValueType.FLOAT,  "<hasPoverty>",             "poverty"));
            callbacks.add(new GenericCallback(places, ValueType.LONG,   "<hasNumberOfPeople>",      "population"));
            callbacks.add(new GenericCallback(places, ValueType.FLOAT,  "<hasUnemployment>",        "unemployment"));
            callbacks.add(new GenericCallback(places, ValueType.FLOAT,  "<hasGini>",                "gini"));
            callbacks.add(new GenericCallback(places, ValueType.FLOAT,  "<hasInflation>",           "inflation"));
            callbacks.add(new GenericCallback(places, ValueType.FLOAT,  "<hasPopulationDensity>",   "populationDensity"));
        }
        //City callbacks
        callbacks.add(new GenericObjectLinkCallback<>(cities,       countries,      City.class,         "<isLocatedIn>",    "country",      false, false));
        //University callbacks
        callbacks.add(new GenericObjectLinkCallback<>(universities, countries,      University.class,   "<isLocatedIn>",    "countries"));
        callbacks.add(new GenericObjectLinkCallback<>(universities, cities,         University.class,   "<isLocatedIn>",    "cities"));
        callbacks.add(new GenericCallback(universities, ValueType.DATE,     "<wasCreatedOnDate>",   "creationDate"));
        //Person callbacks
        callbacks.add(new GenericObjectLinkCallback<>(persons,  cities,         Person.class,   "<wasBornIn>",      "birthCity",    false, false));
        callbacks.add(new GenericObjectLinkCallback<>(persons,  cities,         Person.class,   "<diedIn>",         "deathCity",    false, false));
        callbacks.add(new GenericObjectLinkCallback<>(persons,  universities,   Person.class,   "<graduatedFrom>",  "universities"));
        callbacks.add(new GenericCallback(persons,  ValueType.DATE,     "<diedOnDate>",         "deathDate"));
        callbacks.add(new GenericCallback(persons,  ValueType.DATE,     "<wasBornOnDate>",      "birthDate"));
        //Business callbacks
        callbacks.add(new GenericObjectLinkCallback<>(businesses,   countries,      Business.class,     "<isLocatedIn>",    "countries"));
        callbacks.add(new GenericObjectLinkCallback<>(businesses,   cities,         Business.class,     "<isLocatedIn>",    "cities"));
        callbacks.add(new GenericCallback(businesses,   ValueType.DATE,     "<wasCreatedOnDate>",   "creationDate"));
        callbacks.add(new GenericCallback(businesses,   ValueType.LONG,     "<hasNumberOfPeople>",  "numberOfEmployees"));
        //Politician callbacks
        callbacks.add(new GenericObjectLinkCallback<>(persons,  countries,      Person.class,   "<isPoliticianOf>", "politicianOf"));
        //Creator callbacks
        callbacks.add(new GenericObjectLinkCallback<>(persons,     businesses,     Person.class,      "<created>",        "businesses"));
        //Artifact callbacks
        callbacks.add(new GenericObjectLinkCallback<>(artifacts,    businesses,     Artifact.class,     "<created>",        "businesses",   true, true));
        callbacks.add(new GenericObjectLinkCallback<>(artifacts,    persons,       Artifact.class,     "<created>",        "creators",     true, true));
        callbacks.add(new GenericCallback(artifacts,    ValueType.DATE,     "<wasCreatedOnDate>",   "creationDate"));

        for (String factFile : factFiles) {
            Utils.reduceEntitiesByAttributeFromCollectionWithMatcher(factFile, callbacks);
        }
    }

    /* -------------------- Data Post Processing -------------------- */

    private void postProcessData() {
        postProcessCountries();
        postCitiesProcessor();
        postUniversitiesProcessor();
        postBusinessesProcessor();
        postPersonProcessor();
        postArtifactProcessor();
    }

    private void postProcessCountries() {
        List<String> toRemove = new LinkedList<>();
        for (Map.Entry<String, Country> countryEntry : countries.entrySet()) {
            if (countryEntry.getValue().population == 0 && countryEntry.getValue().creationDate == null && countryEntry.getValue().poverty == 0) {
                toRemove.add(countryEntry.getKey());
            }
        }
        toRemove.forEach(countries::remove);
    }

    private void postCitiesProcessor() {
        List<String> citiesToRemove = new ArrayList<>();

        for (Map.Entry<String, City> cityEntry : cities.entrySet()) {
            if (cityEntry.getValue().country == null || !countries.containsKey(cityEntry.getValue().country.entity)) {
                citiesToRemove.add(cityEntry.getKey());
            }
        }
        citiesToRemove.forEach(cities::remove);
    }

    private void postUniversitiesProcessor() {
        List<String> universitiesToRemove = new ArrayList<>();

        for (Map.Entry<String, University> universitiesEntry : universities.entrySet()) {
            List<City> citiesToBeRemoved = universitiesEntry.getValue().getCities().stream().
                    filter(city -> !cities.containsKey(city.getEntity())).
                    collect(Collectors.toCollection(LinkedList::new));
            citiesToBeRemoved.forEach(universitiesEntry.getValue().getCities()::remove);
            if (universitiesEntry.getValue().getCities().isEmpty() && universitiesEntry.getValue().getCountries().isEmpty()) {
                universitiesToRemove.add(universitiesEntry.getKey());
            }
        }
        universitiesToRemove.forEach(universities::remove);
    }

    private void postPersonProcessor() {
        List<String> personsToRemove = new ArrayList<>();

        for (Map.Entry<String, ? extends Person> personEntry : persons.entrySet()) {
            if (personEntry.getValue().getBirthCity() != null &&
                    !cities.containsKey(personEntry.getValue().getBirthCity().getEntity())) {
                personEntry.getValue().setBirthCity(null);
            }
            if (personEntry.getValue().getDeathCity() != null &&
                    !cities.containsKey(personEntry.getValue().getDeathCity().getEntity())) {
                personEntry.getValue().setDeathCity(null);
            }

            List<University> universitiesToBeRemoved = personEntry.getValue().getUniversities().stream().
                    filter(university -> !universities.containsKey(university.getEntity())).
                    collect(Collectors.toCollection(LinkedList::new));
            universitiesToBeRemoved.forEach(personEntry.getValue().getUniversities()::remove);

            List<Business> businessesToBeRemoved = personEntry.getValue().getBusinesses().stream().
                    filter(business -> !businesses.containsKey(business.getEntity())).
                    collect(Collectors.toCollection(LinkedList::new));
            businessesToBeRemoved.forEach(personEntry.getValue().getBusinesses()::remove);

            if (personEntry.getValue().getBirthCity() == null && personEntry.getValue().getDeathCity() == null &&
                    personEntry.getValue().getPoliticianOf().isEmpty() && personEntry.getValue().getBusinesses().isEmpty() &&
                    personEntry.getValue().getUniversities().isEmpty()) {
                personsToRemove.add(personEntry.getKey());
            }
        }

        personsToRemove.forEach(persons::remove);
    }

    private void postBusinessesProcessor() {
        List<String> businessesToRemove = new ArrayList<>();

        for (Map.Entry<String, Business> businessEntry : businesses.entrySet()) {
            List<City> citiesToBeRemoved = businessEntry.getValue().getCities().stream().
                    filter(city -> !cities.containsKey(city.getEntity())).
                    collect(Collectors.toCollection(LinkedList::new));
            citiesToBeRemoved.forEach(businessEntry.getValue().getCities()::remove);
            if (businessEntry.getValue().getCountries().isEmpty() && businessEntry.getValue().getCities().isEmpty()) {
                businessesToRemove.add(businessEntry.getKey());
            }
        }
        businessesToRemove.forEach(businesses::remove);
    }

    private void postArtifactProcessor() {
        List<String> artifactsToRemove = new ArrayList<>();

        for (Map.Entry<String, Artifact> artifactEntry : artifacts.entrySet()) {
            List<Business> businessesToBeRemoves = artifactEntry.getValue().getBusinesses().stream().
                    filter(business -> !businesses.containsKey(business.getEntity())).
                    collect(Collectors.toCollection(LinkedList::new));
            businessesToBeRemoves.forEach(artifactEntry.getValue().getBusinesses()::remove);

            List<Person> creatorsToBeRemoved = artifactEntry.getValue().getCreators().stream().
                    filter(creator -> !persons.containsKey(creator.getEntity())).
                    collect(Collectors.toList());
            creatorsToBeRemoved.forEach(artifactEntry.getValue().getCreators()::remove);

            if (artifactEntry.getValue().getCreators().isEmpty() && artifactEntry.getValue().getBusinesses().isEmpty()) {
                artifactsToRemove.add(artifactEntry.getKey());
            }
        }
        artifactsToRemove.forEach(artifacts::remove);
    }
}
