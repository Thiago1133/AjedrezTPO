package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.Set;
import java.util.function.Consumer;

/** Visual board sized from the core's dimensions; it does not validate or execute chess rules. */
final class BoardPanel extends JPanel {
    private static final java.awt.Color LIGHT = new java.awt.Color(238, 238, 210);
    private static final java.awt.Color DARK = new java.awt.Color(118, 150, 86);
    private static final java.awt.Color SELECTED = new java.awt.Color(246, 246, 105);
    private static final java.awt.Color LAST_MOVE = new java.awt.Color(190, 210, 90);
    private static final java.awt.Color CAPTURE_BORDER = new java.awt.Color(65, 73, 58);
    /** Preferred length of the board's longest side; cells and glyphs shrink to fit it. */
    private static final int PREFERRED_SIDE = 640;
    private static final int STANDARD_SIZE = 8;
    private static final int PIECE_FONT_SIZE = 46;
    private static final int MARKER_FONT_SIZE = 30;
    private final Consumer<Position> onClick;
    private SquareButton[][] squares = new SquareButton[0][0];
    private int rows, columns;
    private Position selected, lastFrom, lastTo;
    private Set<Position> targets = Set.of();
    private Set<Position> captureTargets = Set.of();
    private boolean flipped;

    BoardPanel(Consumer<Position> onClick) {
        this.onClick = onClick;
        setPreferredSize(new java.awt.Dimension(PREFERRED_SIDE, PREFERRED_SIDE));
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        if (rows == 0 || columns == 0) {
            super.setBounds(x, y, width, height);
            return;
        }
        // Keep the cells square, centered in the space the layout offers.
        int cell = Math.min(width / columns, height / rows);
        int boardWidth = cell * columns;
        int boardHeight = cell * rows;
        super.setBounds(x + (width - boardWidth) / 2, y + (height - boardHeight) / 2, boardWidth, boardHeight);
    }

    void setHighlights(Position selected, Set<Position> targets, Set<Position> captureTargets,
                       Position lastFrom, Position lastTo) {
        this.selected = selected;
        this.targets = Set.copyOf(targets);
        this.captureTargets = Set.copyOf(captureTargets);
        this.lastFrom = lastFrom;
        this.lastTo = lastTo;
    }

    void setFlipped(boolean flipped, Board currentBoard) {
        this.flipped = flipped;
        render(currentBoard);
        revalidate();
        repaint();
    }

    boolean isFlipped() { return flipped; }

    void render(Board board) {
        rebuildIfResized(board);
        for (int r = 0; r < rows; r++) for (int c = 0; c < columns; c++) {
            Position pos = positionForView(r, c);
            SquareButton square = squares[r][c];
            // Reset the prior square state first so selection markers never linger.
            square.setText("");
            square.setBorderPainted(false);
            square.usePieceFont();
            java.awt.Color bg = (pos.getRow() + pos.getColumn()) % 2 == 0 ? LIGHT : DARK;
            if (pos.equals(lastFrom) || pos.equals(lastTo)) bg = LAST_MOVE;
            if (pos.equals(selected)) bg = SELECTED;
            square.setBackground(bg);
            square.setCoordinates(r == rows - 1 ? String.valueOf((char) ('a' + pos.getColumn())) : null,
                    c == 0 ? String.valueOf(pos.getRow() + 1) : null,
                    (pos.getRow() + pos.getColumn()) % 2 == 0);

            Piece piece = board.getPiece(pos);
            boolean target = targets.contains(pos);
            if (piece != null) {
                square.setText(PieceGlyphs.of(piece));
                // Unicode glyphs already distinguish piece colors; a dark ink stays legible on both square colors.
                square.setForeground(new java.awt.Color(35, 38, 35));
                if (target || captureTargets.contains(pos)) {
                    square.setBorder(BorderFactory.createLineBorder(CAPTURE_BORDER, 4));
                    square.setBorderPainted(true);
                }
            } else if (target) {
                if (captureTargets.contains(pos)) {
                    square.setBorder(BorderFactory.createLineBorder(CAPTURE_BORDER, 4));
                    square.setBorderPainted(true);
                } else {
                    square.setText("•");
                    square.useMarkerFont();
                    square.setForeground(new java.awt.Color(75, 82, 68));
                }
            }
        }
    }

    /** Recreates the squares only when the board's dimensions differ from the current grid. */
    private void rebuildIfResized(Board board) {
        if (board.getRows() == rows && board.getColumns() == columns) return;
        rows = board.getRows();
        columns = board.getColumns();
        removeAll();
        setLayout(new GridLayout(rows, columns));
        int longestSide = Math.max(rows, columns);
        int cell = PREFERRED_SIDE / longestSide;
        // Glyphs keep their standard size up to 8x8 and shrink proportionally on larger boards.
        float scale = Math.min(1f, (float) STANDARD_SIZE / longestSide);
        int pieceFontSize = Math.round(PIECE_FONT_SIZE * scale);
        int markerFontSize = Math.round(MARKER_FONT_SIZE * scale);
        squares = new SquareButton[rows][columns];
        for (int viewRow = 0; viewRow < rows; viewRow++) for (int col = 0; col < columns; col++) {
            int row = viewRow;
            int column = col;
            SquareButton square = new SquareButton(pieceFontSize, markerFontSize);
            square.addActionListener(e -> onClick.accept(positionForView(row, column)));
            squares[viewRow][col] = square;
            add(square);
        }
        setPreferredSize(new java.awt.Dimension(cell * columns, cell * rows));
        revalidate();
    }

    private Position positionForView(int viewRow, int viewColumn) {
        return flipped
                ? new Position(viewRow, columns - 1 - viewColumn)
                : new Position(rows - 1 - viewRow, viewColumn);
    }
}
