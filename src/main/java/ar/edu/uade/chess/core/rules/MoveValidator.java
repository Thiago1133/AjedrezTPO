package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.piece.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Full legality check: the piece belongs to the player, its movement (or a special
 * rule) allows the move, and the player's own royal pieces are not left in check.
 */
public class MoveValidator {
    private final CheckDetector checkDetector;
    private final MoveCommandFactory commandFactory;

    public MoveValidator(CheckDetector checkDetector, MoveCommandFactory commandFactory) {
        this.checkDetector = checkDetector;
        this.commandFactory = commandFactory;
    }

    public boolean isLegal(Board board, Move move, MoveHistory history, Color color) {
        Position from = move.getFrom();
        Position to = move.getTo();
        if (!board.isInside(from) || !board.isInside(to) || from.equals(to)) {
            return false;
        }
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != color) {
            return false;
        }
        boolean allowed = piece.canMove(from, to, board) || commandFactory.isSpecialMove(board, move, history);
        return allowed && !leavesKingInCheck(board, move, history, color);
    }

    /** Simulates the move on a copy of the board; the real board is never touched. */
    public boolean leavesKingInCheck(Board board, Move move, MoveHistory history, Color color) {
        Board simulation = board.copy();
        MoveCommand command = commandFactory.create(simulation, move, history);
        command.execute(simulation);
        return checkDetector.isInCheck(simulation, color);
    }

    public List<Move> getLegalMoves(Board board, MoveHistory history, Color color) {
        List<Move> result = new ArrayList<>();
        for (Position from : board.getPositionsOf(color)) {
            List<Move> candidates = new ArrayList<>();
            for (Position to : board.getPiece(from).getReachablePositions(from, board)) {
                candidates.add(new Move(from, to));
            }
            candidates.addAll(commandFactory.getSpecialMoves(board, from, history));
            for (Move candidate : candidates) {
                if (!result.contains(candidate) && isLegal(board, candidate, history, color)) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }
}
