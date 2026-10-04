package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.piece.Piece;

/** A regular move or capture: one piece goes from origin to destination. */
public class NormalMoveCommand implements MoveCommand {
    private final Move move;
    private Piece capturedPiece;
    private boolean wasMoved;

    public NormalMoveCommand(Move move) {
        this.move = move;
    }

    @Override
    public void execute(Board board) {
        Piece piece = board.getPiece(move.getFrom());
        if (piece == null) {
            throw new IllegalStateException("No piece to move at " + move.getFrom());
        }
        wasMoved = piece.hasMoved();
        capturedPiece = board.removePiece(move.getTo());
        board.movePiece(move.getFrom(), move.getTo());
        piece.setMoved(true);
    }

    @Override
    public void undo(Board board) {
        board.movePiece(move.getTo(), move.getFrom());
        board.getPiece(move.getFrom()).setMoved(wasMoved);
        if (capturedPiece != null) {
            board.placePiece(capturedPiece, move.getTo());
        }
    }

    @Override
    public Move getMove() {
        return move;
    }
}
