package br.unijorge.urnacidada.controller;

import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.Cargo;

/** Retrato imutável do que a urna deve mostrar naquele instante. */
public record EstadoTela(Fase fase, Cargo cargo, int posicaoCargo, int totalCargos,
                         String digitos, int quantidadeDigitos, Candidato candidato,
                         boolean votoEmBranco, boolean numeroNulo, String mensagem) {
}
