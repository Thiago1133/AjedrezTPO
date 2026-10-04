package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Move;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Executed and undone commands, for undo/redo and for rules that look at past moves. */
public class MoveHistory {
    private final Deque<MoveCommand> executed = new ArrayDeque<>();
    private final Deque<MoveCommand> undone = new ArrayDeque<>();

    /** Records a newly executed command. A new move discards the redo stack. */
    public void push(MoveCommand command) {
        executed.push(command);
        undone.clear();
    }

    /** Takes the last executed command for undoing, or null if there is none. */
    public MoveCommand popForUndo() {
        if (executed.isEmpty()) {
            return null;
        }
        MoveCommand command = executed.pop();
        undone.push(command);
        return command;
    }

    /** Takes the last undone command for redoing, or null if there is none. */
    public MoveCommand popForRedo() {
        if (undone.isEmpty()) {
            return null;
        }
        MoveCommand command = undone.pop();
        executed.push(command);
        return command;
    }

    /** The last executed move, or null at the start of the game. */
    public Move getLastMove() {
        MoveCommand last = executed.peek();
        return last == null ? null : last.getMove();
    }

    /** Executed commands in chronological order (oldest first). */
    public List<MoveCommand> getExecuted() {
        List<MoveCommand> result = new ArrayList<>(executed);
        Collections.reverse(result);
        return result;
    }
}
