package modele.item;

public class ItemShape extends Item {
    // Tableau des formes pour chaque quart (0=Haut-Droite, 1=Bas-Droite, 2=Bas-Gauche, 3=Haut-Gauche)
    private SubShape[] tabSubShapes;
    // Tableau des couleurs pour chaque quart
    private Color[] tabColors;
    // true = item de couleur pure (ex: cercle rouge)
    private boolean colorItem = false;
    // true = forme a été coupée par un Cutter
    private boolean cut = false;

    public boolean isColorItem() { return colorItem; }
    public void setColorItem(boolean b) { this.colorItem = b; }
    public boolean isCut() { return cut; }
    public void setCut(boolean b) { this.cut = b; }

    /**
     * Détermine quelle moitié de la forme est remplie après découpe
     * Retourne : "RIGHT", "LEFT", "TOP", "BOTTOM" ou "NONE"
     */
    public String getCutDirection() {
        if (!cut) return "NONE";
        boolean h0 = tabSubShapes[0] != SubShape.None;
        boolean h1 = tabSubShapes[1] != SubShape.None;
        boolean h2 = tabSubShapes[2] != SubShape.None;
        boolean h3 = tabSubShapes[3] != SubShape.None;
        if ( h0 &&  h1 && !h2 && !h3) return "RIGHT";
        if (!h0 && !h1 &&  h2 &&  h3) return "LEFT";
        if ( h0 && !h1 && !h2 &&  h3) return "TOP";
        if (!h0 &&  h1 &&  h2 && !h3) return "BOTTOM";
        return "NONE";
    }

    /**
     * Récupère les 4 formes (pour l'affichage)
     */
    public SubShape[] getSubShapes() {
        if (tabSubShapes.length >= 4) {
            return new SubShape[] {tabSubShapes[0], tabSubShapes[1], tabSubShapes[2], tabSubShapes[3]};
        } else if (tabSubShapes.length == 2) {
            return new SubShape[] {tabSubShapes[0], tabSubShapes[1], SubShape.None, SubShape.None};
        } else {
            return new SubShape[] {SubShape.None, SubShape.None, SubShape.None, SubShape.None};
        }
    }

    /**
     * Récupère les 4 couleurs
     */
    public Color[] getColors() {
        if (tabColors.length >= 4) {
            return new Color[] {tabColors[0], tabColors[1], tabColors[2], tabColors[3]};
        } else if (tabColors.length == 2) {
            return new Color[] {tabColors[0], tabColors[1], null, null};
        } else {
            return new Color[] {null, null, null, null};
        }
    }

    /**
     * Constructeur : crée une forme à partir d'une chaîne
     * Exemple: "CrCb--Cb" = Carré Rouge, Carré Bleu, Vide, Carré Bleu
     */
    public ItemShape(String str) {
        int nbQuarts = str.length() / 2;
        tabSubShapes = new SubShape[nbQuarts];
        tabColors = new Color[nbQuarts];

        for (int i = 0; i < nbQuarts; i++) {
            // Lecture de la forme
            switch (str.charAt(i * 2)) {
                case 'C' : tabSubShapes[i] = SubShape.Carre; break;
                case 'c' : tabSubShapes[i] = SubShape.Circle; break;
                case 'F' : tabSubShapes[i] = SubShape.Fan; break;
                case 'S' : tabSubShapes[i] = SubShape.Star; break;
                case '-' : tabSubShapes[i] = SubShape.None; break;
                default:
                    throw new IllegalStateException("Unexpected shape: " + str.charAt(i * 2));
            }

            // Lecture de la couleur
            switch (str.charAt(i * 2 + 1)) {
                case 'r' : tabColors[i] = Color.Red; break;
                case 'g' : tabColors[i] = Color.Green; break;
                case 'b' : tabColors[i] = Color.Blue; break;
                case 'y' : tabColors[i] = Color.Yellow; break;
                case 'p' : tabColors[i] = Color.Purple; break;
                case 'c' : tabColors[i] = Color.Cyan; break;
                case 'w' : tabColors[i] = Color.White; break;
                case '-' : tabColors[i] = null; break;
                default:
                    throw new IllegalStateException("Unexpected color: " + str.charAt(i * 2 + 1));
            }
        }
    }

    /**
     * Rotation 90° horaire de la forme
     * Transformation: [0,1,2,3] → [3,0,1,2]
     */
    public void rotate() {
        if (tabSubShapes.length >= 4) {
            SubShape[] bufferSubShapes = new SubShape[4];
            bufferSubShapes[0] = tabSubShapes[3];
            bufferSubShapes[1] = tabSubShapes[0];
            bufferSubShapes[2] = tabSubShapes[1];
            bufferSubShapes[3] = tabSubShapes[2];

            Color[] bufferColors = new Color[4];
            bufferColors[0] = tabColors[3];
            bufferColors[1] = tabColors[0];
            bufferColors[2] = tabColors[1];
            bufferColors[3] = tabColors[2];

            for (int i = 0; i < 4; i++) {
                tabSubShapes[i] = bufferSubShapes[i];
                tabColors[i] = bufferColors[i];
            }
        }
    }

