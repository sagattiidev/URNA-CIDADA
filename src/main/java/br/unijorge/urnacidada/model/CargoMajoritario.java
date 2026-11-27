package br.unijorge.urnacidada.model;

/** Cargo majoritário (ex.: prefeito): o número é o do partido e o candidato tem vice. */
public class CargoMajoritario extends Cargo {

    private final int quantidadeDigitos;

    public CargoMajoritario(String id, String nome, int quantidadeDigitos) {
        super(id, nome);
        this.quantidadeDigitos = quantidadeDigitos;
    }

    @Override
    public String getTipo() { return "MAJORITARIO"; }

    @Override
    public int getQuantidadeDigitos() { return quantidadeDigitos; }

    @Override
    public Candidato criarCandidato(String numero, String nome, Partido partido, String vice) {
        return new CandidatoMajoritario(this, numero, nome, partido, vice);
    }
}
