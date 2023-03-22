package org.example;

import static org.example.NumbersGame.GuessResponse.EQUAL;

public class NumbersGame {

    private int randomNumber;
    public int guessCounter;
    public boolean gameStarted = false;

    public enum GuessResponse {LESS, EQUAL, BIGGER}

    public void newGame() {
        this.gameStarted = true;
        randomNumber = (int) (Math.random() * 100 + 1);
        guessCounter = 0;
    }

    public void endGame() {
        this.gameStarted = false;
    }

    public GuessResponse guess(String input) {
        if (!gameStarted) {
            throw new RuntimeException("No game to play at the moment!");
        }
        guessCounter++;
        int number = validate(input);
        GuessResponse guessResponse = comparisonResult(number);
        if (guessResponse == EQUAL) {
            endGame();
        }
        return guessResponse;
    }

    private int validate(String input) {
        int number = tryConvertToInt(input);
        if (number < 1 || number > 100) {
            throw new ArithmeticException("Number must be between 1 and 100!");
        }
        return number;
    }

    private int tryConvertToInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException numberFormatException) {
            throw new NumberFormatException("This is not a number!");
        }
    }

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