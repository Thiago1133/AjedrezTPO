package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Position;

import java.util.List;

/**
 * One movement capability. A piece is composed of one or more rules,
 * so new pieces are built by combining existing rules.
 * Rules ignore check: that is validated elsewhere.
 */
public interface MovementRule {
    boolean canMove(Piece piece, Position from, Position to, Board board);

    List<Position> getReachablePositions(Piece piece, Position from, Board board);
}
