package ar.edu.uade.chess.adapter.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * A running UCI engine: commands go to its input, and its output lines are collected by a
 * background reader so that waiting for an answer can have a time limit. The reader only
 * touches the engine's output, never the game.
 */
final class UciProcess implements AutoCloseable {
    private final Process process;
    private final BufferedWriter input;
    private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();

    private UciProcess(Process process) {
        this.process = process;
        this.input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.US_ASCII));
        Thread reader = new Thread(this::readOutput, "uci-engine-output");
        reader.setDaemon(true);
        reader.start();
    }

    static UciProcess start(Path executable) throws IOException {
        Process process = new ProcessBuilder(executable.toString()).redirectErrorStream(true).start();
        return new UciProcess(process);
    }

    void send(String command) throws IOException {
        input.write(command);
        input.newLine();
        input.flush();
    }

    /** Output lines up to and including the first one matching {@code last}. */
    List<String> readUntil(Pattern last, Duration timeout) throws TimeoutException, InterruptedException {
        List<String> read = new ArrayList<>();
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            long left = deadline - System.nanoTime();
            String line = left > 0 ? lines.poll(left, TimeUnit.NANOSECONDS) : null;
            if (line == null) throw new TimeoutException("engine did not answer in " + timeout);
            read.add(line);
            if (last.matcher(line).find()) return read;
        }
    }

    boolean isAlive() {
        return process.isAlive();
    }

    @Override
    public void close() {
        try {
            send("quit");
        } catch (IOException alreadyClosed) {
            // the process is gone already
        }
        process.destroy();
    }

    private void readOutput() {
        try (BufferedReader output = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.US_ASCII))) {
            String line;
            while ((line = output.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException closed) {
            // the engine stopped; waiting callers time out
        }
    }
}
