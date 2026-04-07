package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

import java.util.LinkedList;

public abstract class Machine implements Runnable {
    protected LinkedList<Item> current;
    protected Case c;
    protected Direction d = Direction.North;
    protected boolean movedThisTick = false;

    public Machine() {
        current = new LinkedList<Item>();
    }

    public void resetTickFlag() {
        movedThisTick = false;
    }

    public Machine(Item _item) {
        this();
        current.add(_item);
    }

    public void setCase(Case _c) {
        c = _c;
    }

    public Direction getDirection() {
        return d;
    }

    public void setDirection(Direction d) {
        this.d = d;
    }

    public Item getCurrent() {
        if (current.size() > 0) {
            return current.get(0);
        } else {
            return null;
        }
    }

    public void clearCurrent() {
        current.clear();
    }

    public boolean hasCurrent() {
        return !current.isEmpty();
    }

    public boolean hasPlace() {
        return current.isEmpty();
    }

    // Cette machine peut-elle accepter un item venant de la direction senderDir ?
    // À surcharger dans les machines avec plusieurs slots d'entrée.
    public boolean hasPlaceFor(Direction senderDir) {
        return hasPlace();
    }

    // Reçoit un item de la direction donnée.
    // Retourne true si movedThisTick doit être activé, false sinon.
    // À surcharger pour router les items vers des slots personnalisés.
    public boolean receive(Item item, Direction senderDir) {
        current.add(item);
        return true;
    }

    public void send() {
        if (movedThisTick) return;

        Case nextCase = c.plateau.getCase(c, d);

        if (nextCase != null) {
            Machine nextMachine = nextCase.getMachine();

            if (nextMachine != null && !current.isEmpty() && nextMachine.hasPlaceFor(d)) {
                Item item = current.getFirst();
                boolean setFlag = nextMachine.receive(item, d);
                if (setFlag) nextMachine.movedThisTick = true;
                current.remove(item);
                System.out.println("Envoi d'un item vers " + d);
            } else if (current.size() > 0 && nextMachine == null) {
                System.out.println("Item bloqué : pas de machine devant");
            } else if (current.size() > 0 && nextMachine != null && !nextMachine.hasPlaceFor(d)) {
                System.out.println("Item bloqué : machine suivante pleine");
            }
        } else {
            if (!current.isEmpty()) {
                System.out.println("Item bloqué : bord de la carte");
            }
        }
    }

    public void work() {
        // action de la machine, aucune par défaut
    }

    @Override
    public void run() {
        work();
        send();
    }
}