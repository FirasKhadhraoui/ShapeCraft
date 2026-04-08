package modele.plateau;

import modele.item.Item;

/**
 * BalancerSecondaire : case secondaire du Balancer (machine sur 2 cases).
 *
 * Rôle : Cette machine est la "case droite" du Balancer.
 * Elle reçoit les items qui arrivent par la voie de droite
 * et les stocke dans le Balancer principal (owner).
 *
 * La logique d'échange est gérée par le Balancer principal.
 */
public class BalancerSecondaire extends Machine {

    public Balancer owner; // Référence vers le Balancer principal

    // Constructeur : lie cette case secondaire à son Balancer principal
    public BalancerSecondaire(Balancer owner) {
        this.owner = owner;
    }

    // Accepte un item uniquement par la direction d du Balancer principal
    // L'item est stocké dans inputRight du Balancer principal
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        return senderDir == owner.d && owner.inputRight == null;
    }

    // Reçoit un item et le transmet au Balancer principal (slot droit)
    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == owner.d && owner.inputRight == null) {
            owner.inputRight = item;
        }
        return false;
    }

    // Pas de logique propre : tout est géré par le Balancer principal
    @Override
    public void run() {
        // Toute la logique est dans Balancer.run()
    }

    // Retourne l'item stocké dans le slot droit du Balancer principal
    @Override
    public Item getCurrent() {
        return owner.inputRight;
    }

    // Vide le slot droit du Balancer principal
    @Override
    public void clearCurrent() {
        super.clearCurrent();
        owner.inputRight = null;
    }
}