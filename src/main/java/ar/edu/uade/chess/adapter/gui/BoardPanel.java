package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.adapter.SquareNotation;
import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Visual board sized from the core's dimensions; it does not validate or execute chess rules.
 * Squares stay square and centered. When even the smallest usable square does not fit
 * (a 64x64 board, for example) the board keeps that size and the scroll pane scrolls it.
 */
final class BoardPanel extends JPanel implements Scrollable {
    /** Preferred length of the board's longest side when it fits. */
    private static final int PREFERRED_SIDE = 640;
    private static final int STANDARD_SIZE = 8;
    /** Smallest square that can still be read and clicked. */
    private static final int MIN_CELL = 40;
    private final Consumer<Position> onClick;
    private final JPanel grid = new JPanel();
    private SquareButton[][] squares = new SquareButton[0][0];
    private int rows, columns;
    private Position selected, lastFrom, lastTo;
    private Set<Position> targets = Set.of();
    private Set<Position> captureTargets = Set.of();
    private boolean flipped;
    private PieceStyle style;
    private BoardTheme theme;

    BoardPanel(Consumer<Position> onClick, PieceStyle style, BoardTheme theme) {
        this.onClick = onClick;
        this.style = style;
        this.theme = theme;
        setLayout(null);
        setOpaque(false);
        add(grid);
    }

    @Override
    public void doLayout() {
        if (rows == 0 || columns == 0) return;
        int cell = cellSize();
        int width = cell * columns;
        int height = cell * rows;
        grid.setBounds(Math.max(0, (getWidth() - width) / 2), Math.max(0, (getHeight() - height) / 2), width, height);
    }

    int cellSize() {
        if (rows == 0 || columns == 0) return MIN_CELL;
        return Math.max(MIN_CELL, Math.min(getWidth() / columns, getHeight() / rows));
    }

    /** Top-left corner of the squares inside this panel (they are centered when there is room). */
    Point gridOrigin() {
        return grid.getLocation();
    }

    int getRows() { return rows; }

    int getColumns() { return columns; }

    /** Boards up to 8x8 show coordinates inside the squares; bigger ones use rulers beside the board. */
    boolean showsCoordinatesInside() {
        return rows <= STANDARD_SIZE && columns <= STANDARD_SIZE;
    }

    String fileLabelAt(int viewColumn) {
        return SquareNotation.file(positionForView(0, viewColumn).getColumn());
    }

    String rankLabelAt(int viewRow) {
        return SquareNotation.rank(positionForView(viewRow, 0).getRow());
    }

    @Override
    public Dimension getPreferredSize() {
        if (rows == 0 || columns == 0) return new Dimension(PREFERRED_SIDE, PREFERRED_SIDE);
        int cell = Math.max(MIN_CELL, PREFERRED_SIDE / Math.max(rows, columns));
        return new Dimension(cell * columns, cell * rows);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        Dimension preferred = getPreferredSize();
        return new Dimension(Math.min(preferred.width, PREFERRED_SIDE), Math.min(preferred.height, PREFERRED_SIDE));
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return MIN_CELL;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return orientation == javax.swing.SwingConstants.HORIZONTAL ? visible.width : visible.height;
    }

    /** Stretch with the window while the smallest square fits; otherwise scroll. */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getParent() instanceof JViewport viewport && viewport.getWidth() >= MIN_CELL * Math.max(1, columns);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport viewport && viewport.getHeight() >= MIN_CELL * Math.max(1, rows);
    }

    void setHighlights(Position selected, Set<Position> targets, Set<Position> captureTargets) {
        this.selected = selected;
        this.targets = Set.copyOf(targets);
        this.captureTargets = Set.copyOf(captureTargets);
    }

    /** Squares of the last move played (both null at the start of the game). */
    void setLastMove(Position from, Position to) {
        this.lastFrom = from;
        this.lastTo = to;
    }

    void setFlipped(boolean flipped, Board currentBoard) {
        this.flipped = flipped;
        render(currentBoard);
        revalidate();
        repaint();
    }

    boolean isFlipped() { return flipped; }

    /** Takes effect on the next render. */
    void setAppearance(PieceStyle style, BoardTheme theme) {
        this.style = style;
        this.theme = theme;
    }

    void render(Board board) {
        rebuildIfResized(board);
        boolean coordinatesInside = showsCoordinatesInside();
        for (int r = 0; r < rows; r++) for (int c = 0; c < columns; c++) {
            Position pos = positionForView(r, c);
            SquareButton square = squares[r][c];
            boolean lightSquare = (pos.getRow() + pos.getColumn()) % 2 == 0;
            java.awt.Color bg = theme.squareColor(lightSquare);
            if (pos.equals(lastFrom) || pos.equals(lastTo)) bg = theme.lastMove();
            if (pos.equals(selected)) bg = theme.selected();
            square.setBackground(bg);
            square.setCoordinates(
                    coordinatesInside && r == rows - 1 ? SquareNotation.file(pos.getColumn()) : null,
                    coordinatesInside && c == 0 ? SquareNotation.rank(pos.getRow()) : null,
                    theme.coordinateColor(lightSquare));

            Piece piece = board.getPiece(pos);
            square.setPiece(piece, style);
            SquareButton.TargetMark mark = SquareButton.TargetMark.NONE;
            if (targets.contains(pos)) {
                mark = captureTargets.contains(pos) ? SquareButton.TargetMark.CAPTURE : SquareButton.TargetMark.MOVE;
            }
            square.setTargetMark(mark, theme.marker());
        }
        grid.repaint();
    }

    /** Recreates the squares only when the board's dimensions differ from the current grid. */
    private void rebuildIfResized(Board board) {
        if (board.getRows() == rows && board.getColumns() == columns) return;
        rows = board.getRows();
        columns = board.getColumns();
        grid.removeAll();
        grid.setLayout(new GridLayout(rows, columns));
        squares = new SquareButton[rows][columns];
        for (int viewRow = 0; viewRow < rows; viewRow++) for (int col = 0; col < columns; col++) {
            int row = viewRow;
            int column = col;
            SquareButton square = new SquareButton();
            square.addActionListener(e -> onClick.accept(positionForView(row, column)));
            squares[viewRow][col] = square;
            grid.add(square);
        }
        revalidate();
    }

    private Position positionForView(int viewRow, int viewColumn) {
        return flipped
                ? new Position(viewRow, columns - 1 - viewColumn)
                : new Position(rows - 1 - viewRow, viewColumn);
    }
}
