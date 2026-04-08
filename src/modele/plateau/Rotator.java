package modele.plateau;

import modele.item.ItemShape;

/**
 * Rotator : machine qui fait pivoter une forme de 90° dans le sens horaire.
 *
 * Entrée : par n'importe quelle direction
 * Sortie : dans la direction d (paramétrable par rotation de la machine)
 *
 * La rotation est appliquée à une copie de l'item pour ne pas modifier
 * l'original (évite les effets de bord si le même item est référencé ailleurs).
 */
public class Rotator extends Machine {

    /**
     * Applique une rotation de 90° à l'item.
     * Crée une copie pour préserver l'original.
     */
    @Override
    public void work() {
        if (!current.isEmpty()) {
            // Récupère l'item original
            ItemShape original = (ItemShape) current.getFirst();

            // Crée une copie à partir de la chaîne de caractères
            String representation = original.toString();
            ItemShape copie = new ItemShape(representation);

            // Préserve les propriétés de l'original
            copie.setColorItem(original.isColorItem()); // Item de couleur ?
            copie.setCut(original.isCut());             // État coupé ?

            // Applique la rotation sur la copie
            copie.rotate();

            // Remplace l'original par la copie modifiée
            current.removeFirst();
            current.addFirst(copie);

            System.out.println("Rotator : rotation appliquée à un item");
        }
    }
}