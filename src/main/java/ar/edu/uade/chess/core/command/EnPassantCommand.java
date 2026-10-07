package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;

/** En passant: the captured pawn is not on the destination square but beside the origin. */
public class EnPassantCommand implements MoveCommand {
    private final Move move;
    private final Position capturedAt;
    private Piece capturedPiece;

    public EnPassantCommand(Move move, Position capturedAt) {
        this.move = move;
        this.capturedAt = capturedAt;
    }

    @Override
    public void execute(Board board) {
        capturedPiece = board.removePiece(capturedAt);
        board.movePiece(move.getFrom(), move.getTo());
        board.getPiece(move.getTo()).setMoved(true);
    }

    @Override
    public void undo(Board board) {
        board.movePiece(move.getTo(), move.getFrom());
        board.placePiece(capturedPiece, capturedAt);
    }

    @Override
    public Move getMove() {
        return move;
    }

    @Override
    public boolean isIrreversible() {
        return true;
    }

    @Override
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
}
