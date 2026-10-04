package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;

/** A way the game can end (checkmate, stalemate, draw rules...). New ones are just injected. */
public interface GameEndCondition {
    boolean isMet(Board board, MoveHistory history, Color toMove);

    GameStatus getResult();
}
