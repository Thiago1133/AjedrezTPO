package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;

/** Places the initial pieces. A different board or variant gets its own setup. */
public interface BoardSetup {
    void setup(Board board);
}
