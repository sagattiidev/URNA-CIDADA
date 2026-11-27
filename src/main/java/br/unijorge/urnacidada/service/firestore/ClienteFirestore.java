package br.unijorge.urnacidada.service.firestore;

import br.unijorge.urnacidada.service.PersistenciaException;
import br.unijorge.urnacidada.util.Json;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente da API REST do Cloud Firestore (v1). Converte entre mapas Java e o formato de
 * valores tipados do Firestore (stringValue, integerValue, timestampValue, ...).
 */
public class ClienteFirestore {

    private final ConfiguracaoFirebase config;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public ClienteFirestore(ConfiguracaoFirebase config) {
        this.config = config;
    }

    /** Lista todos os documentos da coleção. Cada mapa inclui o campo extra "_id". */
    public List<Map<String, Object>> listar(String colecao) throws PersistenciaException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        String pagina = null;
        do {
            List<String> params = new ArrayList<>();
            params.add("pageSize=100");
            if (pagina != null) params.add("pageToken=" + codificar(pagina));
            Map<String, Object> corpo = objeto(enviar(HttpRequest.newBuilder(uri(colecao, params)).GET()));
            Object docs = corpo.get("documents");
            if (docs instanceof List<?> lista) {
                for (Object d : lista) {
                    resultado.add(lerDocumento(objeto(d)));
                }
            }
            pagina = corpo.get("nextPageToken") instanceof String t && !t.isEmpty() ? t : null;
        } while (pagina != null);
        return resultado;
    }

    /**
     * Cria um documento. Com idDocumento nulo, o Firestore gera o ID.
     * Retorna false se já existe um documento com esse ID.
     */
    public boolean criar(String colecao, String idDocumento, Map<String, Object> campos) throws PersistenciaException {
        List<String> params = new ArrayList<>();
        if (idDocumento != null) params.add("documentId=" + codificar(idDocumento));
        Map<String, Object> fields = new LinkedHashMap<>();
        campos.forEach((k, v) -> fields.put(k, paraValor(v)));
        String json = Json.escrever(Map.of("fields", fields));
        HttpRequest.Builder req = HttpRequest.newBuilder(uri(colecao, params))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        HttpResponse<String> r = chamar(req);
        if (r.statusCode() == 409 && idDocumento != null) {
            // Só o conflito de documento existente é uma confirmação idempotente.
            try {
                if (Json.ler(r.body()) instanceof Map<?, ?> m
                        && m.get("error") instanceof Map<?, ?> e
                        && "ALREADY_EXISTS".equals(e.get("status"))) return false;
            } catch (RuntimeException ignorado) { }
        }
        verificar(r);
        return true;
    }

    // ---------- HTTP ----------

    private URI uri(String colecao, List<String> params) {
        List<String> todos = new ArrayList<>(params);
        if (config.getApiKey() != null) todos.add("key=" + codificar(config.getApiKey()));
        String q = todos.isEmpty() ? "" : "?" + String.join("&", todos);
        return URI.create(config.urlBase() + "/" + colecao + q);
    }

    private HttpResponse<String> chamar(HttpRequest.Builder req) throws PersistenciaException {
        try {
            return http.send(req.timeout(Duration.ofSeconds(15)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new PersistenciaException("Sem conexão com o Firebase: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenciaException("Operação interrompida.", e);
        }
    }

    private String enviar(HttpRequest.Builder req) throws PersistenciaException {
        HttpResponse<String> r = chamar(req);
        verificar(r);
        return r.body();
    }

    private void verificar(HttpResponse<String> r) throws PersistenciaException {
        if (r.statusCode() / 100 == 2) return;
        String detalhe = "";
        try {
            if (Json.ler(r.body()) instanceof Map<?, ?> m && m.get("error") instanceof Map<?, ?> e) {
                detalhe = " - " + e.get("message");
            }
        } catch (RuntimeException ignorado) {
            // corpo de erro que não é JSON: segue só com o status
        }
        throw new PersistenciaException("Firestore respondeu HTTP " + r.statusCode() + detalhe);
    }

    private static String codificar(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    // ---------- conversão de valores ----------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objeto(Object o) throws PersistenciaException {
        if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
        throw new PersistenciaException("Resposta inesperada do Firestore.");
    }

    private static Map<String, Object> objeto(String json) throws PersistenciaException {
        try {
            return objeto(Json.ler(json));
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Resposta inválida do Firestore: " + e.getMessage(), e);
        }
    }

    private static Map<String, Object> lerDocumento(Map<String, Object> doc) throws PersistenciaException {
        Map<String, Object> campos = new LinkedHashMap<>();
        if (doc.get("fields") instanceof Map<?, ?> fields) {
            for (Map.Entry<?, ?> e : fields.entrySet()) {
                campos.put(String.valueOf(e.getKey()), deValor(objeto(e.getValue())));
            }
        }
        String nome = String.valueOf(doc.get("name"));
        campos.put("_id", nome.substring(nome.lastIndexOf('/') + 1));
        return campos;
    }

    static Map<String, Object> paraValor(Object v) {
        if (v == null) return Map.of("nullValue", "NULL_VALUE");
        if (v instanceof String s) return Map.of("stringValue", s);
        if (v instanceof Boolean b) return Map.of("booleanValue", b);
        if (v instanceof Integer || v instanceof Long) return Map.of("integerValue", String.valueOf(v));
        if (v instanceof Double d) return Map.of("doubleValue", d);
        if (v instanceof Instant i) return Map.of("timestampValue", i.toString());
        throw new IllegalArgumentException("Tipo não suportado no Firestore: " + v.getClass());
    }

    static Object deValor(Map<String, Object> valor) {
        if (valor.containsKey("stringValue")) return valor.get("stringValue");
        if (valor.containsKey("integerValue")) return Long.valueOf(String.valueOf(valor.get("integerValue")));
        if (valor.containsKey("booleanValue")) return valor.get("booleanValue");
        if (valor.containsKey("doubleValue")) return valor.get("doubleValue");
        if (valor.containsKey("timestampValue")) return Instant.parse(String.valueOf(valor.get("timestampValue")));
        return null;
    }
}
