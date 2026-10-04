package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.ChessGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Computer player: scores every legal move with the injected evaluator and plays
 * the best one, breaking ties at random (Random is injected so tests are repeatable).
 */
public class AIPlayerStrategy implements PlayerStrategy {
    private final MoveEvaluationStrategy evaluator;
    private final Random random;

    public AIPlayerStrategy(MoveEvaluationStrategy evaluator, Random random) {
        this.evaluator = evaluator;
        this.random = random;
    }

    @Override
    public Move chooseMove(ChessGame game, Color color) {
        List<Move> legalMoves = game.getLegalMoves();
        if (legalMoves.isEmpty()) {
            return null;
        }
        Board board = game.getBoard();
        List<Move> best = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        for (Move move : legalMoves) {
            int score = evaluator.evaluate(board, move, color);
            if (score > bestScore) {
                bestScore = score;
                best.clear();
            }
            if (score == bestScore) {
                best.add(move);
            }
        }
        return best.get(random.nextInt(best.size()));
    }
}
