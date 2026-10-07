package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * A board square that paints its piece (with the chosen style), its move marker and its
 * coordinate. Everything is sized from the square itself, so it works on any board size.
 */
final class SquareButton extends JButton {
    /** Where a selected piece can go: an empty square or a capture. */
    enum TargetMark { NONE, MOVE, CAPTURE }

    private static final float PIECE_RATIO = 0.58f;
    private static final float DOT_RATIO = 0.2f;
    private static final float RING_RATIO = 0.05f;
    private static final float COORDINATE_RATIO = 0.16f;
    private static final int MIN_COORDINATE_FONT = 9;
    private static final int MAX_COORDINATE_FONT = 13;

    private Piece piece;
    private PieceStyle style;
    private TargetMark targetMark = TargetMark.NONE;
    private Color markColor;
    private String fileLabel;
    private String rankLabel;
    private Color coordinateColor;

    SquareButton() {
        setMargin(new Insets(0, 0, 0, 0));
        setOpaque(true);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setFocusPainted(false);
    }

    void setPiece(Piece piece, PieceStyle style) {
        this.piece = piece;
        this.style = style;
    }

    void setTargetMark(TargetMark mark, Color color) {
        this.targetMark = mark;
        this.markColor = color;
    }

    /** Labels drawn inside the square (only on standard-size boards); null hides them. */
    void setCoordinates(String file, String rank, Color color) {
        fileLabel = file;
        rankLabel = rank;
        coordinateColor = color;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int side = Math.min(width, height);
        if (piece != null && style != null) {
            PiecePainter.paint(g, piece, style, Math.round(side * PIECE_RATIO), 0, 0, width, height);
        }
        paintTargetMark(g, width, height, side);
        paintCoordinates(g, width, height, side);
    }

    private void paintTargetMark(Graphics2D g, int width, int height, int side) {
        if (targetMark == TargetMark.NONE) return;
        g.setColor(markColor);
        if (targetMark == TargetMark.MOVE) {
            int dot = Math.max(4, Math.round(side * DOT_RATIO));
            g.fillOval((width - dot) / 2, (height - dot) / 2, dot, dot);
        } else {
            float ring = Math.max(2f, side * RING_RATIO);
            g.setStroke(new BasicStroke(ring));
            int inset = Math.round(ring / 2);
            g.drawRect(inset, inset, width - 2 * inset - 1, height - 2 * inset - 1);
        }
    }

    private void paintCoordinates(Graphics2D g, int width, int height, int side) {
        if (coordinateColor == null) return;
        int fontSize = Math.max(MIN_COORDINATE_FONT, Math.min(MAX_COORDINATE_FONT, Math.round(side * COORDINATE_RATIO)));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        g.setColor(coordinateColor);
        int margin = Math.max(2, fontSize / 3);
        if (rankLabel != null) g.drawString(rankLabel, margin, margin + g.getFontMetrics().getAscent() - 2);
        if (fileLabel != null) {
            int labelWidth = g.getFontMetrics().stringWidth(fileLabel);
            g.drawString(fileLabel, width - margin - labelWidth, height - margin);
        }
    }
}
