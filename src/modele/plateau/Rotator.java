package modele.plateau;

import modele.item.ItemShape;

/**
 * Rotator : machine qui fait pivoter une forme de 90° dans le sens horaire.
 * La rotation n'est appliquée qu'une seule fois à l'arrivée de l'item.
 */
public class Rotator extends Machine {

    private boolean rotated = false;

    @Override
    public void work() {
        if (!current.isEmpty() && !rotated) {
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

            rotated = true;

            System.out.println("Rotator : rotation appliquée à un item");
        }
    }

    @Override
    public void send() {
        // Sauvegarder l'état de current avant l'envoi
        boolean hadItem = !current.isEmpty();

        super.send(); // Tentative d'envoi

        // Si l'item est parti (current est vide), réinitialiser le flag
        if (hadItem && current.isEmpty()) {
            rotated = false;
        }
    }

    @Override
    public void clearCurrent() {
        super.clearCurrent();
        rotated = false;
    }
}