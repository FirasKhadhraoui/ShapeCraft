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
import modele.plateau.Balancer;
import modele.plateau.BalancerSecondaire;
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

    /** Returns true if the cell cannot be built on. */
    private boolean cellBloquee(int x, int y, String type) {
        Machine existante = plateau.getCases()[x][y].getMachine();
        if (existante instanceof Livraison) {
            System.out.println("Action impossible : Le Hub ne peut pas être remplacé.");
            return true;
        }
        if (existante instanceof Mine) {
            System.out.println("Action impossible : Une mine ne peut pas être remplacée. Supprimez-la d'abord (clic droit).");
            return true;
        }
        if (existante instanceof Cutter || existante instanceof Rotator ||
                existante instanceof AtelierPeinture || existante instanceof Stacker ||
                existante instanceof Poubelle || existante instanceof Balancer ||
                existante instanceof BalancerSecondaire) {
            System.out.println("Action impossible : Cette machine ne peut pas être remplacée. Supprimez-la d'abord (clic droit).");
            return true;
        }
        if (existante instanceof Tapis && !type.equals("Tapis")) {
            System.out.println("Action impossible : Supprimez le tapis d'abord (clic droit).");
            return true;
        }
        return false;
    }

    public void placerMachine(int x, int y, String type) {
        if (cellBloquee(x, y, type)) return;

        // Vérifier si la case a un gisement
        if (plateau.getCases()[x][y].getGisement() != null) {
            ItemShape g = (ItemShape) plateau.getCases()[x][y].getGisement();
            if (g.isColorItem()) {
                System.out.println("Impossible : impossible de placer une machine sur un gisement de couleur !");
                return;
            }
            if (!type.equals("Mine")) {
                System.out.println("Impossible : seule une mine peut être placée sur un gisement !");
                return;
            }
        }

        switch (type) {
            case "Mine":
                if (plateau.getCases()[x][y].getGisement() != null) {
                    plateau.setMachine(x, y, new Mine());
                } else {
                    System.out.println("Impossible : pas de gisement ici !");
                }
                break;
            case "Tapis":
                plateau.setMachine(x, y, new Tapis());
                break;
            case "Poubelle":
                plateau.setMachine(x, y, new Poubelle());
                break;
            case "Cutter":
                plateau.setMachine(x, y, new Cutter());
                break;
            case "Rotater":
                plateau.setMachine(x, y, new Rotator());
                break;
            case "Painter":
                plateau.setMachine(x, y, new AtelierPeinture());
                break;
            case "Stacker":
                plateau.setMachine(x, y, new Stacker());
                break;
            case "Balancer":
                placerBalancer(x, y);
                break;
            default:
                System.out.println("Type de machine inconnu : " + type);
        }
    }

    private void placerBalancer(int x, int y) {
        // Default direction is North; secondary cell is to the East
        Direction sideDir = Direction.North.rotate90CW(); // = East
        int x2 = x + sideDir.getDx();
        int y2 = y + sideDir.getDy();

        if (x2 < 0 || x2 >= Plateau.SIZE_X || y2 < 0 || y2 >= Plateau.SIZE_Y) {
            System.out.println("Impossible : pas assez de place pour le balancer !");
            return;
        }
        if (plateau.getCases()[x2][y2].getMachine() != null) {
            System.out.println("Impossible : la case adjacente est occupée !");
            return;
        }
        if (plateau.getCases()[x2][y2].getGisement() != null) {
            System.out.println("Impossible : la case adjacente a un gisement !");
            return;
        }

        Balancer balancer = new Balancer();
        BalancerSecondaire secondaire = new BalancerSecondaire(balancer);
        plateau.setMachine(x, y, balancer);
        plateau.setMachine(x2, y2, secondaire);
        balancer.secondaryCase = plateau.getCases()[x2][y2];
    }

    public void placerMachine(int x, int y, String type, Direction direction) {
        if (cellBloquee(x, y, type)) return;

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
        if (cellBloquee(x, y, "Tapis")) return;

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

        // If clicking secondary cell, redirect to primary
        if (m instanceof BalancerSecondaire) {
            Balancer owner = ((BalancerSecondaire) m).owner;
            int[] pos = findMachinePosition(owner);
            if (pos != null) rotateMachine(pos[0], pos[1]);
            return;
        }

        if (m instanceof Balancer) {
            Balancer balancer = (Balancer) m;
            // Clear old secondary cell
            int[] secPos = findBalancerSecondaire(balancer);
            if (secPos != null) {
                plateau.getCases()[secPos[0]][secPos[1]].setMachine(null);
            }
            // Rotate
            Direction[] dirs = {Direction.North, Direction.East, Direction.South, Direction.West};
            Direction cur = balancer.getDirection();
            int idx = 0;
            for (int i = 0; i < dirs.length; i++) {
                if (dirs[i] == cur) { idx = i; break; }
            }
            balancer.setDirection(dirs[(idx + 1) % 4]);
            // Place new secondary
            Direction newSide = balancer.getDirection().rotate90CW();
            int nx = x + newSide.getDx();
            int ny = y + newSide.getDy();
            if (nx >= 0 && nx < Plateau.SIZE_X && ny >= 0 && ny < Plateau.SIZE_Y
                    && plateau.getCases()[nx][ny].getMachine() == null) {
                BalancerSecondaire secondaire = new BalancerSecondaire(balancer);
                plateau.setMachine(nx, ny, secondaire);
                balancer.secondaryCase = plateau.getCases()[nx][ny];
            } else {
                balancer.secondaryCase = null;
                System.out.println("Avertissement : pas de place pour la case secondaire après rotation.");
            }
            plateau.forceRefresh();
            return;
        }

        // Normal rotation
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
            if (m instanceof Livraison) {
                System.out.println("Action impossible : Le Hub ne peut pas être supprimé.");
                return;
            }

            if (m instanceof Balancer) {
                // Also remove secondary cell
                int[] secPos = findBalancerSecondaire((Balancer) m);
                if (secPos != null) {
                    plateau.getCases()[secPos[0]][secPos[1]].getMachine().clearCurrent();
                    plateau.getCases()[secPos[0]][secPos[1]].setMachine(null);
                }
            }

            if (m instanceof BalancerSecondaire) {
                // Also remove primary cell
                Balancer owner = ((BalancerSecondaire) m).owner;
                int[] priPos = findMachinePosition(owner);
                if (priPos != null) {
                    owner.clearCurrent();
                    plateau.getCases()[priPos[0]][priPos[1]].setMachine(null);
                }
            }

            m.clearCurrent();
            System.out.println("Suppression de la machine à (" + x + "," + y + ") et de son item");
        }

        plateau.setMachine(x, y, null);
        plateau.forceRefresh();
    }

    /** Finds the grid position of a given machine instance, or null if not found. */
    private int[] findMachinePosition(Machine target) {
        for (int cx = 0; cx < Plateau.SIZE_X; cx++) {
            for (int cy = 0; cy < Plateau.SIZE_Y; cy++) {
                if (plateau.getCases()[cx][cy].getMachine() == target) {
                    return new int[]{cx, cy};
                }
            }
        }
        return null;
    }

    /** Finds the grid position of the BalancerSecondaire belonging to a Balancer. */
    private int[] findBalancerSecondaire(Balancer balancer) {
        for (int cx = 0; cx < Plateau.SIZE_X; cx++) {
            for (int cy = 0; cy < Plateau.SIZE_Y; cy++) {
                Machine m = plateau.getCases()[cx][cy].getMachine();
                if (m instanceof BalancerSecondaire && ((BalancerSecondaire) m).owner == balancer) {
                    return new int[]{cx, cy};
                }
            }
        }
        return null;
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
