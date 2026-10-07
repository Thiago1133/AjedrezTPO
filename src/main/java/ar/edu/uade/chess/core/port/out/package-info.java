/**
 * Driven (output) ports: what the core needs from the outside world without knowing who
 * provides it, such as being told a human's move (MoveInput) or announcing what happened
 * (GameObserver). The core calls these interfaces; adapters implement them.
 */
package ar.edu.uade.chess.core.port.out;
