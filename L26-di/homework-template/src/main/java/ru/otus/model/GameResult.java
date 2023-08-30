package ru.otus.model;

public class GameResult {
    private static final String RESULT_PATTERN = "Dear: %s. Equations amount: %d, correct answers: %d";

    private final Player player;
    private int total;
    private int rightAnswers;

    public GameResult(Player player) {
        this.player = player;
    }

    public void incrementRightAnswers(boolean mustIncremented) {
        total++;
        rightAnswers = mustIncremented? ++rightAnswers: rightAnswers;
    }

    @Override
    public String toString() {
        return String.format(RESULT_PATTERN, player.getName(),total, rightAnswers);
    }
}
