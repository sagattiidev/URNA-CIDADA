package br.unijorge.urnacidada.view;

import br.unijorge.urnacidada.controller.UrnaController;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Teclado numérico com as teclas BRANCO, CORRIGE e CONFIRMA. */
public final class TecladoUrna extends JPanel {

    public TecladoUrna(UrnaController controller) {
        super(new GridBagLayout());
        setBackground(new Color(0x3A3A3A));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        JLabel titulo = new JLabel("URNA CIDADÃ", SwingConstants.CENTER);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titulo.setForeground(Color.WHITE);
        GridBagConstraints t = new GridBagConstraints();
        t.gridx = 0; t.gridy = 0; t.gridwidth = 3; t.fill = GridBagConstraints.HORIZONTAL;
        t.insets = new Insets(0, 0, 14, 0);
        add(titulo, t);

        int[] ordem = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        for (int i = 0; i < ordem.length; i++) {
            int d = ordem[i];
            int linha = 1 + i / 3;
            int coluna = d == 0 ? 1 : i % 3;
            add(tecla(String.valueOf(d), Color.BLACK, Color.WHITE, 22, () -> controller.digitar(d)),
                    posicao(coluna, linha));
        }
        add(tecla("BRANCO", Color.WHITE, Color.BLACK, 12, controller::votarEmBranco), posicao(0, 5));
        add(tecla("CORRIGE", new Color(0xF28C28), Color.BLACK, 12, controller::corrigir), posicao(1, 5));
        add(tecla("CONFIRMA", new Color(0x2E9E3F), Color.BLACK, 12, controller::confirmar), posicao(2, 5));
    }

    private static GridBagConstraints posicao(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y; c.weightx = 1; c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        return c;
    }

    private static JButton tecla(String texto, Color fundo, Color letra, int tamanho, Runnable acao) {
        JButton b = new JButton(texto);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanho));
        b.setBackground(fundo);
        b.setForeground(letra);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(new Color(0x111111), 2));
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.addActionListener(ev -> acao.run());
        return b;
    }
}
