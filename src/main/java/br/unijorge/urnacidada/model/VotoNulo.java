package br.unijorge.urnacidada.model;

import java.util.Map;

/** Voto nulo: o eleitor digitou um número completo que não pertence a nenhum candidato. */
public class VotoNulo extends Voto {

    private final String numeroDigitado;

    public VotoNulo(String sessaoId, Cargo cargo, String numeroDigitado) {
        super(sessaoId, cargo);
        if (numeroDigitado == null || !numeroDigitado.matches("[0-9]{" + cargo.getQuantidadeDigitos() + "}")) {
            throw new IllegalArgumentException("Número do voto nulo deve ter a quantidade de dígitos do cargo.");
        }
        this.numeroDigitado = numeroDigitado;
    }

    @Override
    public TipoVoto getTipo() { return TipoVoto.NULO; }

    @Override
    protected void completarRegistro(Map<String, Object> registro) {
        registro.put("numero", numeroDigitado);
    }
}
