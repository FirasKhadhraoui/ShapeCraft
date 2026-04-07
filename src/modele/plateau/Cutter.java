package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

public class Cutter extends Machine {
    private Item inputSlot = null;  // receives from South (senderDir = North)
    private Item leftSlot  = null;  // left half → sent North
    private Item rightSlot = null;  // right half → sent East

    // Direction d'entrée = d (tapis allant dans la direction d alimente le cutter par derrière)
    // Sortie gauche = d
    // Sortie droite = d tourné de 90° dans le sens des aiguilles d'une montre
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        if (senderDir == d) return inputSlot == null;
        return false;
    }

    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == d) inputSlot = item;
        return false;
    }

    @Override
    public void work() {
        if (inputSlot != null && leftSlot == null && rightSlot == null) {
            ItemShape shape = (ItemShape) inputSlot;
            // Cut() retourne la partie droite, shape devient la partie gauche
            ItemShape rightPart = shape.Cut();
            rightPart.setColorItem(shape.isColorItem());
            leftSlot  = shape;
            rightSlot = rightPart;
            inputSlot = null;
        }
    }

    @Override
    public void send() {
        Direction leftDir  = d;
        Direction rightDir = d.rotate90CW();

        // Moitié gauche → direction de sortie gauche
        Case leftCase = c.plateau.getCase(c, leftDir);
        if (leftCase != null && leftSlot != null) {
            Machine m = leftCase.getMachine();
            if (m != null && m.hasPlaceFor(leftDir)) {
                boolean setFlag = m.receive(leftSlot, leftDir);
                if (setFlag) m.movedThisTick = true;
                leftSlot = null;
            }
        }

        // Moitié droite → direction de sortie droite
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

    @Override
    public Item getCurrent() {
        if (leftSlot != null) return leftSlot;
        if (rightSlot != null) return rightSlot;
        return inputSlot;
    }

    @Override
    public void clearCurrent() {
        super.clearCurrent();
        inputSlot = null;
        leftSlot  = null;
        rightSlot = null;
    }
}
