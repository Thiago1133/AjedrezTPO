package ar.edu.uade.chess.adapter.gui;

import java.awt.Color;

/** Colors of the board squares and their highlights. A new palette is a new value in AppearanceCatalog. */
record BoardTheme(String displayName, Color light, Color dark, Color selected, Color lastMove,
                  Color lightCoordinate, Color darkCoordinate, Color marker) {

    Color squareColor(boolean lightSquare) {
        return lightSquare ? light : dark;
    }

    /** Coordinates use the opposite square color so they stay readable. */
    Color coordinateColor(boolean lightSquare) {
        return lightSquare ? lightCoordinate : darkCoordinate;
    }
}
