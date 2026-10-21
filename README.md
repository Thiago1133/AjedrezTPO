# AjedrezTPO

TPO de Ingeniería de Software (UADE): implementación de ajedrez con un núcleo independiente
de la infraestructura (arquitectura hexagonal), SOLID, inyección de dependencias y composición
sobre herencia.

## Requisitos

- Java 21
- Maven 3.9+
- Internet la primera vez que se compila: Maven baja el motor de ajedrez Stockfish (ver más abajo).
  No hay que instalar nada a mano.

## Comandos

```bash
mvn test        # corre todos los tests (en memoria: sin ventanas ni infraestructura)
mvn package     # compila, corre los tests, baja Stockfish y genera el jar
```

Para correr una sola clase de tests: `mvn test -Dtest=GameTest`. En PowerShell, si hay comas,
va entre comillas: `mvn test "-Dtest=GameTest,ClockTest"`.

## Ejecución

Hay dos adaptadores sobre el mismo núcleo:

```bash
java -jar target/ajedrez-tpo-1.0-SNAPSHOT.jar             # ventana (interfaz gráfica)
java -jar target/ajedrez-tpo-1.0-SNAPSHOT.jar --consola   # consola
```

En los dos se puede jugar de a dos o contra la computadora (la computadora juega con negras),
en tres dificultades: Fácil, Medio y Difícil.

**Ventana.** Antes de cada partida se elige el modo, el tiempo (30, 10 o 5 minutos, o ilimitado;
solo entre dos jugadores), la dificultad (solo contra la computadora), el diseño de las piezas (Clásicas, Rellenas, Letras) y el color del
tablero (Verde, Madera, Azul, Gris). La ventana recuerda la última elección mientras está abierta.
Muestra las jugadas posibles al elegir una pieza, la última jugada, el rey en jaque en rojo, las
piezas capturadas con la ventaja de material y el reloj. Tiene deshacer, rehacer y girar el
tablero. Contra la computadora, deshacer y rehacer devuelven el turno al jugador humano.

**Consola.** El modo, y contra la computadora la dificultad, se preguntan al arrancar.
Comandos: `e2 e4` para mover, `e7 e8 q` para promover (la pieza se escribe con su letra, su
nombre o su id: `q`, `reina`, `queen`), `deshacer`, `rehacer`, `ayuda`, `salir`.

## La computadora

Juega con el motor Stockfish 19, que se usa a través de un puerto del núcleo (`ChessEngine`).
Cada dificultad combina la fuerza del motor con un criterio propio:

| Dificultad | Cómo juega | Elo aproximado |
|---|---|---|
| Fácil | Captura siempre que puede; si no, juega al azar | ~430 |
| Medio | Da jaque siempre que puede; si no, juega lo que propone Stockfish en su fuerza mínima, con un 40% de jugadas al azar | ~800 |
| Difícil | Stockfish con su opción oficial de Elo fijada en 1500 | 1500 |

Stockfish no baja de 1320 de Elo, por eso Fácil y Medio se debilitan con jugadas al azar. Sus Elo
se estimaron jugando partidas contra Stockfish fijado en 1320, así que son aproximados.

**Descarga.** Al compilar, Maven baja de su GitHub oficial el Stockfish que corresponde al sistema
operativo (Windows, Linux o macOS), verifica su SHA-256 y lo deja en `target/engine/`. El archivo
queda en el caché de Maven, así que solo se baja la primera vez, y no se guarda en Git.

**Sin Stockfish.** Si no se pudo bajar (por ejemplo, sin internet), el juego funciona igual: la
computadora juega con su propia IA, y la ventana y la consola lo avisan.

## Alcance

- Obligatorio: tablero 8x8, las 6 piezas con su movimiento, turnos, captura, validación de
  movimientos y detección de jaque.
- Opcional: jaque mate, enroque, captura al paso, promoción, tablas (ahogado, triple repetición,
  regla de los 50 movimientos y material insuficiente), oponente con IA en tres dificultades
  (con Stockfish), deshacer y rehacer,
  reloj con derrota por tiempo e interfaz gráfica completa.

## Arquitectura

- **Núcleo** (`core`): las reglas del juego. Solo usa Java estándar; no conoce Swing, la consola
  ni ninguna otra infraestructura.
- **Puertos** (`core/port`): interfaces por las que el núcleo se comunica con el exterior.
  Los de entrada los llaman los adaptadores; los de salida los implementan los adaptadores.
- **Adaptadores** (`adapter`): la ventana y la consola, que traducen clics y texto en llamadas a
  los puertos y muestran lo que el núcleo informa, y Stockfish, que el núcleo usa a través del puerto
  `ChessEngine`. No contienen reglas del juego.
- **`Main`** es la raíz de composición: el único lugar donde se crean las clases concretas y se
  conectan entre sí (inyección de dependencias por constructor).

Las dependencias siempre apuntan hacia el núcleo. `ArchitectureTest` lo verifica en cada build.

## Estructura

```
src/main/java/ar/edu/uade/chess/
  core/board     Tablero, posiciones, movimientos, colores
  core/piece     Piezas por composición: reglas de movimiento, rasgos, definiciones, fábrica, posición inicial
  core/command   Movimientos como comandos (ejecutar/deshacer), historial y fábrica de comandos
  core/rules     Detección de jaque, validación de movimientos y reglas especiales
  core/status    Condiciones de fin de partida
  core/game      Partida, estados, turnos, jugadores, estrategias (humano, IA, motor), niveles de
                 dificultad y reloj
  core/port/in   Puertos de entrada: GameActions, GameQueries y GameSubscriptions
                 (ChessGame las agrupa) y PromotionOptions
  core/port/out  Puertos de salida: GameObserver, MoveInput y ChessEngine
  adapter        Consola, opciones de partida, notación de casillas y promociones, nombres de
                 piezas y Main
  adapter/engine Stockfish: el proceso, la posición en FEN y la lectura de sus jugadas (UCI)
  adapter/gui    Ventana (Swing): tablero, casillas, reloj, turno de la computadora, diálogo de
                 nueva partida, estilos de piezas y temas de tablero
```

## Tests

Todos corren en memoria, sin abrir ventanas ni arrancar Stockfish.

- **Núcleo:** movimiento de cada pieza, validación (clavadas, jaque), comandos y deshacer, reglas
  especiales, fin de partida, IA, consultas de la partida, capturas y material, reloj, y cada
  dificultad con un motor simulado.
- **Adaptador del motor:** la posición en FEN y la lectura de las jugadas de Stockfish, a partir
  de texto fijo.
- **Arquitectura:** `ArchitectureTest` falla si el núcleo importa algo de afuera, si un adaptador
  usa clases internas del núcleo en lugar de los puertos, si hay comparaciones de texto en el
  código, si hay Singletons o si un test usa Swing.
