package modele.plateau;

import modele.item.ItemShape;

/**
 * Tapis : machine qui transporte les items d'une case à l'autre.
 *
 * Types de tapis :
 * - Tapis droit : une seule direction (d)
 * - Tapis en coin : direction entrante (incoming) et direction sortante (d)
 *
 * Le tapis ne transforme pas les items, il les transmet simplement.
 */
public class Tapis extends Machine {

    private Direction incoming = null; // Direction d'où vient l'item (null = tapis droit)

    // Tapis droit par défaut (vers le Nord)
    public Tapis() {
        this.d = Direction.North;
    }

    // Tapis droit avec direction personnalisée
    public Tapis(Direction direction) {
        this.d = direction;
    }

    // Tapis en coin (avec direction entrante et sortante)
    public Tapis(Direction incoming, Direction outgoing) {
        this.incoming = incoming;
        this.d = outgoing;
    }

    // Retourne la direction d'entrée (pour les tapis en coin)
    public Direction getIncoming() {
        return incoming;
    }

    // Vérifie si le tapis est un coin (entrée différente de sortie)
    public boolean isCorner() {
        return incoming != null && incoming != d;
    }
}