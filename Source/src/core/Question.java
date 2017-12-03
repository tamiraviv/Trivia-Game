package core;

import java.util.ArrayList;
import java.util.Collections;

public class Question {

	private String questionStr ="";
	private String answer1 = "";
	private String answer2 = "";
	private String answer3 = "";
	private String answerRight = "";

	public Question(String strQues, String strRightAns, String ans1, String ans2, String ans3) {
		this.questionStr = strQues;
		this.answerRight = strRightAns;
		this.answer1 = ans1;
		this.answer2 = ans2;
		this.answer3 = ans3;
	}
	
	//get shuffle answers
	public ArrayList<String> getAnswers()
	{
		ArrayList<String> ls = new ArrayList<String>();
		ls.add(this.answer1);
		ls.add(this.answer2);
		ls.add(this.answer3);
		ls.add(this.answerRight);
		
		//Shuffle Answers
		Collections.shuffle(ls);
		
		return ls;
	}
	
	public String getQuestion()
	{
		return (this.questionStr);
	}
	
	//Check if the answer is right
	public boolean checkAnswer(String strAnswer)
	{
		return(strAnswer.compareTo(this.answerRight) == 0);
	}
}
