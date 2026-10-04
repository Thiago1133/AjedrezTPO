package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;

/**
 * Scores a candidate move for the AI (Strategy): higher is better. Swapping the
 * evaluator changes how the AI plays without touching AIPlayerStrategy.
 */
public interface MoveEvaluationStrategy {
    int evaluate(Board board, Move move, Color color);
}
