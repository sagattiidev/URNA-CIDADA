package br.unijorge.urnacidada.service.firestore;

import java.util.function.Function;

/**
 * Configuração lida de variáveis de ambiente (nada é gravado no código ou no repositório):
 * <ul>
 *   <li>FIREBASE_PROJECT_ID – ID do projeto Firebase (obrigatória);</li>
 *   <li>FIREBASE_API_KEY – chave de API Web do projeto (obrigatória, exceto no emulador);</li>
 *   <li>FIRESTORE_EMULATOR_HOST – opcional, ex.: localhost:8080, para usar o Firestore Emulator.</li>
 * </ul>
 * A chave de API Web do Firebase não é um segredo de servidor: quem protege os dados são as
 * regras de segurança do Firestore (arquivo firestore.rules).
 */
public final class ConfiguracaoFirebase {

    private final String projectId;
    private final String apiKey;
    private final String emulatorHost;

    public ConfiguracaoFirebase(String projectId, String apiKey, String emulatorHost) {
        this.projectId = projectId;
        this.apiKey = apiKey;
        this.emulatorHost = emulatorHost;
    }

    public static ConfiguracaoFirebase deAmbiente() throws ConfiguracaoException {
        return deAmbiente(System::getenv);
    }

    public static ConfiguracaoFirebase deAmbiente(Function<String, String> env) throws ConfiguracaoException {
        String projeto = limpar(env.apply("FIREBASE_PROJECT_ID"));
        String chave = limpar(env.apply("FIREBASE_API_KEY"));
        String emulador = limpar(env.apply("FIRESTORE_EMULATOR_HOST"));
        if (projeto == null) {
            throw new ConfiguracaoException("Variável de ambiente FIREBASE_PROJECT_ID não definida.");
        }
        if (chave == null && emulador == null) {
            throw new ConfiguracaoException("Variável de ambiente FIREBASE_API_KEY não definida.");
        }
        return new ConfiguracaoFirebase(projeto, chave, emulador);
    }

    private static String limpar(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    public String urlBase() {
        String host = emulatorHost != null ? "http://" + emulatorHost : "https://firestore.googleapis.com";
        return host + "/v1/projects/" + projectId + "/databases/(default)/documents";
    }

    /** Chave de API ou null (emulador). */
    public String getApiKey() { return emulatorHost != null ? null : apiKey; }
}
