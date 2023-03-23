package org.example;

public class NumbersGameStats {

    public int totalGames = 0;
    public int mostGuesses;
    public int leastGuesses;
    public int totalGuesses = 0;
    public int lastGameGuesses = 0;
    public int guessCounter;

    public void updateStatsOnGameEnd() {
        lastGameGuesses = guessCounter;
        totalGuesses += guessCounter;
        totalGames += 1;
        if (guessCounter < leastGuesses || leastGuesses == 0) leastGuesses = guessCounter;
        if (guessCounter > mostGuesses) mostGuesses = guessCounter;
    }

    @Override
    public String toString() {
        return "Your stats:\n" +
                "Total games: " + totalGames + "\n" +
                "Best game: " + leastGuesses + " guesses" + "\n" +
                "Worst game: " + mostGuesses + " guesses" + "\n" +
                "Avg. num of guesses: " + ((float) totalGuesses / totalGames) + " per game" + "\n" +
                "Last game: " + lastGameGuesses + " guesses" + "\n";
    }
}
