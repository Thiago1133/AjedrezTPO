package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.rules.CheckDetector;
import ar.edu.uade.chess.core.rules.MoveValidator;

/** The player to move is NOT in check but has no legal move: a draw. */
public class StalemateCondition implements GameEndCondition {
    private final MoveValidator validator;
    private final CheckDetector checkDetector;

    public StalemateCondition(MoveValidator validator, CheckDetector checkDetector) {
        this.validator = validator;
        this.checkDetector = checkDetector;
    }

    @Override
    public boolean isMet(Board board, MoveHistory history, Color toMove) {
        return !checkDetector.isInCheck(board, toMove) && validator.getLegalMoves(board, history, toMove).isEmpty();
    }

    @Override
    public GameStatus getResult() {
        return GameStatus.STALEMATE;
    }
}
