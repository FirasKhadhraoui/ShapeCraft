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

/**
 * VueControleur : Vue + Contrôleur de l'application.
 * - Affiche la grille et les machines
 * - Gère les clics souris (placer, supprimer, tourner)
 * - Gère le menu Fichier
 * - Observe le plateau pour se mettre à jour
 */
public class VueControleur extends JFrame implements Observer {

    // RÉFÉRENCES
    private Plateau plateau;      // Le plateau de jeu
    private Jeu jeu;              // Le contrôleur du jeu
    private Main mainController;  // Le contrôleur principal (menu)
    private final int sizeX;      // Largeur de la grille
    private final int sizeY;      // Hauteur de la grille
    private int pxCase;           // Taille d'une case en pixels

    //  IMAGES
    // Couleurs des gisements
    private Image icoRed, icoGreen, icoBlue, icoYellow;
    // Formes
    private Image icoSquare, icoCircle, icoStar;
    // Machines
    private Image icoTapis, icoTapisLeft, icoTapisRight, icoPoubelle, icoMine, icoHub,
            icoCutter, icoRotater, icoPainter, icoStacker, icoBalancer;

    // BOÎTE À OUTILS
    private JToolBar toolBar;                     // Barre verticale
    private JButton btnMine, btnTapis, btnPoubelle, btnCutter, btnRotater, btnPainter, btnStacker, btnBalancer;
    private String machineSelectionnee = "Tapis"; // Machine sélectionnée

    // AFFICHAGE OBJECTIFS
    private JPanel topPanel;
    private JLabel objectifLabel;
    private JLabel formeAttendueLabel;

    //  GRILLE
    private JPanel grilleIP;
    private ImagePanel[][] tabIP;
    private boolean mousePressed = false;

    // Drag & drop pour les tapis
    private int lastBeltX = -1, lastBeltY = -1;
    private Direction currentDragDirection = Direction.North;
    private Direction incomingToLast = null;

    //  CONSTRUCTEUR
    public VueControleur(Jeu _jeu, Main _mainController) {
        jeu = _jeu;
        mainController = _mainController;
        plateau = jeu.getPlateau();
        sizeX = plateau.SIZE_X;
        sizeY = plateau.SIZE_Y;

        calculerTailleAdaptative();    // Ajuste la taille des cases
        chargerLesIcones();            // Charge les images
        initToolBar();                 // Crée la barre d'outils
        placerLesComposantsGraphiques(); // Construit l'interface
        initMenu();                    // Crée le menu Fichier

        plateau.addObserver(this);     // S'abonne au plateau
        mettreAJourAffichage();        // Premier affichage
        mettreAJourObjectifs();        // Affiche les objectifs
    }

    //  MENU
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
        quitterItem.addActionListener(e -> mainController.quitter());

        fichierMenu.add(nouveauItem);
        fichierMenu.add(sauvegarderItem);
        fichierMenu.add(chargerItem);
        fichierMenu.addSeparator();
        fichierMenu.add(quitterItem);

