package modele.plateau;

public class Poubelle extends Machine {

    @Override
    public void work() {
        // Supprimer tous les items reçus
        if (!current.isEmpty()) {
            current.clear();
            System.out.println("Poubelle : items supprimés");
        }
    }

    @Override
    public void send() {
        // La poubelle ne renvoie rien
    }
}