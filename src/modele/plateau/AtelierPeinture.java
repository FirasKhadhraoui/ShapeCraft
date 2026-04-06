package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;
import modele.item.Color;

public class AtelierPeinture extends Machine {

    private Item colorSlot = null; // filled by belt coming from North (sender direction = South)
    private Item shapeSlot = null; // filled by belt coming from West  (sender direction = East)

    public AtelierPeinture() {
        this.d = Direction.East; // always outputs to the right
    }

    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        if (senderDir == Direction.South) return colorSlot == null; // top input
        if (senderDir == Direction.East)  return shapeSlot == null; // left input
        return false;
    }

    // Route item to the correct slot; return false so movedThisTick is NOT set,
    // allowing send() to run in the same tick if a painted item is already waiting.
    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == Direction.South) {
            colorSlot = item;
        } else if (senderDir == Direction.East) {
            shapeSlot = item;
        }
        return false;
    }

    @Override
    public void work() {
        if (colorSlot != null && shapeSlot != null && current.isEmpty()) {
            Color c = extractColor((ItemShape) colorSlot);
            if (c != null) {
                ItemShape painted = new ItemShape(((ItemShape) shapeSlot).toString());
                painted.Color(c);
                current.add(painted);
                System.out.println("AtelierPeinture : item peint en " + c);
            }
            colorSlot = null;
            shapeSlot = null;
        }
    }

    private Color extractColor(ItemShape colorItem) {
        for (Color c : colorItem.getColors(ItemShape.Layer.one)) {
            if (c != null) return c;
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
