package modele.plateau;

import modele.item.ItemShape;

public class Cutter extends Machine {
    private ItemShape buffer;
    private boolean aGauche = true;

    public Cutter() {
        super();
        buffer = null;
        aGauche = true;
    }

    @Override
    public void work() {
        if (!current.isEmpty() && buffer == null) {
            ItemShape original = (ItemShape) current.removeFirst();

            String representation = original.toString();
            ItemShape copie = new ItemShape(representation);

            System.out.println("Cutter : item reçu = " + representation);

            ItemShape partieHaute = copie.Cut();
            ItemShape partieBasse = copie;

            current.addFirst(partieHaute);
            buffer = partieBasse;
            aGauche = true;
        }
    }

    @Override
    public void send() {
        // Envoyer la partie haute vers le Nord
        if (!current.isEmpty() && aGauche) {
            Case nextCaseNord = c.plateau.getCase(c, Direction.North);
            if (nextCaseNord != null) {
                Machine nextMachine = nextCaseNord.getMachine();
                if (nextMachine != null && nextMachine.hasPlace()) {
                    ItemShape item = (ItemShape) current.removeFirst();
                    nextMachine.current.add(item);
                    aGauche = false;
                    System.out.println("Cutter : envoi partie haute vers Nord -> " + item);
                }
            }
        }

        // Envoyer la partie basse vers l'Est
        if (buffer != null && !aGauche) {
            Case nextCaseEst = c.plateau.getCase(c, Direction.East);
            if (nextCaseEst != null) {
                Machine nextMachine = nextCaseEst.getMachine();
                if (nextMachine != null && nextMachine.hasPlace()) {
                    nextMachine.current.add(buffer);
                    System.out.println("Cutter : envoi partie basse vers Est -> " + buffer);
                    buffer = null;
                    aGauche = true;
                }
            }
        }
    }
}