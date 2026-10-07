package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Captured pieces drawn with the chosen style and their real color, on a strip of the
 * board's light color so that both white and black pieces stay visible on the dark sidebar.
 */
final class CapturedPiecesView extends JComponent {
    private static final int CELL = 26;
    private static final int PIECE_SIZE = 22;
    private static final int PER_ROW = 7;
    private static final int PADDING = 3;
    private static final int ARC = 8;

    private List<Piece> pieces = List.of();
    private PieceStyle style;
    private BoardTheme theme;

    CapturedPiecesView() {
        setAlignmentX(LEFT_ALIGNMENT);
    }

    void setPieces(List<Piece> pieces, PieceStyle style, BoardTheme theme) {
        this.pieces = List.copyOf(pieces);
        this.style = style;
        this.theme = theme;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        int rows = Math.max(1, (pieces.size() + PER_ROW - 1) / PER_ROW);
        return new Dimension(PER_ROW * CELL + 2 * PADDING, rows * CELL + 2 * PADDING);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (pieces.isEmpty() || style == null) return;
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int rows = (pieces.size() + PER_ROW - 1) / PER_ROW;
        int columns = Math.min(pieces.size(), PER_ROW);
        g.setColor(theme.light());
        g.fillRoundRect(0, 0, columns * CELL + 2 * PADDING, rows * CELL + 2 * PADDING, ARC, ARC);
        for (int i = 0; i < pieces.size(); i++) {
            int x = PADDING + (i % PER_ROW) * CELL;
            int y = PADDING + (i / PER_ROW) * CELL;
            PiecePainter.paint(g, pieces.get(i), style, PIECE_SIZE, x, y, CELL, CELL);
        }
    }
}
