package ar.edu.uade.chess.adapter.gui;

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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Swing adapter: translates clicks into moves and displays core state. */
public final class ChessWindow extends JFrame implements GameObserver {
    private final Function<Boolean, ChessGame> gameFactory;
    private final BoardPanel board = new BoardPanel(this::squareClicked);
    private final JLabel turn = new JLabel();
    private final JLabel status = new JLabel();
    private final JLabel capturedByWhite = new JLabel(" ");
    private final JLabel whiteAdvantage = new JLabel(" ");
    private final JLabel capturedByBlack = new JLabel(" ");
    private final JLabel blackAdvantage = new JLabel(" ");
    private final JLabel lastMoveText = new JLabel("—");
    private final JLabel message = new JLabel(" ");
    private final JButton restart = new JButton("Nueva partida");
    private final JButton undo = new JButton("Deshacer jugada");
    private final JButton rotate = new JButton("Girar tablero");
    private final List<Piece> whiteCaptures = new ArrayList<>();
    private final List<Piece> blackCaptures = new ArrayList<>();
    private final Deque<UiMoveState> uiMoveHistory = new ArrayDeque<>();
    private ChessGame game;
    private Board boardBeforeLastMove;
    private Position selected, lastFrom, lastTo;
    private Color checkedColor;
    private boolean versusComputer;

