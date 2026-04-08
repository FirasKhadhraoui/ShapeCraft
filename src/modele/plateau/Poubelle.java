package modele.plateau;

/**
 * Poubelle : machine qui détruit les items qu'elle reçoit.
 *
 - Entrée : par n'importe quelle direction (tapis pointant vers la poubelle)
 - Sortie : aucune (les items sont supprimés définitivement)
 *
 * Utile pour :
 * - Nettoyer les items indésirables
 * - Éviter les blocages dans les chaînes de production
 */
public class Poubelle extends Machine {

    /**
     * Détruit tous les items reçus.
     * La poubelle ne conserve aucun item dans sa file.
     */
    @Override
    public void work() {
        if (!current.isEmpty()) {
            current.clear();           // Vide la file d'attente
            System.out.println("Poubelle : items supprimés");
        }
    }

    /**
     * La poubelle n'envoie jamais rien.
     * Surcharge pour éviter l'envoi vers la direction d.
     */
    @Override
    public void send() {
        // Ne rien faire
    }
}