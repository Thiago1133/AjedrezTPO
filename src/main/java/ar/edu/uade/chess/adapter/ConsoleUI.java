package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.port.ChessGame;
import ar.edu.uade.chess.core.port.GameObserver;
import ar.edu.uade.chess.core.port.MoveInput;
import ar.edu.uade.chess.core.port.PromotionOptions;

import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Text adapter. Translates console input into calls on the ChessGame port and
 * renders what the core announces. All user-facing text and notation live here.
 */
public class ConsoleUI implements MoveInput, GameObserver {
    private static final Map<Color, String> COLOR_NAMES = Map.of(Color.WHITE, "blancas", Color.BLACK, "negras");
    private static final Map<GameStatus, String> RESULT_MESSAGES = Map.of(
            GameStatus.CHECKMATE, "¡Jaque mate!",
            GameStatus.STALEMATE, "Tablas por ahogado.",
            GameStatus.DRAW, "Tablas.");

    private final Scanner scanner;
    private final PromotionNotation promotionNotation;
    private final Map<String, Runnable> commands;
    /** Colors this console has been asked to play for; the others are the computer. */
    private final Set<Color> humanColors = EnumSet.noneOf(Color.class);
    private ChessGame game;
    private boolean quitRequested;
    private boolean turnInterrupted;

    public ConsoleUI(Scanner scanner, PromotionOptions promotionOptions) {
        this.scanner = scanner;
        this.promotionNotation = new PromotionNotation(promotionOptions);
        Runnable quit = () -> quitRequested = true;
        Runnable undo = () -> stepUntilHumanTurn(game::undo);
        Runnable redo = () -> stepUntilHumanTurn(game::redo);
        Runnable help = this::showHelp;
        this.commands = Map.of(
                "salir", quit, "quit", quit,
                "deshacer", undo, "undo", undo,
                "rehacer", redo, "redo", redo,
                "ayuda", help, "help", help);
    }

    public void run(ChessGame game) {
        this.game = game;
        quitRequested = false;
        game.addObserver(this);
        try {
            showHelp();
            game.start();
            while (!quitRequested && !game.getStatus().isGameOver()) {
                turnInterrupted = false;
                if (!game.playTurn() && !quitRequested && !turnInterrupted) {
                    showMessage("Movimiento inválido. Probá de nuevo.");
                }
            }
        } finally {
            game.removeObserver(this);
        }
    }

    @Override
    public Move readMove(Color color) {
        humanColors.add(color);
        while (true) {
            System.out.print(COLOR_NAMES.get(game.getCurrentTurn()) + " > ");
            if (!scanner.hasNextLine()) {
                quitRequested = true;
                return null;
            }
            String line = scanner.nextLine().trim().toLowerCase();
            Runnable command = commands.get(line);
            if (command != null) {
                command.run();
                if (quitRequested) {
                    return null;
                }
                if (game.getCurrentTurn() != color) {
                    // Undo/redo handed the turn to the other player: let the game ask them.
                    turnInterrupted = true;
                    return null;
                }
            } else if (!line.isEmpty()) {
                Move move = parseMove(line);
                if (move != null) {
                    return move;
                }
                showMessage("No entendí \"" + line + "\". Usá el formato: e2 e4 (escribí 'ayuda').");
            }
        }
    }

    @Override
    public void onMoveExecuted(Move move) {
        showMessage("Jugada: " + SquareNotation.square(move.getFrom()) + " -> " + SquareNotation.square(move.getTo()));
    }

    @Override
    public void onTurnChanged(Color turn) {
        render(game.getBoard());
        showMessage("Turno de las " + COLOR_NAMES.get(turn) + ".");
    }

    @Override
    public void onCheck(Color color) {
        showMessage("¡Jaque a las " + COLOR_NAMES.get(color) + "!");
    }

    @Override
    public void onGameOver(GameStatus status) {
        render(game.getBoard());
        showMessage(RESULT_MESSAGES.get(status));
        if (status == GameStatus.CHECKMATE) {
            showMessage("Ganan las " + COLOR_NAMES.get(game.getCurrentTurn().opposite()) + ".");
        }
    }

    /** Columns are as wide as the longest column name, so boards wider than 26 columns line up. */
    private void render(Board board) {
        int cellWidth = SquareNotation.file(board.getColumns() - 1).length() + 1;
        int rankWidth = SquareNotation.rank(board.getRows() - 1).length() + 2;
        StringBuilder out = new StringBuilder("\n");
        for (int row = board.getRows() - 1; row >= 0; row--) {
            out.append(pad(SquareNotation.rank(row), rankWidth));
            for (int column = 0; column < board.getColumns(); column++) {
                Piece piece = board.getPiece(new Position(row, column));
                out.append(pad(String.valueOf(piece == null ? '.' : piece.getSymbol()), cellWidth));
            }
            out.append('\n');
        }
        out.append(" ".repeat(rankWidth));
        for (int column = 0; column < board.getColumns(); column++) {
            out.append(pad(SquareNotation.file(column), cellWidth));
        }
        System.out.println(out);
    }

    private static String pad(String text, int width) {
        return " ".repeat(Math.max(0, width - text.length() - 1)) + text + " ";
    }

    /** Parses "e2 e4" or "e7 e8 q" (promotion). Returns null if the text is not a move. */
    private Move parseMove(String line) {
        String[] tokens = line.split("\\s+");
        if (tokens.length < 2 || tokens.length > 3) {
            return null;
        }
        Position from = SquareNotation.parse(tokens[0]);
        Position to = SquareNotation.parse(tokens[1]);
        if (from == null || to == null) {
            return null;
        }
        String promotionId = tokens.length == 3 ? promotionNotation.idFor(tokens[2], game.getCurrentTurn()) : null;
        return new Move(from, to, promotionId);
    }

    /**
     * Undoes (or redoes) one move, and keeps going while it is the computer's turn,
     * so that against the AI one "deshacer" takes back both the AI's move and ours.
     */
    private void stepUntilHumanTurn(Runnable step) {
        do {
            Color before = game.getCurrentTurn();
            step.run();
            if (game.getCurrentTurn() == before) {
                return; // nothing left to undo/redo
            }
        } while (!humanColors.contains(game.getCurrentTurn()));
    }

    private void showHelp() {
        showMessage("""
                Comandos:
                  e2 e4      mover de e2 a e4
                  e7 e8 q    mover y promover (%s)
                  deshacer   deshace la última jugada
                  rehacer    rehace la jugada deshecha
                  salir      termina la partida""".formatted(promotionNotation.describe(Color.WHITE)));
    }

    private void showMessage(String message) {
        System.out.println(message);
    }
}
