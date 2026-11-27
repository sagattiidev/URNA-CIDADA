package br.unijorge.urnacidada.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstração de um voto num cargo. O voto não guarda nenhuma identificação do eleitor:
 * apenas um identificador aleatório da sessão (cédula), que liga os votos de uma mesma votação.
 */
public abstract class Voto {

    private final String sessaoId;
    private final Cargo cargo;
    private final Instant registradoEm;

    protected Voto(String sessaoId, Cargo cargo) {
        if (sessaoId == null || sessaoId.isBlank() || cargo == null) {
            throw new IllegalArgumentException("Sessão e cargo são obrigatórios.");
        }
        this.sessaoId = sessaoId;
        this.cargo = cargo;
        this.registradoEm = Instant.now();
    }

    public String getSessaoId() { return sessaoId; }
    public Cargo getCargo() { return cargo; }
    public Instant getRegistradoEm() { return registradoEm; }

    public abstract TipoVoto getTipo();

    /** Acrescenta ao registro os campos específicos de cada tipo de voto. */
    protected abstract void completarRegistro(Map<String, Object> registro);

    /** Template method: monta o documento gravado na coleção "votos". */
    public final Map<String, Object> paraRegistro() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("sessaoId", sessaoId);
        r.put("cargo", cargo.getId());
        r.put("tipoCargo", cargo.getTipo());
        r.put("tipo", getTipo().name());
        r.put("numero", null);
        r.put("candidato", null);
        r.put("registradoEm", registradoEm);
        completarRegistro(r);
        return r;
    }
}
