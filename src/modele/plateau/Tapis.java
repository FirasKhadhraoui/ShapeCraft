package modele.plateau;

import modele.item.ItemShape;

public class Tapis extends Machine {

    private Direction incoming = null; // null = straight belt

    public Tapis() {
        this.d = Direction.North;
    }

    public Tapis(Direction direction) {
        this.d = direction;
    }

    public Tapis(Direction incoming, Direction outgoing) {
        this.incoming = incoming;
        this.d = outgoing;
    }

    public Direction getIncoming() {
        return incoming;
    }

    public boolean isCorner() {
        return incoming != null && incoming != d;
    }
}
