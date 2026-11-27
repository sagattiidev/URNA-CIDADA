package br.unijorge.urnacidada.model;

/** Cargo proporcional (ex.: vereador): número com dígitos do partido + dígitos do candidato. */
public class CargoProporcional extends Cargo {

    private final int quantidadeDigitos;

    public CargoProporcional(String id, String nome, int quantidadeDigitos) {
        super(id, nome);
        this.quantidadeDigitos = quantidadeDigitos;
    }

    @Override
    public String getTipo() { return "PROPORCIONAL"; }

    @Override
    public int getQuantidadeDigitos() { return quantidadeDigitos; }

    @Override
    public Candidato criarCandidato(String numero, String nome, Partido partido, String vice) {
        return new CandidatoProporcional(this, numero, nome, partido);
    }
}
