package ar.edu.uade.chess.adapter.gui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

/** A board square that paints its coordinate over the normal button contents. */
final class SquareButton extends JButton {
    private static final Color LIGHT_COORDINATE = new Color(78, 103, 57);
    private static final Color DARK_COORDINATE = new Color(231, 235, 211);
    private static final Font COORDINATE_FONT = new Font("SansSerif", Font.BOLD, 13);

    private String fileLabel;
    private String rankLabel;
    private Color coordinateColor;

    SquareButton() {
        setMargin(new Insets(0, 0, 0, 0));
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setFont(new Font("Serif", Font.PLAIN, 46));
        setForeground(new Color(35, 38, 35));
        setOpaque(true);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    void setCoordinates(String file, String rank, boolean lightSquare) {
        fileLabel = file;
        rankLabel = rank;
        coordinateColor = lightSquare ? LIGHT_COORDINATE : DARK_COORDINATE;
    }

    void usePieceFont() { setFont(new Font("Serif", Font.PLAIN, 46)); }
    void useMarkerFont() { setFont(new Font("Serif", Font.PLAIN, 30)); }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
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
