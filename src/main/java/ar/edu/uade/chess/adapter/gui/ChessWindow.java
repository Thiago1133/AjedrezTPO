package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.adapter.PieceNames;
import ar.edu.uade.chess.adapter.SquareNotation;
import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceTrait;
import ar.edu.uade.chess.core.port.ChessGame;
import ar.edu.uade.chess.core.port.GameObserver;
import ar.edu.uade.chess.core.port.PromotionOptions;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/** Swing adapter: translates clicks into moves and displays core state. */
public final class ChessWindow extends JFrame implements GameObserver {
    private static final int VALUE_FONT_SIZE = 18;
    private static final int SECTION_GAP = 12;
    private static final int ADVANTAGE_GAP = 7;
    private final Function<Boolean, ChessGame> gameFactory;
    private final List<Piece> previewPieces;
    private final PromotionOptions promotionOptions;
    /** Last options chosen; offered again when the next game starts (only while the window is open). */
    private NewGameDialog.NewGameChoice lastChoice = new NewGameDialog.NewGameChoice(false,
            AppearanceCatalog.pieceStyles().get(0), AppearanceCatalog.boardThemes().get(0));
    private final BoardPanel board = new BoardPanel(this::squareClicked, lastChoice.style(), lastChoice.theme());
    private final JScrollPane boardScroll = new JScrollPane(board);
    private final CoordinateRuler fileRuler = new CoordinateRuler(board, CoordinateRuler.Axis.FILES);
    private final CoordinateRuler rankRuler = new CoordinateRuler(board, CoordinateRuler.Axis.RANKS);
    private final JLabel turn = new JLabel();
    private final JLabel status = new JLabel();
    private final CapturedPiecesView capturedByWhite = new CapturedPiecesView();
    private final JLabel whiteAdvantage = new JLabel(" ");
    private final CapturedPiecesView capturedByBlack = new CapturedPiecesView();
    private final JLabel blackAdvantage = new JLabel(" ");
    private final JLabel lastMoveText = new JLabel("—");
    private final JLabel message = new JLabel(" ");
    private final JButton restart = new JButton("Nueva partida");
    private final JButton undo = new JButton("Deshacer jugada");
    private final JButton redo = new JButton("Rehacer jugada");
    private final JButton rotate = new JButton("Girar tablero");
    private final Deque<LastMove> uiMoveHistory = new ArrayDeque<>();
    private ChessGame game;
    private CapturedPiecesTracker captures;
    private Position selected, lastFrom, lastTo;
    private Color checkedColor;
    private boolean versusComputer;
    private boolean handlingGameOver;
    /** Moves undone that can still be redone; a new move discards them, as in the core. */
    private int redoablePlies;

