package ar.edu.uade.chess.core.port.out;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.game.GameStatus;

/**
 * Driven port (Observer): the core announces what happened without knowing who
 * listens. Whoever subscribes must also unsubscribe.
 */
public interface GameObserver {
    void onMoveExecuted(Move move);

    void onTurnChanged(Color turn);

    void onCheck(Color color);

    void onGameOver(GameStatus status);
}
