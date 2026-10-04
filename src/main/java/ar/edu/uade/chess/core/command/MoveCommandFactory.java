package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.rules.SpecialMoveRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Chooses the command for a move: the first special rule that applies, or a
 * normal move otherwise. Special rules are injected, never hard-coded.
 */
public class MoveCommandFactory {
    private final List<SpecialMoveRule> specialRules;

    public MoveCommandFactory(List<SpecialMoveRule> specialRules) {
        this.specialRules = List.copyOf(specialRules);
    }

    public boolean isSpecialMove(Board board, Move move, MoveHistory history) {
        return findRule(board, move, history) != null;
    }

    public MoveCommand create(Board board, Move move, MoveHistory history) {
        SpecialMoveRule rule = findRule(board, move, history);
        return rule != null ? rule.createCommand(board, move) : new NormalMoveCommand(move);
    }

    public List<Move> getSpecialMoves(Board board, Position from, MoveHistory history) {
        List<Move> result = new ArrayList<>();
        for (SpecialMoveRule rule : specialRules) {
            result.addAll(rule.getCandidateMoves(board, from, history));
        }
        return result;
    }

    private SpecialMoveRule findRule(Board board, Move move, MoveHistory history) {
        for (SpecialMoveRule rule : specialRules) {
            if (rule.canApply(board, move, history)) {
                return rule;
            }
        }
        return null;
    }
}
