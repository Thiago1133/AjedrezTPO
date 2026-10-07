/**
 * Driving (input) ports: what the outside world may ask the core to do or tell it.
 * Adapters such as the window or the console call these interfaces; the core implements
 * them (Game, PromotionRule). Dependencies point inwards: adapters know these ports, the
 * core never knows the adapters.
 */
package ar.edu.uade.chess.core.port.in;
