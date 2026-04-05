package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

public class Case {
    protected Plateau plateau;
    protected Machine machine;
    protected Item gisement;

    public void setMachine(Machine m) {
        this.machine = m;
        if (m != null) {  // AJOUTER CETTE CONDITION
            m.setCase(this);
        }
    }

    public Machine getMachine() {
        return machine;
    }

    public void setGisement(Item gisement) {
        this.gisement = gisement;
    }

    public Item getGisement() {
        return gisement;
    }

    public Case(Plateau _plateau) {
        plateau = _plateau;
    }
}