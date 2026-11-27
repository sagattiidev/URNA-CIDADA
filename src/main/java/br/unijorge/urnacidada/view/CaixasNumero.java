package br.unijorge.urnacidada.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Quadradinhos onde aparecem os dígitos digitados, como na tela da urna. */
final class CaixasNumero extends JPanel {

    private JLabel[] caixas = new JLabel[0];

    CaixasNumero() {
        super(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setOpaque(false);
    }

    void atualizar(int quantidade, String digitos) {
        if (caixas.length != quantidade) {
            removeAll();
            caixas = new JLabel[quantidade];
            for (int i = 0; i < quantidade; i++) {
                JLabel c = new JLabel("", SwingConstants.CENTER);
                c.setPreferredSize(new Dimension(46, 56));
                c.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
                c.setOpaque(true);
                c.setBackground(Color.WHITE);
                c.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                caixas[i] = c;
                add(c);
            }
            revalidate();
        }
        for (int i = 0; i < caixas.length; i++) {
            caixas[i].setText(i < digitos.length() ? String.valueOf(digitos.charAt(i)) : "");
        }
        repaint();
    }
}
