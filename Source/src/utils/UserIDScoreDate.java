package utils;

import java.util.Date;

public class UserIDScoreDate {

    public UserIDScoreDate(int userId, int score, Date date) {
        this.userID = userId;
        this.score = score;
        this.date = date;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private int userID;

    private int score;

    private Date date;
}
