package br.unijorge.urnacidada.view;

import br.unijorge.urnacidada.controller.UrnaController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/** Janela principal: tela da urna à esquerda e teclado à direita; também aceita o teclado do computador. */
public final class JanelaUrna extends JFrame {

    public JanelaUrna(UrnaController controller) {
        super("Urna Cidadã - Simulador educativo de voto eletrônico");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        TelaUrna tela = new TelaUrna();
        TecladoUrna teclado = new TecladoUrna(controller);
        tela.setPreferredSize(new Dimension(660, 480));
        teclado.setPreferredSize(new Dimension(300, 480));

        JPanel conteudo = new JPanel(new BorderLayout(12, 0));
        conteudo.setBackground(new Color(0x222222));
        conteudo.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        conteudo.add(tela, BorderLayout.CENTER);
        conteudo.add(teclado, BorderLayout.EAST);
        setContentPane(conteudo);

        controller.adicionarListener(tela::atualizar);
        tela.atualizar(controller.getEstado());
        configurarTeclasDoComputador(controller);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void configurarTeclasDoComputador(UrnaController controller) {
        for (int d = 0; d <= 9; d++) {
            int digito = d;
            associar("d" + d, KeyEvent.VK_0 + d, () -> controller.digitar(digito));
            associar("n" + d, KeyEvent.VK_NUMPAD0 + d, () -> controller.digitar(digito));
        }
        associar("branco", KeyEvent.VK_B, controller::votarEmBranco);
        associar("corrige", KeyEvent.VK_BACK_SPACE, controller::corrigir);
        associar("corrige2", KeyEvent.VK_ESCAPE, controller::corrigir);
        associar("confirma", KeyEvent.VK_ENTER, controller::confirmar);
    }

    private void associar(String nome, int tecla, Runnable acao) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(tecla, 0), nome);
        getRootPane().getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
            }
        });
    }
}
