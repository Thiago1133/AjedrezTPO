package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.port.in.ChessGame;
import ar.edu.uade.chess.core.port.out.GameObserver;
import ar.edu.uade.chess.core.rules.MoveValidator;
import ar.edu.uade.chess.core.status.GameStatusEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates a match. It owns no rule itself: validation, move execution,
 * turns and status are delegated to injected collaborators.
 */
public class Game implements ChessGame {
    private final Board board;
    private final TurnManager turnManager;
    private final MoveValidator moveValidator;
    private final GameStatusEvaluator statusEvaluator;
    private final MoveCommandFactory commandFactory;
    private final MoveHistory history;
    private final List<GameObserver> observers = new ArrayList<>();
    private GameStatus status = GameStatus.IN_PROGRESS;

    public Game(Board board, TurnManager turnManager, MoveValidator moveValidator,
                GameStatusEvaluator statusEvaluator, MoveCommandFactory commandFactory, MoveHistory history) {
        this.board = board;
        this.turnManager = turnManager;
        this.moveValidator = moveValidator;
        this.statusEvaluator = statusEvaluator;
        this.commandFactory = commandFactory;
        this.history = history;
    }

    @Override
    public void start() {
        updateStatus();
        notifyTurn();
    }

    @Override
    public boolean playTurn() {
        if (status.isGameOver()) {
            return false;
        }
        Move move = turnManager.getCurrentPlayer().chooseMove(this);
        return move != null && move(move);
    }

    @Override
    public boolean move(Move move) {
        Color color = turnManager.getCurrentColor();
        if (status.isGameOver() || !moveValidator.isLegal(board, move, history, color)) {
            return false;
        }
        MoveCommand command = commandFactory.create(board, move, history);
        command.execute(board);
        history.push(command);
        turnManager.next();
        notifyMove(move);
        updateStatus();
        notifyTurn();
        return true;
    }

    /**
     * Takes back the last move, and keeps going while the player to move is automatic:
     * against the computer one undo takes back its reply and the human's move, so the
     * human is to move again. Between two humans it takes back exactly one move.
     */
    @Override
    public void undo() {
        if (!undoOneMove()) {
            return;
        }
        while (turnManager.getCurrentPlayer().isAutomatic() && history.canUndo()) {
            undoOneMove();
        }
        updateStatus();
        notifyTurn();
    }

    private boolean undoOneMove() {
        MoveCommand command = history.popForUndo();
        if (command == null) {
            return false;
        }
        command.undo(board);
        turnManager.previous();
        return true;
    }

    /** Replays what undo took back, again until a human is to move. */
    @Override
    public void redo() {
        if (!redoOneMove()) {
            return;
        }
        while (turnManager.getCurrentPlayer().isAutomatic() && history.canRedo()) {
            redoOneMove();
        }
        updateStatus();
        notifyTurn();
    }

    private boolean redoOneMove() {
        MoveCommand command = history.popForRedo();
        if (command == null) {
            return false;
        }
        command.execute(board);
        turnManager.next();
        notifyMove(command.getMove());
        return true;
    }

    @Override
    public boolean canUndo() {
        return history.canUndo();
    }

    @Override
    public boolean canRedo() {
        return history.canRedo();
    }

    @Override
    public Board getBoard() {
        return board.copy();
    }

    @Override
    public Color getCurrentTurn() {
        return turnManager.getCurrentColor();
    }

    @Override
    public List<Move> getLegalMoves() {
        if (status.isGameOver()) {
            return List.of();
        }
        return moveValidator.getLegalMoves(board, history, turnManager.getCurrentColor());
    }

    @Override
    public boolean isCapture(Move move) {
        MoveCommand preview = preview(move);
        return preview != null && preview.getCapturedPiece() != null;
    }

    @Override
    public boolean isPromotion(Move move) {
        MoveCommand preview = preview(move);
        return preview != null && preview.isPromotion();
    }

    @Override
    public Move getLastMove() {
        return history.getLastMove();
    }

    @Override
    public List<Piece> getCapturedPieces(Color capturer) {
        return history.getCapturedPiecesOf(capturer.opposite());
    }

    @Override
    public int getMaterialAdvantage(Color color) {
        return Math.max(0, capturedValue(color) - capturedValue(color.opposite()));
    }

    private int capturedValue(Color capturer) {
        return getCapturedPieces(capturer).stream().mapToInt(Piece::getValue).sum();
    }

    /**
     * Plays the move on a copy of the board and returns the executed command, so the
     * answer comes from the real rules while the game itself does not change. Null if illegal.
     */
    private MoveCommand preview(Move move) {
        Color color = turnManager.getCurrentColor();
        if (status.isGameOver() || !moveValidator.isLegal(board, move, history, color)) {
            return null;
        }
        Board copy = board.copy();
        MoveCommand command = commandFactory.create(copy, move, history);
        command.execute(copy);
        return command;
    }

    @Override
    public GameStatus getStatus() {
        return status;
    }

    @Override
    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    private void updateStatus() {
        status = statusEvaluator.evaluate(board, history, turnManager.getCurrentColor());
        if (status == GameStatus.CHECK) {
            observers.forEach(o -> o.onCheck(turnManager.getCurrentColor()));
        } else if (status.isGameOver()) {
            observers.forEach(o -> o.onGameOver(status));
        }
    }

    private void notifyMove(Move move) {
        observers.forEach(o -> o.onMoveExecuted(move));
    }

    private void notifyTurn() {
        if (!status.isGameOver()) {
            observers.forEach(o -> o.onTurnChanged(turnManager.getCurrentColor()));
        }
    }
}
