package modele.jeu;

import modele.plateau.Mine;
import modele.plateau.Plateau;
import modele.plateau.Poubelle;
import modele.plateau.Tapis;
import modele.plateau.Livraison;
import modele.plateau.Machine;
import modele.plateau.Cutter;
import modele.plateau.Rotator;
import modele.plateau.AtelierPeinture;
import modele.plateau.Stacker;
import modele.plateau.Case;
import modele.plateau.Direction;
import modele.item.ItemShape;

public class Jeu extends Thread{
    private Plateau plateau;

    public Jeu() {
        plateau = new Plateau();

        // Définir les objectifs (forme + quantité)
        ItemShape[] objectifsFormes = {
                new ItemShape("CrCr----"),
                new ItemShape("CrCr----"),
                new ItemShape("CrCr----")
        };

        int[] objectifsQuantites = {5, 10, 15};

        Livraison hubCentral = new Livraison(objectifsFormes, objectifsQuantites);

        // Placement du Hub au centre de la grille (zone 3x3)
        int centerX = Plateau.SIZE_X / 2;
        int centerY = Plateau.SIZE_Y / 2;
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY - 1; y <= centerY + 1; y++) {
                plateau.setMachine(x, y, hubCentral);
            }
        }

        start();
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public void placerMachine(int x, int y, String type) {
        Machine nouvelleMachine = null;

        // Empêcher de remplacer le Hub
        if (plateau.getCases()[x][y].getMachine() instanceof Livraison) {
            System.out.println("Action impossible : Le Hub ne peut pas être remplacé.");
            return;
        }

        // Empêcher de remplacer une machine existante (quelle qu'elle soit)
        if (plateau.getCases()[x][y].getMachine() != null) {
            System.out.println("Action impossible : Une machine existe déjà ici. Supprimez-la d'abord (clic droit).");
            return;
        }

        // Vérifier si la case a un gisement - SEULE UNE MINE PEUT ÊTRE PLACÉE
        if (plateau.getCases()[x][y].getGisement() != null) {
            if (!type.equals("Mine")) {
                System.out.println("Impossible : seule une mine peut être placée sur un gisement !");
                return;
            }
        }

        switch (type) {
            case "Mine":
                if (plateau.getCases()[x][y].getGisement() != null) {
                    nouvelleMachine = new Mine();
                } else {
                    System.out.println("Impossible : pas de gisement ici !");
                    return;
                }
                break;
            case "Tapis":
                nouvelleMachine = new Tapis();
                break;
            case "Poubelle":
                nouvelleMachine = new Poubelle();
                break;
            case "Cutter":
                nouvelleMachine = new Cutter();
                break;
            case "Rotater":
                nouvelleMachine = new Rotator();
                break;
            case "Painter":
                nouvelleMachine = new AtelierPeinture();
                break;
            case "Stacker":
                nouvelleMachine = new Stacker();
                break;
            default:
                System.out.println("Type de machine inconnu : " + type);
                return;
        }

        if (nouvelleMachine != null) {
            plateau.setMachine(x, y, nouvelleMachine);
        }
    }

    public void placerMachine(int x, int y, String type, Direction direction) {
        // Empêcher de remplacer le Hub
        if (plateau.getCases()[x][y].getMachine() instanceof Livraison) {
            System.out.println("Action impossible : Le Hub ne peut pas être remplacé.");
            return;
        }

        // Empêcher de remplacer une machine existante
        if (plateau.getCases()[x][y].getMachine() != null) {
            System.out.println("Action impossible : Une machine existe déjà ici. Supprimez-la d'abord (clic droit).");
            return;
        }

        // Vérifier si la case a un gisement - SEULE UNE MINE PEUT ÊTRE PLACÉE
        if (plateau.getCases()[x][y].getGisement() != null) {
            System.out.println("Impossible : seule une mine peut être placée sur un gisement !");
            return;
        }

        if (!type.equals("Tapis")) {
            placerMachine(x, y, type);
            return;
        }
        plateau.setMachine(x, y, new Tapis(direction));
    }

    public void placerTapisCorner(int x, int y, Direction incoming, Direction outgoing) {
        // Empêcher de remplacer le Hub
        if (plateau.getCases()[x][y].getMachine() instanceof Livraison) {
            System.out.println("Action impossible : Le Hub ne peut pas être remplacé.");
            return;
        }

        // Empêcher de remplacer une machine existante
        if (plateau.getCases()[x][y].getMachine() != null) {
            System.out.println("Action impossible : Une machine existe déjà ici. Supprimez-la d'abord (clic droit).");
            return;
        }

        // Vérifier si la case a un gisement - SEULE UNE MINE PEUT ÊTRE PLACÉE
        if (plateau.getCases()[x][y].getGisement() != null) {
            System.out.println("Impossible : seule une mine peut être placée sur un gisement !");
            return;
        }

        plateau.setMachine(x, y, new Tapis(incoming, outgoing));
    }

    public void rotateMine(int x, int y) {
        rotateMachine(x, y);
    }

    public void rotateMachine(int x, int y) {
        Machine m = plateau.getCases()[x][y].getMachine();
        if (m == null) return;
        Direction[] dirs = {Direction.North, Direction.East, Direction.South, Direction.West};
        Direction cur = m.getDirection();
        int idx = 0;
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i] == cur) { idx = i; break; }
        }
        m.setDirection(dirs[(idx + 1) % 4]);
        plateau.forceRefresh();
    }

    public void supprimerMachine(int x, int y) {
        Case c = plateau.getCases()[x][y];
        Machine m = c.getMachine();

        if (m != null) {
            // Empêcher la suppression si c'est le Hub uniquement
            if (m instanceof Livraison) {
                System.out.println("Action impossible : Le Hub ne peut pas être supprimé.");
                return;
            }
            // Toutes les autres machines peuvent être supprimées
            m.clearCurrent();
            System.out.println("Suppression de la machine à (" + x + "," + y + ") et de son item");
        }

        plateau.setMachine(x, y, null);
        plateau.forceRefresh();
    }

    public void press(int x, int y) {
        placerMachine(x, y, "Tapis");
    }

    public void slide(int x, int y) {
        placerMachine(x, y, "Tapis");
    }

    public void run() {
        jouerPartie();
    }

    public void jouerPartie() {
        while(true) {
            try {
                plateau.run();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}