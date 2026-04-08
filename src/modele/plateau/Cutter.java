package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

/**
 * Cutter : machine qui découpe une forme en deux parties.
 *
 * Entrée : par derrière (direction d)
 * Sortie gauche : direction d
 * Sortie droite : direction d tournée de 90° vers la droite
 *
 * Exemple avec d = North :
 *   Entrée : Sud (tapis vers Nord)
 *   Sortie gauche : Nord (partie haute)
 *   Sortie droite : Est (partie basse)
 */
public class Cutter extends Machine {

    private Item inputSlot = null;  // Item reçu en entrée
    private Item leftSlot  = null;  // Partie gauche (va vers leftDir)
    private Item rightSlot = null;  // Partie droite (va vers rightDir)

    // Accepte un item uniquement par la direction d (derrière le cutter)
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        return senderDir == d && inputSlot == null;
    }

    // Reçoit un item et le stocke dans inputSlot
    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == d) inputSlot = item;
        return false;
    }

    // Découpe l'item en deux parties
    @Override
    public void work() {
        if (inputSlot != null && leftSlot == null && rightSlot == null) {
            ItemShape shape = (ItemShape) inputSlot;
            // Cut() retourne la partie droite, shape devient la partie gauche
            ItemShape rightPart = shape.Cut();
            rightPart.setColorItem(shape.isColorItem());
            leftSlot  = shape;      // Partie gauche
            rightSlot = rightPart;  // Partie droite
            inputSlot = null;       // Entrée vidée
        }
    }

    // Envoie les deux parties vers leurs sorties respectives
    @Override
    public void send() {
        Direction leftDir  = d;              // Sortie gauche = direction d
        Direction rightDir = d.rotate90CW(); // Sortie droite = d + 90°

        // Envoi de la partie gauche
        Case leftCase = c.plateau.getCase(c, leftDir);
        if (leftCase != null && leftSlot != null) {
            Machine m = leftCase.getMachine();
            if (m != null && m.hasPlaceFor(leftDir)) {
                boolean setFlag = m.receive(leftSlot, leftDir);
                if (setFlag) m.movedThisTick = true;
                leftSlot = null;
            }
        }

        // Envoi de la partie droite
        Case rightCase = c.plateau.getCase(c, rightDir);
        if (rightCase != null && rightSlot != null) {
            Machine m = rightCase.getMachine();
            if (m != null && m.hasPlaceFor(rightDir)) {
                boolean setFlag = m.receive(rightSlot, rightDir);
                if (setFlag) m.movedThisTick = true;
                rightSlot = null;
            }
        }
    }

    // Retourne l'item actuellement dans le cutter (priorité: leftSlot, rightSlot, inputSlot)
    @Override
    public Item getCurrent() {
        if (leftSlot != null) return leftSlot;
        if (rightSlot != null) return rightSlot;
        return inputSlot;
    }

    // Vide tous les slots
    @Override
    public void clearCurrent() {
        super.clearCurrent();
        inputSlot = null;
        leftSlot  = null;
        rightSlot = null;
    }
}