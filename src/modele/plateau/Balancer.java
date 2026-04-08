package modele.plateau;

import modele.item.Item;

/**
 * Balancer : machine sur 2 cases qui échange les items entre deux voies.
 *
 * Principe :
 * - L'item qui arrive sur la case principale (inputLeft) ressort sur la voie
 *   de la case secondaire (nord de secondaryCase)
 * - L'item qui arrive sur la case secondaire (inputRight) ressort sur la voie
 *   de la case principale (nord de la case principale)
 *
 * Les deux items sont donc échangés (swapped).
 */
public class Balancer extends Machine {

    Item inputLeft  = null;  // Item reçu sur la case principale (voie gauche)
    Item inputRight = null;  // Item reçu sur la case secondaire (voie droite)
    public Case secondaryCase = null; // Référence vers la case secondaire

    private boolean ranThisTick = false; // Évite l'exécution multiple par tick

    // Accepte un item uniquement par la direction d (arrière de la case principale)
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        return senderDir == d && inputLeft == null;
    }

    // Reçoit un item sur la case principale
    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == d && inputLeft == null) {
            inputLeft = item;
        }
        return false;
    }

    // Réinitialise le flag de tick (appelé avant chaque cycle)
    @Override
    public void resetTickFlag() {
        super.resetTickFlag();
        ranThisTick = false;
    }

    // Exécution du Balancer (une seule fois par tick)
    @Override
    public void run() {
        if (ranThisTick) return;
        ranThisTick = true;
        send();
    }

    // Échange les items entre les deux voies
    @Override
    public void send() {
        // L'item de la case principale (gauche) part vers la voie de la case secondaire
        if (inputLeft != null && secondaryCase != null) {
            Case dest = secondaryCase.plateau.getCase(secondaryCase, d);
            if (dest != null) {
                Machine m = dest.getMachine();
                if (m != null && m.hasPlaceFor(d)) {
                    m.receive(inputLeft, d);
                    inputLeft = null;
                }
            }
        }

        // L'item de la case secondaire (droite) part vers la voie de la case principale
        if (inputRight != null) {
            Case dest = c.plateau.getCase(c, d);
            if (dest != null) {
                Machine m = dest.getMachine();
                if (m != null && m.hasPlaceFor(d)) {
                    m.receive(inputRight, d);
                    inputRight = null;
                }
            }
        }
    }

    // Retourne l'item en attente sur la case principale
    @Override
    public Item getCurrent() {
        return inputLeft;
    }

    // Vide les deux slots
    @Override
    public void clearCurrent() {
        super.clearCurrent();
        inputLeft  = null;
        inputRight = null;
    }
}