package br.unijorge.urnacidada.service.firestore;

import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.Cargo;
import br.unijorge.urnacidada.model.Cargos;
import br.unijorge.urnacidada.model.Partido;
import br.unijorge.urnacidada.model.Voto;
import br.unijorge.urnacidada.service.PersistenciaException;
import br.unijorge.urnacidada.service.RepositorioVotacao;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Persistência no Cloud Firestore.
 * Coleções: "candidatos" (documento {cargo}_{numero}) e "votos" (ID estável da sessão e cargo).
 */
public class RepositorioFirestore implements RepositorioVotacao {

    static final String COLECAO_CANDIDATOS = "candidatos";
    static final String COLECAO_VOTOS = "votos";

    private final ClienteFirestore cliente;

    public RepositorioFirestore(ClienteFirestore cliente) {
        this.cliente = cliente;
    }

    @Override
    public List<Candidato> listarCandidatos() throws PersistenciaException {
        List<Candidato> candidatos = new ArrayList<>();
        for (Map<String, Object> doc : cliente.listar(COLECAO_CANDIDATOS)) {
            try {
                Cargo cargo = Cargos.porId((String) doc.get("cargo"));
                Partido partido = new Partido((String) doc.get("partidoNumero"),
                        (String) doc.get("partidoSigla"), (String) doc.get("partidoNome"));
                candidatos.add(cargo.criarCandidato((String) doc.get("numero"),
                        (String) doc.get("nome"), partido, (String) doc.get("vice")));
            } catch (RuntimeException e) {
                throw new PersistenciaException("Documento de candidato inválido ("
                        + doc.get("_id") + "): " + e.getMessage(), e);
            }
        }
        return candidatos;
    }

    @Override
    public boolean salvarCandidato(Candidato candidato) throws PersistenciaException {
        return cliente.criar(COLECAO_CANDIDATOS, candidato.getId(), candidato.paraRegistro());
    }

    @Override
    public void registrarVoto(Voto voto) throws PersistenciaException {
        // A confirmação pode ter sido gravada antes de uma falha de rede.
        // O mesmo voto em retry mantém o ID; 409 ALREADY_EXISTS não duplica.
        String id = voto.getSessaoId() + "_" + voto.getCargo().getId();
        cliente.criar(COLECAO_VOTOS, id, voto.paraRegistro());
    }
}
