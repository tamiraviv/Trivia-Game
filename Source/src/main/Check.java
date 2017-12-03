package main;

import db.dao.DAO;
import utils.DBUser;
import utils.IDName;

public class Check {

    public static void main(String[] args) throws Exception {
        DAO dao = new DAO();

        dao.connect(DBUser.MODIFIER, null);

        System.out.println(dao.getPersonBirthDate(32596));

        for(IDName x : dao.getTwoBusinessesThatThereIsNotOneOfEachOneCreatorLearnedInTheSameCountryAsTheOtherOne(254)) {
            System.out.println(x.getId() + " " + x.getName());
        }



        dao.disconnect();
    }
}
