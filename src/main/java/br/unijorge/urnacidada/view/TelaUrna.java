package br.unijorge.urnacidada.view;

import br.unijorge.urnacidada.controller.EstadoTela;
import br.unijorge.urnacidada.controller.Fase;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Tela (display) da urna: mostra avisos, o cargo, o número digitado e o candidato identificado. */
public final class TelaUrna extends JPanel {

    private static final String CARTA_AVISO = "aviso";
    private static final String CARTA_VOTO = "voto";
    private static final Color VERMELHO = new Color(0xB00020);

    private final CardLayout cartas = new CardLayout();

    private final JLabel avisoTitulo = rotulo("", 60, Font.BOLD);
    private final JLabel avisoDetalhe = rotulo("", 20, Font.PLAIN);

    private final JLabel seuVoto = rotulo("SEU VOTO PARA", 16, Font.PLAIN);
    private final JLabel cargo = rotulo("", 34, Font.BOLD);
    private final JLabel rotuloNumero = rotulo("Número:", 18, Font.PLAIN);
    private final CaixasNumero caixas = new CaixasNumero();
    private final JPanel detalhes = alturaNatural();
    private final FotoCandidato foto = new FotoCandidato();
    private final JLabel rodapeTeclas = rotulo("", 14, Font.PLAIN);
    private final JLabel rodapeMensagem = rotulo(" ", 14, Font.BOLD);

    public TelaUrna() {
        setLayout(cartas);
        setBackground(new Color(0xF4F4F4));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        add(montarAviso(), CARTA_AVISO);
        add(montarVoto(), CARTA_VOTO);
        atualizar(new EstadoTela(Fase.CARREGANDO, null, 1, 1, "", 0, null, false, false, null));
    }

