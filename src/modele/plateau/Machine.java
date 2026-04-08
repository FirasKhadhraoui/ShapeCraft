package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

import java.util.LinkedList;

/**
 * Classe abstraite représentant une machine dans le jeu.
 *
 * Toutes les machines (Mine, Tapis, Cutter, etc.) héritent de cette classe.
 *
 * Fonctionnement général d'une machine :
 * 1. work() : transforme l'item (ou ne fait rien)
 * 2. send() : envoie l'item vers la case suivante (dans la direction d)
 *
 * Le cycle est exécuté une fois par seconde (1 tick).
 */
public abstract class Machine implements Runnable {

    protected LinkedList<Item> current; // File d'attente des items (taille 1 en général)
    protected Case c;                   // Case où se trouve la machine
    protected Direction d = Direction.North; // Direction de sortie
    protected boolean movedThisTick = false; // Empêche d'envoyer plusieurs fois par tick

    // Constructeur : file vide
    public Machine() {
        current = new LinkedList<Item>();
    }

    // Réinitialise le flag de tick (appelé avant chaque cycle)
    public void resetTickFlag() {
        movedThisTick = false;
    }

    // Constructeur avec un item initial
    public Machine(Item _item) {
        this();
        current.add(_item);
    }

    // Lie la machine à sa case
    public void setCase(Case _c) {
        c = _c;
    }

    // Getters / Setters pour la direction
    public Direction getDirection() { return d; }
    public void setDirection(Direction d) { this.d = d; }

    // Retourne le premier item de la file (sans le retirer)
    public Item getCurrent() {
        if (current.size() > 0) {
            return current.get(0);
        } else {
            return null;
        }
    }

    // Vide la file d'attente
    public void clearCurrent() {
        current.clear();
    }

    // Vérifie si la file contient un item
    public boolean hasCurrent() {
        return !current.isEmpty();
    }

    // Vérifie si la file est vide (place disponible)
    public boolean hasPlace() {
        return current.isEmpty();
    }

    /**
     * Vérifie si la machine peut accepter un item venant d'une direction donnée.
     * À surcharger pour les machines à plusieurs entrées (Painter, Stacker, etc.)
     */
    public boolean hasPlaceFor(Direction senderDir) {
        return hasPlace();
    }

    /**
     * Reçoit un item depuis une direction donnée.
     * Retourne true si movedThisTick doit être activé.
     * À surcharger pour les machines à plusieurs entrées.
     */
    public boolean receive(Item item, Direction senderDir) {
        current.add(item);
        return true;
    }

    /**
     * Envoie l'item vers la case suivante (dans la direction d).
     * Vérifie que la case suivante existe et peut recevoir l'item.
     */
    public void send() {
        if (movedThisTick) return; // Déjà envoyé ce tick

        Case nextCase = c.plateau.getCase(c, d);

        if (nextCase != null) {
            Machine nextMachine = nextCase.getMachine();

            if (nextMachine != null && !current.isEmpty() && nextMachine.hasPlaceFor(d)) {
                Item item = current.getFirst();
                boolean setFlag = nextMachine.receive(item, d);
                if (setFlag) nextMachine.movedThisTick = true;
                current.remove(item);
            }
        }
    }

    /**
     * Travail principal de la machine (à surcharger).
     * Exemples : Mine produit un item, Cutter découpe, Rotator tourne, etc.
     */
    public void work() {
        // Rien par défaut
    }

    /**
     * Cycle complet de la machine : travail puis envoi.
     */
    @Override
    public void run() {
        work();
        send();
    }
}