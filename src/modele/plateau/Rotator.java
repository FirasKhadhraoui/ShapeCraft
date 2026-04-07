package modele.plateau;

import modele.item.ItemShape;

public class Rotator extends Machine {

    @Override
    public void work() {
        if (!current.isEmpty()) {
            ItemShape original = (ItemShape) current.getFirst();

            // Créer une copie de l'item original
            String representation = original.toString();
            ItemShape copie = new ItemShape(representation);
            copie.setColorItem(original.isColorItem());
            copie.setCut(original.isCut());

            // Appliquer la rotation sur la copie
            copie.rotate();

            // Remplacer l'original par la copie modifiée
            current.removeFirst();
            current.addFirst(copie);

            System.out.println("Rotator : rotation appliquée à un item");
        }
    }
}