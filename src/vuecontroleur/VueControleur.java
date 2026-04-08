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
import modele.item.SubShape;
import modele.jeu.Jeu;
import modele.plateau.*;
import modele.plateau.Balancer;
import modele.plateau.BalancerSecondaire;
import modele.plateau.Direction;
import controleur.Main;


public class VueControleur extends JFrame implements Observer {
    private Plateau plateau;
    private Jeu jeu;
    private Main mainController;
    private final int sizeX;
    private final int sizeY;
    private int pxCase;

    // Images pour les couleurs des gisements
    private Image icoRed;
    private Image icoGreen;
    private Image icoBlue;
    private Image icoYellow;

    // Images pour les zones de formes
    private Image icoSquare;
    private Image icoCircle;
    private Image icoStar;

    // Images pour les machines
    private Image icoTapis;
    private Image icoTapisLeft;
    private Image icoTapisRight;
    private Image icoPoubelle;
    private Image icoMine;
    private Image icoHub;
    private Image icoCutter;
    private Image icoRotater;
    private Image icoPainter;
    private Image icoStacker;
    private Image icoBalancer;

    // Composants de la boîte à outils
    private JToolBar toolBar;
    private JButton btnMine;
    private JButton btnTapis;
    private JButton btnPoubelle;
    private JButton btnCutter;
    private JButton btnRotater;
    private JButton btnPainter;
    private JButton btnStacker;
    private JButton btnBalancer;
    private String machineSelectionnee = "Tapis";

    // Composants pour l'affichage des objectifs
    private JPanel topPanel;
    private JLabel objectifLabel;
    private JLabel formeAttendueLabel;

    private JPanel grilleIP;
    private boolean mousePressed = false;
    private ImagePanel[][] tabIP;
    private int lastBeltX = -1;
    private int lastBeltY = -1;
    private Direction currentDragDirection = Direction.North;
    private Direction incomingToLast = null;

    public VueControleur(Jeu _jeu, Main _mainController) {
        jeu = _jeu;
        mainController = _mainController;
        plateau = jeu.getPlateau();
        sizeX = plateau.SIZE_X;
        sizeY = plateau.SIZE_Y;

        calculerTailleAdaptative();

        chargerLesIcones();
        initToolBar();
        placerLesComposantsGraphiques();
        initMenu();

        plateau.addObserver(this);
        mettreAJourAffichage();
        mettreAJourObjectifs();
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fichierMenu = new JMenu("Fichier");

        JMenuItem nouveauItem = new JMenuItem("Nouvelle partie");
        JMenuItem sauvegarderItem = new JMenuItem("Sauvegarder");
        JMenuItem chargerItem = new JMenuItem("Charger");
        JMenuItem quitterItem = new JMenuItem("Quitter");

        nouveauItem.addActionListener(e -> mainController.nouvellePartie());
        sauvegarderItem.addActionListener(e -> mainController.sauvegarder());
        chargerItem.addActionListener(e -> mainController.charger());
        quitterItem.addActionListener(e -> System.exit(0));

        fichierMenu.add(nouveauItem);
        fichierMenu.add(sauvegarderItem);
        fichierMenu.add(chargerItem);
        fichierMenu.addSeparator();
        fichierMenu.add(quitterItem);

        menuBar.add(fichierMenu);
        setJMenuBar(menuBar);
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
        icoRed = new ImageIcon("./data/sprites/colors/red.png").getImage();
        icoGreen = new ImageIcon("./data/sprites/colors/green.png").getImage();
        icoBlue = new ImageIcon("./data/sprites/colors/blue.png").getImage();
        icoYellow = new ImageIcon("./data/sprites/colors/yellow.png").getImage();

        icoSquare = new ImageIcon("./data/sprites/shapes/square.png").getImage();
        icoCircle = new ImageIcon("./data/sprites/shapes/circle.png").getImage();
        icoStar   = new ImageIcon("./data/sprites/shapes/star.png").getImage();

        icoTapis = new ImageIcon("./data/sprites/buildings/belt_top.png").getImage();
        icoTapisLeft = new ImageIcon("./data/sprites/buildings/belt_left.png").getImage();
        icoTapisRight = new ImageIcon("./data/sprites/buildings/belt_right.png").getImage();
        icoPoubelle = new ImageIcon("./data/sprites/buildings/trash.png").getImage();
        icoMine = new ImageIcon("./data/sprites/buildings/miner.png").getImage();
        icoHub = new ImageIcon("./data/sprites/buildings/hub.png").getImage();
        icoCutter = new ImageIcon("./data/sprites/buildings/cutter.png").getImage();
        icoRotater = new ImageIcon("./data/sprites/buildings/rotater.png").getImage();
        icoPainter = new ImageIcon("./data/sprites/buildings/painter.png").getImage();
        icoStacker = new ImageIcon("./data/sprites/buildings/stacker.png").getImage();
        icoBalancer = new ImageIcon("./data/sprites/buildings/balancer.png").getImage();
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
        ImageIcon balancerIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/balancer.png"), btnWidth - 20, btnHeight - 40);

        btnMine = new JButton("Mine", mineIcon);
        btnTapis = new JButton("Tapis", tapisIcon);
        btnPoubelle = new JButton("Poubelle", poubelleIcon);
        btnCutter = new JButton("Cutter", cutterIcon);
        btnRotater = new JButton("Rotator", rotaterIcon);
        btnPainter = new JButton("Painter", painterIcon);
        btnStacker = new JButton("Stacker", stackerIcon);
        btnBalancer = new JButton("Balancer", balancerIcon);

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
        btnBalancer.setPreferredSize(buttonSize);
        btnBalancer.setMaximumSize(buttonSize);
        btnBalancer.setMinimumSize(buttonSize);

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
        btnBalancer.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnBalancer.setHorizontalTextPosition(SwingConstants.CENTER);

        btnMine.setToolTipText("Mine - à placer sur un gisement");
        btnTapis.setToolTipText("Tapis - transporte les items");
        btnPoubelle.setToolTipText("Poubelle - détruit les items");
        btnCutter.setToolTipText("Cutter - découpe les formes");
        btnRotater.setToolTipText("Rotator - fait pivoter les formes");
        btnPainter.setToolTipText("Painter - colore les formes");
        btnStacker.setToolTipText("Stacker - empile les formes");
        btnBalancer.setToolTipText("Balancer - répartit les items entre deux sorties");

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
        btnBalancer.addActionListener(e -> {
            machineSelectionnee = "Balancer";
            System.out.println("Machine sélectionnée : Balancer");
        });

        toolBar.add(btnMine);
        toolBar.add(btnTapis);
        toolBar.add(btnPoubelle);
        toolBar.add(btnCutter);
        toolBar.add(btnRotater);
        toolBar.add(btnPainter);
        toolBar.add(btnStacker);
        toolBar.add(btnBalancer);

        toolBar.add(Box.createVerticalGlue());
    }

