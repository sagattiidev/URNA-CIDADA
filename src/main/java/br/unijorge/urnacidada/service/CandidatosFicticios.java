package br.unijorge.urnacidada.service;

import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.Cargos;
import br.unijorge.urnacidada.model.Partido;
import java.util.List;

/**
 * Dados de partida da simulação: partidos e candidatos totalmente fictícios, sem relação com
 * legendas ou pessoas reais. São gravados no Firestore automaticamente na primeira execução.
 */
public final class CandidatosFicticios {

    private CandidatosFicticios() { }

    public static List<Candidato> lista() {
        Partido pac = new Partido("91", "PAC", "Partido Acarajé Cívico");
        Partido ufb = new Partido("92", "UFB", "União Farol Brilhante");
        Partido mma = new Partido("93", "MMA", "Movimento Maré Alta");
        Partido abv = new Partido("94", "ABV", "Aliança Bem-te-vi");

        return List.of(
                Cargos.VEREADOR.criarCandidato("91123", "Ana Cajueiro", pac, null),
                Cargos.VEREADOR.criarCandidato("91456", "Rui Cacau", pac, null),
                Cargos.VEREADOR.criarCandidato("92111", "Bia Dendê", ufb, null),
                Cargos.VEREADOR.criarCandidato("92222", "Zeca Jaqueira", ufb, null),
                Cargos.VEREADOR.criarCandidato("93333", "Tiago Coqueiral", mma, null),
                Cargos.VEREADOR.criarCandidato("93789", "Lúcia Tamarindo", mma, null),
                Cargos.VEREADOR.criarCandidato("94555", "Léo Mangabeira", abv, null),
                Cargos.VEREADOR.criarCandidato("94777", "Nina Pitanga", abv, null),

                Cargos.PREFEITO.criarCandidato("91", "Heitor Jenipapo", pac, "Marta Umbu"),
                Cargos.PREFEITO.criarCandidato("92", "Clara Sapoti", ufb, "Nando Araçá"),
                Cargos.PREFEITO.criarCandidato("93", "Otávio Licuri", mma, "Dandara Cambuci"),
                Cargos.PREFEITO.criarCandidato("94", "Iara Mandacaru", abv, "Davi Seriguela"));
    }
}