    private JPanel montarAviso() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        avisoTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        avisoDetalhe.setHorizontalAlignment(SwingConstants.CENTER);
        avisoTitulo.setAlignmentX(CENTER_ALIGNMENT);
        avisoDetalhe.setAlignmentX(CENTER_ALIGNMENT);
        p.add(Box.createVerticalGlue());
        p.add(avisoTitulo);
        p.add(Box.createVerticalStrut(18));
        p.add(avisoDetalhe);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel montarVoto() {
        JPanel esquerda = new JPanel();
        esquerda.setOpaque(false);
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));

        detalhes.setOpaque(false);
        detalhes.setLayout(new BoxLayout(detalhes, BoxLayout.Y_AXIS));

        JPanel linhaNumero = alturaNatural();
        linhaNumero.setLayout(new BoxLayout(linhaNumero, BoxLayout.X_AXIS));
        linhaNumero.setOpaque(false);
        rotuloNumero.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        linhaNumero.add(rotuloNumero);
        linhaNumero.add(caixas);
        linhaNumero.add(Box.createHorizontalGlue());

        for (JComponent c : new JComponent[]{seuVoto, cargo, linhaNumero, detalhes}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
        }
        esquerda.add(seuVoto);
        esquerda.add(Box.createVerticalStrut(6));
        esquerda.add(cargo);
        esquerda.add(Box.createVerticalStrut(22));
        esquerda.add(linhaNumero);
        esquerda.add(Box.createVerticalStrut(18));
        esquerda.add(detalhes);
        esquerda.add(Box.createVerticalGlue());

        JPanel direita = new JPanel(new BorderLayout());
        direita.setOpaque(false);
        direita.add(foto, BorderLayout.NORTH);

        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.Y_AXIS));
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        rodapeTeclas.setAlignmentX(LEFT_ALIGNMENT);
        rodapeMensagem.setAlignmentX(LEFT_ALIGNMENT);
        rodapeMensagem.setForeground(VERMELHO);
        rodape.add(Box.createVerticalStrut(6));
        rodape.add(rodapeTeclas);
        rodape.add(Box.createVerticalStrut(4));
        rodape.add(rodapeMensagem);

        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(22, 26, 14, 26));
        p.add(esquerda, BorderLayout.CENTER);
        p.add(direita, BorderLayout.EAST);
        p.add(rodape, BorderLayout.SOUTH);
        return p;
    }

    public void atualizar(EstadoTela e) {
        switch (e.fase()) {
            case CARREGANDO -> aviso("URNA CIDADÃ", "Conectando ao Firebase e carregando candidatos...", Color.BLACK, 60);
            case INICIO -> aviso("URNA PRONTA",
                    "<html><center>Simulador educativo - candidatos e partidos fictícios.<br><br>"
                            + "Aperte <b>CONFIRMA</b> para iniciar a votação.</center></html>", Color.BLACK, 60);
            case SALVANDO -> aviso("AGUARDE", "Registrando seu voto...", Color.BLACK, 60);
            case FIM -> aviso("FIM", "<html><center>Voto registrado.<br><br>"
                    + "Aperte <b>CONFIRMA</b> para um novo eleitor.</center></html>", Color.BLACK, 110);
            case ERRO -> aviso("ERRO", "<html><center>" + escapar(e.mensagem()) + "<br><br>"
                    + "Aperte <b>CONFIRMA</b> para tentar novamente.</center></html>", VERMELHO, 60);
            case VOTANDO -> mostrarVoto(e);
        }
    }

    private void aviso(String titulo, String detalhe, Color cor, int tamanho) {
        avisoTitulo.setText(titulo);
        avisoTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanho));
        avisoTitulo.setForeground(cor);
        avisoDetalhe.setText(detalhe);
        avisoDetalhe.setForeground(cor);
        cartas.show(this, CARTA_AVISO);
    }

    private void mostrarVoto(EstadoTela e) {
        cartas.show(this, CARTA_VOTO);
        cargo.setText(e.cargo().getNome().toUpperCase());
        seuVoto.setText("SEU VOTO PARA   (cargo " + e.posicaoCargo() + " de " + e.totalCargos() + ")");
        boolean emBranco = e.votoEmBranco();
        rotuloNumero.setVisible(!emBranco);
        caixas.setVisible(!emBranco);
        caixas.atualizar(e.quantidadeDigitos(), e.digitos());

        detalhes.removeAll();
        foto.setCandidato(e.candidato());
        if (emBranco) {
            detalhes.add(rotulo("VOTO EM BRANCO", 30, Font.BOLD));
        } else if (e.candidato() != null) {
            e.candidato().getLinhasDetalhe().forEach(l -> detalhes.add(rotulo(l, 20, Font.PLAIN)));
        } else if (e.numeroNulo()) {
            detalhes.add(rotulo("NÚMERO ERRADO", 22, Font.PLAIN));
            detalhes.add(rotulo("VOTO NULO", 30, Font.BOLD));
        }
        detalhes.revalidate();
        detalhes.repaint();

        String teclas = "<html>Aperte a tecla:<br>"
                + "&nbsp;&nbsp;<font color='#1E7B34'><b>VERDE</b></font> para CONFIRMAR<br>"
                + "&nbsp;&nbsp;<font color='#C26A00'><b>LARANJA</b></font> para CORRIGIR"
                + (e.digitos().isEmpty() && !emBranco ? "<br>&nbsp;&nbsp;<b>BRANCO</b> para votar em branco" : "")
                + "</html>";
        rodapeTeclas.setText(teclas);
        rodapeMensagem.setText(e.mensagem() == null ? " " : e.mensagem());
    }

    /** Painel que não estica na vertical dentro de um BoxLayout. */
    private static JPanel alturaNatural() {
        return new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
    }

    private static String escapar(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JLabel rotulo(String texto, int tamanho, int estilo) {
        JLabel l = new JLabel(texto, SwingConstants.LEFT);
        l.setFont(new Font(Font.SANS_SERIF, estilo, tamanho));
        return l;
    }
}
