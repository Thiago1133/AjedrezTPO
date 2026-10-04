package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.ChessGame;
import ar.edu.uade.chess.core.port.GameObserver;

import java.util.List;

/** Stub of the driving port: a fixed board and fixed legal moves, nothing else. */
class FakeChessGame implements ChessGame {
    private final Board board;
    private final List<Move> legalMoves;

    FakeChessGame(Board board, List<Move> legalMoves) {
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
    public Color getCurrentTurn() {
        return Color.BLACK;
    }

    @Override
    public GameStatus getStatus() {
        return GameStatus.IN_PROGRESS;
    }

    @Override
    public void start() {
    }

    @Override
    public boolean playTurn() {
        return false;
    }

    @Override
    public boolean move(Move move) {
        return false;
    }

    @Override
    public void undo() {
    }

    @Override
    public void redo() {
    }

    @Override
    public void addObserver(GameObserver observer) {
    }

    @Override
    public void removeObserver(GameObserver observer) {
    }
}
