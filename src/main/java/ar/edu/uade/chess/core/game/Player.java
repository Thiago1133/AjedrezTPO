package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;

public class Player {
    private final Color color;
    private final PlayerStrategy strategy;

    public Player(Color color, PlayerStrategy strategy) {
        this.color = color;
        this.strategy = strategy;
    }

    public Color getColor() {
        return color;
    }

    public boolean isAutomatic() {
        return strategy.isAutomatic();
    }

    public Move chooseMove(GameQueries game) {
        return strategy.chooseMove(game, color);
    }
}
