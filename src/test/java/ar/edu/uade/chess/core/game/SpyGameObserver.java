package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.out.GameObserver;

import java.util.ArrayList;
import java.util.List;

/** Spy: records every notification so tests can assert on what the core announced. */
class SpyGameObserver implements GameObserver {
    final List<String> events = new ArrayList<>();

    @Override
    public void onMoveExecuted(Move move) {
        events.add("move " + move);
    }

    @Override
    public void onTurnChanged(Color turn) {
        events.add("turn " + turn);
    }

    @Override
    public void onCheck(Color color) {
        events.add("check " + color);
    }

    @Override
    public void onGameOver(GameStatus status) {
        events.add("over " + status);
    }
}
