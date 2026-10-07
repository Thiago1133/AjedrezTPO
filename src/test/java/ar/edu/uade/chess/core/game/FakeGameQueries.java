package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.port.in.GameQueries;

import java.util.List;

/** Stub of the read-only port the computer receives: a fixed board and fixed legal moves. */
class FakeGameQueries implements GameQueries {
    private final Board board;
    private final List<Move> legalMoves;

    FakeGameQueries(Board board, List<Move> legalMoves) {
        this.board = board;
        this.legalMoves = legalMoves;
    }

    @Override
    public Board getBoard() {
        return board.copy();
    }

    @Override
    public List<Move> getLegalMoves() {
        return legalMoves;
    }

    @Override
    public boolean isCapture(Move move) {
        return false;
    }

    @Override
    public boolean isPromotion(Move move) {
        return false;
    }

    @Override
    public Move getLastMove() {
        return null;
    }

    @Override
    public List<Piece> getCapturedPieces(Color capturer) {
        return List.of();
    }

    @Override
    public int getMaterialAdvantage(Color color) {
        return 0;
    }

    @Override
    public Color getCurrentTurn() {
        return Color.BLACK;
    }

    @Override
    public GameStatus getStatus() {
        return GameStatus.IN_PROGRESS;
    }

    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public boolean canRedo() {
        return false;
    }

}
