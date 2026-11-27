package br.unijorge.urnacidada.model;

import java.util.List;

/** Cargos da simulação, na ordem em que aparecem para o eleitor (como na urna real). */
public final class Cargos {

    public static final Cargo VEREADOR = new CargoProporcional("vereador", "Vereador", 5);
    public static final Cargo PREFEITO = new CargoMajoritario("prefeito", "Prefeito", 2);

    private Cargos() { }

    public static List<Cargo> sequencia() {
        return List.of(VEREADOR, PREFEITO);
    }

    public static Cargo porId(String id) {
        return sequencia().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cargo desconhecido: " + id));
    }
}
