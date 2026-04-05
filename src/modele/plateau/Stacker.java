package modele.plateau;

import modele.item.ItemShape;

public class Stacker extends Machine {
    private ItemShape buffer;

    public Stacker() {
        super();
        buffer = null;
    }

    @Override
    public void work() {
        if (buffer != null && !current.isEmpty()) {
            // On a un item en buffer et un item en entrée
            ItemShape item1 = buffer;
            ItemShape item2 = (ItemShape) current.removeFirst();

            // Empiler item1 sur item2
            item2.stack(item1);
            current.addFirst(item2);
            buffer = null;
            System.out.println("Stacker : empilement effectué");

        } else if (current.size() >= 2) {
            // Deux items en entrée
            ItemShape item1 = (ItemShape) current.removeFirst();
            ItemShape item2 = (ItemShape) current.removeFirst();

            item2.stack(item1);
            current.addFirst(item2);
            System.out.println("Stacker : empilement effectué");

        } else if (current.size() == 1 && buffer == null) {
            // Un seul item, on le met en buffer
            buffer = (ItemShape) current.removeFirst();
            System.out.println("Stacker : item mis en buffer, en attente d'un second");
        }
    }

    @Override
    public void send() {
        // On n'envoie que si on n'attend pas un deuxième item
        if (buffer == null && !current.isEmpty()) {
            super.send();
        }
    }
}