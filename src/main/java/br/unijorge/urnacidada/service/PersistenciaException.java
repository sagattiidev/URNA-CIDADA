package br.unijorge.urnacidada.service;

/** Falha ao ler ou gravar dados no banco. */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensagem) { super(mensagem); }
    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
