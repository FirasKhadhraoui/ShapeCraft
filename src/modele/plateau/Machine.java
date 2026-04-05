package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

import java.util.LinkedList;

public abstract class Machine implements Runnable {
    protected LinkedList<Item> current;
    protected Case c;
    protected Direction d = Direction.North;

    public Machine() {
        current = new LinkedList<Item>();
    }

    public Machine(Item _item) {
        this();
        current.add(_item);
    }

    public void setCase(Case _c) {
        c = _c;
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

    public void send() {
        Case nextCase = c.plateau.getCase(c, d);

        if (nextCase != null) {
            Machine nextMachine = nextCase.getMachine();

            if (nextMachine != null && !current.isEmpty() && nextMachine.hasPlace()) {
                Item item = current.getFirst();
                nextMachine.current.add(item);
                current.remove(item);
                System.out.println("Envoi d'un item vers " + d);
            } else if (current.size() > 0 && nextMachine == null) {
                System.out.println("Item bloqué : pas de machine devant");
            } else if (current.size() > 0 && nextMachine != null && !nextMachine.hasPlace()) {
                System.out.println("Item bloqué : machine suivante pleine");
            }
        } else {
            // BORD DE LA GRILLE : l'item reste sur place
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