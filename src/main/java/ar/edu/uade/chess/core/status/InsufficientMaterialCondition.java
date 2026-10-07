package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceTrait;

import java.util.ArrayList;
import java.util.List;

/**
 * Draw when neither side can possibly checkmate: royal pieces alone, royal pieces plus a
 * single MINOR piece, or two opposing COLOR_BOUND pieces on squares of the same color.
 * Pieces are recognised by their traits, never by name, so new pieces opt in by
 * declaring traits.
 */
public class InsufficientMaterialCondition implements GameEndCondition {

    @Override
    public boolean isMet(Board board, MoveHistory history, Color toMove) {
        List<LocatedPiece> nonRoyal = new ArrayList<>();
        for (Color color : Color.values()) {
            int royalPieces = 0;
            for (Position position : board.getPositionsOf(color)) {
                Piece piece = board.getPiece(position);
                if (piece.hasTrait(PieceTrait.ROYAL)) {
                    royalPieces++;
                } else {
                    nonRoyal.add(new LocatedPiece(piece, position));
                }
            }
            if (royalPieces != 1) {
                return false;
            }
        }

        if (nonRoyal.isEmpty()) {
            return true;
        }
        if (nonRoyal.size() == 1) {
            return nonRoyal.get(0).piece().hasTrait(PieceTrait.MINOR);
        }
        return nonRoyal.size() == 2 && opposingColorBoundOnSameSquareColor(nonRoyal.get(0), nonRoyal.get(1));
    }

    @Override
    public GameStatus getResult() {
        return GameStatus.DRAW;
    }

    private boolean opposingColorBoundOnSameSquareColor(LocatedPiece first, LocatedPiece second) {
        return first.piece().hasTrait(PieceTrait.COLOR_BOUND)
                && second.piece().hasTrait(PieceTrait.COLOR_BOUND)
                && first.piece().getColor() != second.piece().getColor()
                && isLightSquare(first.position()) == isLightSquare(second.position());
    }

    private boolean isLightSquare(Position position) {
        return (position.getRow() + position.getColumn()) % 2 != 0;
    }

    private record LocatedPiece(Piece piece, Position position) { }
}
