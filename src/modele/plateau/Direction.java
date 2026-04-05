package modele.plateau;

public enum Direction {
    North(0, -1),
    South(0, 1),
    East(1, 0),   // Ajouter si pas présent
    West(-1, 0);

    int dx;
    int dy;

    private Direction(int _dx, int _dy) {
        dx = _dx;
        dy = _dy;
    }
}