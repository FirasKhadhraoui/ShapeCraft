package modele.item;

public class ItemShape extends Item {
    private SubShape[] tabSubShapes;
    private Color[] tabColors;
    private boolean colorItem = false; // true pour les items issus des gisements de couleur (coins)
    private boolean cut = false;       // true après un appel à Cut()
    public enum Layer {one, two, three};

    public boolean isColorItem() { return colorItem; }
    public void setColorItem(boolean b) { this.colorItem = b; }
    public boolean isCut() { return cut; }
    public void setCut(boolean b) { this.cut = b; }

    /**
     * Retourne la moitié occupée par cet item coupé selon les quadrants remplis.
     * Indices : 0=Haut-Droite, 1=Bas-Droite, 2=Bas-Gauche, 3=Haut-Gauche
     * Retourne : "RIGHT" (0+1), "LEFT" (2+3), "TOP" (0+3), "BOTTOM" (1+2), ou "NONE"
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

    public SubShape[] getSubShapes(Layer l) {
        switch(l) {
            case one :
                if (tabSubShapes.length >= 4) {
                    return new SubShape[] {tabSubShapes[0], tabSubShapes[1], tabSubShapes[2], tabSubShapes[3]};
                } else if (tabSubShapes.length == 2) {
                    return new SubShape[] {tabSubShapes[0], tabSubShapes[1], SubShape.None, SubShape.None};
                } else {
                    return new SubShape[] {SubShape.None, SubShape.None, SubShape.None, SubShape.None};
                }
            case two :
                if (tabSubShapes.length >= 8) {
                    return new SubShape[] {tabSubShapes[4], tabSubShapes[5], tabSubShapes[6], tabSubShapes[7]};
                } else {
                    return new SubShape[] {SubShape.None, SubShape.None, SubShape.None, SubShape.None};
                }
            case three :
                if (tabSubShapes.length >= 12) {
                    return new SubShape[] {tabSubShapes[8], tabSubShapes[9], tabSubShapes[10], tabSubShapes[11]};
                } else {
                    return new SubShape[] {SubShape.None, SubShape.None, SubShape.None, SubShape.None};
                }
            default:
                throw new IllegalStateException("Unexpected value: " + l);
        }
    }

    public Color[] getColors(Layer l) {
        switch(l) {
            case one :
                if (tabColors.length >= 4) {
                    return new Color[] {tabColors[0], tabColors[1], tabColors[2], tabColors[3]};
                } else if (tabColors.length == 2) {
                    return new Color[] {tabColors[0], tabColors[1], null, null};
                } else {
                    return new Color[] {null, null, null, null};
                }
            case two :
                if (tabColors.length >= 8) {
                    return new Color[] {tabColors[4], tabColors[5], tabColors[6], tabColors[7]};
                } else {
                    return new Color[] {null, null, null, null};
                }
            case three :
                if (tabColors.length >= 12) {
                    return new Color[] {tabColors[8], tabColors[9], tabColors[10], tabColors[11]};
                } else {
                    return new Color[] {null, null, null, null};
                }
            default:
                throw new IllegalStateException("Unexpected value: " + l);
        }
    }

    public ItemShape(String str) {
        int nbQuarts = str.length() / 2;
        tabSubShapes = new SubShape[nbQuarts];
        tabColors = new Color[nbQuarts];

        for (int i = 0; i < nbQuarts; i++) {
            switch (str.charAt(i * 2)) {
                case 'C' : tabSubShapes[i] = SubShape.Carre; break;
                case 'c' : tabSubShapes[i] = SubShape.Circle; break;
                case 'F' : tabSubShapes[i] = SubShape.Fan; break;
                case 'S' : tabSubShapes[i] = SubShape.Star; break;
                case '-' : tabSubShapes[i] = SubShape.None; break;
                default:
                    throw new IllegalStateException("Unexpected shape: " + str.charAt(i * 2));
            }

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

    public void stack(ItemShape shapeSup) {
        System.out.println("Empilement effectué (à implémenter)");
    }

    /**
     * Découpe la forme horizontalement (haut/bas)
     * @return la partie haute (indices 0 et 1)
     * this devient la partie basse (indices 2 et 3)
     */
    public ItemShape Cut() {
        if (tabSubShapes.length < 4) {
            System.out.println("Impossible de découper : forme déjà coupée");
            return new ItemShape("----");
        }

        // Partie HAUTE (Nord) : indices 0 et 1
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

        // Partie BASSE (Est) : indices 2 et 3
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

        System.out.println("=== DÉCOUPE HORIZONTALE ===");
        System.out.println("Original: " + this.toString());
        System.out.println("Partie haute (Nord): " + chaineHaut);
        System.out.println("Partie basse (Est): " + chaineBas);

        ItemShape formeHaut = new ItemShape(chaineHaut);
        ItemShape formeBas = new ItemShape(chaineBas);

        this.tabSubShapes = formeBas.tabSubShapes;
        this.tabColors = formeBas.tabColors;
        this.cut = true;
        formeHaut.cut = true;

        return formeHaut;
    }

    private String construireChaine(SubShape[] subs, Color[] colors) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subs.length; i++) {
            // Forme
            switch (subs[i]) {
                case Carre: sb.append('C'); break;
                case Circle: sb.append('c'); break;
                case Fan: sb.append('F'); break;
                case Star: sb.append('S'); break;
                case None: sb.append('-'); break;
                default: sb.append('?');
            }
            // Couleur
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

    public void Color(Color c) {
        for (int i = 0; i < tabColors.length; i++) {
            if (tabSubShapes[i] != SubShape.None) {
                tabColors[i] = c;
            }
        }
        System.out.println("Coloration en " + c);
    }

    public void Color(Color c, Layer layer) {
        int startIdx;
        switch(layer) {
            case one: startIdx = 0; break;
            case two: startIdx = 4; break;
            case three: startIdx = 8; break;
            default: return;
        }

        for (int i = startIdx; i < startIdx + 4 && i < tabColors.length; i++) {
            if (tabColors[i] != null) {
                tabColors[i] = c;
            }
        }
    }

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