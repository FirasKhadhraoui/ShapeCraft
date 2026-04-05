package modele.plateau;

import modele.item.ItemShape;
import modele.item.Color;

public class AtelierPeinture extends Machine {
    private Color couleur;

    public AtelierPeinture() {
        this.couleur = Color.Red;
    }

    public AtelierPeinture(Color couleur) {
        this.couleur = couleur;
    }

    @Override
    public void work() {
        if (!current.isEmpty()) {
            ItemShape item = (ItemShape) current.getFirst();
            item.Color(couleur);
            System.out.println("AtelierPeinture : coloration en " + couleur);
        }
    }

    public void setCouleur(Color couleur) {
        this.couleur = couleur;
    }
}