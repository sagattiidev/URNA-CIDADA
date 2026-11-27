package br.unijorge.urnacidada.service;

import br.unijorge.urnacidada.model.Voto;
import java.util.UUID;

/** Regras de uma votação: abre uma cédula anônima e registra cada voto no banco. */
public class VotacaoService {

    private final RepositorioVotacao repositorio;

    public VotacaoService(RepositorioVotacao repositorio) {
        this.repositorio = repositorio;
    }

    public String abrirSessao() {
        return UUID.randomUUID().toString();
    }

    public void registrar(Voto voto) throws PersistenciaException {
        repositorio.registrarVoto(voto);
    }
}
