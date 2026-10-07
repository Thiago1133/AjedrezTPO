package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/** Draws a piece centered in an area with a given style; shared by the board and the previews. */
final class PiecePainter {
    private PiecePainter() {
    }

    static void paint(Graphics2D graphics, Piece piece, PieceStyle style, int fontSize,
                      int x, int y, int width, int height) {
        String symbol = style.symbolFor(piece);
        if (symbol.isEmpty()) return;
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            TextLayout layout = new TextLayout(symbol, style.font(fontSize), g.getFontRenderContext());
            Shape outline = layout.getOutline(null);
            Rectangle2D bounds = outline.getBounds2D();
            double dx = x + (width - bounds.getWidth()) / 2 - bounds.getX();
            double dy = y + (height - bounds.getHeight()) / 2 - bounds.getY();
            Shape shape = AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(outline);
            g.setColor(style.fillFor(piece));
            g.fill(shape);
            if (style.outlineFor(piece) != null) {
                g.setColor(style.outlineFor(piece));
                g.setStroke(new BasicStroke(Math.max(1f, fontSize / 28f)));
                g.draw(shape);
            }
        } finally {
            g.dispose();
        }
    }
}
