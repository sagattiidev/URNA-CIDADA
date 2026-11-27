package br.unijorge.urnacidada.model;

import java.util.List;

public class CandidatoProporcional extends Candidato {

    public CandidatoProporcional(Cargo cargo, String numero, String nome, Partido partido) {
        super(cargo, numero, nome, partido);
    }

    @Override
    public List<String> getLinhasDetalhe() {
        return List.of("Nome: " + getNome(),
                "Partido: " + getPartido().getSigla());
    }
}
