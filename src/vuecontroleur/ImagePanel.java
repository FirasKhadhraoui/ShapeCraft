package vuecontroleur;

import modele.item.ItemShape;
import modele.item.SubShape;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panneau graphique qui affiche une case du plateau.
 *
 * Peut afficher :
 * - Une image de fond (machine, gisement)
 * - Une image de premier plan (item transporté)
 * - Une forme (dessinée directement si pas d'image)
 *
 * Gère également :
 * - La rotation de l'image de fond
 * - La teinte (tint) de l'image de premier plan
 * - Le clipping pour les formes coupées (moitié gauche/droite/haut/bas)
 */
public class ImagePanel extends JPanel {

    // Types de découpe pour l'affichage des formes coupées
    public enum CutHalf { NONE, LEFT, RIGHT, TOP, BOTTOM }
    // Types de découpe pour l'image de fond (ex: balancer)
    public enum BackgroundHalf { NONE, LEFT, RIGHT }

    private Image imgBackground;           // Image de fond (machine, gisement)
    private Image imgFront;                // Image de premier plan (item)
    private Color frontTint = null;        // Teinte à appliquer à l'image de premier plan
    private ItemShape shape;               // Forme à dessiner (si pas d'image)
    private double backgroundRotation = 0; // Rotation de l'image de fond (radians)
    private CutHalf cutHalf = CutHalf.NONE;        // Découpe pour l'image de premier plan
    private BackgroundHalf backgroundHalf = BackgroundHalf.NONE; // Découpe pour le fond

    // Setters avec repaint() automatique
    public void setBackgroundHalf(BackgroundHalf bh) { this.backgroundHalf = bh; repaint(); }
    public void setFrontTint(Color tint) { this.frontTint = tint; repaint(); }
    public void setCutHalf(CutHalf ch) { this.cutHalf = ch; repaint(); }
    public void setShape(ItemShape _shape) { this.shape = _shape; repaint(); }
    public void setImageBackground(Image _imgBackground) { this.imgBackground = _imgBackground; repaint(); }
    public void setBackgroundRotation(double radians) { this.backgroundRotation = radians; repaint(); }
    public void setFront(Image _imgFront) { this.imgFront = _imgFront; repaint(); }

    /**
     * Dessine le panneau.
     * Ordre de dessin :
     * 1. Fond (avec rotation et clipping éventuels)
     * 2. Image de premier plan (avec teinte et clipping)
     * 3. Forme (dessinée directement si shape != null)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dimensions et positions
        final int bordure = 1;
        final int xBack = bordure;
        final int yBack = bordure;
        final int widthBack = getWidth() - bordure * 2;
        final int heigthBack = getHeight() - bordure * 2;

        final int subPartWidth = widthBack / 4;
        final int subPartHeigth = heigthBack / 4;

        final int xFront = bordure + subPartWidth;
        final int yFront = bordure + subPartHeigth;
        final int widthFront = subPartWidth * 2;
        final int heigthFront = subPartHeigth * 2;

        // Cadre autour de la case
        g.drawRoundRect(bordure, bordure, widthBack, heigthBack, bordure, bordure);

        // 1. DESSIN DU FOND
        if (imgBackground != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            // Clipping (pour les balancers)
            if (backgroundHalf == BackgroundHalf.LEFT) {
                g2d.clipRect(xBack, yBack, widthBack / 2, heigthBack);
            } else if (backgroundHalf == BackgroundHalf.RIGHT) {
                g2d.clipRect(xBack + widthBack / 2, yBack, widthBack / 2, heigthBack);
            }
            // Rotation
            if (backgroundRotation != 0) {
                double cx = xBack + widthBack / 2.0;
                double cy = yBack + heigthBack / 2.0;
                g2d.rotate(backgroundRotation, cx, cy);
            }
            g2d.drawImage(imgBackground, xBack, yBack, widthBack, heigthBack, this);
            g2d.dispose();
        }

        // 2. DESSIN DE L'IMAGE DE PREMIER PLAN
        if (imgFront != null) {
            Image drawImg = imgFront;
            // Application de la teinte (tint)
            if (frontTint != null) {
                BufferedImage tinted = new BufferedImage(widthFront, heigthFront, BufferedImage.TYPE_INT_ARGB);
                Graphics2D tg = tinted.createGraphics();
                tg.drawImage(imgFront, 0, 0, widthFront, heigthFront, null);
                tg.setComposite(AlphaComposite.SrcIn);
                tg.setColor(frontTint);
                tg.fillRect(0, 0, widthFront, heigthFront);
                tg.dispose();
                drawImg = tinted;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            // Clipping pour les formes coupées
            if (cutHalf == CutHalf.LEFT) {
                g2d.clipRect(xFront, yFront, widthFront / 2, heigthFront);
            } else if (cutHalf == CutHalf.RIGHT) {
                g2d.clipRect(xFront + widthFront / 2, yFront, widthFront / 2, heigthFront);
            } else if (cutHalf == CutHalf.TOP) {
                g2d.clipRect(xFront, yFront, widthFront, heigthFront / 2);
            } else if (cutHalf == CutHalf.BOTTOM) {
                g2d.clipRect(xFront, yFront + heigthFront / 2, widthFront, heigthFront / 2);
            }
            g2d.drawImage(drawImg, xFront, yFront, widthFront, heigthFront, this);
            g2d.dispose();
        }

        // 3. DESSIN DE LA FORME (SI PAS D'IMAGE) 
        if (shape != null) {
            SubShape[] tabS = shape.getSubShapes();
            modele.item.Color[] tabC = shape.getColors();

            int nbFormes = tabS.length;

            if (nbFormes == 4) {
                // Forme complète : dessin 2x2
                for (int i = 0; i < 4; i++) {
                    dessinerSousForme(g, tabS[i], tabC[i], i, xFront, yFront, widthFront, heigthFront);
                }
            } else if (nbFormes == 2) {
                // Forme coupée : 2 blocs empilés verticalement
                for (int i = 0; i < 2; i++) {
                    dessinerSousFormeCoupe(g, tabS[i], tabC[i], i, xFront, yFront, widthFront, heigthFront);
                }
            }
        }
    }

    // Dessine une sous-forme dans une grille 2x2 (index 0 à 3)
    private void dessinerSousForme(Graphics g, SubShape ss, modele.item.Color color, int index,
                                   int xFront, int yFront, int widthFront, int heigthFront) {
        if (ss != SubShape.None) {
            setColor(g, color);
            int xPos = xFront + (widthFront / 2) * ((index >> 1) ^ 1);
            int yPos = yFront + (heigthFront / 2) * ((index & 1) ^ ((index >> 1) & 1));
            int w = widthFront / 2;
            int h = heigthFront / 2;
            dessinerForme(g, ss, xPos, yPos, w, h);
        }
    }

    // Dessine une sous-forme coupée (2 blocs empilés verticalement)
    private void dessinerSousFormeCoupe(Graphics g, SubShape ss, modele.item.Color color, int index,
                                        int xFront, int yFront, int widthFront, int heigthFront) {
        if (ss != SubShape.None) {
            setColor(g, color);
            int xPos = xFront + widthFront / 4;
            int yPos = yFront + (index * heigthFront / 2);
            int w = widthFront / 2;
            int h = heigthFront / 2;
            dessinerForme(g, ss, xPos, yPos, w, h);
        }
    }

    // Définit la couleur de dessin à partir d'une couleur du modèle
    private void setColor(Graphics g, modele.item.Color color) {
        if (color == null) {
            g.setColor(new Color(64, 64, 64)); // Gris foncé
        } else {
            switch (color) {
                case Red:    g.setColor(Color.RED); break;
                case Green:  g.setColor(Color.GREEN); break;
                case Blue:   g.setColor(Color.BLUE); break;
                case Yellow: g.setColor(Color.YELLOW); break;
                case Purple: g.setColor(Color.MAGENTA); break;
                case Cyan:   g.setColor(Color.CYAN); break;
                case White:  g.setColor(Color.WHITE); break;
                default:     g.setColor(Color.GRAY);
            }
        }
    }

    // Dessine une forme géométrique (carré, cercle, éventail, étoile)
    private void dessinerForme(Graphics g, SubShape ss, int x, int y, int w, int h) {
        switch (ss) {
            case Carre:
                g.fillRect(x, y, w, h);
                break;
            case Circle:
                g.fillOval(x, y, w, h);
                break;
            case Fan:
                g.fillArc(x, y, w, h, 0, 90);
                break;
            case Star:
                // Triangle simple pour représenter une étoile
                int[] xPoints = {x + w/2, x, x + w};
                int[] yPoints = {y, y + h, y + h};
                g.fillPolygon(xPoints, yPoints, 3);
                break;
            default:
                break;
        }
    }
}