        menuBar.add(fichierMenu);
        setJMenuBar(menuBar);
    }

    //  TAILLE ADAPTATIVE
    private void calculerTailleAdaptative() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxHauteur = (int)(screenSize.height * 0.7);
        int maxLargeur = (int)(screenSize.width * 0.7);
        int maxCaseHauteur = maxHauteur / sizeY;
        int maxCaseLargeur = maxLargeur / sizeX;
        pxCase = Math.min(maxCaseHauteur, maxCaseLargeur);
        pxCase = Math.max(40, Math.min(pxCase, 100));
    }

    //  CHARGEMENT DES IMAGES
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

    // Redimensionne une icône
    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    //  BARRE D'OUTILS
    private void initToolBar() {
        toolBar = new JToolBar("Boîte à outils", JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setLayout(new GridLayout(0, 1, 5, 5));

        int btnWidth = pxCase + 10;
        int btnHeight = pxCase + 20;

        // Création des icônes redimensionnées
        ImageIcon mineIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/miner.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon tapisIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/belt_top.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon poubelleIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/trash.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon cutterIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/cutter.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon rotaterIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/rotater.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon painterIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/painter.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon stackerIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/stacker.png"), btnWidth - 20, btnHeight - 40);
        ImageIcon balancerIcon = resizeIcon(new ImageIcon("./data/sprites/buildings/balancer.png"), btnWidth - 20, btnHeight - 40);

        // Création des boutons
        btnMine = new JButton("Mine", mineIcon);
        btnTapis = new JButton("Tapis", tapisIcon);
        btnPoubelle = new JButton("Poubelle", poubelleIcon);
        btnCutter = new JButton("Cutter", cutterIcon);
        btnRotater = new JButton("Rotator", rotaterIcon);
        btnPainter = new JButton("Painter", painterIcon);
        btnStacker = new JButton("Stacker", stackerIcon);
        btnBalancer = new JButton("Balancer", balancerIcon);

        Dimension buttonSize = new Dimension(btnWidth, btnHeight);
        for (JButton btn : new JButton[]{btnMine, btnTapis, btnPoubelle, btnCutter, btnRotater, btnPainter, btnStacker, btnBalancer}) {
            btn.setPreferredSize(buttonSize);
            btn.setMaximumSize(buttonSize);
            btn.setMinimumSize(buttonSize);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
        }

        // Tooltips
        btnMine.setToolTipText("Mine - à placer sur un gisement");
        btnTapis.setToolTipText("Tapis - transporte les items");
        btnPoubelle.setToolTipText("Poubelle - détruit les items");
        btnCutter.setToolTipText("Cutter - découpe les formes");
        btnRotater.setToolTipText("Rotator - fait pivoter les formes");
        btnPainter.setToolTipText("Painter - colore les formes");
        btnStacker.setToolTipText("Stacker - empile les formes");
        btnBalancer.setToolTipText("Balancer - répartit les items entre deux sorties");

        // Sélection de la machine
        btnMine.addActionListener(e -> { machineSelectionnee = "Mine"; System.out.println("Machine sélectionnée : Mine"); });
        btnTapis.addActionListener(e -> { machineSelectionnee = "Tapis"; System.out.println("Machine sélectionnée : Tapis"); });
        btnPoubelle.addActionListener(e -> { machineSelectionnee = "Poubelle"; System.out.println("Machine sélectionnée : Poubelle"); });
        btnCutter.addActionListener(e -> { machineSelectionnee = "Cutter"; System.out.println("Machine sélectionnée : Cutter"); });
        btnRotater.addActionListener(e -> { machineSelectionnee = "Rotater"; System.out.println("Machine sélectionnée : Rotater"); });
        btnPainter.addActionListener(e -> { machineSelectionnee = "Painter"; System.out.println("Machine sélectionnée : Painter"); });
        btnStacker.addActionListener(e -> { machineSelectionnee = "Stacker"; System.out.println("Machine sélectionnée : Stacker"); });
        btnBalancer.addActionListener(e -> { machineSelectionnee = "Balancer"; System.out.println("Machine sélectionnée : Balancer"); });

        // Ajout à la toolbar
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

    //  AFFICHAGE DES OBJECTIFS
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
            if (numero == 1) explication = "Objectif 1 : Carré plein";
            else if (numero == 2) explication = "Objectif 2 : Partie droite d'un rond";
            else if (numero == 3) explication = "Objectif 3 : Partie basse d'une étoile";
            else if (numero == 4) explication = "Objectif 4 : Partie droite d'un carré vert";

            formeAttendueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            formeAttendueLabel.setText(explication);
        }
    }

    //  CONSTRUCTION DE L'INTERFACE
    private void placerLesComposantsGraphiques() {
        setTitle("ShapeCraft");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Géré par Main

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel des objectifs (en haut)
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        objectifLabel = new JLabel();
        objectifLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formeAttendueLabel = new JLabel();
        formeAttendueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(objectifLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        topPanel.add(formeAttendueLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Grille des cases
        grilleIP = new JPanel(new GridLayout(sizeY, sizeX));
        tabIP = new ImagePanel[sizeX][sizeY];

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                ImagePanel iP = new ImagePanel();
                iP.setPreferredSize(new Dimension(pxCase, pxCase));
                iP.setMinimumSize(new Dimension(pxCase, pxCase));
                iP.setMaximumSize(new Dimension(pxCase, pxCase));
                tabIP[x][y] = iP;

                final int xx = x, yy = y;
                iP.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            jeu.supprimerMachine(xx, yy);                       // Suppression
                        } else if (machineSelectionnee.equals("Tapis")) {
                            jeu.placerMachine(xx, yy, "Tapis", currentDragDirection); // Tapis
                        } else if (plateau.getCases()[xx][yy].getMachine() != null
                                && !(plateau.getCases()[xx][yy].getMachine() instanceof Tapis)) {
                            jeu.rotateMachine(xx, yy);                         // Rotation
                        } else {
                            jeu.placerMachine(xx, yy, machineSelectionnee);     // Placement
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (mousePressed) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                jeu.supprimerMachine(xx, yy);
                            } else if (machineSelectionnee.equals("Tapis")) {
                                // Drag & drop pour les tapis
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
                            // Rotation gérée par mouseClicked
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

        // ScrollPane avec zoom Ctrl+molette
        JScrollPane scrollPane = new JScrollPane(grilleIP);
        scrollPane.setPreferredSize(new Dimension(sizeX * pxCase + 20, sizeY * pxCase + 20));
        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                int delta = e.getWheelRotation() < 0 ? 4 : -4;
                zoomGrille(delta);
                e.consume();
            }
        });

        mainPanel.add(toolBar, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);

        setSize(sizeX * pxCase + 150, sizeY * pxCase + 130);
        setLocationRelativeTo(null);
    }

    // Zoom de la grille
    private void zoomGrille(int delta) {
        pxCase = Math.max(10, Math.min(100, pxCase + delta));
        Dimension d = new Dimension(pxCase, pxCase);
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                tabIP[x][y].setPreferredSize(d);
                tabIP[x][y].setMinimumSize(d);
                tabIP[x][y].setMaximumSize(d);
            }
        }
        grilleIP.revalidate();
        grilleIP.repaint();
    }

    // UTILITAIRES POUR L'AFFICHAGE
    private boolean isColorItem(ItemShape shape) {
        return shape.isColorItem();
    }

    private Image getItemImage(ItemShape shape) {
        if (isColorItem(shape)) {
            switch (shape.getColors()[0]) {
                case Red:    return icoRed;
                case Green:  return icoGreen;
                case Blue:   return icoBlue;
                case Yellow: return icoYellow;
                default: break;
            }
        }
        for (SubShape s : shape.getSubShapes()) {
            if (s == SubShape.Carre)  return icoSquare;
            if (s == SubShape.Circle) return icoCircle;
            if (s == SubShape.Star)   return icoStar;
        }
        return null;
    }

    private java.awt.Color getItemTint(ItemShape shape) {
        if (isColorItem(shape)) return null;
        modele.item.Color[] colors = shape.getColors();
        SubShape[] subs = shape.getSubShapes();
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

    // Calcule la direction entre deux cases (pour drag & drop)
    private Direction computeBeltDirection(int fromX, int fromY, int toX, int toY) {
        int dx = toX - fromX;
        int dy = toY - fromY;
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx >= 0 ? Direction.East : Direction.West;
        } else {
            return dy >= 0 ? Direction.South : Direction.North;
        }
    }

    // Convertit une direction en angle de rotation (radians)
    private double directionToRotation(Direction d) {
        switch (d) {
            case East:  return Math.PI / 2;
            case South: return Math.PI;
            case West:  return 3 * Math.PI / 2;
            default:    return 0;
        }
    }

    // Vérifie si un virage est à droite
    private boolean isRightTurn(Direction incoming, Direction outgoing) {
        return (incoming == Direction.North && outgoing == Direction.East)
                || (incoming == Direction.East  && outgoing == Direction.South)
                || (incoming == Direction.South && outgoing == Direction.West)
                || (incoming == Direction.West  && outgoing == Direction.North);
    }

    // Calcule l'angle de rotation pour un tapis en coin
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

    //  RAFRAÎCHISSEMENT DE L'AFFICHAGE
    private void mettreAJourAffichage() {
        int midX = sizeX / 2 - 1;
        int midY = sizeY / 2 - 1;

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                // Réinitialisation
                tabIP[x][y].setImageBackground(null);
                tabIP[x][y].setFront(null);
                tabIP[x][y].setFrontTint(null);
                tabIP[x][y].setShape(null);
                tabIP[x][y].setBackgroundRotation(0);
                tabIP[x][y].setCutHalf(ImagePanel.CutHalf.NONE);
                tabIP[x][y].setBackgroundHalf(ImagePanel.BackgroundHalf.NONE);

                // Affichage des gisements de couleur dans les coins (2x2)
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
                    //  AFFICHAGE DE LA MACHINE
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

                    //  AFFICHAGE DE L'ITEM
                    Item current = m.getCurrent();
                    if (current instanceof ItemShape) {
                        ItemShape is = (ItemShape) current;
                        if (!(m instanceof Livraison)) { // Le hub n'affiche pas les items
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
                    // AFFICHAGE DES ZONES DE FORMES
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

        // Second passage : affichage des cases secondaires du Balancer
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

    //  OBSERVER 
    @Override
    public void update(Observable o, Object arg) {
        SwingUtilities.invokeLater(() -> {
            mettreAJourAffichage();
            mettreAJourObjectifs();
        });
    }
}