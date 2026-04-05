package modele.plateau;

import modele.item.ItemShape;

public class Livraison extends Machine {
    private static ItemShape formeAttendue;
    private static int quantiteRecue = 0;
    private static int objectifCourant = 0;
    private static final int[] OBJECTIFS = {5, 10, 15}; // 3 objectifs consécutifs

    public Livraison(ItemShape formeAttendue) {
        if (Livraison.formeAttendue == null) {
            Livraison.formeAttendue = formeAttendue;
        }
    }

    @Override
    public void work() {
        if (!current.isEmpty() && objectifCourant < OBJECTIFS.length) {
            ItemShape recu = (ItemShape) current.getFirst();
            current.removeFirst();

            // Vérifier si la forme correspond à celle attendue
            if (recu.toString().equals(formeAttendue.toString())) {
                quantiteRecue++;
                System.out.println("Livraison : reçu " + quantiteRecue + "/" + OBJECTIFS[objectifCourant] + " items");

                // Vérifier si l'objectif est atteint
                if (quantiteRecue >= OBJECTIFS[objectifCourant]) {
                    objectifCourant++;
                    quantiteRecue = 0;
                    if (objectifCourant < OBJECTIFS.length) {
                        System.out.println("*** OBJECTIF " + objectifCourant + " ATTEINT ! ***");
                        System.out.println("Nouvel objectif : " + OBJECTIFS[objectifCourant] + " items");
                    } else {
                        System.out.println("*** FÉLICITATIONS ! TOUS LES OBJECTIFS SONT ATTEINTS ! ***");
                    }
                }
            } else {
                System.out.println("Livraison : forme incorrecte reçue !");
            }
        }
    }

    public static int getObjectifCourant() {
        return objectifCourant;
    }

    public static int getQuantiteRecue() {
        return quantiteRecue;
    }

    public static int getObjectifRequis() {
        if (objectifCourant < OBJECTIFS.length) {
            return OBJECTIFS[objectifCourant];
        }
        return 0;
    }

    public static boolean isTermine() {
        return objectifCourant >= OBJECTIFS.length;
    }

    @Override
    public void send() {
        // La livraison ne renvoie rien
    }
}