package ar.edu.uade.chess.core.port.in;

import ar.edu.uade.chess.core.port.out.GameObserver;

/** Driving port, subscriptions: who is told what happens. Whoever subscribes must also unsubscribe. */
public interface GameSubscriptions {
    void addObserver(GameObserver observer);

    void removeObserver(GameObserver observer);
}
