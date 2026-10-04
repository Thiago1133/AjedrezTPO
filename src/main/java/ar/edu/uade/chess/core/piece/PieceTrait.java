package ar.edu.uade.chess.core.piece;

/**
 * Rule-relevant capabilities a piece may have. Rules ask for a trait instead of
 * asking "is this a king?", so a new piece can opt into existing rules.
 */
public enum PieceTrait {
    /** Its capture ends the game: check and checkmate apply to it. */
    ROYAL,
    /** Can take part in castling. */
    CASTLES,
    /** Promotes on reaching the last row. */
    PROMOTES,
    /** Can capture and be captured en passant. */
    EN_PASSANT
}
