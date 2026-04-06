package modele.plateau;

import modele.item.ItemShape;

public class Mine extends Machine {
    private int compteur = 0;
    private static final int DELAI_PRODUCTION = 4;

    @Override
    public void work() {
        if (c.getGisement() != null) {
            compteur++;
            if (compteur >= DELAI_PRODUCTION && hasPlace()) {
                ItemShape gisement = (ItemShape) c.getGisement();
                ItemShape produced = new ItemShape(gisement.toString());
                produced.setColorItem(gisement.isColorItem());
                current.add(produced);
                compteur = 0;
                System.out.println("Mine : production d'un item");
            }
        }
    }

    @Override
    public void send() {
        super.send();
    }
}