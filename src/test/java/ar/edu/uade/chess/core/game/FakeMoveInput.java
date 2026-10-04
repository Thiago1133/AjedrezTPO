package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.MoveInput;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/** Stub: returns pre-loaded moves in order, then null (as if the player quit). */
class FakeMoveInput implements MoveInput {
    private final Queue<Move> moves;

    FakeMoveInput(List<Move> moves) {
        this.moves = new ArrayDeque<>(moves);
    }

    @Override
    public Move readMove(Color color) {
        return moves.poll();
    }
}
