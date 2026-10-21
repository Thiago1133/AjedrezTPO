package ar.edu.uade.chess.adapter.engine;

import ar.edu.uade.chess.adapter.SquareNotation;
import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.port.in.GameQueries;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Writes the game's position in FEN, the text format chess engines read
 * (for example "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1").
 * FEN describes pieces by their letters, so this class works with piece symbols only; the
 * castling rights and the en passant square are asked to the game, which applies the rules.
 * The move counters are written as "0 1": the game does not expose them and the engine
 * only needs them for the fifty-move rule, which the core already applies.
 */
final class FenWriter {
    private static final int SIZE = 8;
    private static final Set<Character> FEN_LETTERS = Set.of(
            'K', 'Q', 'R', 'B', 'N', 'P', 'k', 'q', 'r', 'b', 'n', 'p');

    private FenWriter() {
    }

    /** The position in FEN, or empty when it cannot be written in that format. */
    static Optional<String> write(GameQueries game) {
        Board board = game.getBoard();
        if (board.getRows() != SIZE || board.getColumns() != SIZE || !onlyFenLetters(board)) {
            return Optional.empty();
        }
        return Optional.of(placement(board) + " " + sideToMove(game) + " " + castling(game) + " "
                + enPassantTarget(game) + " 0 1");
    }

    private static boolean onlyFenLetters(Board board) {
        for (Color color : Color.values()) {
            for (Position position : board.getPositionsOf(color)) {
                if (!FEN_LETTERS.contains(board.getPiece(position).getSymbol())) return false;
            }
        }
        return true;
    }

    /** Rows from 8 down to 1; runs of empty squares are written as digits. */
    private static String placement(Board board) {
        StringBuilder fen = new StringBuilder();
        for (int row = SIZE - 1; row >= 0; row--) {
            int empty = 0;
            for (int column = 0; column < SIZE; column++) {
                Piece piece = board.getPiece(new Position(row, column));
                if (piece == null) {
                    empty++;
                    continue;
                }
                if (empty > 0) fen.append(empty);
                empty = 0;
                fen.append(piece.getSymbol());
            }
            if (empty > 0) fen.append(empty);
            if (row > 0) fen.append('/');
        }
        return fen.toString();
    }

    private static String sideToMove(GameQueries game) {
        return game.getCurrentTurn() == Color.WHITE ? "w" : "b";
    }

    /** K and Q for White's king and queen side, k and q for Black's (the rooks on the h and a files). */
    private static String castling(GameQueries game) {
        StringBuilder rights = new StringBuilder();
        for (Color color : List.of(Color.WHITE, Color.BLACK)) {
            List<Position> partners = game.getCastlingPartners(color);
            if (partners.stream().anyMatch(rook -> rook.getColumn() > SIZE / 2)) rights.append(side(color, 'K'));
            if (partners.stream().anyMatch(rook -> rook.getColumn() < SIZE / 2)) rights.append(side(color, 'Q'));
        }
        return rights.isEmpty() ? "-" : rights.toString();
    }

    private static char side(Color color, char letter) {
        return color == Color.WHITE ? letter : Character.toLowerCase(letter);
    }

    private static String enPassantTarget(GameQueries game) {
        Position square = game.getEnPassantSquare();
        return square == null ? "-" : SquareNotation.square(square);
    }
}
