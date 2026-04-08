package modele.plateau;

import modele.item.ItemShape;

/**
 * Mine : machine qui produit des items à partir d'un gisement.
 *
 * La mine doit être placée sur un gisement (forme ou couleur).
 * Elle produit un item toutes les 4 secondes (DELAI_PRODUCTION = 4 ticks).
 *
 * Sortie : l'item produit est envoyé dans la direction d (North par défaut,
 *          mais la mine peut être tournée).
 */
public class Mine extends Machine {

    private int compteur = 0;                     // Compteur de ticks écoulés
    private static final int DELAI_PRODUCTION = 4; // Production toutes les 4 secondes

    /**
     * Production d'un item.
     * Incrémente le compteur à chaque tick. Quand il atteint DELAI_PRODUCTION,
     * crée une copie du gisement et la place dans la file de sortie.
     */
    @Override
    public void work() {
        if (c.getGisement() != null) {
            compteur++;
            // Si le délai est atteint ET que la machine a de la place
            if (compteur >= DELAI_PRODUCTION && hasPlace()) {
                // Récupère le gisement de la case
                ItemShape gisement = (ItemShape) c.getGisement();
                // Crée une copie (ne pas modifier le gisement original)
                ItemShape produced = new ItemShape(gisement.toString());
                produced.setColorItem(gisement.isColorItem()); // Préserve le type (couleur ou forme)
                current.add(produced); // Met l'item dans la file de sortie
                compteur = 0;          // Réinitialise le compteur
                System.out.println("Mine : production d'un item");
            }
        }
    }

    // Envoie l'item vers la direction d (comportement par défaut)
    @Override
    public void send() {
        super.send();
    }
}