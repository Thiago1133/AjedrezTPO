package ar.edu.uade.chess.adapter.engine;

import ar.edu.uade.chess.adapter.SquareNotation;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.piece.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the moves in an engine's UCI output. Lines are recognised with fixed patterns,
 * never by comparing text. While searching the engine prints, for each of its best lines,
 * "info ... multipv N ... pv e2e4 ...", and it finishes with "bestmove e2e4".
 */
final class UciMoves {
    private static final Pattern CANDIDATE = Pattern.compile("^info .*\\bmultipv (\\d+)\\b.*\\bpv (\\S+)");
    private static final Pattern BEST_MOVE = Pattern.compile("^bestmove (\\S+)");
    private static final Pattern MOVE = Pattern.compile("^([a-h][1-8])([a-h][1-8])([qrbn])?$");

    private UciMoves() {
    }

    /**
     * The engine's candidate moves, best first. Each line is taken from the deepest search the
     * engine printed for it; without candidate lines, the best move alone.
     *
     * @param promotionChoices the pieces a pawn may become, to read promotions such as "a7a8n"
     */
    static List<Move> candidates(List<String> output, List<Piece> promotionChoices) {
        TreeMap<Integer, String> byRank = new TreeMap<>();
        String best = null;
        for (String line : output) {
            Matcher candidate = CANDIDATE.matcher(line);
            if (candidate.find()) byRank.put(Integer.parseInt(candidate.group(1)), candidate.group(2));
            Matcher bestMove = BEST_MOVE.matcher(line);
            if (bestMove.find()) best = bestMove.group(1);
        }
        List<String> texts = new ArrayList<>(byRank.values());
        if (texts.isEmpty() && best != null) texts.add(best);
        List<Move> moves = new ArrayList<>();
        for (String text : texts) {
            parse(text, promotionChoices).ifPresent(moves::add);
        }
        return moves;
    }

    /** "e2e4" or "a7a8q"; empty for anything else, such as "(none)". */
    static Optional<Move> parse(String text, List<Piece> promotionChoices) {
        Matcher matcher = MOVE.matcher(text);
        if (!matcher.matches()) return Optional.empty();
        String promotion = matcher.group(3);
        String promotionId = promotion == null ? null : promotionId(promotion.charAt(0), promotionChoices);
        return Optional.of(new Move(SquareNotation.parse(matcher.group(1)), SquareNotation.parse(matcher.group(2)),
                promotionId));
    }

    /** UCI writes the new piece with its lowercase letter, which is also its symbol here. */
    private static String promotionId(char letter, List<Piece> promotionChoices) {
        return promotionChoices.stream()
                .filter(piece -> Character.toLowerCase(piece.getSymbol()) == letter)
                .map(Piece::getId)
                .findFirst()
                .orElse(null);
    }
}
