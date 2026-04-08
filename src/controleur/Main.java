package controleur;

import vuecontroleur.VueControleur;
import modele.jeu.Jeu;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        // Ajouter un listener pour la fermeture de la fenêtre
        vc.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        vc.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitter();
            }
        });

        vc.setVisible(true);
    }

    private boolean demanderSauvegarde(String message) {
        int response = JOptionPane.showConfirmDialog(null,
                message,
                "Sauvegarder", JOptionPane.YES_NO_CANCEL_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            sauvegarder();
            return true;
        } else if (response == JOptionPane.NO_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    public void nouvellePartie() {
        if (!demanderSauvegarde("Voulez-vous sauvegarder avant de commencer une nouvelle partie ?")) {
            return;
        }

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

    public void quitter() {
        if (demanderSauvegarde("Voulez-vous sauvegarder avant de quitter ?")) {
            System.exit(0);
        }
    }
}