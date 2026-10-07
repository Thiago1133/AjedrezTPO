package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

/** A board square that paints its piece (with the chosen style) and its coordinate. */
final class SquareButton extends JButton {
    private static final Font COORDINATE_FONT = new Font("SansSerif", Font.BOLD, 13);

    private final int pieceFontSize;
    private final Font markerFont;
    private Piece piece;
    private PieceStyle style;
    private String fileLabel;
    private String rankLabel;
    private Color coordinateColor;

    SquareButton(int pieceFontSize, int markerFontSize) {
        this.pieceFontSize = pieceFontSize;
        markerFont = new Font("Serif", Font.PLAIN, markerFontSize);
        setMargin(new Insets(0, 0, 0, 0));
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setFont(markerFont);
        setOpaque(true);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    void setPiece(Piece piece, PieceStyle style) {
        this.piece = piece;
        this.style = style;
    }

    void setCoordinates(String file, String rank, Color color) {
        fileLabel = file;
        rankLabel = rank;
        coordinateColor = color;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (piece != null && style != null) {
            PiecePainter.paint((Graphics2D) graphics, piece, style, pieceFontSize, 0, 0, getWidth(), getHeight());
        }
        if (coordinateColor == null) return;
        graphics.setFont(COORDINATE_FONT);
        graphics.setColor(coordinateColor);
        Insets insets = getInsets();
        int left = Math.max(4, insets.left + 4);
        int top = Math.max(14, insets.top + 13);
        if (rankLabel != null) graphics.drawString(rankLabel, left, top);
        if (fileLabel != null) {
            int right = getWidth() - Math.max(5, insets.right + 5);
            int baseline = getHeight() - Math.max(4, insets.bottom + 4);
            int labelWidth = graphics.getFontMetrics().stringWidth(fileLabel);
            graphics.drawString(fileLabel, right - labelWidth, baseline);
        }
    }
}
