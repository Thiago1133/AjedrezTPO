package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.port.in.ChessGame;

import javax.swing.Timer;
import java.util.Random;

/**
 * The computer's reply, played after a short "thinking" pause. The pause is presentation
 * only, so it lives in the window and not in the core. Everything runs on the Swing thread:
 * the computer answers in milliseconds and the game is never used from two threads.
 */
final class ComputerTurn {
    private static final int MIN_DELAY_MS = 2_000;
    private static final int MAX_DELAY_MS = 3_000;

    private final Random random;
    private final Runnable afterMove;
    private Timer pending;

    /** @param afterMove what the window does once the computer has moved (refresh itself). */
    ComputerTurn(Random random, Runnable afterMove) {
        this.random = random;
        this.afterMove = afterMove;
    }

    /** Plays the game's next turn after the pause, unless it is cancelled first. */
    void schedule(ChessGame game) {
        if (isPending() || game.getStatus().isGameOver()) return;
        int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS + 1);
        Timer timer = new Timer(delay, event -> {
            if (pending != event.getSource()) return; // cancelled while the event was queued
            pending = null;
            if (!game.getStatus().isGameOver()) game.playTurn();
            afterMove.run();
        });
        timer.setRepeats(false);
        pending = timer;
        timer.start();
    }

    /** Drops a reply that has not been played yet (undo, redo, a new game...). */
    void cancel() {
        if (pending != null) {
            pending.stop();
            pending = null;
        }
    }

    /** While the computer is "thinking" the human cannot move, undo or redo. */
    boolean isPending() {
        return pending != null;
    }
}
