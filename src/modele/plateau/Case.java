package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

/**
 * Case : une cellule de la grille du plateau.
 *
 * Une case peut contenir :
 * - Une machine (Mine, Tapis, Cutter, etc.)
 * - Un gisement (forme ou couleur) sur lequel on peut placer une mine
 */
public class Case {

    protected Plateau plateau;   // Référence vers le plateau parent
    protected Machine machine;   // Machine présente sur la case (peut être null)
    protected Item gisement;     // Gisement présent sur la case (peut être null)

    /**
     * Place une machine sur la case.
     * @param m la machine à placer (ou null pour supprimer)
     */
    public void setMachine(Machine m) {
        this.machine = m;
        if (m != null) {
            m.setCase(this); // Lie la machine à cette case
        }
    }

    /**
     * Retourne la machine présente sur la case.
     * @return la machine, ou null si aucune
     */
    public Machine getMachine() {
        return machine;
    }

    /**
     * Définit un gisement sur la case.
     * @param gisement l'item de gisement (forme ou couleur)
     */
    public void setGisement(Item gisement) {
        this.gisement = gisement;
    }

    /**
     * Retourne le gisement présent sur la case.
     * @return le gisement, ou null si aucun
     */
    public Item getGisement() {
        return gisement;
    }

    /**
     * Constructeur.
     * @param _plateau référence vers le plateau parent
     */
    public Case(Plateau _plateau) {
        plateau = _plateau;
    }
}