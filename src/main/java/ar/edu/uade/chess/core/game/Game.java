package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.port.ChessGame;
import ar.edu.uade.chess.core.port.GameObserver;
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

    @Override
    public void undo() {
        MoveCommand command = history.popForUndo();
        if (command == null) {
            return;
        }
        command.undo(board);
        turnManager.previous();
        updateStatus();
        notifyTurn();
    }

    @Override
    public void redo() {
        MoveCommand command = history.popForRedo();
        if (command == null) {
            return;
        }
        command.execute(board);
        turnManager.next();
        notifyMove(command.getMove());
        updateStatus();
        notifyTurn();
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
