package br.unijorge.urnacidada.service;

import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.Voto;
import java.util.List;

/** Abstração da persistência. A implementação real usa o Firebase Cloud Firestore. */
public interface RepositorioVotacao {

    List<Candidato> listarCandidatos() throws PersistenciaException;

    /** Grava o candidato se ainda não existir. Retorna true se criou, false se já existia. */
    boolean salvarCandidato(Candidato candidato) throws PersistenciaException;

    void registrarVoto(Voto voto) throws PersistenciaException;
}
