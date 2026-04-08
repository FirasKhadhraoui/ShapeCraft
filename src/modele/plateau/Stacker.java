package modele.plateau;

import modele.item.Item;
import modele.item.ItemShape;

/**
 * Stacker : machine qui combine deux formes en une seule (empilement).
 *
 * Entrées :
 *   - Entrée basse (bottom) : vient de la direction d (derrière le stacker)
 *   - Entrée droite (right) : vient de la direction d.rotate90CCW() (côté droit)
 * Sortie :
 *   - Résultat empilé : part vers la direction d
 *
 * Exemple avec d = North :
 *   - Entrée basse : vient du Sud (tapis vers Nord)
 *   - Entrée droite : vient de l'Est (tapis vers Ouest)
 *   - Sortie : vers le Nord
 */
public class Stacker extends Machine {

    private ItemShape inputBottom = null;  // Item reçu par la direction d (base)
    private ItemShape inputRight  = null;  // Item reçu par le côté droit (à empiler dessus)
    private ItemShape output      = null;  // Résultat de l'empilement

    /**
     * Direction d'où vient l'item de droite.
     * Pour d = North : rotate90CCW() = West (l'item vient de l'Ouest)
     */
    private Direction rightSenderDir() {
        return d.rotate90CCW();
    }

    // Accepte un item si le slot correspondant est vide
    @Override
    public boolean hasPlaceFor(Direction senderDir) {
        if (senderDir == d)               return inputBottom == null;
        if (senderDir == rightSenderDir()) return inputRight  == null;
        return false;
    }

    // Stocke l'item dans le bon slot
    @Override
    public boolean receive(Item item, Direction senderDir) {
        if (senderDir == d && inputBottom == null) {
            inputBottom = (ItemShape) item;
        } else if (senderDir == rightSenderDir() && inputRight == null) {
            inputRight = (ItemShape) item;
        }
        return false;
    }

    // Quand les deux entrées sont présentes, on empile
    @Override
    public void work() {
        if (inputBottom != null && inputRight != null && output == null) {
            // Crée une copie de la forme de base
            ItemShape result = new ItemShape(inputBottom.toString());
            // Empile la forme de droite par-dessus
            result.stack(inputRight);
            result.setColorItem(false);
            output = result;
            // Vide les slots d'entrée
            inputBottom = null;
            inputRight  = null;
        }
    }

    // Envoie le résultat vers la direction d
    @Override
    public void send() {
        if (output == null) return;
        Case nextCase = c.plateau.getCase(c, d);
        if (nextCase != null) {
            Machine m = nextCase.getMachine();
            if (m != null && m.hasPlaceFor(d)) {
                m.receive(output, d);
                output = null;
            }
        }
    }

    // Retourne l'item actuellement dans le stacker (priorité: output, inputBottom, inputRight)
    @Override
    public Item getCurrent() {
        if (output != null)      return output;
        if (inputBottom != null) return inputBottom;
        return inputRight;
    }

    // Vide tous les slots
    @Override
    public void clearCurrent() {
        super.clearCurrent();
        inputBottom = null;
        inputRight  = null;
        output      = null;
    }
}