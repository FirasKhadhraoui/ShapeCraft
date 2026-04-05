package vuecontroleur;

import modele.item.ItemShape;
import modele.item.SubShape;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private Image imgBackground;
    private Image imgFront;
    private ItemShape shape;

    public void setShape(ItemShape _shape) {
        this.shape = _shape;
        repaint();
    }

    public void setImageBackground(Image _imgBackground) {
        this.imgBackground = _imgBackground;
        repaint();
    }

    public void setFront(Image _imgFront) {
        this.imgFront = _imgFront;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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

        g.drawRoundRect(bordure, bordure, widthBack, heigthBack, bordure, bordure);

        if (imgBackground != null) {
            g.drawImage(imgBackground, xBack, yBack, widthBack, heigthBack, this);
        }

        if (imgFront != null) {
            g.drawImage(imgFront, xFront, yFront, widthFront, heigthFront, this);
        }

        if (shape != null) {
            SubShape[] tabS = shape.getSubShapes(ItemShape.Layer.one);
            modele.item.Color[] tabC = shape.getColors(ItemShape.Layer.one);

            // Vérifier si la forme a 4 sous-formes (complète) ou 2 (coupée)
            int nbFormes = tabS.length;

            if (nbFormes == 4) {
                // Affichage normal 2x2
                for (int i = 0; i < 4; i++) {
                    dessinerSousForme(g, tabS[i], tabC[i], i, xFront, yFront, widthFront, heigthFront);
                }
            } else if (nbFormes == 2) {
                // Affichage pour forme coupée (2 sous-formes empilées verticalement)
                for (int i = 0; i < 2; i++) {
                    dessinerSousFormeCoupe(g, tabS[i], tabC[i], i, xFront, yFront, widthFront, heigthFront);
                }
            }
        }
    }

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

    private void setColor(Graphics g, modele.item.Color color) {
        if (color == null) {
            g.setColor(Color.GRAY);
        } else {
            switch (color) {
                case Red: g.setColor(Color.RED); break;
                case Green: g.setColor(Color.GREEN); break;
                case Blue: g.setColor(Color.BLUE); break;
                case Yellow: g.setColor(Color.YELLOW); break;
                case Purple: g.setColor(Color.MAGENTA); break;
                case Cyan: g.setColor(Color.CYAN); break;
                case White: g.setColor(Color.WHITE); break;
                default: g.setColor(Color.GRAY);
            }
        }
    }

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