    private void mettreAJourObjectifs() {
        if (Livraison.isTermine()) {
            objectifLabel.setText("🎉 FELICITATIONS ! 🎉");
            objectifLabel.setFont(new Font("Arial", Font.BOLD, 16));
            formeAttendueLabel.setText("Tous les objectifs sont atteints !");
            formeAttendueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        } else {
            int numero = Livraison.getObjectifNumero();
            int recu = Livraison.getQuantiteRecue();
            int requis = Livraison.getObjectifRequis();

            objectifLabel.setFont(new Font("Arial", Font.BOLD, 14));
            objectifLabel.setText("Objectif " + numero + " : " + recu + " / " + requis + " items");

            String explication = "";
            if (numero == 1) {
                explication = "Objectif 1 : Carré plein";
            } else if (numero == 2) {
                explication = "Objectif 2 : Partie droite d'un rond";
            } else if (numero == 3) {
                explication = "Objectif 3 : Partie basse d'une étoile";
            } else if (numero == 4) {
                explication = "Objectif 4 : Partie droite d'un carré vert";
            }

            formeAttendueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            formeAttendueLabel.setText(explication);
        }
    }

    private void placerLesComposantsGraphiques() {
        setTitle("ShapeCraft");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel du haut pour les objectifs
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        objectifLabel = new JLabel();
        objectifLabel.setFont(new Font("Arial", Font.BOLD, 14));
        objectifLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        formeAttendueLabel = new JLabel();
        formeAttendueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        formeAttendueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(objectifLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        topPanel.add(formeAttendueLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

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
                        if (SwingUtilities.isRightMouseButton(e)) {
                            jeu.supprimerMachine(xx, yy);
                        } else if (machineSelectionnee.equals("Tapis")) {
                            jeu.placerMachine(xx, yy, "Tapis", currentDragDirection);
                        } else if (plateau.getCases()[xx][yy].getMachine() != null
                                && !(plateau.getCases()[xx][yy].getMachine() instanceof Tapis)) {
                            jeu.rotateMachine(xx, yy);
                        } else {
                            jeu.placerMachine(xx, yy, machineSelectionnee);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (mousePressed) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                jeu.supprimerMachine(xx, yy);
                            } else if (machineSelectionnee.equals("Tapis")) {
                                if (lastBeltX != -1) {
                                    Direction dir = computeBeltDirection(lastBeltX, lastBeltY, xx, yy);
                                    currentDragDirection = dir;
                                    if (incomingToLast != null && incomingToLast != dir) {
                                        jeu.placerTapisCorner(lastBeltX, lastBeltY, incomingToLast, dir);
                                    } else {
                                        jeu.placerMachine(lastBeltX, lastBeltY, "Tapis", dir);
                                    }
                                    jeu.placerMachine(xx, yy, "Tapis", dir);
                                    incomingToLast = dir;
                                } else {
                                    jeu.placerMachine(xx, yy, "Tapis", currentDragDirection);
                                }
                                lastBeltX = xx;
                                lastBeltY = yy;
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
                        } else if (machineSelectionnee.equals("Tapis")) {
                            currentDragDirection = Direction.North;
                            incomingToLast = null;
                            lastBeltX = xx;
                            lastBeltY = yy;
                            jeu.placerMachine(xx, yy, "Tapis", Direction.North);
                        } else if (plateau.getCases()[xx][yy].getMachine() != null
                                && !(plateau.getCases()[xx][yy].getMachine() instanceof Tapis)) {
                            // ignoré — mouseClicked gère la rotation
                        } else {
                            jeu.placerMachine(xx, yy, machineSelectionnee);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        mousePressed = false;
                        lastBeltX = -1;
                        lastBeltY = -1;
                        incomingToLast = null;
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

        // Taille fixe de la fenêtre
        setSize(sizeX * pxCase + 150, sizeY * pxCase + 130);
        setLocationRelativeTo(null);
    }

    private boolean isColorItem(ItemShape shape) {
        return shape.isColorItem();
    }

    private Image getItemImage(ItemShape shape) {
        if (isColorItem(shape)) {
            switch (shape.getColors(ItemShape.Layer.one)[0]) {
                case Red:    return icoRed;
                case Green:  return icoGreen;
                case Blue:   return icoBlue;
                case Yellow: return icoYellow;
                default: break;
            }
        }
        for (SubShape s : shape.getSubShapes(ItemShape.Layer.one)) {
            if (s == SubShape.Carre)  return icoSquare;
            if (s == SubShape.Circle) return icoCircle;
            if (s == SubShape.Star)   return icoStar;
        }
        return null;
    }

    private java.awt.Color getItemTint(ItemShape shape) {
        if (isColorItem(shape)) return null;
        modele.item.Color[] colors = shape.getColors(ItemShape.Layer.one);
        SubShape[] subs = shape.getSubShapes(ItemShape.Layer.one);
        for (int i = 0; i < 4; i++) {
            if (subs[i] != SubShape.None && colors[i] != null) {
                return toAwtColor(colors[i]);
            }
        }
        return null;
    }

    private java.awt.Color toAwtColor(modele.item.Color c) {
        switch (c) {
            case Red:    return java.awt.Color.RED;
            case Green:  return java.awt.Color.GREEN;
            case Blue:   return java.awt.Color.BLUE;
            case Yellow: return java.awt.Color.YELLOW;
            case Purple: return java.awt.Color.MAGENTA;
            case Cyan:   return java.awt.Color.CYAN;
            case White:  return java.awt.Color.WHITE;
            default:     return java.awt.Color.GRAY;
        }
    }

    private Direction computeBeltDirection(int fromX, int fromY, int toX, int toY) {
        int dx = toX - fromX;
        int dy = toY - fromY;
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx >= 0 ? Direction.East : Direction.West;
        } else {
            return dy >= 0 ? Direction.South : Direction.North;
        }
    }

    private double directionToRotation(Direction d) {
        switch (d) {
            case East:  return Math.PI / 2;
            case South: return Math.PI;
            case West:  return 3 * Math.PI / 2;
            default:    return 0;
        }
    }

    private boolean isRightTurn(Direction incoming, Direction outgoing) {
        return (incoming == Direction.North && outgoing == Direction.East)
                || (incoming == Direction.East  && outgoing == Direction.South)
                || (incoming == Direction.South && outgoing == Direction.West)
                || (incoming == Direction.West  && outgoing == Direction.North);
    }

    private double cornerRotation(Direction incoming, Direction outgoing) {
        if (isRightTurn(incoming, outgoing)) {
            if (incoming == Direction.North) return 0;
            if (incoming == Direction.East)  return Math.PI / 2;
            if (incoming == Direction.South) return Math.PI;
            return 3 * Math.PI / 2;
        } else {
            if (incoming == Direction.North) return 0;
            if (incoming == Direction.East)  return Math.PI / 2;
            if (incoming == Direction.South) return Math.PI;
            return 3 * Math.PI / 2;
        }
    }

    private void mettreAJourAffichage() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                tabIP[x][y].setImageBackground(null);
                tabIP[x][y].setFront(null);
                tabIP[x][y].setFrontTint(null);
                tabIP[x][y].setShape(null);
                tabIP[x][y].setBackgroundRotation(0);
                tabIP[x][y].setCutHalf(ImagePanel.CutHalf.NONE);
                tabIP[x][y].setBackgroundHalf(ImagePanel.BackgroundHalf.NONE);

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
                        Tapis t = (Tapis) m;
                        if (t.isCorner()) {
                            boolean rightTurn = isRightTurn(t.getIncoming(), t.getDirection());
                            tabIP[x][y].setImageBackground(rightTurn ? icoTapisRight : icoTapisLeft);
                            tabIP[x][y].setBackgroundRotation(cornerRotation(t.getIncoming(), t.getDirection()));
                        } else {
                            tabIP[x][y].setBackgroundRotation(directionToRotation(m.getDirection()));
                            tabIP[x][y].setImageBackground(icoTapis);
                        }
                    } else if (m instanceof Poubelle) {
                        tabIP[x][y].setImageBackground(icoPoubelle);
                    } else if (m instanceof Mine) {
                        tabIP[x][y].setBackgroundRotation(directionToRotation(m.getDirection()));
                        tabIP[x][y].setImageBackground(icoMine);
                    } else if (m instanceof Livraison) {
                        tabIP[x][y].setImageBackground(icoHub);
                    } else if (m instanceof Cutter) {
                        tabIP[x][y].setBackgroundRotation(directionToRotation(m.getDirection()));
                        tabIP[x][y].setImageBackground(icoCutter);
                    } else if (m instanceof Rotator) {
                        tabIP[x][y].setBackgroundRotation(directionToRotation(m.getDirection()));
                        tabIP[x][y].setImageBackground(icoRotater);
                    } else if (m instanceof AtelierPeinture) {
                        tabIP[x][y].setBackgroundRotation(directionToRotation(m.getDirection()));
                        tabIP[x][y].setImageBackground(icoPainter);
                    } else if (m instanceof Stacker) {
                        tabIP[x][y].setBackgroundRotation(directionToRotation(m.getDirection()));
                        tabIP[x][y].setImageBackground(icoStacker);
                    } else if (m instanceof Balancer) {
                        tabIP[x][y].setBackgroundRotation(directionToRotation(m.getDirection()));
                        tabIP[x][y].setImageBackground(icoBalancer);
                    }

                    Item current = m.getCurrent();
                    if (current instanceof ItemShape) {
                        ItemShape is = (ItemShape) current;
                        if (m instanceof Livraison) {
                            tabIP[x][y].setFront(null);
                            tabIP[x][y].setFrontTint(null);
                            tabIP[x][y].setCutHalf(ImagePanel.CutHalf.NONE);
                        } else {
                            tabIP[x][y].setFront(getItemImage(is));
                            tabIP[x][y].setFrontTint(getItemTint(is));
                            if (is.isCut()) {
                                switch (is.getCutDirection()) {
                                    case "RIGHT":  tabIP[x][y].setCutHalf(ImagePanel.CutHalf.RIGHT);  break;
                                    case "LEFT":   tabIP[x][y].setCutHalf(ImagePanel.CutHalf.LEFT);   break;
                                    case "TOP":    tabIP[x][y].setCutHalf(ImagePanel.CutHalf.TOP);    break;
                                    case "BOTTOM": tabIP[x][y].setCutHalf(ImagePanel.CutHalf.BOTTOM); break;
                                    default:       tabIP[x][y].setCutHalf(ImagePanel.CutHalf.NONE);   break;
                                }
                            }
                        }
                    }
                } else {
                    int midX = sizeX / 2 - 1;
                    int midY = sizeY / 2 - 1;
                    if (x >= midX && x <= midX + 2 && y < 3) {
                        tabIP[x][y].setFront(icoSquare);
                    } else if (x >= midX && x <= midX + 2 && y >= sizeY - 3) {
                        tabIP[x][y].setFront(icoStar);
                    } else if (x < 3 && y >= midY && y <= midY + 2) {
                        tabIP[x][y].setFront(icoCircle);
                    }
                }
            }
        }
        // Second pass: render secondary cells for 2-cell Balancer
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                Machine m = plateau.getCases()[x][y].getMachine();
                if (m instanceof Balancer) {
                    Direction sideDir = m.getDirection().rotate90CW();
                    int x2 = x + sideDir.getDx();
                    int y2 = y + sideDir.getDy();
                    if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY) {
                        tabIP[x2][y2].setImageBackground(icoBalancer);
                        tabIP[x2][y2].setBackgroundRotation(directionToRotation(m.getDirection()));
                    }
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
                mettreAJourObjectifs();
            }
        });
    }
}