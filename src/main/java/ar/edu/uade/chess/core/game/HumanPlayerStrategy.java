package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.ChessGame;
import ar.edu.uade.chess.core.port.MoveInput;

/** Delegates the decision to a person through the MoveInput port. */
public class HumanPlayerStrategy implements PlayerStrategy {
    private final MoveInput input;

    public HumanPlayerStrategy(MoveInput input) {
        this.input = input;
    }

    @Override
    public Move chooseMove(ChessGame game, Color color) {
        return input.readMove(color);
    }
}
