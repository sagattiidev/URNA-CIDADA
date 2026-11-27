package br.unijorge.urnacidada.model;

import java.util.Map;

public class VotoBranco extends Voto {

    public VotoBranco(String sessaoId, Cargo cargo) {
        super(sessaoId, cargo);
    }

    @Override
    public TipoVoto getTipo() { return TipoVoto.BRANCO; }

    @Override
    protected void completarRegistro(Map<String, Object> registro) {
        // voto em branco não tem número nem candidato
    }
}
