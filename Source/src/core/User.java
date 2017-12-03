package core;


import java.util.Date;

import utils.DataNotFoundException;
import utils.EntityNotFound;
import utils.IDName;
import db.dao.DAO;
import db.dao.DAOException;

public class User {

	private boolean isLogged = false;
	private String strUserName;
	private int nUserId;
	private IDName strFavCountry = null;
	private int playerScore;
	private DAO access;
	
	public User(String userName, String password, DAO access) throws DAOException, DataNotFoundException, EntityNotFound {
		this.access = access;
		this.nUserId = this.access.getUserID(userName);
		this.strUserName = userName;
		this.isLogged = this.access.checkPassword(this.nUserId, password); 
	}
	
	public String getUserName()
	{
		return(this.strUserName);
	}
	
	public boolean isLoggedIn()
	{
		return this.isLogged;
	}
	
	public void setFavCountry(IDName strCountry)
	{
		this.strFavCountry = strCountry;
	}

	public IDName getFavCountry()
	{
		return (this.strFavCountry);
	}
	
	public int getUserScore()
	{
		return (this.playerScore);
	}
	
	public void addRightAnswerScore()
	{
		this.playerScore++;
	}

	public void initPlayerScore()
	{
		this.playerScore = 0;
	}
	
	public void savePlayerScore() throws DAOException, EntityNotFound
	{
		  //get current date time with Date()
		  java.util.Date date = new Date();
		  date.getTime();
		  this.access.setScore(this.nUserId, this.playerScore, date);
			
	}

	//update on db user statistic
	public void setUserAnsweredCorrectly() throws DAOException, EntityNotFound
	{
		this.access.setUserAnsweredCorrectly(this.nUserId);
	}
	
	//update on db user statistic
	public void setUserAnsweredWrong() throws DAOException, EntityNotFound
	{
		this.access.setUserAnsweredWrong(this.nUserId);
	}
	
	//Update User latest game in DB
	public void setUserStartedNewGame() throws DAOException, EntityNotFound
	{
		this.access.setUserStartedNewGame(this.nUserId);
	}
	
	public static int registerUser(String userName, String Password, DAO access) throws DAOException
	{
		//add here saving player Score to DB		 
		//Return success
		return (access.createUser(userName, Password));
	}
}
