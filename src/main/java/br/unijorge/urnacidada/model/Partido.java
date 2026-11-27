package br.unijorge.urnacidada.model;

/** Partido fictício. Os números (91 a 94) não coincidem com legendas reais. */
public final class Partido {

    private final String numero;
    private final String sigla;
    private final String nome;

    public Partido(String numero, String sigla, String nome) {
        if (numero == null || !numero.matches("\\d{2}")) {
            throw new IllegalArgumentException("O número do partido deve ter 2 dígitos.");
        }
        if (sigla == null || sigla.isBlank() || nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Sigla e nome do partido são obrigatórios.");
        }
        this.numero = numero;
        this.sigla = sigla;
        this.nome = nome;
    }

    public String getNumero() { return numero; }
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
}
