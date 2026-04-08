package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

public class Stacker extends Machine {
    private ItemShape inputBottom = null;  // from direction d (below)
    private ItemShape inputRight  = null;  // from direction d.rotate90CCW() (right side)
    private ItemShape output      = null;

    // The right-side input comes from a belt entering from the right:
    // e.g. d=North → right side is East → belt goes West → senderDir=d.rotate90CCW()=West
    private Direction rightSenderDir() {
        return d.rotate90CCW();
    }

    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        if (senderDir == d)               return inputBottom == null;
        if (senderDir == rightSenderDir()) return inputRight  == null;
        return false;
    }

    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == d && inputBottom == null) {
            inputBottom = (ItemShape) item;
        } else if (senderDir == rightSenderDir() && inputRight == null) {
            inputRight = (ItemShape) item;
        }
        return false;
    }

    @Override
    public void work() {
        if (inputBottom != null && inputRight != null && output == null) {
            ItemShape result = new ItemShape(inputBottom.toString());
            result.stack(inputRight);
            result.setColorItem(false);
            output = result;
            inputBottom = null;
            inputRight  = null;
        }
    }

    @Override
    public void send() {
        if (output == null) return;
        Case nextCase = c.plateau.getCase(c, d);
        if (nextCase != null) {
            Machine m = nextCase.getMachine();
            if (m != null && m.hasPlaceFor(d)) {
                m.receive(output, d);
                output = null;
            }
        }
    }

    @Override
    public Item getCurrent() {
        if (output != null)      return output;
        if (inputBottom != null) return inputBottom;
        return inputRight;
    }

    @Override
    public void clearCurrent() {
        super.clearCurrent();
        inputBottom = null;
        inputRight  = null;
        output      = null;
    }
}
