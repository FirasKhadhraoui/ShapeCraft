package controleur;

import vuecontroleur.VueControleur;
import modele.jeu.Jeu;

import javax.swing.*;

public class Main {
    private Jeu jeu;
    private VueControleur vc;

    public static void main(String[] args) {
        Main main = new Main();
        SwingUtilities.invokeLater(() -> main.demarrerJeu());
    }

    public void demarrerJeu() {
        jeu = new Jeu();
        vc = new VueControleur(jeu, this);
        jeu.getPlateau().addObserver(vc);
        vc.setVisible(true);
    }

    public void nouvellePartie() {
        if (jeu != null) {
            jeu.interrupt();
            try {
                jeu.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (vc != null) {
            vc.dispose();
        }
        demarrerJeu();
    }

    public void sauvegarder() {
        if (jeu != null) {
            jeu.sauvegarder();
        }
    }

    public void charger() {
        if (jeu != null) {
            jeu.charger();
            if (vc != null) {
                vc.dispose();
            }
            vc = new VueControleur(jeu, this);
            jeu.getPlateau().addObserver(vc);
            vc.setVisible(true);
        }
    }
}