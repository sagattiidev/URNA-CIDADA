package br.unijorge.urnacidada.service;

import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.Cargo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Carrega os candidatos do Firestore (criando os dados fictícios na primeira execução) e os consulta. */
public class CandidatoService {

    private final RepositorioVotacao repositorio;
    private final Map<String, Candidato> porId = new HashMap<>();

    public CandidatoService(RepositorioVotacao repositorio) {
        this.repositorio = repositorio;
    }

    public synchronized void inicializar() throws PersistenciaException {
        List<Candidato> existentes = repositorio.listarCandidatos();
        // Completa uma carga interrompida, sem sobrescrever candidatos existentes.
        Map<String, Candidato> esperados = new HashMap<>();
        for (Candidato c : CandidatosFicticios.lista()) esperados.put(c.getId(), c);
        Map<String, Candidato> carregados = validarCatalogo(existentes, esperados);
        if (carregados.size() != esperados.size()) {
            for (Candidato c : CandidatosFicticios.lista()) {
                if (!carregados.containsKey(c.getId())) repositorio.salvarCandidato(c);
            }
            carregados = validarCatalogo(repositorio.listarCandidatos(), esperados);
        }
        if (carregados.size() != esperados.size()) {
            throw new PersistenciaException("Catálogo fictício incompleto; confira a configuração do Firestore.");
        }
        porId.clear();
        porId.putAll(carregados);
    }

    private static Map<String, Candidato> validarCatalogo(List<Candidato> existentes,
            Map<String, Candidato> esperados) throws PersistenciaException {
        Map<String, Candidato> resultado = new HashMap<>();
        for (Candidato c : existentes) {
            Candidato esperado = esperados.get(c.getId());
            if (esperado == null || !esperado.paraRegistro().equals(c.paraRegistro())
                    || resultado.putIfAbsent(c.getId(), c) != null) {
                throw new PersistenciaException("Catálogo diferente dos candidatos fictícios oficiais deste simulador. Confira o banco de demonstração.");
            }
        }
        return resultado;
    }

    public synchronized Optional<Candidato> buscar(Cargo cargo, String numero) {
        return Optional.ofNullable(porId.get(cargo.getId() + "_" + numero));
    }
}
