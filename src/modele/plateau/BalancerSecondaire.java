package modele.plateau;

import modele.item.Item;

public class BalancerSecondaire extends Machine {
    public Balancer owner;

    public BalancerSecondaire(Balancer owner) {
        this.owner = owner;
    }

    // senderDir == owner.d  →  item enters the RIGHT lane (secondary cell)
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        return senderDir == owner.d && owner.inputRight == null;
    }

    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == owner.d && owner.inputRight == null) {
            owner.inputRight = item;
        }
        return false;
    }

    @Override
    public void run() {
        // All logic handled by Balancer.run()
    }

    @Override
    public Item getCurrent() {
        return owner.inputRight;
    }

    @Override
    public void clearCurrent() {
        super.clearCurrent();
        owner.inputRight = null;
    }
}
