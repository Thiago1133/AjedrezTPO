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
java -jar target/ajedrez-tpo-1.0-SNAPSHOT.jar   # juega en consola
```

En la consola: `e2 e4` para mover, `deshacer`, `rehacer`, `ayuda`, `salir`.

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
```

## Avance

- [x] Módulo 1: tablero y piezas (movimiento propio de las 6 piezas)
- [x] Módulo 2: validación, jaque, comandos (deshacer/rehacer)
- [x] Módulo 3: partida, turnos, puertos y consola
- [ ] Módulo 4: fin de partida (mate, ahogado, tablas)
- [ ] Módulo 5: reglas especiales (enroque, al paso, promoción)
- [ ] Módulo 6: oponente con IA
