package modele.plateau;

import modele.item.Item;

public class Balancer extends Machine {
    Item inputLeft  = null;  // entered primary (left lane)
    Item inputRight = null;  // entered secondary (right lane)
    public Case secondaryCase = null;

    private boolean ranThisTick = false;

    // senderDir == d  →  item enters the LEFT lane (primary cell)
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        return senderDir == d && inputLeft == null;
    }

    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == d && inputLeft == null) {
            inputLeft = item;
        }
        return false;
    }

    @Override
    public void resetTickFlag() {
        super.resetTickFlag();
        ranThisTick = false;
    }

    @Override
    public void run() {
        if (ranThisTick) return;
        ranThisTick = true;
        send();
    }

    @Override
    public void send() {
        // Left item exits from the RIGHT lane (north of secondary) — SWAP
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

        // Right item exits from the LEFT lane (north of primary) — SWAP
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

    @Override
    public Item getCurrent() {
        return inputLeft;
    }

    @Override
    public void clearCurrent() {
        super.clearCurrent();
        inputLeft  = null;
        inputRight = null;
    }
}
