# AjedrezTPO

TPO de Ingeniería de Software (UADE): implementación de ajedrez con núcleo independiente
de la infraestructura (arquitectura hexagonal), SOLID y composición sobre herencia.

## Requisitos

- Java 21
- Maven 3.9+

## Comandos

```bash
mvn test        # corre los tests del núcleo (sin UI ni infraestructura)
mvn package     # compila, corre los tests y genera el jar
```

## Ejecución

Hay dos adaptadores sobre el mismo núcleo:

```bash
java -jar target/ajedrez-tpo-1.0-SNAPSHOT.jar             # ventana (interfaz gráfica)
java -jar target/ajedrez-tpo-1.0-SNAPSHOT.jar --consola   # consola
```

En los dos modos se puede jugar de a dos o contra la computadora (la IA juega con negras).
En la ventana, antes de cada partida se elige el modo, el diseño de las piezas (Clásicas, Rellenas, Letras)
y el color del tablero (Verde, Madera, Azul, Gris); la ventana recuerda la última elección mientras está abierta.
En la consola el modo se pregunta al arrancar.
En la consola: `e2 e4` para mover, `e7 e8 n` para promover a caballo, `deshacer`, `rehacer`, `ayuda`, `salir`.

## Estructura

```
src/main/java/ar/edu/uade/chess/
  core/board     Tablero, posiciones, movimientos, colores
  core/piece     Piezas por composición: reglas de movimiento, rasgos, definiciones, fábrica, setup
  core/command   Movimientos como comandos (ejecutar/deshacer), historial y fábrica de comandos
  core/rules     Detección de jaque, validación de movimientos y reglas especiales
  core/status    Estado de la partida y condiciones de fin
  core/game      Partida, turnos, jugadores y estrategias
  core/port      Puertos: ChessGame (entrada), GameObserver y MoveInput (salida)
  adapter        Consola y Main (único lugar donde se instancia y conecta todo)
  adapter/gui    Interfaz gráfica (Swing): ventana, tablero, casillas, estilos de piezas y colores de tablero
```

## Avance

- [x] Módulo 1: tablero y piezas (movimiento propio de las 6 piezas)
- [x] Módulo 2: validación, jaque, comandos (deshacer/rehacer)
- [x] Módulo 3: partida, turnos, puertos y consola
- [x] Módulo 4: fin de partida (mate, ahogado, tablas)
- [x] Módulo 5: reglas especiales (enroque, al paso, promoción)
- [x] Módulo 6: oponente con IA
