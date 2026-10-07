package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;
import ar.edu.uade.chess.core.port.out.MoveInput;

/** Delegates the decision to a person through the MoveInput port. */
public class HumanPlayerStrategy implements PlayerStrategy {
    private final MoveInput input;

    public HumanPlayerStrategy(MoveInput input) {
        this.input = input;
    }

    @Override
    public Move chooseMove(GameQueries game, Color color) {
        return input.readMove(color);
    }
}
