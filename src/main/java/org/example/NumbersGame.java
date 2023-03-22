package org.example;

import static org.example.NumbersGame.GuessResponse.EQUAL;

public class NumbersGame {

    private int randomNumber;
    public int guessCounter;
    public boolean gameStarted = false;

    public enum GuessResponse {LESS, EQUAL, BIGGER}

    public void start() {
        this.gameStarted = true;
        randomNumber = (int) (Math.random() * 100 + 1);
        guessCounter = 0;
    }

    public void end() {
        this.gameStarted = false;
    }

    public GuessResponse guess(int input) {
        guessCounter++;
        //int number = validate(input);
        GuessResponse guessResponse = comparisonResult(input);
        if (guessResponse == EQUAL) {
            end();
        }
        return guessResponse;
    }

//    private int validate(String input) {
//        int number = tryConvertToInt(input);
//        if (number < 1 || number > 100) {
//            throw new RuntimeException("Number must be between 1 and 100!");
//        }
//        return number;
//    }

//    private int tryConvertToInt(String input) {
//        try {
//            return Integer.parseInt(input);
//        } catch (Exception e) {
//            throw new RuntimeException("This is not a number!");
//        }
//    }

    private GuessResponse comparisonResult(int userNumber) {
        if (userNumber < randomNumber) {
            return GuessResponse.LESS;
        } else if (userNumber > randomNumber) {
            return GuessResponse.BIGGER;
        } else {
            return EQUAL;
        }
    }
}