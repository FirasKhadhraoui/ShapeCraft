package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;
import modele.item.Color;

public class AtelierPeinture extends Machine {

    private ItemShape colorSlot = null;
    private ItemShape shapeSlot = null;

    public AtelierPeinture() {
        this.d = Direction.East;
    }

    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        if (senderDir == Direction.South) {
            return colorSlot == null;
        }
        if (senderDir == Direction.East) {
            return shapeSlot == null;
        }
        return false;
    }

    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == Direction.South) {
            colorSlot = (ItemShape) item;
        } else if (senderDir == Direction.East) {
            shapeSlot = (ItemShape) item;
        }
        return false;
    }

    @Override
    public void work() {
        if (colorSlot != null && shapeSlot != null && current.isEmpty()) {
            Color c = extractColor(colorSlot);
            if (c != null) {
                // Créer une copie à partir de la chaîne
                ItemShape painted = new ItemShape(shapeSlot.toString());
                painted.Color(c);
                painted.setColorItem(false);
                // Préserver l'état coupé
                painted.setCut(shapeSlot.isCut());
                current.add(painted);
            }
            colorSlot = null;
            shapeSlot = null;
        }
    }

    private Color extractColor(ItemShape colorItem) {
        for (Color c : colorItem.getColors(ItemShape.Layer.one)) {
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @Override
    public void send() {
        if (!current.isEmpty()) {
            Case nextCase = c.plateau.getCase(c, Direction.East);
            if (nextCase != null) {
                Machine nextMachine = nextCase.getMachine();
                if (nextMachine != null && nextMachine.hasPlaceFor(Direction.East)) {
                    Item item = current.removeFirst();
                    nextMachine.receive(item, Direction.East);
                }
            }
        }
    }

    @Override
    public Item getCurrent() {
        if (!current.isEmpty()) {
            return current.getFirst();
        }
        return null;
    }

    @Override
    public void clearCurrent() {
        super.clearCurrent();
        colorSlot = null;
        shapeSlot = null;
    }
}