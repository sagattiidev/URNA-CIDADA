package br.unijorge.urnacidada.model;

import java.util.Map;

public class VotoCandidato extends Voto {

    private final Candidato candidato;

    public VotoCandidato(String sessaoId, Candidato candidato) {
        super(sessaoId, candidato.getCargo());
        this.candidato = candidato;
    }

    public Candidato getCandidato() { return candidato; }

    @Override
    public TipoVoto getTipo() { return TipoVoto.CANDIDATO; }

    @Override
    protected void completarRegistro(Map<String, Object> registro) {
        registro.put("numero", candidato.getNumero());
        registro.put("candidato", candidato.getNome());
    }
}
