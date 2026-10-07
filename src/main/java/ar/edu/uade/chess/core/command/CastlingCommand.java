package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.piece.Piece;

/** Castling: the king and the rook move in a single turn. */
public class CastlingCommand implements MoveCommand {
    private final Move kingMove;
    private final Move rookMove;

    public CastlingCommand(Move kingMove, Move rookMove) {
        this.kingMove = kingMove;
        this.rookMove = rookMove;
    }

    @Override
    public void execute(Board board) {
        board.movePiece(kingMove.getFrom(), kingMove.getTo());
        board.movePiece(rookMove.getFrom(), rookMove.getTo());
        board.getPiece(kingMove.getTo()).setMoved(true);
        board.getPiece(rookMove.getTo()).setMoved(true);
    }

    /** Castling requires both pieces to be unmoved, so undo restores them as unmoved. */
    @Override
    public void undo(Board board) {
        board.movePiece(rookMove.getTo(), rookMove.getFrom());
        board.movePiece(kingMove.getTo(), kingMove.getFrom());
        board.getPiece(kingMove.getFrom()).setMoved(false);
        board.getPiece(rookMove.getFrom()).setMoved(false);
    }

    @Override
    public Move getMove() {
        return kingMove;
    }

    @Override
    public boolean isIrreversible() {
        return true;
    }

    /** Castling never captures. */
    @Override
    public Piece getCapturedPiece() {
        return null;
    }
}