    public ChessWindow(Function<Boolean, ChessGame> gameFactory, List<Piece> previewPieces,
                       PromotionOptions promotionOptions) {
        super("Ajedrez");
        this.gameFactory = gameFactory;
        this.previewPieces = List.copyOf(previewPieces);
        this.promotionOptions = promotionOptions;
        buildLayout();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 650));
        startNewGame();
        if (game == null) {
            dispose();
            return;
        }
        pack();
        setLocationRelativeTo(null);
    }

    public boolean hasGame() {
        return game != null;
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(24, 0));
        root.setBackground(new java.awt.Color(34, 37, 43));
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JLabel heading = new JLabel("AJEDREZ");
        heading.setForeground(java.awt.Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        root.add(heading, BorderLayout.NORTH);
        java.awt.Color background = new java.awt.Color(34, 37, 43);
        boardScroll.setBorder(BorderFactory.createEmptyBorder());
        boardScroll.getViewport().setBackground(background);
        boardScroll.setBackground(background);
        JPanel corner = new JPanel();
        corner.setBackground(background);
        boardScroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);
        root.add(boardScroll, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new java.awt.Color(48, 52, 60));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        addInfo(sidebar, "TURNO", turn);
        addInfo(sidebar, "ESTADO", status);
        addTitle(sidebar, "CAPTURADAS POR BLANCAS");
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(capturedByWhite);
        addValue(sidebar, whiteAdvantage, VALUE_FONT_SIZE, ADVANTAGE_GAP);
        addTitle(sidebar, "CAPTURADAS POR NEGRAS");
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(capturedByBlack);
        addValue(sidebar, blackAdvantage, VALUE_FONT_SIZE, ADVANTAGE_GAP);
        addInfo(sidebar, "ÚLTIMA JUGADA", lastMoveText);
        message.setForeground(new java.awt.Color(235, 201, 92));
        message.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        sidebar.add(message);
        undo.setAlignmentX(LEFT_ALIGNMENT);
        undo.setEnabled(false);
        undo.addActionListener(e -> undoLastMove());
        sidebar.add(undo);
        sidebar.add(Box.createVerticalStrut(10));
        redo.setAlignmentX(LEFT_ALIGNMENT);
        redo.setEnabled(false);
        redo.addActionListener(e -> redoLastMove());
        sidebar.add(redo);
        sidebar.add(Box.createVerticalStrut(10));
        rotate.setAlignmentX(LEFT_ALIGNMENT);
        rotate.addActionListener(e -> {
            board.setFlipped(!board.isFlipped(), game.getBoard());
            fileRuler.repaint();
            rankRuler.repaint();
        });
        sidebar.add(rotate);
        sidebar.add(Box.createVerticalStrut(10));
        restart.setAlignmentX(LEFT_ALIGNMENT);
        restart.setEnabled(true);
        restart.addActionListener(e -> startNewGame());
        sidebar.add(restart);
        root.add(sidebar, BorderLayout.EAST);
        setContentPane(root);
    }

    private void addInfo(JPanel panel, String title, JLabel value) {
        addTitle(panel, title);
        addValue(panel, value, VALUE_FONT_SIZE, SECTION_GAP);
    }

    private void addTitle(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        label.setForeground(new java.awt.Color(175, 181, 191));
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        panel.add(label);
    }

    private void addValue(JPanel panel, JLabel value, int fontSize, int bottomGap) {
        value.setForeground(java.awt.Color.WHITE);
        value.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        value.setBorder(BorderFactory.createEmptyBorder(3, 0, bottomGap, 0));
        panel.add(value);
    }

    private void startNewGame() {
        Optional<NewGameDialog.NewGameChoice> choice = NewGameDialog.ask(this, lastChoice, previewPieces);
        if (choice.isEmpty()) return;
        lastChoice = choice.get();

        if (game != null) game.removeObserver(this);
        versusComputer = lastChoice.versusComputer();
        board.setAppearance(lastChoice.style(), lastChoice.theme());
        game = gameFactory.apply(versusComputer);
        game.addObserver(this);
        captures = new CapturedPiecesTracker(game.getBoard());
        selected = lastFrom = lastTo = null;
        checkedColor = null;
        uiMoveHistory.clear();
        undo.setEnabled(false);
        redoablePlies = 0;
        redo.setEnabled(false);
        board.setFlipped(false, game.getBoard());
        showRulersIfNeeded();
        lastMoveText.setText("—");
        message.setText(" ");
        updateCapturedPieces();
        board.setHighlights(null, Set.of(), Set.of(), null, null);
        restart.setText("Nueva partida");
        restart.setEnabled(true);
        game.start();
        refresh();
    }

    private void squareClicked(Position pos) {
        if (game.getStatus().isGameOver()) return;
        Piece piece = game.getBoard().getPiece(pos);
        if (selected == null) {
            if (piece != null && piece.getColor() == game.getCurrentTurn()) select(pos);
            return;
        }
        if (pos.equals(selected)) {
            clearSelection();
            return;
        }
        if (piece != null && piece.getColor() == game.getCurrentTurn()) {
            select(pos);
            return;
        }
        Move legalMove = game.getLegalMoves().stream()
                .filter(candidate -> candidate.getFrom().equals(selected) && candidate.getTo().equals(pos))
                .findFirst().orElse(null);
        if (legalMove == null) {
            message.setText("Movimiento inválido");
            clearSelection();
            refresh();
            return;
        }
        Piece movingPiece = game.getBoard().getPiece(selected);
        Move move = isPromotionMove(movingPiece, pos)
                ? new Move(selected, pos, promotionChoice(movingPiece.getColor()))
                : legalMove;
        if (game.move(move)) {
            message.setText(" ");
            redoablePlies = 0;
            redo.setEnabled(false);
            if (versusComputer && !game.getStatus().isGameOver()) game.playTurn();
        } else {
            message.setText("Movimiento inválido");
        }
        clearSelection();
        refresh();
    }

    private boolean isPromotionMove(Piece piece, Position to) {
        if (piece == null || !piece.hasTrait(PieceTrait.PROMOTES)) return false;
        return !game.getBoard().isInside(to.offset(new Direction(piece.getColor().forward(), 0)));
    }

    /** Offers exactly the pieces the core accepts; closing the dialog takes the first (most valuable). */
    private String promotionChoice(Color color) {
        List<Piece> choices = promotionOptions.getPromotionChoices(color);
        Object[] names = choices.stream().map(PieceNames::of).toArray();
        int choice = JOptionPane.showOptionDialog(this, "Elegí una pieza para promocionar", "Promoción",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
        return choices.get(Math.max(choice, 0)).getId();
    }

    private void select(Position pos) {
        selected = pos;
        Set<Position> targets = new HashSet<>();
        Set<Position> captureTargets = new HashSet<>();
        Board current = game.getBoard();
        Piece piece = current.getPiece(pos);
        for (Move move : game.getLegalMoves()) {
            if (move.getFrom().equals(pos)) {
                targets.add(move.getTo());
                if (looksLikeEnPassantCapture(current, piece, move)) captureTargets.add(move.getTo());
            }
        }
        board.setHighlights(selected, targets, captureTargets, lastFrom, lastTo);
        message.setText(" ");
        refresh();
    }

    /**
     * Presentation-only guess used to highlight en-passant captures: a legal diagonal step onto an
     * empty square by a piece with the EN_PASSANT trait. The real rule stays in EnPassantRule.
     */
    private boolean looksLikeEnPassantCapture(Board current, Piece piece, Move move) {
        return piece != null && piece.hasTrait(PieceTrait.EN_PASSANT)
                && current.getPiece(move.getTo()) == null
                && move.getTo().getColumn() != move.getFrom().getColumn();
    }

    private void clearSelection() {
        selected = null;
        board.setHighlights(null, Set.of(), Set.of(), lastFrom, lastTo);
    }

    private void undoLastMove() {
        if (uiMoveHistory.isEmpty()) return;
        int maxUndos = versusComputer ? 2 : 1;
        for (int i = 0; i < maxUndos; i++) {
            Color before = game.getCurrentTurn();
            game.undo();
            if (game.getCurrentTurn() == before) break;
            captures.undoLastMove();
            redoablePlies++;
            if (!uiMoveHistory.isEmpty()) restoreLastMove(uiMoveHistory.pop());
            if (!versusComputer || game.getCurrentTurn() == Color.WHITE) break;
        }
        selected = null;
        message.setText(" ");
        board.setHighlights(null, Set.of(), Set.of(), lastFrom, lastTo);
        updateCapturedPieces();
        undo.setEnabled(!uiMoveHistory.isEmpty());
        redo.setEnabled(redoablePlies > 0);
        refresh();
    }

    /**
     * Redoes what undoLastMove took back. Game.redo notifies onMoveExecuted like a normal
     * move, so captures and the last-move highlight are recorded again there.
     */
    private void redoLastMove() {
        if (redoablePlies == 0) return;
        int maxRedos = versusComputer ? 2 : 1;
        for (int i = 0; i < maxRedos && redoablePlies > 0; i++) {
            Color before = game.getCurrentTurn();
            game.redo();
            if (game.getCurrentTurn() == before) break;
            redoablePlies--;
            if (!versusComputer || game.getCurrentTurn() == Color.WHITE) break;
        }
        selected = null;
        message.setText(" ");
        board.setHighlights(null, Set.of(), Set.of(), lastFrom, lastTo);
        redo.setEnabled(redoablePlies > 0);
        refresh();
    }

    private void restoreLastMove(LastMove previous) {
        lastFrom = previous.from();
        lastTo = previous.to();
        lastMoveText.setText(lastFrom == null ? "—" : format(lastFrom) + " → " + format(lastTo));
    }

    /**
     * Big boards show their coordinates in rulers beside the board and start scrolled to
     * the white side (bottom-left); standard boards keep them inside the squares.
     */
    private void showRulersIfNeeded() {
        boolean rulers = !board.showsCoordinatesInside();
        boardScroll.setColumnHeaderView(rulers ? fileRuler : null);
        boardScroll.setRowHeaderView(rulers ? rankRuler : null);
        if (boardScroll.getColumnHeader() != null) boardScroll.getColumnHeader().setBackground(boardScroll.getBackground());
        if (boardScroll.getRowHeader() != null) boardScroll.getRowHeader().setBackground(boardScroll.getBackground());
        boardScroll.revalidate();
        SwingUtilities.invokeLater(() -> board.scrollRectToVisible(new Rectangle(0, board.getHeight() - 1, 1, 1)));
    }

    private void refresh() {
        board.render(game.getBoard());
        turn.setText(game.getStatus().isGameOver() ? "—" : colorName(game.getCurrentTurn()));
        if (game.getStatus() != GameStatus.CHECK) checkedColor = null;
        status.setText(statusName(game.getStatus()));
        board.revalidate();
        board.repaint();
    }

    @Override public void onMoveExecuted(Move move) {
        uiMoveHistory.push(new LastMove(lastFrom, lastTo));
        captures.recordMove(game.getBoard(), game.getCurrentTurn().opposite());
        lastFrom = move.getFrom();
        lastTo = move.getTo();
        lastMoveText.setText(format(lastFrom) + " → " + format(lastTo));
        updateCapturedPieces();
        undo.setEnabled(true);
    }

    @Override public void onTurnChanged(Color color) { refresh(); }

    @Override public void onCheck(Color color) {
        checkedColor = color;
        status.setText("Jaque a " + colorName(color).toLowerCase());
        message.setText("¡Jaque a " + colorName(color).toLowerCase() + "!");
    }

    @Override public void onGameOver(GameStatus result) {
        if (handlingGameOver) return;
        handlingGameOver = true;
        refresh();
        status.setText("Finalizada");
        message.setText(" ");
        try {
            JOptionPane.showMessageDialog(this, finalResultText(result), "Fin de la partida",
                    JOptionPane.INFORMATION_MESSAGE);
        } finally {
            // startNewGame removes this observer, so wait until Game finishes notifying observers.
            SwingUtilities.invokeLater(() -> {
                try {
                    startNewGame();
                } finally {
                    handlingGameOver = false;
                }
            });
        }
    }

    private String finalResultText(GameStatus result) {
        return switch (result) {
            case CHECKMATE -> "Jaque mate - ganan "
                    + colorName(game.getCurrentTurn().opposite()).toLowerCase();
            case STALEMATE -> "Ahogado";
            case DRAW -> "Tablas";
            default -> "Partida finalizada";
        };
    }

    private void updateCapturedPieces() {
        capturedByWhite.setPieces(captures.capturedBy(Color.WHITE), lastChoice.style(), lastChoice.theme());
        capturedByBlack.setPieces(captures.capturedBy(Color.BLACK), lastChoice.style(), lastChoice.theme());
        whiteAdvantage.setText(advantageText(captures.advantageOf(Color.WHITE)));
        blackAdvantage.setText(advantageText(captures.advantageOf(Color.BLACK)));
        whiteAdvantage.setForeground(new java.awt.Color(150, 210, 130));
        blackAdvantage.setForeground(new java.awt.Color(150, 210, 130));
    }

    private String advantageText(int advantage) {
        return advantage > 0 ? "+" + advantage : " ";
    }

    private String colorName(Color color) { return color == Color.WHITE ? "Blancas" : "Negras"; }

    private String statusName(GameStatus value) {
        return switch (value) {
            case IN_PROGRESS -> "En juego";
            case CHECK -> checkedColor == null ? "Jaque" : "Jaque a " + colorName(checkedColor).toLowerCase();
            case CHECKMATE -> "<html>Jaque mate<br>Ganan "
                    + colorName(game.getCurrentTurn().opposite()).toLowerCase() + "</html>";
            case STALEMATE -> "Ahogado";
            case DRAW -> "Tablas";
        };
    }

    private String format(Position pos) { return SquareNotation.square(pos); }

    private record LastMove(Position from, Position to) { }
}
