package ru.otus.services;

import ru.otus.model.Equation;
import ru.otus.model.GameResult;
import ru.otus.model.Player;

import java.util.List;

public class GameProcessorImpl implements GameProcessor {

    private static final String MSG_HEADER = "Multiplication table knowledge test";
    private static final String MSG_INPUT_BASE = "Enter a number from 1 to 10";
    private static final String MSG_RIGHT_ANSWER = "That's right\n";
    private static final String MSG_WRONG_ANSWER = "Wrong\n";

    private final IOService ioService;
    private final EquationPreparer equationPreparer;
    private final PlayerService playerService;

    public GameProcessorImpl(IOService ioService,
                             EquationPreparer equationPreparer,
                             PlayerService playerService) {
        this.ioService = ioService;
        this.equationPreparer = equationPreparer;
        this.playerService = playerService;
    }

    @Override
    public void startGame() {
        ioService.out(MSG_HEADER);
        ioService.out("---------------------------------------");
        Player player = playerService.getPlayer();
        GameResult gameResult = new GameResult(player);

        int base = ioService.readInt(MSG_INPUT_BASE);
        List<Equation> equations = equationPreparer.prepareEquationsFor(base);
        equations.forEach(e -> {
            boolean isRight = ioService.readInt(e.toString()) == e.getResult();
            gameResult.incrementRightAnswers(isRight);
            ioService.out(isRight ? MSG_RIGHT_ANSWER : MSG_WRONG_ANSWER);
        });
        ioService.out(gameResult.toString());
    }
}
