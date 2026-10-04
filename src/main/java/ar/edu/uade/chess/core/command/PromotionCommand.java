package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.piece.Piece;

/** Promotion: the pawn leaves the board and the new piece appears on the destination. */
public class PromotionCommand implements MoveCommand {
    private final Move move;
    private final Piece promotedPiece;
    private Piece pawn;
    private Piece capturedPiece;

    public PromotionCommand(Move move, Piece promotedPiece) {
        this.move = move;
        this.promotedPiece = promotedPiece;
    }

    @Override
    public void execute(Board board) {
        pawn = board.removePiece(move.getFrom());
        capturedPiece = board.removePiece(move.getTo());
        board.placePiece(promotedPiece, move.getTo());
        promotedPiece.setMoved(true);
    }

    @Override
    public void undo(Board board) {
        board.removePiece(move.getTo());
        board.placePiece(pawn, move.getFrom());
        if (capturedPiece != null) {
            board.placePiece(capturedPiece, move.getTo());
        }
    }

    @Override
    public Move getMove() {
        return move;
    }

    @Override
    public boolean isIrreversible() {
        return true;
    }
}
