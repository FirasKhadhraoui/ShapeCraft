package modele.plateau;

import modele.item.ItemShape;

public class Livraison extends Machine {
    private static ItemShape[] formesAttendues;
    private static int[] quantitesObjectifs;
    private static int quantiteRecue = 0;
    private static int objectifCourant = 0;

    public Livraison(ItemShape[] formesAttendues, int[] quantitesObjectifs) {
        if (Livraison.formesAttendues == null) {
            Livraison.formesAttendues = formesAttendues;
            Livraison.quantitesObjectifs = quantitesObjectifs;
        }
    }

    @Override
    public void work() {
        if (!current.isEmpty() && objectifCourant < formesAttendues.length) {
            ItemShape recu = (ItemShape) current.getFirst();
            current.removeFirst();

            // Vérifier si la forme correspond à celle attendue pour l'objectif courant
            if (recu.toString().equals(formesAttendues[objectifCourant].toString())) {
                quantiteRecue++;
                System.out.println("Livraison : reçu " + quantiteRecue + "/" + quantitesObjectifs[objectifCourant] + " items");

                if (quantiteRecue >= quantitesObjectifs[objectifCourant]) {
                    objectifCourant++;
                    quantiteRecue = 0;
                    if (objectifCourant < formesAttendues.length) {
                        System.out.println("*** OBJECTIF " + (objectifCourant + 1) + " ATTEINT ! ***");
                        System.out.println("Nouvelle forme attendue : " + formesAttendues[objectifCourant].toString());
                        System.out.println("Nouvel objectif : " + quantitesObjectifs[objectifCourant] + " items");
                    } else {
                        System.out.println("*** FÉLICITATIONS ! TOUS LES OBJECTIFS SONT ATTEINTS ! ***");
                    }
                }
            } else {
                System.out.println("Livraison : forme incorrecte reçue ! Attendu: " +
                        formesAttendues[objectifCourant].toString() + " Reçu: " + recu.toString());
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
        if (objectifCourant < quantitesObjectifs.length) {
            return quantitesObjectifs[objectifCourant];
        }
        return 0;
    }

    public static int getObjectifMax() {
        if (objectifCourant < quantitesObjectifs.length) {
            return quantitesObjectifs[objectifCourant];
        }
        return 0;
    }

    public static int getObjectifNumero() {
        return objectifCourant + 1;
    }

    public static String getFormeAttendue() {
        if (objectifCourant < formesAttendues.length && formesAttendues[objectifCourant] != null) {
            return formesAttendues[objectifCourant].toString();
        }
        return "None";
    }

    public static boolean isTermine() {
        return objectifCourant >= formesAttendues.length;
    }

    @Override
    public void send() {
        // La livraison ne renvoie rien
    }
}