package ar.edu.uade.chess;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the hexagonal architecture and the course rules by reading the source code,
 * so a violation fails the build instead of being found by hand (or by the professor).
 * It only reads text files: no window, no console, no game is started.
 */
class ArchitectureTest {
    private static final Path MAIN = Path.of("src/main/java/ar/edu/uade/chess");
    private static final Path TEST = Path.of("src/test/java");
    private static final Path CORE = MAIN.resolve("core");
    private static final Path ADAPTER = MAIN.resolve("adapter");
    private static final Path PORTS = CORE.resolve("port");
    private static final Path INPUT_PORTS = PORTS.resolve("in");
    private static final Path OUTPUT_PORTS = PORTS.resolve("out");
    /** The composition root: the only adapter class allowed to wire concrete core classes. */
    private static final Path COMPOSITION_ROOT = ADAPTER.resolve("Main.java");

    private static final Pattern IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?([\\w.]+?)(?:\\.\\*)?;", Pattern.MULTILINE);

    /** The core may only use itself and the standard collections. */
    private static final List<String> CORE_ALLOWED_IMPORTS = List.of("ar.edu.uade.chess.core.", "java.util.");

    /** Infrastructure the core must not mention at all, not even with a fully qualified name. */
    private static final Pattern CORE_FORBIDDEN_REFERENCE = Pattern.compile(
            "\\b(javax\\.|java\\.awt\\.|java\\.io\\.|java\\.nio\\.|java\\.net\\.|java\\.sql\\.|ar\\.edu\\.uade\\.chess\\.adapter\\.)");

    /**
     * What adapters may use from the core: the ports and the value types those ports
     * exchange. Rules, conditions, commands and the concrete Game stay hidden behind the ports.
     */
    private static final Set<String> ADAPTER_ALLOWED_CORE_TYPES = Set.of(
            "ar.edu.uade.chess.core.board.Board",
            "ar.edu.uade.chess.core.board.Color",
            "ar.edu.uade.chess.core.board.Move",
            "ar.edu.uade.chess.core.board.Position",
            "ar.edu.uade.chess.core.piece.Piece",
            "ar.edu.uade.chess.core.game.GameStatus");
    private static final String PORT_PACKAGE = "ar.edu.uade.chess.core.port.";

    /** Text comparisons (forbidden in conditionals by the course rules) and Singleton-style global access. */
    private static final Pattern STRING_COMPARISON = Pattern.compile(
            "\\.equals(IgnoreCase)?\\(\\s*\"|\"\\s*\\.equals\\(|case\\s+\"|==\\s*\"|\"\\s*==");
    private static final Pattern SINGLETON = Pattern.compile("\\bgetInstance\\s*\\(|static\\s+\\w+\\s+instance\\b");

    private static final Pattern PUBLIC_INTERFACE = Pattern.compile("\\bpublic\\s+interface\\s+(\\w+)");
    private static final Pattern IMPLEMENTS = Pattern.compile("\\bimplements\\s+([\\w\\s,<>]+?)\\s*\\{");

