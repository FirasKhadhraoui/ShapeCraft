package vuecontroleur;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;

import modele.item.Item;
import modele.item.ItemColor;
import modele.item.ItemShape;
import modele.jeu.Jeu;
import modele.plateau.*;

public class VueControleur extends JFrame implements Observer {
    private Plateau plateau;
    private Jeu jeu;
    private final int sizeX;
    private final int sizeY;
    private int pxCase;

    // Images pour les couleurs des gisements
    private Image icoRed;
    private Image icoGreen;
    private Image icoBlue;
    private Image icoYellow;

    // Images pour les machines
    private Image icoTapis;
    private Image icoPoubelle;
    private Image icoMine;
    private Image icoHub;
    private Image icoCutter;
    private Image icoRotater;
    private Image icoPainter;
    private Image icoStacker;

    // Composants de la boîte à outils
    private JToolBar toolBar;
    private JButton btnMine;
    private JButton btnTapis;
    private JButton btnPoubelle;
    private JButton btnCutter;
    private JButton btnRotater;
    private JButton btnPainter;
    private JButton btnStacker;
    private String machineSelectionnee = "Tapis";

    private JPanel grilleIP;
    private boolean mousePressed = false;
    private ImagePanel[][] tabIP;

    public VueControleur(Jeu _jeu) {
        jeu = _jeu;
        plateau = jeu.getPlateau();
        sizeX = plateau.SIZE_X;
        sizeY = plateau.SIZE_Y;

        calculerTailleAdaptative();

        chargerLesIcones();
        initToolBar();
        placerLesComposantsGraphiques();

        plateau.addObserver(this);
        mettreAJourAffichage();
    }

    private void calculerTailleAdaptative() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxHauteur = (int)(screenSize.height * 0.7);
        int maxLargeur = (int)(screenSize.width * 0.7);
        int maxCaseHauteur = maxHauteur / sizeY;
        int maxCaseLargeur = maxLargeur / sizeX;
        pxCase = Math.min(maxCaseHauteur, maxCaseLargeur);
        pxCase = Math.max(40, Math.min(pxCase, 100));

