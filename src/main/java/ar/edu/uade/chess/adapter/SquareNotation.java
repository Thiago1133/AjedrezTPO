package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Position;

/**
 * Square names for any board size: columns a..z, then aa, ab... (like spreadsheet
 * columns) and rows 1, 2, 3... So "e2" on 8x8 and "bl64" on 64x64 both work.
 */
public final class SquareNotation {
    private static final int LETTERS = 26;

    private SquareNotation() {
    }

    public static String file(int column) {
        StringBuilder name = new StringBuilder();
        for (int n = column + 1; n > 0; n = (n - 1) / LETTERS) {
            name.insert(0, (char) ('a' + (n - 1) % LETTERS));
        }
        return name.toString();
    }

    public static String rank(int row) {
        return String.valueOf(row + 1);
    }

    public static String square(Position position) {
        return file(position.getColumn()) + rank(position.getRow());
    }

    /** Parses "e2" or "aa10" (case-insensitive). Returns null if the text is not a square. */
    public static Position parse(String text) {
        int letters = 0;
        int column = 0;
        while (letters < text.length() && Character.isLetter(text.charAt(letters))) {
            char letter = Character.toLowerCase(text.charAt(letters));
            if (letter < 'a' || letter > 'z') {
                return null;
            }
            column = column * LETTERS + (letter - 'a' + 1);
            letters++;
        }
        String digits = text.substring(letters);
        if (letters == 0 || digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            return new Position(Integer.parseInt(digits) - 1, column - 1);
        } catch (NumberFormatException tooLong) {
            return null;
        }
    }
}
