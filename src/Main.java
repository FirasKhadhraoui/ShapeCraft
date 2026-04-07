import vuecontroleur.VueControleur;
import modele.jeu.Jeu;

import javax.swing.*;

// Point d'entrée de l'application ShapeCraft
public class Main {
    public static void main(String[] args) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Jeu jeu = new Jeu();
                VueControleur vc = new VueControleur(jeu);
                jeu.getPlateau().addObserver(vc);
                vc.setVisible(true);
            }
        };

        SwingUtilities.invokeLater(r);

        }


    }
