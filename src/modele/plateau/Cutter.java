package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

public class Cutter extends Machine {
    private Item inputSlot = null;  // receives from South (senderDir = North)
    private Item leftSlot  = null;  // left half → sent North
    private Item rightSlot = null;  // right half → sent East

    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        if (senderDir == Direction.North) return inputSlot == null;
        return false;
    }

    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == Direction.North) inputSlot = item;
        return false;
    }

    @Override
    public void work() {
        if (inputSlot != null && leftSlot == null && rightSlot == null) {
            ItemShape shape = (ItemShape) inputSlot;
            // Cut() returns right part, shape becomes left part
            ItemShape rightPart = shape.Cut();
            rightPart.setColorItem(shape.isColorItem());
            leftSlot  = shape;
            rightSlot = rightPart;
            inputSlot = null;
            System.out.println("Cutter : left=" + shape + " right=" + rightPart);
        }
    }

    @Override
    public void send() {
        // Left half → North
        Case northCase = c.plateau.getCase(c, Direction.North);
        if (northCase != null && leftSlot != null) {
            Machine m = northCase.getMachine();
            if (m != null && m.hasPlaceFor(Direction.North)) {
                boolean setFlag = m.receive(leftSlot, Direction.North);
                if (setFlag) m.movedThisTick = true;
                leftSlot = null;
                System.out.println("Cutter : envoi gauche vers Nord");
            }
        }

        // Right half → East
        Case eastCase = c.plateau.getCase(c, Direction.East);
        if (eastCase != null && rightSlot != null) {
            Machine m = eastCase.getMachine();
            if (m != null && m.hasPlaceFor(Direction.East)) {
                boolean setFlag = m.receive(rightSlot, Direction.East);
                if (setFlag) m.movedThisTick = true;
                rightSlot = null;
                System.out.println("Cutter : envoi droit vers Est");
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
