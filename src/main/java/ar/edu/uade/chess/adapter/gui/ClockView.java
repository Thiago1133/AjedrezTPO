package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.port.in.ChessGame;

import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * The clock in the sidebar. It is the outside half of the core's clock: once a second it
 * tells the game that time passed (advanceClock) and shows what the game reports. Who runs
 * out of time, and what that means, is decided by the core.
 */
final class ClockView {
    private static final int SECOND_MS = 1_000;

    private final JLabel whiteTitle = new JLabel("TIEMPO BLANCAS");
    private final JLabel whiteTime = new JLabel();
    private final JLabel blackTitle = new JLabel("TIEMPO NEGRAS");
    private final JLabel blackTime = new JLabel();
    private final Timer ticker = new Timer(SECOND_MS, event -> tick());
    private ChessGame game;

    JLabel whiteTitle() { return whiteTitle; }

    JLabel whiteTime() { return whiteTime; }

    JLabel blackTitle() { return blackTitle; }

    JLabel blackTime() { return blackTime; }

    /** Shows the clock only for games with a time limit and starts counting for them. */
    void startFor(ChessGame game) {
        this.game = game;
        boolean limited = !game.getTimeControl().isUnlimited();
        setVisible(limited);
        show(game);
        if (limited && !game.getStatus().isGameOver()) ticker.start();
    }

    /** Resumes a game whose clock was paused (for example while a dialog was open). */
    void resume() {
        if (game != null && !game.getTimeControl().isUnlimited() && !game.getStatus().isGameOver()) {
            ticker.start();
        }
    }

    void stop() {
        ticker.stop();
    }

    void show(ChessGame game) {
        whiteTime.setText(remaining(game, Color.WHITE));
        blackTime.setText(remaining(game, Color.BLACK));
    }

    /** Running out of time ends the game in the core, which notifies the window. */
    private void tick() {
        game.advanceClock(1);
        show(game);
        if (game.getStatus().isGameOver()) ticker.stop();
    }

    private void setVisible(boolean visible) {
        whiteTitle.setVisible(visible);
        whiteTime.setVisible(visible);
        blackTitle.setVisible(visible);
        blackTime.setVisible(visible);
    }

    private static String remaining(ChessGame game, Color color) {
        int seconds = Math.max(0, game.getRemainingTime(color));
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