    private static List<Path> javaFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(file -> file.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static List<String> importsOf(Path file) throws IOException {
        List<String> imports = new ArrayList<>();
        Matcher matcher = IMPORT.matcher(Files.readString(file));
        while (matcher.find()) {
            imports.add(matcher.group(1));
        }
        return imports;
    }

    /** Source without comments, so a word in a comment never counts as code. */
    private static String codeOf(Path file) throws IOException {
        return Files.readString(file).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");
    }

    private static String name(Path root, Path file) {
        return root.relativize(file).toString().replace('\\', '/');
    }

    @Test
    void sourcesAreFound() throws IOException {
        assertFalse(javaFiles(CORE).isEmpty(), "run the tests from the project folder");
        assertFalse(javaFiles(ADAPTER).isEmpty());
    }

    /** Names of the port interfaces declared in a port package (package-info excluded). */
    private static Set<String> portNames(Path ports) throws IOException {
        Set<String> names = new TreeSet<>();
        for (Path file : javaFiles(ports)) {
            Matcher matcher = PUBLIC_INTERFACE.matcher(codeOf(file));
            if (matcher.find()) names.add(matcher.group(1));
        }
        return names;
    }

    /** Classes in the given tree that declare they implement any of the given interfaces. */
    private static List<String> implementorsOf(Path root, Set<String> interfaces) throws IOException {
        List<String> implementors = new ArrayList<>();
        for (Path file : javaFiles(root)) {
            Matcher matcher = IMPLEMENTS.matcher(codeOf(file));
            while (matcher.find()) {
                for (String implemented : matcher.group(1).split("[,\\s]+")) {
                    if (interfaces.contains(implemented)) implementors.add(name(MAIN, file) + " implements " + implemented);
                }
            }
        }
        return implementors;
    }

    @Test
    void ports_areInterfaces_groupedByDirection() throws IOException {
        List<String> misplaced = new ArrayList<>();
        for (Path file : javaFiles(PORTS)) {
            boolean packageInfo = file.getFileName().toString().equals("package-info.java");
            boolean grouped = file.startsWith(INPUT_PORTS) || file.startsWith(OUTPUT_PORTS);
            if (!packageInfo && (!grouped || !PUBLIC_INTERFACE.matcher(codeOf(file)).find())) {
                misplaced.add(name(PORTS, file));
            }
        }
        assertEquals(List.of(), misplaced, "every port is an interface in port/in (driving) or port/out (driven)");
        assertFalse(portNames(INPUT_PORTS).isEmpty());
        assertFalse(portNames(OUTPUT_PORTS).isEmpty());
    }

    /** Input ports are implemented by the core; output ports by the adapters, never the other way. */
    @Test
    void ports_pointInTheRightDirection() throws IOException {
        Set<String> inputPorts = portNames(INPUT_PORTS);
        Set<String> outputPorts = portNames(OUTPUT_PORTS);

        assertFalse(implementorsOf(CORE, inputPorts).isEmpty(), "the core implements its input ports");
        assertEquals(List.of(), implementorsOf(CORE, outputPorts), "the core uses output ports, adapters implement them");
        assertEquals(List.of(), implementorsOf(ADAPTER, inputPorts), "adapters call input ports, they do not implement them");
        assertFalse(implementorsOf(ADAPTER, outputPorts).isEmpty(), "adapters implement the output ports");
    }

    /**
     * Interface segregation: inside the core only Game (which implements them) knows the
     * actions and subscriptions; player strategies such as the computer only see GameQueries.
     */
    @Test
    void insideTheCore_onlyGameKnowsTheActions() throws IOException {
        Pattern wholePort = Pattern.compile("\\b(ChessGame|GameActions|GameSubscriptions)\\b");
        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles(CORE)) {
            if (file.startsWith(INPUT_PORTS) || file.equals(CORE.resolve("game/Game.java"))) continue;
            Matcher matcher = wholePort.matcher(codeOf(file));
            if (matcher.find()) violations.add(name(CORE, file) + " -> " + matcher.group(1));
        }
        assertEquals(List.of(), violations, "strategies receive GameQueries: they may look, never move");
    }

    @Test
    void core_importsOnlyItselfAndJavaUtil() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles(CORE)) {
            for (String imported : importsOf(file)) {
                if (CORE_ALLOWED_IMPORTS.stream().noneMatch(imported::startsWith)) {
                    violations.add(name(CORE, file) + " -> " + imported);
                }
            }
        }
        assertEquals(List.of(), violations, "the core must not depend on frameworks, UI or infrastructure");
    }

    @Test
    void core_neverMentionsInfrastructureOrAdapters() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles(CORE)) {
            Matcher matcher = CORE_FORBIDDEN_REFERENCE.matcher(codeOf(file));
            while (matcher.find()) {
                violations.add(name(CORE, file) + " -> " + matcher.group(1));
            }
        }
        assertEquals(List.of(), violations, "dependencies point inwards: the core never knows the adapters");
    }

    @Test
    void adapters_useTheCoreOnlyThroughPortsAndValueTypes() throws IOException {
        Set<String> violations = new TreeSet<>();
        for (Path file : javaFiles(ADAPTER)) {
            if (file.equals(COMPOSITION_ROOT)) continue;
            for (String imported : importsOf(file)) {
                boolean core = imported.startsWith("ar.edu.uade.chess.core.");
                boolean allowed = imported.startsWith(PORT_PACKAGE) || ADAPTER_ALLOWED_CORE_TYPES.contains(imported);
                if (core && !allowed) {
                    violations.add(name(ADAPTER, file) + " -> " + imported);
                }
            }
        }
        assertEquals(Set.of(), violations, "adapters only talk to the core through its ports");
    }

    @Test
    void noStringComparisons_inProductionCode() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles(MAIN)) {
            String[] lines = codeOf(file).split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (STRING_COMPARISON.matcher(lines[i]).find()) {
                    violations.add(name(MAIN, file) + ": " + lines[i].trim());
                }
            }
        }
        assertEquals(List.of(), violations, "course rule: no text comparisons in conditionals; use maps, enums or polymorphism");
    }

    @Test
    void noSingletons_dependenciesAreInjected() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles(MAIN)) {
            if (SINGLETON.matcher(codeOf(file)).find()) {
                violations.add(name(MAIN, file));
            }
        }
        assertEquals(List.of(), violations, "dependencies enter through constructors, not global instances");
    }

    @Test
    void tests_neverOpenWindows() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles(TEST)) {
            for (String imported : importsOf(file)) {
                if (imported.startsWith("javax.swing") || imported.startsWith("java.awt")) {
                    violations.add(name(TEST, file) + " -> " + imported);
                }
            }
        }
        assertEquals(List.of(), violations, "course rule: no test needs to draw a board");
    }
}
