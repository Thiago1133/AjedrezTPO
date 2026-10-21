package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;

import java.util.List;

/** Classic starting position: back rank and pawns for each side, in the middle columns. */
public class StandardChessSetup implements BoardSetup {
    private static final List<String> BACK_RANK =
            List.of("rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook");

    private final PieceFactory pieceFactory;

    public StandardChessSetup(PieceFactory pieceFactory) {
        this.pieceFactory = pieceFactory;
    }

    @Override
    public void setup(Board board) {
        if (board.getColumns() < BACK_RANK.size() || board.getRows() < 4) {
            throw new IllegalArgumentException("Standard setup needs at least 8 columns and 4 rows");
        }
        int firstColumn = (board.getColumns() - BACK_RANK.size()) / 2;
        int lastRow = board.getRows() - 1;
        placeArmy(board, Color.WHITE, 0, 1, firstColumn);
        placeArmy(board, Color.BLACK, lastRow, lastRow - 1, firstColumn);
    }

    private void placeArmy(Board board, Color color, int backRow, int pawnRow, int firstColumn) {
        for (int i = 0; i < BACK_RANK.size(); i++) {
            int column = firstColumn + i;
            board.placePiece(pieceFactory.create(BACK_RANK.get(i), color), new Position(backRow, column));
            board.placePiece(pieceFactory.create("pawn", color), new Position(pawnRow, column));
        }
    }
}
