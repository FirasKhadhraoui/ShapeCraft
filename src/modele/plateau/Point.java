package modele.plateau;

/**
 * Point : représente une position (coordonnées) sur la grille du plateau.
 *
 * Utilisé pour :
 * - Stocker la position d'une Case dans la HashMap (map)
 * - Naviguer entre les cases via les directions (ajout de dx/dy)
 *
 * Les attributs sont publics pour un accès direct et rapide.
 */
public class Point {

    public int x; // Coordonnée horizontale (0 à SIZE_X-1)
    public int y; // Coordonnée verticale (0 à SIZE_Y-1)

    /**
     * Constructeur.
     * @param x coordonnée X
     * @param y coordonnée Y
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}