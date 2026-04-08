package modele.plateau;

import java.util.HashMap;
import java.util.Observable;
import modele.item.ItemShape;

/**
 * Plateau de jeu : grille de cases (30x30).
 *
 * Gère :
 * - Les gisements (couleurs dans les coins, formes sur les bords)
 * - Le placement des machines
 * - La simulation (exécution de toutes les machines à chaque tick)
 *
 * Extends Observable pour notifier la vue (VueControleur) des changements.
 */
public class Plateau extends Observable implements Runnable {

    // Taille de la grille (30x30 pour plus d'espace)
    public static final int SIZE_X = 30;
    public static final int SIZE_Y = 30;

    // Map pour retrouver la position (Point) d'une Case à partir de sa référence
    private HashMap<Case, Point> map = new HashMap<Case, Point>();
    private Case[][] grilleCases = new Case[SIZE_X][SIZE_Y];

    /**
     * Constructeur : initialise la grille vide puis ajoute les gisements.
     */
    public Plateau() {
        initPlateauVide();
        initGisements();
    }

    /**
     * Initialise tous les gisements sur le plateau.
     *
     * - Coins (2x2) : gisements de couleur (rouge, vert, bleu, jaune)
     * - Bords (3x3) : gisements de forme (carrés en haut, étoiles en bas, cercles à gauche)
     */
    private void initGisements() {
        // GISEMENTS DE COULEUR DANS LES 4 COINS (zones 2x2)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                // Rouge en haut-gauche (cercle rouge)
                ItemShape red    = new ItemShape("crcrcrcr"); red.setColorItem(true);
                // Vert en haut-droite (cercle vert)
                ItemShape green  = new ItemShape("cgcgcgcg"); green.setColorItem(true);
                // Bleu en bas-gauche (cercle bleu)
                ItemShape blue   = new ItemShape("cbcbcbcb"); blue.setColorItem(true);
                // Jaune en bas-droite (cercle jaune)
                ItemShape yellow = new ItemShape("cycycycy"); yellow.setColorItem(true);

                grilleCases[i][j].setGisement(red);                               // Coin haut-gauche
                grilleCases[SIZE_X - 1 - i][j].setGisement(green);               // Coin haut-droite
                grilleCases[i][SIZE_Y - 1 - j].setGisement(blue);                // Coin bas-gauche
                grilleCases[SIZE_X - 1 - i][SIZE_Y - 1 - j].setGisement(yellow); // Coin bas-droite
            }
        }

        //  GISEMENTS DE FORMES AU MILIEU DES BORDS (zones 3x3)
        int midX = SIZE_X / 2 - 1; // Centre X (14)
        int midY = SIZE_Y / 2 - 1; // Centre Y (14)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // Bord haut (Nord) : Carrés gris
                grilleCases[midX + i][j].setGisement(new ItemShape("C-C-C-C-"));

                // Bord bas (Sud) : Étoiles grises
                grilleCases[midX + i][SIZE_Y - 1 - j].setGisement(new ItemShape("S-S-S-S-"));

                // Bord gauche (Ouest) : Cercles gris
                grilleCases[i][midY + j].setGisement(new ItemShape("c-c-c-c-"));
            }
        }
    }

    /**
     * Retourne la grille complète des cases.
     */
    public Case[][] getCases() {
        return grilleCases;
    }

    /**
     * Retourne la case adjacente à une case source dans une direction donnée.
     */
    public Case getCase(Case source, Direction d) {
        Point p = map.get(source);
        return caseALaPosition(new Point(p.x + d.dx, p.y + d.dy));
    }

    /**
     * Initialise une grille vide (sans machines, sans gisements).
     */
    private void initPlateauVide() {
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                grilleCases[x][y] = new Case(this);
                map.put(grilleCases[x][y], new Point(x, y));
            }
        }
    }

    /**
     * Place une machine sur une case et notifie la vue.
     */
    public void setMachine(int x, int y, Machine m) {
        grilleCases[x][y].setMachine(m);
        setChanged();
        notifyObservers();
    }

    /**
     * Force un rafraîchissement de la vue.
     */
    public void forceRefresh() {
        setChanged();
        notifyObservers();
    }

    /**
     * Vérifie si un point est dans les limites de la grille.
     */
    private boolean contenuDansGrille(Point p) {
        return p.x >= 0 && p.x < SIZE_X && p.y >= 0 && p.y < SIZE_Y;
    }

    /**
     * Retourne la case à une position donnée (ou null si hors limites).
     */
    private Case caseALaPosition(Point p) {
        Case retour = null;
        if (contenuDansGrille(p)) {
            retour = grilleCases[p.x][p.y];
        }
        return retour;
    }

    /**
     * Simulation d'un tick (1 seconde).
     *
     * 1. Réinitialise les flags movedThisTick de toutes les machines
     * 2. Exécute toutes les machines (work() + send())
     * 3. Notifie la vue pour rafraîchir l'affichage
     */
    @Override
    public void run() {
        // Étape 1 : reset des flags pour éviter les envois multiples par tick
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                Case c = grilleCases[x][y];
                if (c.getMachine() != null) {
                    c.getMachine().resetTickFlag();
                }
            }
        }

        // Étape 2 : exécution de toutes les machines
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                Case c = grilleCases[x][y];
                if (c.getMachine() != null) {
                    c.getMachine().run();
                }
            }
        }

        // Étape 3 : notification de la vue
        setChanged();
        notifyObservers();
    }
}