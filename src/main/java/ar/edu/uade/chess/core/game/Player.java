package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.ChessGame;

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

    public Move chooseMove(ChessGame game) {
        return strategy.chooseMove(game, color);
    }
}