        System.out.println("Taille d'écran : " + screenSize.width + "x" + screenSize.height);
        System.out.println("Taille des cases : " + pxCase + "px");
        System.out.println("Grille totale : " + (sizeX * pxCase) + "x" + (sizeY * pxCase));
    }

    private void chargerLesIcones() {
        // Images pour les couleurs des gisements
        icoRed = new ImageIcon("./data/sprites/colors/red.png").getImage();
        icoGreen = new ImageIcon("./data/sprites/colors/green.png").getImage();
        icoBlue = new ImageIcon("./data/sprites/colors/blue.png").getImage();
        icoYellow = new ImageIcon("./data/sprites/colors/yellow.png").getImage();

        // Images pour les machines
        icoTapis = new ImageIcon("./data/sprites/buildings/belt_top.png").getImage();
        icoPoubelle = new ImageIcon("./data/sprites/buildings/trash.png").getImage();
        icoMine = new ImageIcon("./data/sprites/buildings/miner.png").getImage();
        icoHub = new ImageIcon("./data/sprites/buildings/hub.png").getImage();
        icoCutter = new ImageIcon("./data/sprites/buildings/cutter.png").getImage();
        icoRotater = new ImageIcon("./data/sprites/buildings/rotater.png").getImage();
        icoPainter = new ImageIcon("./data/sprites/buildings/painter.png").getImage();
        icoStacker = new ImageIcon("./data/sprites/buildings/stacker.png").getImage();
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    private void initToolBar() {
        toolBar = new JToolBar("Boîte à outils", JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setLayout(new GridLayout(0, 1, 5, 5));

        int btnWidth = pxCase + 10;
        int btnHeight = pxCase + 20;

        ImageIcon mineIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/miner.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon tapisIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/belt_top.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon poubelleIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/trash.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon cutterIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/cutter.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon rotaterIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/rotater.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon painterIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/painter.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon stackerIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/stacker.png"), btnWidth - 20, btnHeight - 40);

        btnMine = new JButton("Mine", mineIcon);
        btnTapis = new JButton("Tapis", tapisIcon);
        btnPoubelle = new JButton("Poubelle", poubelleIcon);
        btnCutter = new JButton("Cutter", cutterIcon);
        btnRotater = new JButton("Rotator", rotaterIcon);
        btnPainter = new JButton("Painter", painterIcon);
        btnStacker = new JButton("Stacker", stackerIcon);

        Dimension buttonSize = new Dimension(btnWidth, btnHeight);
        btnMine.setPreferredSize(buttonSize);
        btnMine.setMaximumSize(buttonSize);
        btnMine.setMinimumSize(buttonSize);
        btnTapis.setPreferredSize(buttonSize);
        btnTapis.setMaximumSize(buttonSize);
        btnTapis.setMinimumSize(buttonSize);
        btnPoubelle.setPreferredSize(buttonSize);
        btnPoubelle.setMaximumSize(buttonSize);
        btnPoubelle.setMinimumSize(buttonSize);
        btnCutter.setPreferredSize(buttonSize);
        btnCutter.setMaximumSize(buttonSize);
        btnCutter.setMinimumSize(buttonSize);
        btnRotater.setPreferredSize(buttonSize);
        btnRotater.setMaximumSize(buttonSize);
        btnRotater.setMinimumSize(buttonSize);
        btnPainter.setPreferredSize(buttonSize);
        btnPainter.setMaximumSize(buttonSize);
        btnPainter.setMinimumSize(buttonSize);
        btnStacker.setPreferredSize(buttonSize);
        btnStacker.setMaximumSize(buttonSize);
        btnStacker.setMinimumSize(buttonSize);

        btnMine.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnMine.setHorizontalTextPosition(SwingConstants.CENTER);
        btnTapis.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnTapis.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPoubelle.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnPoubelle.setHorizontalTextPosition(SwingConstants.CENTER);
        btnCutter.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnCutter.setHorizontalTextPosition(SwingConstants.CENTER);
        btnRotater.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnRotater.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPainter.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnPainter.setHorizontalTextPosition(SwingConstants.CENTER);
        btnStacker.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnStacker.setHorizontalTextPosition(SwingConstants.CENTER);

        btnMine.setToolTipText("Mine - à placer sur un gisement");
        btnTapis.setToolTipText("Tapis - transporte les items vers le haut");
        btnPoubelle.setToolTipText("Poubelle - détruit les items");
        btnCutter.setToolTipText("Cutter - découpe les formes");
        btnRotater.setToolTipText("Rotator - fait pivoter les formes");
        btnPainter.setToolTipText("Painter - colore les formes");
        btnStacker.setToolTipText("Stacker - empile les formes");

        btnMine.addActionListener(e -> {
            machineSelectionnee = "Mine";
            System.out.println("Machine sélectionnée : Mine");
        });
        btnTapis.addActionListener(e -> {
            machineSelectionnee = "Tapis";
            System.out.println("Machine sélectionnée : Tapis");
        });
        btnPoubelle.addActionListener(e -> {
            machineSelectionnee = "Poubelle";
            System.out.println("Machine sélectionnée : Poubelle");
        });
        btnCutter.addActionListener(e -> {
            machineSelectionnee = "Cutter";
            System.out.println("Machine sélectionnée : Cutter");
        });
        btnRotater.addActionListener(e -> {
            machineSelectionnee = "Rotater";
            System.out.println("Machine sélectionnée : Rotater");
        });
        btnPainter.addActionListener(e -> {
            machineSelectionnee = "Painter";
            System.out.println("Machine sélectionnée : Painter");
        });
        btnStacker.addActionListener(e -> {
            machineSelectionnee = "Stacker";
            System.out.println("Machine sélectionnée : Stacker");
        });

        toolBar.add(btnMine);
        toolBar.add(btnTapis);
        toolBar.add(btnPoubelle);
        toolBar.add(btnCutter);
        toolBar.add(btnRotater);
        toolBar.add(btnPainter);
        toolBar.add(btnStacker);

        toolBar.add(Box.createVerticalGlue());
    }

    private void placerLesComposantsGraphiques() {
        setTitle("ShapeCraft");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        grilleIP = new JPanel(new GridLayout(sizeY, sizeX));
        tabIP = new ImagePanel[sizeX][sizeY];

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                ImagePanel iP = new ImagePanel();
                iP.setPreferredSize(new Dimension(pxCase, pxCase));
                iP.setMinimumSize(new Dimension(pxCase, pxCase));
                iP.setMaximumSize(new Dimension(pxCase, pxCase));
                tabIP[x][y] = iP;

                final int xx = x;
                final int yy = y;

                iP.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mousePressed = false;
                        if (SwingUtilities.isRightMouseButton(e)) {
                            jeu.supprimerMachine(xx, yy);
                            System.out.println("Supprimer machine à " + xx + "-" + yy);
                        } else {
                            jeu.placerMachine(xx, yy, machineSelectionnee);
                            System.out.println("Placer " + machineSelectionnee + " à " + xx + "-" + yy);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (mousePressed) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                jeu.supprimerMachine(xx, yy);
                            } else {
                                jeu.placerMachine(xx, yy, machineSelectionnee);
                            }
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        mousePressed = true;
                        if (SwingUtilities.isRightMouseButton(e)) {
                            jeu.supprimerMachine(xx, yy);
                        } else {
                            jeu.placerMachine(xx, yy, machineSelectionnee);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        mousePressed = false;
                    }
                });

                grilleIP.add(iP);
            }
        }

        JScrollPane scrollPane = new JScrollPane(grilleIP);
        scrollPane.setPreferredSize(new Dimension(sizeX * pxCase + 20, sizeY * pxCase + 20));

        mainPanel.add(toolBar, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void mettreAJourAffichage() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                tabIP[x][y].setImageBackground(null);
                tabIP[x][y].setFront(null);

                // Affichage des couleurs aux 4 coins de la grille (2x2)
                if (x < 2 && y < 2) {
                    tabIP[x][y].setImageBackground(icoRed);
                } else if (x >= sizeX - 2 && y < 2) {
                    tabIP[x][y].setImageBackground(icoGreen);
                } else if (x < 2 && y >= sizeY - 2) {
                    tabIP[x][y].setImageBackground(icoBlue);
                } else if (x >= sizeX - 2 && y >= sizeY - 2) {
                    tabIP[x][y].setImageBackground(icoYellow);
                }

                Case c = plateau.getCases()[x][y];
                Machine m = c.getMachine();

                if (m != null) {
                    if (m instanceof Tapis) {
                        tabIP[x][y].setImageBackground(icoTapis);
                    } else if (m instanceof Poubelle) {
                        tabIP[x][y].setImageBackground(icoPoubelle);
                    } else if (m instanceof Mine) {
                        tabIP[x][y].setImageBackground(icoMine);
                    } else if (m instanceof Livraison) {
                        tabIP[x][y].setImageBackground(icoHub);
                    } else if (m instanceof Cutter) {
                        tabIP[x][y].setImageBackground(icoCutter);
                    } else if (m instanceof Rotator) {
                        tabIP[x][y].setImageBackground(icoRotater);
                    } else if (m instanceof AtelierPeinture) {
                        tabIP[x][y].setImageBackground(icoPainter);
                    } else if (m instanceof Stacker) {
                        tabIP[x][y].setImageBackground(icoStacker);
                    }

                    Item current = m.getCurrent();
                    if (current instanceof ItemShape) {
                        tabIP[x][y].setShape((ItemShape) current);
                    } else {
                        tabIP[x][y].setShape(null);
                    }
                } else {
                    tabIP[x][y].setShape(null);
                }
            }
        }
        grilleIP.repaint();
    }

    @Override
    public void update(Observable o, Object arg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mettreAJourAffichage();
            }
        });
    }
}