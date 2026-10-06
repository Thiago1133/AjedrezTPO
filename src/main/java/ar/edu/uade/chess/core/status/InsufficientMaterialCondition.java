package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.Piece;

import java.util.ArrayList;
import java.util.List;

/** Draw for the explicitly supported insufficient-material combinations. */
public class InsufficientMaterialCondition implements GameEndCondition {
    @Override
    public boolean isMet(Board board, MoveHistory history, Color toMove) {
        int whiteKings = 0;
        int blackKings = 0;
        List<LocatedPiece> otherPieces = new ArrayList<>();

        for (int row = 0; row < board.getRows(); row++) {
            for (int column = 0; column < board.getColumns(); column++) {
                Position position = new Position(row, column);
                Piece piece = board.getPiece(position);
                if (piece == null) continue;
                if (piece.getId().equals("king")) {
                    if (piece.getColor() == Color.WHITE) whiteKings++;
                    else blackKings++;
                } else {
                    otherPieces.add(new LocatedPiece(piece, position));
                }
            }
        }

        if (whiteKings != 1 || blackKings != 1) return false;
        if (otherPieces.isEmpty()) return true;
        if (otherPieces.size() == 1) {
            String id = otherPieces.getFirst().piece().getId();
            return id.equals("bishop") || id.equals("knight");
        }
        if (otherPieces.size() != 2) return false;

        LocatedPiece first = otherPieces.get(0);
        LocatedPiece second = otherPieces.get(1);
        return first.piece().getId().equals("bishop")
                && second.piece().getId().equals("bishop")
                && first.piece().getColor() != second.piece().getColor()
                && squareColor(first.position()) == squareColor(second.position());
    }

    @Override
    public GameStatus getResult() {
        return GameStatus.DRAW;
    }

    private boolean squareColor(Position position) {
        return (position.getRow() + position.getColumn()) % 2 == 0;
    }

    private record LocatedPiece(Piece piece, Position position) { }
}
