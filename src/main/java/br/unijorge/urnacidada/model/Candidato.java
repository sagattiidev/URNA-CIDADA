package br.unijorge.urnacidada.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Candidato fictício. Subclasses definem o que é exibido e o que é persistido a mais. */
public abstract class Candidato {

    private final Cargo cargo;
    private final String numero;
    private final String nome;
    private final Partido partido;

    protected Candidato(Cargo cargo, String numero, String nome, Partido partido) {
        if (cargo == null || partido == null) {
            throw new IllegalArgumentException("Cargo e partido são obrigatórios.");
        }
        if (numero == null || !numero.matches("\\d{" + cargo.getQuantidadeDigitos() + "}")) {
            throw new IllegalArgumentException("O número deve ter " + cargo.getQuantidadeDigitos()
                    + " dígitos para o cargo " + cargo.getNome() + ".");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do candidato é obrigatório.");
        }
        this.cargo = cargo;
        this.numero = numero;
        this.nome = nome;
        this.partido = partido;
    }

    public Cargo getCargo() { return cargo; }
    public String getNumero() { return numero; }
    public String getNome() { return nome; }
    public Partido getPartido() { return partido; }

    /** Identificador do documento no Firestore. */
    public String getId() { return cargo.getId() + "_" + numero; }

    /** Linhas exibidas na tela da urna quando o candidato é identificado. */
    public abstract List<String> getLinhasDetalhe();

    /** Campos gravados na coleção "candidatos". */
    public Map<String, Object> paraRegistro() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("cargo", cargo.getId());
        r.put("numero", numero);
        r.put("nome", nome);
        r.put("partidoNumero", partido.getNumero());
        r.put("partidoSigla", partido.getSigla());
        r.put("partidoNome", partido.getNome());
        r.put("vice", null);
        return r;
    }
}