    public ChessWindow(Function<Boolean, ChessGame> gameFactory) {
        super("Ajedrez");
        this.gameFactory = gameFactory;
        buildLayout();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 650));
        startNewGame();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(24, 0));
        root.setBackground(new java.awt.Color(34, 37, 43));
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JLabel heading = new JLabel("AJEDREZ");
        heading.setForeground(java.awt.Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        root.add(heading, BorderLayout.NORTH);
        root.add(board, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new java.awt.Color(48, 52, 60));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        addInfo(sidebar, "TURNO", turn);
        addInfo(sidebar, "ESTADO", status);
        addInfo(sidebar, "CAPTURADAS POR BLANCAS", capturedByWhite);
        addInfo(sidebar, "", whiteAdvantage);
        addInfo(sidebar, "CAPTURADAS POR NEGRAS", capturedByBlack);
        addInfo(sidebar, "", blackAdvantage);
        addInfo(sidebar, "ÚLTIMA JUGADA", lastMoveText);
        message.setForeground(new java.awt.Color(235, 201, 92));
        message.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        sidebar.add(message);
        undo.setAlignmentX(LEFT_ALIGNMENT);
        undo.setEnabled(false);
        undo.addActionListener(e -> undoLastMove());
        sidebar.add(undo);
        rotate.setAlignmentX(LEFT_ALIGNMENT);
        rotate.addActionListener(e -> board.setFlipped(!board.isFlipped(), game.getBoard()));
        sidebar.add(rotate);
        restart.setAlignmentX(LEFT_ALIGNMENT);
        restart.setEnabled(true);
        restart.addActionListener(e -> startNewGame());
        sidebar.add(restart);
        root.add(sidebar, BorderLayout.EAST);
        setContentPane(root);
    }

    private void addInfo(JPanel panel, String title, JLabel value) {
        if (!title.isEmpty()) {
            JLabel label = new JLabel(title);
            label.setForeground(new java.awt.Color(175, 181, 191));
            label.setFont(new Font("SansSerif", Font.BOLD, 11));
            panel.add(label);
        }
        value.setForeground(java.awt.Color.WHITE);
        value.setFont(new Font("SansSerif", Font.PLAIN, title.startsWith("CAPTURADAS") ? 19 : 18));
        value.setBorder(BorderFactory.createEmptyBorder(3, 0, title.isEmpty() ? 7 : 12, 0));
        panel.add(value);
    }

    private void startNewGame() {
        if (game != null) game.removeObserver(this);
        Object[] modes = {"Dos jugadores", "Contra la computadora"};
        int mode = JOptionPane.showOptionDialog(this, "Elegí el modo de juego", "Nueva partida",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);
        versusComputer = mode == 1;
        game = gameFactory.apply(versusComputer);
        game.addObserver(this);
        boardBeforeLastMove = game.getBoard();
        selected = lastFrom = lastTo = null;
        checkedColor = null;
        whiteCaptures.clear();
        blackCaptures.clear();
        uiMoveHistory.clear();
        undo.setEnabled(false);
        board.setFlipped(false, game.getBoard());
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
                ? new Move(selected, pos, promotionChoice())
                : legalMove;
        if (game.move(move)) {
            message.setText(" ");
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

    private String promotionChoice() {
        Object[] choices = {"Reina", "Torre", "Alfil", "Caballo"};
        int choice = JOptionPane.showOptionDialog(this, "Elegí una pieza para promocionar", "Promoción",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
        return switch (choice) {
            case 1 -> "rook";
            case 2 -> "bishop";
            case 3 -> "knight";
            default -> "queen";
        };
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
            if (!uiMoveHistory.isEmpty()) restoreUiState(uiMoveHistory.pop());
            if (!versusComputer || game.getCurrentTurn() == Color.WHITE) break;
        }
        selected = null;
        message.setText(" ");
        board.setHighlights(null, Set.of(), Set.of(), lastFrom, lastTo);
        boardBeforeLastMove = game.getBoard();
        updateCapturedPieces();
        undo.setEnabled(!uiMoveHistory.isEmpty());
        refresh();
    }

    private void restoreUiState(UiMoveState previous) {
        whiteCaptures.clear();
        whiteCaptures.addAll(previous.whiteCaptures());
        blackCaptures.clear();
        blackCaptures.addAll(previous.blackCaptures());
        lastFrom = previous.lastFrom();
        lastTo = previous.lastTo();
        lastMoveText.setText(lastFrom == null ? "—" : format(lastFrom) + " → " + format(lastTo));
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
        Board after = game.getBoard();
        uiMoveHistory.push(new UiMoveState(List.copyOf(whiteCaptures), List.copyOf(blackCaptures), lastFrom, lastTo));
        Color capturer = game.getCurrentTurn().opposite();
        List<Piece> captureList = capturer == Color.WHITE ? whiteCaptures : blackCaptures;
        recordCapturedPieces(boardBeforeLastMove, after, game.getCurrentTurn(), captureList);
        boardBeforeLastMove = after;
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
        refresh();
        message.setText("<html>Partida finalizada.<br>Iniciá otra cuando quieras.</html>");
        restart.setText("Nueva partida");
        restart.setEnabled(true);
    }

    private void recordCapturedPieces(Board before, Board after, Color victimColor, List<Piece> captures) {
        Map<String, Integer> beforeCounts = countPieces(before, victimColor);
        Map<String, Integer> afterCounts = countPieces(after, victimColor);
        Map<String, Integer> losses = new HashMap<>();
        beforeCounts.forEach((id, count) -> {
            int lost = count - afterCounts.getOrDefault(id, 0);
            if (lost > 0) losses.put(id, lost);
        });
        for (Position pos : before.getPositionsOf(victimColor)) {
            Piece piece = before.getPiece(pos);
            int lost = losses.getOrDefault(piece.getId(), 0);
            if (lost > 0) {
                captures.add(piece.copy());
                losses.put(piece.getId(), lost - 1);
            }
        }
    }

    private Map<String, Integer> countPieces(Board board, Color color) {
        Map<String, Integer> counts = new HashMap<>();
        for (Position pos : board.getPositionsOf(color)) {
            String id = board.getPiece(pos).getId();
            counts.merge(id, 1, Integer::sum);
        }
        return counts;
    }

    private void updateCapturedPieces() {
        Comparator<Piece> order = Comparator.<Piece>comparingInt(Piece::getValue).reversed()
                .thenComparing(Piece::getSymbol);
        whiteCaptures.sort(order);
        blackCaptures.sort(order);
        capturedByWhite.setText(capturedSymbols(whiteCaptures));
        capturedByBlack.setText(capturedSymbols(blackCaptures));
        int difference = capturedValue(whiteCaptures) - capturedValue(blackCaptures);
        whiteAdvantage.setText(difference > 0 ? "+" + difference : " ");
        blackAdvantage.setText(difference < 0 ? "+" + -difference : " ");
        whiteAdvantage.setForeground(new java.awt.Color(150, 210, 130));
        blackAdvantage.setForeground(new java.awt.Color(150, 210, 130));
    }

    private String capturedSymbols(List<Piece> captures) {
        if (captures.isEmpty()) return " ";
        StringBuilder result = new StringBuilder("<html>");
        for (int i = 0; i < captures.size(); i++) {
            if (i > 0) result.append(i % 7 == 0 ? "<br>" : " ");
            result.append(PieceGlyphs.of(captures.get(i)));
        }
        return result.append("</html>").toString();
    }

    private int capturedValue(List<Piece> captures) {
        return captures.stream().mapToInt(Piece::getValue).sum();
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

    private String format(Position pos) { return "" + (char) ('a' + pos.getColumn()) + (pos.getRow() + 1); }

    private record UiMoveState(List<Piece> whiteCaptures, List<Piece> blackCaptures,
                               Position lastFrom, Position lastTo) { }
}
