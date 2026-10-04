package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;

import java.util.List;

/** Classic starting position on an 8-column board. */
public class StandardChessSetup implements BoardSetup {
    private static final List<String> BACK_RANK =
            List.of("rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook");

    private final PieceFactory pieceFactory;

    public StandardChessSetup(PieceFactory pieceFactory) {
        this.pieceFactory = pieceFactory;
    }

    @Override
    public void setup(Board board) {
        if (board.getColumns() != BACK_RANK.size() || board.getRows() < 4) {
            throw new IllegalArgumentException("Standard setup needs 8 columns and at least 4 rows");
        }
        int lastRow = board.getRows() - 1;
        placeArmy(board, Color.WHITE, 0, 1);
        placeArmy(board, Color.BLACK, lastRow, lastRow - 1);
    }

    private void placeArmy(Board board, Color color, int backRow, int pawnRow) {
        for (int column = 0; column < BACK_RANK.size(); column++) {
            board.placePiece(pieceFactory.create(BACK_RANK.get(column), color), new Position(backRow, column));
            board.placePiece(pieceFactory.create("pawn", color), new Position(pawnRow, column));
        }
    }
}
