package questions;

import java.util.*;

public class Question {

    /** The question string */
    private String question;

    /** A list of 3 elements with false answers */
    private List<String> falseAnswers;

    /** The right answer to the question */
    private String correctAnswer;

    /**
     * Gets the question, correct answer and three more false answer.
     * It randoms the answers order and initialize correctly the answersArray and the correctAnswer.
     *
     * Note: the order of the false answer doesn't matter.
     */
    public Question(String question, String correctAnswer, String falseAnswerA, String falseAnswerB, String falseAnswerC) {
        this.question=question;

        this.falseAnswers = new ArrayList<>();
        this.falseAnswers.add(falseAnswerA);
        this.falseAnswers.add(falseAnswerB);
        this.falseAnswers.add(falseAnswerC);

        this.correctAnswer = correctAnswer;
    }

    /** Returns the question string */
    public String getQuestion() {
        return question;
    }

    /** Returns a 4 elements list, one of the answers is correct, the answers are shuffled */
    public List<String> getAnswers() {
        List<String> returnList = new ArrayList<>(falseAnswers);
        returnList.add(correctAnswer);
        Collections.shuffle(returnList);

        return returnList;
    }

    /** Returns true if the answer is correct, false otherwise */
    public boolean checkAnswer(String userAnswer) {
        return correctAnswer == userAnswer;
    }
}
