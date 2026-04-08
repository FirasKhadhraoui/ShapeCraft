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

    public static void reset() {
        quantiteRecue = 0;
        objectifCourant = 0;
    }

    public static void setEtat(int objCourant, int qteRecue) {
        objectifCourant = objCourant;
        quantiteRecue = qteRecue;
    }

    public static int getObjectifCourant() {
        return objectifCourant;
    }

    public static int getQuantiteRecue() {
        return quantiteRecue;
    }

    @Override
    public void work() {
        if (!current.isEmpty() && objectifCourant < formesAttendues.length) {
            ItemShape recu = (ItemShape) current.getFirst();
            current.removeFirst();

            if (recu.toString().equals(formesAttendues[objectifCourant].toString())) {
                quantiteRecue++;
                System.out.println("Livraison : reçu " + quantiteRecue + "/" + quantitesObjectifs[objectifCourant] + " items");

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

    public static int getObjectifRequis() {
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
    }
}