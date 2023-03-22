package org.example;

public class NumbersGame {

    private int randomNumber;
    public int guessCounter;
    public boolean gameStarted = false;

    public enum gameResponse {LESS, EQUAL, BIGGER}

    public void newGame() {
        this.gameStarted = true;
        randomNumber = (int) (Math.random() * 100 + 1);
        guessCounter = 0;
    }

    public void endGame() {
        this.gameStarted = false;
    }

    public gameResponse guess(String input) {
        if (!gameStarted) {
            throw new RuntimeException("No game to play at the moment!");
        }
        guessCounter++;

        try {
            int inputAsInt = Integer.parseInt(input);
            if (inputAsInt < 1 || inputAsInt > 100) {
                throw new ArithmeticException("Number must be between 1 and 100!");
            }
            return displayComparisonResult(inputAsInt);

        } catch (NumberFormatException numberFormatException) {
            throw new NumberFormatException("This is not a number!");
        }
    }

    private gameResponse displayComparisonResult(int userNumber) {
        if (userNumber < randomNumber) {
            return gameResponse.LESS;
        } else if (userNumber > randomNumber) {
            return gameResponse.BIGGER;
        } else {
            endGame();
            return gameResponse.EQUAL;
        }
    }
}