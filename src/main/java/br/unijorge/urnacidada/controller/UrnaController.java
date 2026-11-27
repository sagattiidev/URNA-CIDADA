package br.unijorge.urnacidada.controller;

import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.Cargo;
import br.unijorge.urnacidada.model.Voto;
import br.unijorge.urnacidada.model.VotoBranco;
import br.unijorge.urnacidada.model.VotoCandidato;
import br.unijorge.urnacidada.model.VotoNulo;
import br.unijorge.urnacidada.service.CandidatoService;
import br.unijorge.urnacidada.service.PersistenciaException;
import br.unijorge.urnacidada.service.VotacaoService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Controla o fluxo da urna: inicialização, votação sequencial nos cargos, correção, branco,
 * nulo, confirmação e encerramento. Independe de Swing: o acesso ao banco roda no executor de
 * segundo plano e os resultados voltam pelo executor da interface, onde todo o estado é alterado.
 */
public class UrnaController {

    private final CandidatoService candidatos;
    private final VotacaoService votacao;
    private final List<Cargo> cargos;
    private final Executor segundoPlano;
    private final Executor threadDaInterface;
    private final List<UrnaListener> listeners = new ArrayList<>();

    private Fase fase = Fase.CARREGANDO;
    private int posicao;
    private String sessaoId;
    private final StringBuilder digitos = new StringBuilder();
    private Candidato candidatoAtual;
    private boolean branco;
    private String mensagem;
    private Runnable repetir;

    public UrnaController(CandidatoService candidatos, VotacaoService votacao, List<Cargo> cargos,
                          Executor segundoPlano, Executor threadDaInterface) {
        this.candidatos = candidatos;
        this.votacao = votacao;
        this.cargos = List.copyOf(cargos);
        this.segundoPlano = segundoPlano;
        this.threadDaInterface = threadDaInterface;
    }

    public void adicionarListener(UrnaListener listener) {
        listeners.add(listener);
    }

    public EstadoTela getEstado() {
        Cargo cargo = fase == Fase.VOTANDO || fase == Fase.SALVANDO || fase == Fase.ERRO
                ? cargos.get(Math.min(posicao, cargos.size() - 1)) : null;
        int qtd = cargo == null ? 0 : cargo.getQuantidadeDigitos();
        boolean completo = cargo != null && digitos.length() == qtd;
        return new EstadoTela(fase, cargo, posicao + 1, cargos.size(), digitos.toString(), qtd,
                candidatoAtual, branco, completo && candidatoAtual == null, mensagem);
    }

    // ---------- ações do eleitor ----------

    /** Carrega os candidatos do Firestore (cria os dados fictícios se a coleção estiver vazia). */
    public void inicializar() {
        fase = Fase.CARREGANDO;
        mensagem = null;
        notificar();
        segundoPlano.execute(() -> {
            try {
                candidatos.inicializar();
                threadDaInterface.execute(() -> { fase = Fase.INICIO; notificar(); });
            } catch (PersistenciaException e) {
                threadDaInterface.execute(() -> falhar(e.getMessage(), this::inicializar));
            }
        });
    }

    public void digitar(int digito) {
        if (fase != Fase.VOTANDO || branco || digito < 0 || digito > 9) return;
        Cargo cargo = cargoAtual();
        if (digitos.length() >= cargo.getQuantidadeDigitos()) return;
        digitos.append(digito);
        mensagem = null;
        if (digitos.length() == cargo.getQuantidadeDigitos()) {
            candidatoAtual = candidatos.buscar(cargo, digitos.toString()).orElse(null);
        }
        notificar();
    }

    public void corrigir() {
        if (fase != Fase.VOTANDO) return;
        limparEscolha();
        notificar();
    }

    public void votarEmBranco() {
        if (fase != Fase.VOTANDO) return;
        if (digitos.length() > 0) {
            mensagem = "Para votar em BRANCO, aperte CORRIGE e depois BRANCO.";
        } else {
            branco = true;
            mensagem = null;
        }
        notificar();
    }

    /** A tecla CONFIRMA faz o que a fase atual pede: iniciar, confirmar voto, novo eleitor ou tentar de novo. */
    public void confirmar() {
        switch (fase) {
            case INICIO -> iniciarVotacao();
            case VOTANDO -> confirmarVoto();
            case FIM -> { fase = Fase.INICIO; notificar(); }
            case ERRO -> { if (repetir != null) repetir.run(); }
            default -> { }
        }
    }

    // ---------- implementação ----------

    private void iniciarVotacao() {
        sessaoId = votacao.abrirSessao();
        posicao = 0;
        limparEscolha();
        fase = Fase.VOTANDO;
        notificar();
    }

    private void confirmarVoto() {
        Voto voto = montarVoto();
        if (voto == null) {
            mensagem = "Digite o número completo ou aperte BRANCO.";
            notificar();
            return;
        }
        persistir(voto);
    }

    /** Polimorfismo: o controller trata qualquer Voto do mesmo jeito, seja candidato, branco ou nulo. */
    private Voto montarVoto() {
        Cargo cargo = cargoAtual();
        if (branco) return new VotoBranco(sessaoId, cargo);
        if (digitos.length() != cargo.getQuantidadeDigitos()) return null;
        if (candidatoAtual != null) return new VotoCandidato(sessaoId, candidatoAtual);
        return new VotoNulo(sessaoId, cargo, digitos.toString());
    }

    private void persistir(Voto voto) {
        fase = Fase.SALVANDO;
        mensagem = null;
        notificar();
        segundoPlano.execute(() -> {
            try {
                votacao.registrar(voto);
                threadDaInterface.execute(this::votoRegistrado);
            } catch (PersistenciaException e) {
                threadDaInterface.execute(() -> falhar(e.getMessage(), () -> persistir(voto)));
            }
        });
    }

    private void votoRegistrado() {
        posicao++;
        limparEscolha();
        fase = posicao < cargos.size() ? Fase.VOTANDO : Fase.FIM;
        notificar();
    }

    private void falhar(String detalhe, Runnable acaoRepetir) {
        fase = Fase.ERRO;
        mensagem = detalhe;
        repetir = acaoRepetir;
        notificar();
    }

    private void limparEscolha() {
        digitos.setLength(0);
        candidatoAtual = null;
        branco = false;
        mensagem = null;
    }

    private Cargo cargoAtual() {
        return cargos.get(posicao);
    }

    private void notificar() {
        EstadoTela estado = getEstado();
        for (UrnaListener l : listeners) {
            l.estadoAlterado(estado);
        }
    }
}
