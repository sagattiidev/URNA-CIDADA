package br.unijorge.urnacidada.view;

import br.unijorge.urnacidada.model.Candidato;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** Espaço da foto: como os candidatos são fictícios, mostra uma silhueta ilustrativa. */
final class FotoCandidato extends JComponent {

    private Candidato candidato;

    FotoCandidato() {
        setPreferredSize(new Dimension(150, 200));
    }

    void setCandidato(Candidato candidato) {
        this.candidato = candidato;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        if (candidato == null) return;
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 4, h = getHeight() - 30;
        g.setClip(2, 2, w, h);
        g.setColor(new Color(0xC9D6E2));
        g.fillRect(2, 2, w, h);
        g.setColor(new Color(0x7C8FA3));
        g.fillOval(w / 2 - 28, h / 4, 60, 60);
        g.fillOval(w / 2 - 55, h / 4 + 70, 115, 140);
        g.setClip(null);
        g.setColor(Color.BLACK);
        g.drawRect(2, 2, w, h);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();
        String legenda = "Foto ilustrativa";
        g.drawString(legenda, 2 + (w - fm.stringWidth(legenda)) / 2, h + 18);
        g.dispose();
    }
}
