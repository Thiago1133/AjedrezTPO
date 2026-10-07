package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Asked before each game: game mode, piece style and board colors. The selected card is
 * outlined and can be changed freely until "Empezar"; closing the dialog cancels.
 */
final class NewGameDialog extends JDialog {
    private static final java.awt.Color BACKGROUND = new java.awt.Color(34, 37, 43);
    private static final java.awt.Color CARD = new java.awt.Color(48, 52, 60);
    private static final java.awt.Color TEXT = java.awt.Color.WHITE;
    private static final java.awt.Color SUBTITLE = new java.awt.Color(175, 181, 191);
    private static final java.awt.Color HIGHLIGHT = new java.awt.Color(235, 201, 92);

    private final List<Piece> previewPieces;
    private final List<JComponent> previews = new ArrayList<>();
    private final List<JPanel> styleCards = new ArrayList<>();
    private final List<JPanel> themeCards = new ArrayList<>();
    private final JToggleButton twoPlayers = new JToggleButton("Dos jugadores");
    private final JToggleButton versusComputer = new JToggleButton("Contra la computadora");
    private PieceStyle style;
    private BoardTheme theme;
    private NewGameChoice result;

    private NewGameDialog(Frame owner, NewGameChoice initial, List<Piece> previewPieces) {
        super(owner, "Nueva partida", true);
        this.previewPieces = previewPieces;
        this.style = initial.style();
        this.theme = initial.theme();
        twoPlayers.setSelected(!initial.versusComputer());
        versusComputer.setSelected(initial.versusComputer());
        buildLayout();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Shows the dialog and waits for the player. The preview pieces (a white and a black one,
     * for example) come from the caller, so this class does not create pieces itself.
     */
    static Optional<NewGameChoice> ask(Frame owner, NewGameChoice initial, List<Piece> previewPieces) {
        NewGameDialog dialog = new NewGameDialog(owner, initial, previewPieces);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private void buildLayout() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        root.add(title("MODO DE JUEGO"));
        ButtonGroup modes = new ButtonGroup();
        modes.add(twoPlayers);
        modes.add(versusComputer);
        root.add(row(twoPlayers, versusComputer));

        root.add(title("PIEZAS"));
        JPanel styleRow = row();
        for (PieceStyle option : AppearanceCatalog.pieceStyles()) {
            JPanel card = card(option.displayName(), preview(() -> option, () -> theme));
            card.addMouseListener(onClick(() -> {
                style = option;
                refreshSelection();
            }));
            styleCards.add(card);
            styleRow.add(card);
        }
        root.add(styleRow);

        root.add(title("TABLERO"));
        JPanel themeRow = row();
        for (BoardTheme option : AppearanceCatalog.boardThemes()) {
            JPanel card = card(option.displayName(), preview(() -> style, () -> option));
            card.addMouseListener(onClick(() -> {
                theme = option;
                refreshSelection();
            }));
            themeCards.add(card);
            themeRow.add(card);
        }
        root.add(themeRow);

        JButton start = new JButton("Empezar");
        start.addActionListener(e -> {
            result = new NewGameChoice(versusComputer.isSelected(), style, theme);
            dispose();
        });
        JPanel footer = row(start);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        root.add(footer);
        getRootPane().setDefaultButton(start);

        setContentPane(root);
        refreshSelection();
    }

    /** Outlines the chosen cards and repaints the previews, which mix the current style and theme. */
    private void refreshSelection() {
        List<PieceStyle> styles = AppearanceCatalog.pieceStyles();
        for (int i = 0; i < styleCards.size(); i++) outline(styleCards.get(i), styles.get(i) == style);
        List<BoardTheme> themes = AppearanceCatalog.boardThemes();
        for (int i = 0; i < themeCards.size(); i++) outline(themeCards.get(i), themes.get(i) == theme);
        previews.forEach(JComponent::repaint);
    }

    private void outline(JPanel card, boolean selected) {
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? HIGHLIGHT : CARD, 3),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(SUBTITLE);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setBorder(BorderFactory.createEmptyBorder(12, 0, 6, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JPanel row(JComponent... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        for (JComponent component : components) row.add(component);
        return row;
    }

    private JPanel card(String name, JComponent preview) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel label = new JLabel(name, JLabel.CENTER);
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        card.add(preview, BorderLayout.CENTER);
        card.add(label, BorderLayout.SOUTH);
        return card;
    }

    /** A strip of alternating squares showing the preview pieces. */
    private JComponent preview(java.util.function.Supplier<PieceStyle> styleOf,
                               java.util.function.Supplier<BoardTheme> themeOf) {
        int cell = 40;
        JComponent preview = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics;
                for (int i = 0; i < previewPieces.size(); i++) {
                    g.setColor(themeOf.get().squareColor(i % 2 == 0));
                    g.fillRect(i * cell, 0, cell, cell);
                    PiecePainter.paint(g, previewPieces.get(i), styleOf.get(), 30, i * cell, 0, cell, cell);
                }
            }
        };
        preview.setPreferredSize(new Dimension(cell * previewPieces.size(), cell));
        previews.add(preview);
        return preview;
    }

    private MouseAdapter onClick(Runnable action) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                action.run();
            }
        };
    }

    /** What the player chose for the next game. */
    record NewGameChoice(boolean versusComputer, PieceStyle style, BoardTheme theme) {
    }
}
