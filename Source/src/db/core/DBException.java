package db.core;

/**
 * Created by Oded on 09/05/2015.
 */
public class DBException extends Exception 
{
	private static final long serialVersionUID = 7526472295622776147L; 
    public DBException(String s) 
    {
        super(s);
    }
}
