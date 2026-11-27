package br.unijorge.urnacidada.service;

import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.Voto;
import br.unijorge.urnacidada.util.Json;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

/** Persistência offline para uma demonstração em um computador. */
public final class RepositorioLocal implements RepositorioVotacao {
    private final Path pasta;
    public RepositorioLocal(Path pasta) {
        this.pasta = pasta.toAbsolutePath().normalize();
    }
    @Override public List<Candidato> listarCandidatos() {
        return CandidatosFicticios.lista();
    }
    @Override public boolean salvarCandidato(Candidato candidato) throws PersistenciaException {
        if (listarCandidatos().stream().noneMatch(c -> c.paraRegistro().equals(candidato.paraRegistro()))) {
            throw new PersistenciaException("Candidato fora do catálogo fictício.");
        }
        return false;
    }
    @Override public void registrarVoto(Voto voto) throws PersistenciaException {
        String sessao = voto.getSessaoId(), cargo = voto.getCargo().getId();
        if (!sessao.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                || !List.of("vereador", "prefeito").contains(cargo)) {
            throw new PersistenciaException("Sessão ou cargo inválido.");
        }
        Map<String,Object> registro = voto.paraRegistro();
        registro.put("registradoEm", voto.getRegistradoEm().toString());
        String json = Json.escrever(registro);
        Path destino = pasta.resolve(sessao + "_" + cargo + ".json");
        synchronized (RepositorioLocal.class) {
            try {
                Files.createDirectories(pasta);
                try (FileChannel canal = FileChannel.open(pasta.resolve(".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                     FileLock lock = canal.lock()) {
                    if (Files.exists(destino)) {
                        if (!Files.readString(destino).equals(json)) {
                            throw new PersistenciaException("Já existe um voto diferente para esta sessão e cargo.");
                        }
                        return;
                    }
                    Path temporario = Files.createTempFile(pasta, ".voto-", ".tmp");
                    try {
                        Files.writeString(temporario, json);
                        try (FileChannel dados = FileChannel.open(temporario, StandardOpenOption.WRITE)) { dados.force(true); }
                        Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE);
                    } finally { Files.deleteIfExists(temporario); }
                }
            } catch (IOException | SecurityException e) {
                throw new PersistenciaException("Não foi possível gravar o voto local: " + e.getMessage(), e);
            }
        }
    }
}
