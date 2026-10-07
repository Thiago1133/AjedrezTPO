package ar.edu.uade.chess.core.port.in;

/**
 * The whole driving port for adapters that run a game (window, console): actions, queries
 * and subscriptions together. Each part is a small interface of its own (Interface
 * Segregation), so a client that needs less, like the computer player, depends on less.
 * Adapters depend on these interfaces, never on the concrete Game.
 */
public interface ChessGame extends GameActions, GameQueries, GameSubscriptions {
}