    /**
     * Empilement (Stacker) : fusionne une autre forme dans celle-ci
     * Remplace les vides par les formes de l'autre item
     */
    public void stack(ItemShape other) {
        for (int i = 0; i < 4 && i < tabSubShapes.length && i < other.tabSubShapes.length; i++) {
            if (tabSubShapes[i] == SubShape.None && other.tabSubShapes[i] != SubShape.None) {
                tabSubShapes[i] = other.tabSubShapes[i];
                tabColors[i]    = other.tabColors[i];
            }
        }
        this.cut = false;
    }

    /**
     * Découpe (Cutter) : sépare la forme en deux parties
     * @return la partie haute (indices 0 et 1)
     * this devient la partie basse (indices 2 et 3)
     */
    public ItemShape Cut() {
        if (tabSubShapes.length < 4) {
            System.out.println("Impossible de découper : forme déjà coupée");
            return new ItemShape("----");
        }

        // Partie HAUTE : indices 0 et 1
        SubShape[] hautSub = new SubShape[4];
        Color[] hautColor = new Color[4];
        hautSub[0] = tabSubShapes[0];
        hautColor[0] = tabColors[0];
        hautSub[1] = tabSubShapes[1];
        hautColor[1] = tabColors[1];
        hautSub[2] = SubShape.None;
        hautColor[2] = null;
        hautSub[3] = SubShape.None;
        hautColor[3] = null;

        // Partie BASSE : indices 2 et 3
        SubShape[] basSub = new SubShape[4];
        Color[] basColor = new Color[4];
        basSub[0] = SubShape.None;
        basColor[0] = null;
        basSub[1] = SubShape.None;
        basColor[1] = null;
        basSub[2] = tabSubShapes[2];
        basColor[2] = tabColors[2];
        basSub[3] = tabSubShapes[3];
        basColor[3] = tabColors[3];

        String chaineHaut = construireChaine(hautSub, hautColor);
        String chaineBas = construireChaine(basSub, basColor);

        ItemShape formeHaut = new ItemShape(chaineHaut);
        ItemShape formeBas = new ItemShape(chaineBas);

        this.tabSubShapes = formeBas.tabSubShapes;
        this.tabColors = formeBas.tabColors;
        this.cut = true;
        formeHaut.cut = true;

        return formeHaut;
    }

    // Convertit un tableau de formes/couleurs en chaîne
    private String construireChaine(SubShape[] subs, Color[] colors) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subs.length; i++) {
            switch (subs[i]) {
                case Carre: sb.append('C'); break;
                case Circle: sb.append('c'); break;
                case Fan: sb.append('F'); break;
                case Star: sb.append('S'); break;
                case None: sb.append('-'); break;
                default: sb.append('?');
            }
            if (colors[i] == null) {
                sb.append('-');
            } else {
                switch (colors[i]) {
                    case Red: sb.append('r'); break;
                    case Green: sb.append('g'); break;
                    case Blue: sb.append('b'); break;
                    case Yellow: sb.append('y'); break;
                    case Purple: sb.append('p'); break;
                    case Cyan: sb.append('c'); break;
                    case White: sb.append('w'); break;
                    default: sb.append('?');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Coloration (Painter) : applique une couleur à tous les blocs non vides
     */
    public void Color(Color c) {
        for (int i = 0; i < tabColors.length; i++) {
            if (tabSubShapes[i] != SubShape.None) {
                tabColors[i] = c;
            }
        }
    }

    /**
     * Convertit la forme en chaîne (inverse du constructeur)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabSubShapes.length; i++) {
            switch (tabSubShapes[i]) {
                case Carre: sb.append('C'); break;
                case Circle: sb.append('c'); break;
                case Fan: sb.append('F'); break;
                case Star: sb.append('S'); break;
                case None: sb.append('-'); break;
                default: sb.append('?');
            }
            if (tabColors[i] == null) {
                sb.append('-');
            } else {
                switch (tabColors[i]) {
                    case Red: sb.append('r'); break;
                    case Green: sb.append('g'); break;
                    case Blue: sb.append('b'); break;
                    case Yellow: sb.append('y'); break;
                    case Purple: sb.append('p'); break;
                    case Cyan: sb.append('c'); break;
                    case White: sb.append('w'); break;
                    default: sb.append('?');
                }
            }
        }
        return sb.toString();
    }
}