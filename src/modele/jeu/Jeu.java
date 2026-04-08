package modele.jeu;

import java.io.*;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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

        // Objectifs : Carré plein, Partie droite d'un rond, Partie basse d'une étoile, Partie droite d'un carré vert
        ItemShape[] objectifsFormes = {
                new ItemShape("C-C-C-C-"),      // Objectif 1: Carré plein
                new ItemShape("c-c-----"),      // Objectif 2: Partie droite d'un rond
                new ItemShape("--S-S---"),      // Objectif 3: Partie basse d'une étoile
                new ItemShape("CgCg----")       // Objectif 4: Partie droite d'un carré vert
        };

        int[] objectifsQuantites = {3, 3, 3, 3};

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
        // Les Tapis peuvent être remplacés (on ne bloque pas)
        return false;
    }

    public void placerMachine(int x, int y, String type) {
        if (cellBloquee(x, y, type)) return;

        // Vérifier si la case a un gisement
        if (plateau.getCases()[x][y].getGisement() != null) {
            // Seule une mine peut être placée sur un gisement (couleur ou forme)
            if (!type.equals("Mine")) {
                System.out.println("Impossible : seule une mine peut être placée sur un gisement !");
                return;
            }
            // Une mine peut se placer ici (on continue)
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

        // Vérifier si la case adjacente a un gisement
        if (plateau.getCases()[x2][y2].getGisement() != null) {
            System.out.println("Impossible : la case adjacente a un gisement !");
            return;
        }

        // Si la case adjacente a un tapis, on le supprime
        Machine existante = plateau.getCases()[x2][y2].getMachine();
        if (existante != null && !(existante instanceof Tapis)) {
            System.out.println("Impossible : la case adjacente est occupée par une machine !");
            return;
        }

        // Supprimer le tapis si présent
        if (existante instanceof Tapis) {
            plateau.setMachine(x2, y2, null);
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

            // Calculer la nouvelle direction
            Direction[] dirs = {Direction.North, Direction.East, Direction.South, Direction.West};
            Direction cur = balancer.getDirection();
            int idx = 0;
            for (int i = 0; i < dirs.length; i++) {
                if (dirs[i] == cur) { idx = i; break; }
            }
            Direction newDir = dirs[(idx + 1) % 4];
            Direction newSide = newDir.rotate90CW();
            int nx = x + newSide.getDx();
            int ny = y + newSide.getDy();

            // Vérifier si la nouvelle case secondaire est dans la grille
            if (nx < 0 || nx >= Plateau.SIZE_X || ny < 0 || ny >= Plateau.SIZE_Y) {
                System.out.println("Action impossible : la nouvelle position du balancer est en dehors de la grille !");
                return;
            }

            // Vérifier si la nouvelle case secondaire est libre ou contient un tapis
            Machine occupante = plateau.getCases()[nx][ny].getMachine();
            if (occupante != null && !(occupante instanceof Tapis)) {
                System.out.println("Action impossible : la nouvelle position du balancer est occupée par une machine !");
                return;
            }

            // Si c'est un tapis, on le supprime
            if (occupante instanceof Tapis) {
                plateau.setMachine(nx, ny, null);
            }

            // Clear old secondary cell
            int[] secPos = findBalancerSecondaire(balancer);
            if (secPos != null) {
                plateau.getCases()[secPos[0]][secPos[1]].setMachine(null);
            }

            // Rotate
            balancer.setDirection(newDir);

            // Place new secondary
            BalancerSecondaire secondaire = new BalancerSecondaire(balancer);
            plateau.setMachine(nx, ny, secondaire);
            balancer.secondaryCase = plateau.getCases()[nx][ny];

            plateau.forceRefresh();
            return;
        }

        // Normal rotation for other machines
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

    // ==================== SAUVEGARDE / CHARGEMENT ====================

    public void sauvegarder() {
        JFileChooser fileChooser = new JFileChooser(".");
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Sauvegarder l'état du hub
                writer.write("hub:" + Livraison.getObjectifCourant() + "," + Livraison.getQuantiteRecue());
                writer.newLine();

                // Sauvegarder toutes les machines
                for (int x = 0; x < Plateau.SIZE_X; x++) {
                    for (int y = 0; y < Plateau.SIZE_Y; y++) {
                        Machine m = plateau.getCases()[x][y].getMachine();
                        if (m != null && !(m instanceof Livraison)) {
                            String type = getMachineType(m);
                            if (type != null) {
                                writer.write(x + "," + y + "," + type + "," + m.getDirection());
                                writer.newLine();
                            }
                        }
                    }
                }
                JOptionPane.showMessageDialog(null, "Partie sauvegardée !");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erreur lors de la sauvegarde : " + e.getMessage());
            }
        }
    }

    public void charger() {
        JFileChooser fileChooser = new JFileChooser(".");
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Réinitialiser
                Livraison.reset();
                plateau = new Plateau();

                String line;
                int hubObjectif = 0;
                int hubQuantite = 0;
                boolean hubDataFound = false;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("hub:")) {
                        String[] hubData = line.substring(4).split(",");
                        if (hubData.length >= 2) {
                            hubObjectif = Integer.parseInt(hubData[0]);
                            hubQuantite = Integer.parseInt(hubData[1]);
                            hubDataFound = true;
                        }
                    } else {
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            String type = parts[2];
                            Direction dir = Direction.valueOf(parts[3]);

                            Machine m = createMachineFromType(type, dir);
                            if (m != null) {
                                plateau.setMachine(x, y, m);
                            }
                        }
                    }
                }

                // Restaurer l'état du hub
                if (hubDataFound) {
                    Livraison.setEtat(hubObjectif, hubQuantite);
                }

                // Recréer le hub (zone 3x3)
                ItemShape[] objectifsFormes = {
                        new ItemShape("C-C-C-C-"),
                        new ItemShape("c-c-----"),
                        new ItemShape("--S-S---"),
                        new ItemShape("CgCg----")
                };
                int[] objectifsQuantites = {3, 3, 3, 3};
                Livraison hubCentral = new Livraison(objectifsFormes, objectifsQuantites);

                int centerX = Plateau.SIZE_X / 2;
                int centerY = Plateau.SIZE_Y / 2;
                for (int x = centerX - 1; x <= centerX + 1; x++) {
                    for (int y = centerY - 1; y <= centerY + 1; y++) {
                        if (plateau.getCases()[x][y].getMachine() == null) {
                            plateau.setMachine(x, y, hubCentral);
                        }
                    }
                }

                plateau.forceRefresh();
                JOptionPane.showMessageDialog(null, "Partie chargée !");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erreur lors du chargement : " + e.getMessage());
            }
        }
    }

    private String getMachineType(Machine m) {
        if (m instanceof Mine) return "Mine";
        if (m instanceof Tapis) return "Tapis";
        if (m instanceof Cutter) return "Cutter";
        if (m instanceof Rotator) return "Rotator";
        if (m instanceof AtelierPeinture) return "Painter";
        if (m instanceof Stacker) return "Stacker";
        if (m instanceof Poubelle) return "Poubelle";
        if (m instanceof Balancer) return "Balancer";
        return null;
    }

    private Machine createMachineFromType(String type, Direction dir) {
        switch (type) {
            case "Mine":
                Mine mine = new Mine();
                mine.setDirection(dir);
                return mine;
            case "Tapis":
                return new Tapis(dir);
            case "Cutter":
                Cutter cutter = new Cutter();
                cutter.setDirection(dir);
                return cutter;
            case "Rotator":
                Rotator rotator = new Rotator();
                rotator.setDirection(dir);
                return rotator;
            case "Painter":
                AtelierPeinture painter = new AtelierPeinture();
                painter.setDirection(dir);
                return painter;
            case "Stacker":
                Stacker stacker = new Stacker();
                stacker.setDirection(dir);
                return stacker;
            case "Poubelle":
                return new Poubelle();
            case "Balancer":
                return new Balancer();
            default:
                return null;
        }
    }

    public void run() {
        jouerPartie();
    }

    public void jouerPartie() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                plateau.run();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}