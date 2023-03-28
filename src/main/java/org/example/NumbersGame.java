package org.example;

import java.time.LocalDateTime;

import static org.example.NumbersGame.GuessResponse.EQUAL;

public class NumbersGame {

    private int randomNumber;

    public NumbersGameStats stats = new NumbersGameStats();
    public boolean gameStarted = false;

    public enum GuessResponse {LESS, EQUAL, BIGGER}



    public void start() {
        this.gameStarted = true;
        randomNumber = (int) (Math.random() * 100 + 1);
        System.out.println(randomNumber);
        stats.guessCounter = 0;


    }

    public void end() {
        this.gameStarted = false;
    }

    public GuessResponse guess(int input) {
        stats.guessCounter++;
        GuessResponse guessResponse = comparisonResult(input);
        if (guessResponse == EQUAL) {
            stats.updateStatsOnGameEnd();
            end();
        }
        return guessResponse;
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