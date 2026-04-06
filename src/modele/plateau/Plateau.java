package modele.plateau;

import java.util.HashMap;
import java.util.Observable;
import modele.item.ItemShape;

public class Plateau extends Observable implements Runnable {

    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 16;

    private HashMap<Case, Point> map = new HashMap<Case, Point>();
    private Case[][] grilleCases = new Case[SIZE_X][SIZE_Y];

    public Plateau() {
        initPlateauVide();
        initGisements();
    }

    private void initGisements() {
        // Gisements existants
        grilleCases[5][10].setGisement(new ItemShape("CrCb--Cb"));
        grilleCases[7][7].setGisement(new ItemShape("CbCr--Cr"));
        grilleCases[3][4].setGisement(new ItemShape("CrCb--Cb"));

        // NOUVEAUX GISEMENTS
        grilleCases[7][12].setGisement(new ItemShape("CrCb--Cb"));
        grilleCases[8][12].setGisement(new ItemShape("CrCb--Cb"));

        // Gisements de couleur dans les coins (zones 2x2)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                // Coin haut-gauche : Rouge (Cercles rouges)
                grilleCases[i][j].setGisement(new ItemShape("crcrcrcr"));

                // Coin haut-droite : Vert (Cercles verts)
                grilleCases[SIZE_X - 1 - i][j].setGisement(new ItemShape("cgcgcgcg"));

                // Coin bas-gauche : Bleu (Cercles bleus)
                grilleCases[i][SIZE_Y - 1 - j].setGisement(new ItemShape("cbcbcbcb"));

                // Coin bas-droite : Jaune (Cercles jaunes)
                grilleCases[SIZE_X - 1 - i][SIZE_Y - 1 - j].setGisement(new ItemShape("cycycycy"));
            }
        }
    }

    public Case[][] getCases() {
        return grilleCases;
    }

    public Case getCase(Case source, Direction d) {
        Point p = map.get(source);
        return caseALaPosition(new Point(p.x + d.dx, p.y + d.dy));
    }

    private void initPlateauVide() {
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                grilleCases[x][y] = new Case(this);
                map.put(grilleCases[x][y], new Point(x, y));
            }
        }
    }

    public void setMachine(int x, int y, Machine m) {
        grilleCases[x][y].setMachine(m);
        setChanged();
        notifyObservers();
    }

    public void forceRefresh() {
        setChanged();
        notifyObservers();
    }

    private boolean contenuDansGrille(Point p) {
        return p.x >= 0 && p.x < SIZE_X && p.y >= 0 && p.y < SIZE_Y;
    }

    private Case caseALaPosition(Point p) {
        Case retour = null;
        if (contenuDansGrille(p)) {
            retour = grilleCases[p.x][p.y];
        }
        return retour;
    }

    @Override
    public void run() {
        // Reset tick flags so items that arrive this tick can't be forwarded again in the same tick
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                Case c = grilleCases[x][y];
                if (c.getMachine() != null) {
                    c.getMachine().resetTickFlag();
                }
            }
        }
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                Case c = grilleCases[x][y];
                if (c.getMachine() != null) {
                    c.getMachine().run();
                }
            }
        }
        setChanged();
        notifyObservers();
    }
}