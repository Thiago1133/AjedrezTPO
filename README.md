# AjedrezTPO

TPO de Ingeniería de Software (UADE): implementación de ajedrez con núcleo independiente
de la infraestructura (arquitectura hexagonal), SOLID y composición sobre herencia.

## Requisitos

- Java 21
- Maven 3.9+

## Comandos

```bash
mvn test        # corre los tests del núcleo (sin UI ni infraestructura)
mvn package     # compila y genera el jar
```

## Estructura

```
src/main/java/ar/edu/uade/chess/
  core/board     Tablero, posiciones, movimientos, colores
  core/piece     Piezas por composición: reglas de movimiento, rasgos, definiciones, fábrica, setup
  core/command   Movimientos como comandos (ejecutar/deshacer), historial y fábrica de comandos
  core/rules     Detección de jaque, validación de movimientos y reglas especiales
```

## Avance

- [x] Módulo 1: tablero y piezas (movimiento propio de las 6 piezas)
- [x] Módulo 2: validación, jaque, comandos (deshacer/rehacer)
- [ ] Módulo 3: partida, turnos, puertos y consola
- [ ] Módulo 4: fin de partida (mate, ahogado, tablas)
- [ ] Módulo 5: reglas especiales (enroque, al paso, promoción)
- [ ] Módulo 6: oponente con IA
