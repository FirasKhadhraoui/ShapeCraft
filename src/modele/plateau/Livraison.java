package modele.plateau;

import modele.item.ItemShape;

/**
 * Livraison : zone de livraison (Hub) où le joueur doit amener les formes.
 *
 * La livraison a plusieurs objectifs successifs :
 * - Chaque objectif demande une forme spécifique et une quantité
 * - Les attributs sont statiques car le hub est partagé entre ses 9 cases
 *
 * Entrée : une forme (par n'importe quelle direction)
 * Sortie : aucune (les items sont consommés)
 */
public class Livraison extends Machine {

    // STATIQUES : partagés entre toutes les cases du hub (zone 3x3)
    private static ItemShape[] formesAttendues;  // Forme demandée pour chaque objectif
    private static int[] quantitesObjectifs;     // Quantité demandée pour chaque objectif
    private static int quantiteRecue = 0;        // Quantité reçue pour l'objectif courant
    private static int objectifCourant = 0;      // Objectif en cours (0 = premier)

    /**
     * Constructeur.
     * @param formesAttendues tableau des formes pour chaque objectif
     * @param quantitesObjectifs tableau des quantités pour chaque objectif
     */
    public Livraison(ItemShape[] formesAttendues, int[] quantitesObjectifs) {
        if (Livraison.formesAttendues == null) {
            Livraison.formesAttendues = formesAttendues;
            Livraison.quantitesObjectifs = quantitesObjectifs;
        }
    }

    // Réinitialise l'état du hub (nouvelle partie)
    public static void reset() {
        quantiteRecue = 0;
        objectifCourant = 0;
    }

    // Restaure l'état du hub (chargement de partie)
    public static void setEtat(int objCourant, int qteRecue) {
        objectifCourant = objCourant;
        quantiteRecue = qteRecue;
    }

    // Getters statiques pour l'affichage des objectifs
    public static int getObjectifCourant() { return objectifCourant; }
    public static int getQuantiteRecue() { return quantiteRecue; }

    /**
     * Travail principal : vérifie si l'item reçu correspond à l'objectif.
     * Si oui, incrémente le compteur et vérifie si l'objectif est atteint.
     */
    @Override
    public void work() {
        if (!current.isEmpty() && objectifCourant < formesAttendues.length) {
            ItemShape recu = (ItemShape) current.getFirst();
            current.removeFirst();

            // Compare la forme reçue avec celle attendue
            if (recu.toString().equals(formesAttendues[objectifCourant].toString())) {
                quantiteRecue++;
                System.out.println("Livraison : reçu " + quantiteRecue + "/" + quantitesObjectifs[objectifCourant] + " items");

                // Vérifie si l'objectif est atteint
                if (quantiteRecue >= quantitesObjectifs[objectifCourant]) {
                    objectifCourant++;
                    quantiteRecue = 0;
                    if (objectifCourant < formesAttendues.length) {
                        System.out.println("*** OBJECTIF " + (objectifCourant + 1) + " ATTEINT ! ***");
                    } else {
                        System.out.println("*** FÉLICITATIONS ! TOUS LES OBJECTIFS SONT ATTEINTS ! ***");
                    }
                }
            } else {
                System.out.println("Livraison : forme incorrecte reçue !");
            }
        }
    }

    // Retourne la quantité requise pour l'objectif courant
    public static int getObjectifRequis() {
        if (objectifCourant < quantitesObjectifs.length) {
            return quantitesObjectifs[objectifCourant];
        }
        return 0;
    }

    // Retourne le numéro de l'objectif courant (1, 2, 3...)
    public static int getObjectifNumero() {
        return objectifCourant + 1;
    }

    // Retourne la forme attendue pour l'objectif courant (sous forme de chaîne)
    public static String getFormeAttendue() {
        if (objectifCourant < formesAttendues.length && formesAttendues[objectifCourant] != null) {
            return formesAttendues[objectifCourant].toString();
        }
        return "None";
    }

    // Vérifie si tous les objectifs sont terminés
    public static boolean isTermine() {
        return objectifCourant >= formesAttendues.length;
    }

    // La livraison ne renvoie jamais rien
    @Override
    public void send() {
    }
}