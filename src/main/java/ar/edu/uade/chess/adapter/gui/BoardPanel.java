package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.Set;
import java.util.function.Consumer;

/** Visual 8x8 board; it does not validate or execute chess rules. */
final class BoardPanel extends JPanel {
    private static final java.awt.Color LIGHT = new java.awt.Color(238, 238, 210);
    private static final java.awt.Color DARK = new java.awt.Color(118, 150, 86);
    private static final java.awt.Color SELECTED = new java.awt.Color(246, 246, 105);
    private static final java.awt.Color LAST_MOVE = new java.awt.Color(190, 210, 90);
    private static final java.awt.Color CAPTURE_BORDER = new java.awt.Color(65, 73, 58);
    private final SquareButton[][] squares = new SquareButton[8][8];
    private final Consumer<Position> onClick;
    private Position selected, lastFrom, lastTo;
    private Set<Position> targets = Set.of();
    private Set<Position> captureTargets = Set.of();
    private boolean flipped;

    BoardPanel(Consumer<Position> onClick) {
        super(new GridLayout(8, 8));
        this.onClick = onClick;
        setPreferredSize(new java.awt.Dimension(640, 640));
        for (int viewRow = 0; viewRow < 8; viewRow++) for (int col = 0; col < 8; col++) {
            int row = viewRow;
            int column = col;
            SquareButton square = new SquareButton();
            square.addActionListener(e -> onClick.accept(positionForView(row, column)));
            squares[viewRow][col] = square;
            add(square);
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        int side = Math.min(width, height);
        super.setBounds(x + (width - side) / 2, y + (height - side) / 2, side, side);
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
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
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
            square.setCoordinates(r == 7 ? String.valueOf((char) ('a' + pos.getColumn())) : null,
                    c == 0 ? String.valueOf(pos.getRow() + 1) : null,
                    (pos.getRow() + pos.getColumn()) % 2 == 0);

            Piece piece = board.getPiece(pos);
            boolean target = targets.contains(pos);
            if (piece != null) {
                square.setText(symbol(piece));
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

    private Position positionForView(int viewRow, int viewColumn) {
        return flipped
                ? new Position(viewRow, 7 - viewColumn)
                : new Position(7 - viewRow, viewColumn);
    }

    private String symbol(Piece p) {
        if (p == null) {
        return "";
        }

        boolean white = p.getColor() == Color.WHITE;

        return switch (p.getId()) {
            case "king" -> white ? "♔" : "♚";
            case "queen" -> white ? "♕" : "♛";
            case "rook" -> white ? "♖" : "♜";
            case "bishop" -> white ? "♗" : "♝";
            case "knight" -> white ? "♘" : "♞";
            case "pawn" -> white ? "♙" : "♟";
            default -> "";
        };
    }
}
