package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;
import modele.item.Color;

/**
 * Painter : colore une forme avec une couleur.
 * Entrée couleur : Nord (tapis vers Sud)
 * Entrée forme : Ouest (tapis vers Est)
 * Sortie : Est (tapis vers Est)
 */
public class AtelierPeinture extends Machine {

    private ItemShape colorSlot = null; // Stocke la couleur reçue
    private ItemShape shapeSlot = null; // Stocke la forme reçue

    // Sortie toujours vers l'Est
    public AtelierPeinture() {
        this.d = Direction.East;
    }

    // Vérifie si le slot correspondant à la direction est libre
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        if (senderDir == Direction.South) return colorSlot == null; // Couleur
        if (senderDir == Direction.East) return shapeSlot == null;  // Forme
        return false;
    }

    // Stocke l'item dans le bon slot
    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == Direction.South) colorSlot = (ItemShape) item;
        else if (senderDir == Direction.East) shapeSlot = (ItemShape) item;
        return false;
    }

    // Si on a couleur ET forme, on colore et on sort
    @Override
    public void work() {
        if (colorSlot != null && shapeSlot != null && current.isEmpty()) {
            Color c = extractColor(colorSlot);
            if (c != null) {
                // Copie de la forme (ne pas modifier l'original)
                ItemShape painted = new ItemShape(shapeSlot.toString());
                painted.Color(c);           // Applique la couleur
                painted.setColorItem(false);
                painted.setCut(shapeSlot.isCut()); // Garde l'état coupé
                current.add(painted);
            }
            // Vide les slots
            colorSlot = null;
            shapeSlot = null;
        }
    }

    // Extrait la couleur d'un item de couleur (première couleur non nulle)
    private Color extractColor(ItemShape colorItem) {
        for (Color c : colorItem.getColors()) {
            if (c != null) return c;
        }
        return null;
    }

    // Envoie l'item coloré vers l'Est
    @Override
    public void send() {
        if (!current.isEmpty()) {
            Case nextCase = c.plateau.getCase(c, Direction.East);
            if (nextCase != null) {
                Machine nextMachine = nextCase.getMachine();
                if (nextMachine != null && nextMachine.hasPlaceFor(Direction.East)) {
                    Item item = current.removeFirst();
                    nextMachine.receive(item, Direction.East);
                }
            }
        }
    }

    // Retourne l'item en sortie
    @Override
    public Item getCurrent() {
        if (!current.isEmpty()) return current.getFirst();
        return null;
    }

    // Vide tout (sortie + slots)
    @Override
    public void clearCurrent() {
        super.clearCurrent();
        colorSlot = null;
        shapeSlot = null;
    }
}