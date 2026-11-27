package br.unijorge.urnacidada.model;

import java.util.Objects;

/**
 * Abstração de um cargo em disputa. Cada tipo de cargo (proporcional ou majoritário)
 * define, por polimorfismo, quantos dígitos tem o número e como cria seus candidatos.
 */
public abstract class Cargo {

    private final String id;
    private final String nome;

    protected Cargo(String id, String nome) {
        if (id == null || id.isBlank() || nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Id e nome do cargo são obrigatórios.");
        }
        this.id = id;
        this.nome = nome;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }

    /** "PROPORCIONAL" ou "MAJORITARIO". */
    public abstract String getTipo();

    /** Quantidade de dígitos do número de um candidato a este cargo. */
    public abstract int getQuantidadeDigitos();

    /** Fábrica polimórfica: cada cargo cria o tipo de candidato que lhe corresponde. */
    public abstract Candidato criarCandidato(String numero, String nome, Partido partido, String vice);

    @Override
    public boolean equals(Object o) {
        return o instanceof Cargo outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
