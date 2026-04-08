package modele.plateau;

/**
 * Énumération des 4 directions cardinales.
 *
 * Chaque direction a un décalage (dx, dy) pour se déplacer sur la grille.
 *
 * Exemple : North → (0, -1) : x ne change pas, y diminue de 1
 */
public enum Direction {
    North(0, -1),  // Haut : y - 1
    South(0, 1),   // Bas  : y + 1
    East(1, 0),    // Droite : x + 1
    West(-1, 0);   // Gauche : x - 1

    int dx;  // Décalage en X
    int dy;  // Décalage en Y

    // Constructeur privé (enum)
    private Direction(int _dx, int _dy) {
        dx = _dx;
        dy = _dy;
    }

    // Getters
    public int getDx() { return dx; }
    public int getDy() { return dy; }

    /**
     * Rotation de 90° dans le sens horaire (clockwise).
     * North → East → South → West → North
     */
    public Direction rotate90CW() {
        switch (this) {
            case North: return East;
            case East:  return South;
            case South: return West;
            case West:  return North;
            default:    return this;
        }
    }

    /**
     * Rotation de 90° dans le sens anti-horaire (counter-clockwise).
     * North → West → South → East → North
     */
    public Direction rotate90CCW() {
        switch (this) {
            case North: return West;
            case West:  return South;
            case South: return East;
            case East:  return North;
            default:    return this;
        }
    }
}