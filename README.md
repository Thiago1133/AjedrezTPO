# AjedrezTPO

TPO de Ingeniería de Software (UADE): implementación de ajedrez con un núcleo independiente
de la infraestructura (arquitectura hexagonal), SOLID, inyección de dependencias y composición
sobre herencia.

## Requisitos

- Java 21
- Maven 3.9+

## Comandos

```bash
mvn test        # corre todos los tests (en memoria: sin ventanas ni infraestructura)
mvn package     # compila, corre los tests y genera el jar
```

Para correr una sola clase de tests: `mvn test -Dtest=GameTest`. En PowerShell, si hay comas,
va entre comillas: `mvn test "-Dtest=CaciqueExtensionTest,LargeBoardExtensionTest"`.

## Ejecución

Hay dos adaptadores sobre el mismo núcleo:

```bash
java -jar target/ajedrez-tpo-1.0-SNAPSHOT.jar             # ventana (interfaz gráfica)
java -jar target/ajedrez-tpo-1.0-SNAPSHOT.jar --consola   # consola
```

En los dos se puede jugar de a dos o contra la computadora (la computadora juega con negras).

**Ventana.** Antes de cada partida se elige el modo, el tiempo (30, 10 o 5 minutos, o ilimitado;
solo entre dos jugadores), el diseño de las piezas (Clásicas, Rellenas, Letras) y el color del
tablero (Verde, Madera, Azul, Gris). La ventana recuerda la última elección mientras está abierta.
Muestra las jugadas posibles al elegir una pieza, la última jugada, el rey en jaque en rojo, las
piezas capturadas con la ventaja de material y el reloj. Tiene deshacer, rehacer y girar el
tablero. Contra la computadora, deshacer y rehacer devuelven el turno al jugador humano.
Los tableros más grandes que 8x8 se muestran con barras de desplazamiento y coordenadas al costado.

**Consola.** El modo se pregunta al arrancar. Comandos: `e2 e4` para mover, `e7 e8 q` para
promover (la pieza se escribe con su letra, su nombre o su id: `q`, `reina`, `queen`), `deshacer`,
`rehacer`, `ayuda`, `salir`. En tableros de más de 26 columnas se siguen con `aa`, `ab`... (`bl64`).

## Alcance

- Obligatorio: tablero 8x8, las 6 piezas con su movimiento, turnos, captura, validación de
  movimientos y detección de jaque.
- Opcional: jaque mate, enroque, captura al paso, promoción, tablas (ahogado, triple repetición,
  regla de los 50 movimientos y material insuficiente), oponente con IA, deshacer y rehacer,
  reloj con derrota por tiempo e interfaz gráfica completa.

## Arquitectura

- **Núcleo** (`core`): las reglas del juego. Solo usa Java estándar; no conoce Swing, la consola
  ni ninguna otra infraestructura.
- **Puertos** (`core/port`): interfaces por las que el núcleo se comunica con el exterior.
  Los de entrada los llaman los adaptadores; los de salida los implementan los adaptadores.
- **Adaptadores** (`adapter`): la ventana y la consola. Traducen clics y texto en llamadas a los
  puertos y muestran lo que el núcleo informa; no contienen reglas del juego.
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
  core/game      Partida, estados, turnos, jugadores, estrategias (humano, IA) y reloj
  core/port/in   Puertos de entrada: GameActions, GameQueries y GameSubscriptions
                 (ChessGame las agrupa) y PromotionOptions
  core/port/out  Puertos de salida: GameObserver y MoveInput
  adapter        Consola, notación de casillas y promociones, nombres de piezas y Main
  adapter/gui    Ventana (Swing): tablero, casillas, reloj, turno de la computadora, diálogo de
                 nueva partida, estilos de piezas y temas de tablero
```

## Tests

Todos corren en memoria, sin abrir ventanas.

- **Núcleo:** movimiento de cada pieza, validación (clavadas, jaque), comandos y deshacer, reglas
  especiales, fin de partida, IA, consultas de la partida, capturas y material, reloj.
- **Extensibilidad:** `CaciqueExtensionTest` agrega una pieza nueva ("El Cacique") y
  `LargeBoardExtensionTest` juega en un tablero de 64x64, sin modificar clases existentes.
- **Arquitectura:** `ArchitectureTest` falla si el núcleo importa algo de afuera, si un adaptador
  usa clases internas del núcleo en lugar de los puertos, si hay comparaciones de texto en el
  código, si hay Singletons o si un test usa Swing.

## Extender el juego

- **Pieza nueva:** una clase que implemente `PieceDefinition` (combinando reglas de movimiento
  existentes) y registrarla en la fábrica de piezas de `Main`. Aparece sola en el menú de
  promoción y se dibuja con su letra.
- **Otro tamaño de tablero:** cambiar el tamaño en `Main` y, si no son 8 columnas, una clase
  nueva que implemente `BoardSetup` con la posición inicial.
