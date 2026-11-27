package br.unijorge.urnacidada.model;

import java.util.List;
import java.util.Map;

public class CandidatoMajoritario extends Candidato {

    private final String vice;

    public CandidatoMajoritario(Cargo cargo, String numero, String nome, Partido partido, String vice) {
        super(cargo, numero, nome, partido);
        if (vice == null || vice.isBlank()) {
            throw new IllegalArgumentException("Candidato majoritário exige vice.");
        }
        this.vice = vice;
    }

    public String getVice() { return vice; }

    @Override
    public List<String> getLinhasDetalhe() {
        return List.of("Nome: " + getNome(),
                "Partido: " + getPartido().getSigla(),
                "Vice: " + vice);
    }

    @Override
    public Map<String, Object> paraRegistro() {
        Map<String, Object> r = super.paraRegistro();
        r.put("vice", vice);
        return r;
    }
}
