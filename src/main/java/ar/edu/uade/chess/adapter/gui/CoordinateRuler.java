package ar.edu.uade.chess.adapter.gui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Column or row names beside a board too big for coordinates inside its squares. It sits
 * in the scroll pane's header, so the names stay visible while the board scrolls.
 */
final class CoordinateRuler extends JComponent {
    /** Which names the ruler shows: files above the board or ranks to its left. */
    enum Axis { FILES, RANKS }

    private static final int THICKNESS = 24;
    private static final int RANK_WIDTH = 34;
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    private static final Color TEXT = new Color(175, 181, 191);

    private final BoardPanel board;
    private final Axis axis;

    CoordinateRuler(BoardPanel board, Axis axis) {
        this.board = board;
        this.axis = axis;
        board.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                revalidate();
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return axis == Axis.FILES
                ? new Dimension(board.getWidth(), THICKNESS)
                : new Dimension(RANK_WIDTH, board.getHeight());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(FONT);
        g.setColor(TEXT);
        FontMetrics metrics = g.getFontMetrics();
        int cell = board.cellSize();
        Point origin = board.gridOrigin();
        if (axis == Axis.FILES) {
            for (int column = 0; column < board.getColumns(); column++) {
                String label = board.fileLabelAt(column);
                int x = origin.x + column * cell + (cell - metrics.stringWidth(label)) / 2;
                g.drawString(label, x, (THICKNESS + metrics.getAscent()) / 2 - 1);
            }
        } else {
            for (int row = 0; row < board.getRows(); row++) {
                String label = board.rankLabelAt(row);
                int y = origin.y + row * cell + (cell + metrics.getAscent()) / 2 - 1;
                g.drawString(label, RANK_WIDTH - metrics.stringWidth(label) - 6, y);
            }
        }
    }
}
