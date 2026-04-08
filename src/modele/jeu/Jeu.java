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
import modele.item.Item;
import modele.item.ItemShape;

/**
 * Classe principale du jeu. Gère le plateau, les machines, les objectifs,
 * la sauvegarde/chargement et le thread de simulation.
 */
public class Jeu extends Thread {
    private Plateau plateau; // Le plateau de jeu (grille de cases)

    /**
     * Constructeur : initialise le plateau, les objectifs et le hub.
     */
    public Jeu() {
        plateau = new Plateau();

        /**
         * OBJECTIFS DU JEU
         * Chaque objectif est une forme spécifique à livrer
         */
        ItemShape[] objectifsFormes = {
                new ItemShape("C-C-C-C-"),      // Objectif 1: Carré plein (bord haut)
                new ItemShape("c-c-----"),      // Objectif 2: Partie droite d'un rond (après Cutter)
                new ItemShape("--S-S---"),      // Objectif 3: Partie basse d'une étoile (après Cutter + Rotator)
                new ItemShape("CgCg----")       // Objectif 4: Partie droite d'un carré vert (Cutter + Painter)
        };

        // Quantités requises pour chaque objectif
        int[] objectifsQuantites = {3, 3, 3, 3};

        // Création du hub (zone de livraison)
        Livraison hubCentral = new Livraison(objectifsFormes, objectifsQuantites);

        // Placement du Hub au centre de la grille (zone 3x3)
        int centerX = Plateau.SIZE_X / 2;
        int centerY = Plateau.SIZE_Y / 2;
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY - 1; y <= centerY + 1; y++) {
                plateau.setMachine(x, y, hubCentral);
            }
        }

        start(); // Démarre le thread de simulation
    }

    /**
     * Retourne le plateau de jeu.
     */
    public Plateau getPlateau() {
        return plateau;
    }

    /**
     * Vérifie si une case est bloquée (ne peut pas être remplacée).
     * @return true si la case ne peut pas être construite
     */
    private boolean cellBloquee(int x, int y, String type) {
        Machine existante = plateau.getCases()[x][y].getMachine();

        // Le Hub ne peut jamais être remplacé
        if (existante instanceof Livraison) {
            System.out.println("Action impossible : Le Hub ne peut pas être remplacé.");
            return true;
        }
        // Une mine ne peut pas être remplacée (il faut la supprimer d'abord)
        if (existante instanceof Mine) {
            System.out.println("Action impossible : Une mine ne peut pas être remplacée. Supprimez-la d'abord (clic droit).");
            return true;
        }
        // Les machines spéciales ne peuvent pas être remplacées
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

    /**
     * Place une machine sur le plateau.
     * @param x coordonnée X
     * @param y coordonnée Y
     * @param type type de machine ("Mine", "Tapis", "Cutter", etc.)
     */
    public void placerMachine(int x, int y, String type) {
        if (cellBloquee(x, y, type)) return;

        // Vérifier si la case a un gisement
        if (plateau.getCases()[x][y].getGisement() != null) {
            // Seule une mine peut être placée sur un gisement
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

    /**
     * Place un Balancer (machine sur 2 cases).
     * Le Balancer occupe la case principale et la case à l'Est.
     */
    private void placerBalancer(int x, int y) {
        Direction sideDir = Direction.North.rotate90CW(); // = East
        int x2 = x + sideDir.getDx();
        int y2 = y + sideDir.getDy();

        // Vérifications de placement
        if (x2 < 0 || x2 >= Plateau.SIZE_X || y2 < 0 || y2 >= Plateau.SIZE_Y) {
            System.out.println("Impossible : pas assez de place pour le balancer !");
            return;
        }
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
        if (existante instanceof Tapis) {
            plateau.setMachine(x2, y2, null);
        }

        // Création des deux parties du Balancer
        Balancer balancer = new Balancer();
        BalancerSecondaire secondaire = new BalancerSecondaire(balancer);
        plateau.setMachine(x, y, balancer);
        plateau.setMachine(x2, y2, secondaire);
        balancer.secondaryCase = plateau.getCases()[x2][y2];
    }

    /**
     * Place une machine avec une direction spécifique (pour les tapis).
     */
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

    /**
     * Place un tapis en coin (avec direction entrante et sortante).
     */
    public void placerTapisCorner(int x, int y, Direction incoming, Direction outgoing) {
        if (cellBloquee(x, y, "Tapis")) return;

        if (plateau.getCases()[x][y].getGisement() != null) {
            System.out.println("Impossible : seule une mine peut être placée sur un gisement !");
            return;
        }

        plateau.setMachine(x, y, new Tapis(incoming, outgoing));
    }

    /**
     * Rotation d'une mine (appelle rotateMachine).
     */
    public void rotateMine(int x, int y) {
        rotateMachine(x, y);
    }

    /**
     * Rotation d'une machine (change sa direction).
     * Gère spécialement le Balancer (2 cases).
     */
    public void rotateMachine(int x, int y) {
        Machine m = plateau.getCases()[x][y].getMachine();
        if (m == null) return;

        // Si on clique sur la case secondaire du Balancer, on redirige vers la case principale
        if (m instanceof BalancerSecondaire) {
            Balancer owner = ((BalancerSecondaire) m).owner;
            int[] pos = findMachinePosition(owner);
            if (pos != null) rotateMachine(pos[0], pos[1]);
            return;
        }

        // Rotation spéciale pour le Balancer (déplace la case secondaire)
        if (m instanceof Balancer) {
            Balancer balancer = (Balancer) m;

            // Calcul de la nouvelle direction
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

            // Vérifications
            if (nx < 0 || nx >= Plateau.SIZE_X || ny < 0 || ny >= Plateau.SIZE_Y) {
                System.out.println("Action impossible : la nouvelle position du balancer est en dehors de la grille !");
                return;
            }

            Machine occupante = plateau.getCases()[nx][ny].getMachine();
            if (occupante != null && !(occupante instanceof Tapis)) {
                System.out.println("Action impossible : la nouvelle position du balancer est occupée par une machine !");
                return;
            }
            if (occupante instanceof Tapis) {
                plateau.setMachine(nx, ny, null);
            }

            // Suppression de l'ancienne case secondaire
            int[] secPos = findBalancerSecondaire(balancer);
            if (secPos != null) {
                plateau.getCases()[secPos[0]][secPos[1]].setMachine(null);
            }

            // Rotation et placement de la nouvelle case secondaire
            balancer.setDirection(newDir);
            BalancerSecondaire secondaire = new BalancerSecondaire(balancer);
            plateau.setMachine(nx, ny, secondaire);
            balancer.secondaryCase = plateau.getCases()[nx][ny];

            plateau.forceRefresh();
            return;
        }

        // Rotation normale pour les autres machines
        Direction[] dirs = {Direction.North, Direction.East, Direction.South, Direction.West};
        Direction cur = m.getDirection();
        int idx = 0;
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i] == cur) { idx = i; break; }
        }
        m.setDirection(dirs[(idx + 1) % 4]);
        plateau.forceRefresh();
    }

    /**
     * Supprime une machine du plateau (clic droit).
     */
    public void supprimerMachine(int x, int y) {
        Case c = plateau.getCases()[x][y];
        Machine m = c.getMachine();

        if (m != null) {
            // Le Hub ne peut pas être supprimé
            if (m instanceof Livraison) {
                System.out.println("Action impossible : Le Hub ne peut pas être supprimé.");
                return;
            }

            // Suppression d'un Balancer : supprime aussi la case secondaire
            if (m instanceof Balancer) {
                int[] secPos = findBalancerSecondaire((Balancer) m);
                if (secPos != null) {
                    plateau.getCases()[secPos[0]][secPos[1]].getMachine().clearCurrent();
                    plateau.getCases()[secPos[0]][secPos[1]].setMachine(null);
                }
            }

            // Suppression de la case secondaire : supprime aussi la case principale
            if (m instanceof BalancerSecondaire) {
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

    /**
     * Trouve la position d'une machine sur le plateau.
     */
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

    /**
     * Trouve la position de la case secondaire d'un Balancer.
     */
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

    //  SAUVEGARDE / CHARGEMENT

    /**
     * Récupère la chaîne représentant l'item sur une machine (pour sauvegarde).
     */
    private String getItemsString(Machine m) {
        StringBuilder sb = new StringBuilder();
        Item current = m.getCurrent();
        if (current instanceof ItemShape) {
            sb.append(((ItemShape) current).toString());
        }
        return sb.toString();
    }

    /**
     * Restaure l'item sur une machine après chargement.
     */
    private void restoreItems(Machine m, String itemsStr) {
        if (itemsStr != null && !itemsStr.isEmpty() && m instanceof Tapis) {
            ItemShape item = new ItemShape(itemsStr);
            m.receive(item, m.getDirection());
        }
    }

    /**
     * Sauvegarde la partie dans un fichier.
     */
    public void sauvegarder() {
        JFileChooser fileChooser = new JFileChooser(".");
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Sauvegarde de l'état du hub (objectif courant, quantité reçue)
                writer.write("hub:" + Livraison.getObjectifCourant() + "," + Livraison.getQuantiteRecue());
                writer.newLine();

                // Sauvegarde de toutes les machines (sauf le hub)
                for (int x = 0; x < Plateau.SIZE_X; x++) {
                    for (int y = 0; y < Plateau.SIZE_Y; y++) {
                        Machine m = plateau.getCases()[x][y].getMachine();
                        if (m != null && !(m instanceof Livraison)) {
                            String type = getMachineType(m);
                            if (type != null) {
                                String items = getItemsString(m);
                                writer.write(x + "," + y + "," + type + "," + m.getDirection() + "," + items);
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

    /**
     * Charge une partie depuis un fichier.
     */
    public void charger() {
        JFileChooser fileChooser = new JFileChooser(".");
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Réinitialisation
                Livraison.reset();
                plateau = new Plateau();

                String line;
                int hubObjectif = 0;
                int hubQuantite = 0;
                boolean hubDataFound = false;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("hub:")) {
                        // Lecture de l'état du hub
                        String[] hubData = line.substring(4).split(",");
                        if (hubData.length >= 2) {
                            hubObjectif = Integer.parseInt(hubData[0]);
                            hubQuantite = Integer.parseInt(hubData[1]);
                            hubDataFound = true;
                        }
                    } else {
                        // Lecture d'une machine
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            String type = parts[2];
                            Direction dir = Direction.valueOf(parts[3]);
                            String itemsStr = parts.length > 4 ? parts[4] : "";

                            Machine m = createMachineFromType(type, dir);
                            if (m != null) {
                                plateau.setMachine(x, y, m);
                                restoreItems(m, itemsStr);
                            }
                        }
                    }
                }

                // Restauration de l'état du hub
                if (hubDataFound) {
                    Livraison.setEtat(hubObjectif, hubQuantite);
                }

                // Recréation du hub (zone 3x3)
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

    /**
     * Retourne le type d'une machine sous forme de chaîne.
     */
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

    /**
     * Crée une machine à partir de son type et de sa direction.
     */
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

    /**
     * Thread principal : lance la simulation.
     */
    @Override
    public void run() {
        jouerPartie();
    }

    /**
     * Boucle principale de simulation.
     * À chaque seconde, le plateau est mis à jour.
     */
    public void jouerPartie() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                plateau.run();      // Exécute toutes les machines
                Thread.sleep(1000); // Attend 1 seconde
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}