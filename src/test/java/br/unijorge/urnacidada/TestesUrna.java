package br.unijorge.urnacidada;

import br.unijorge.urnacidada.controller.EstadoTela;
import br.unijorge.urnacidada.controller.Fase;
import br.unijorge.urnacidada.controller.UrnaController;
import br.unijorge.urnacidada.model.Candidato;
import br.unijorge.urnacidada.model.CandidatoMajoritario;
import br.unijorge.urnacidada.model.CandidatoProporcional;
import br.unijorge.urnacidada.model.Cargos;
import br.unijorge.urnacidada.model.Partido;
import br.unijorge.urnacidada.model.TipoVoto;
import br.unijorge.urnacidada.model.Voto;
import br.unijorge.urnacidada.model.VotoBranco;
import br.unijorge.urnacidada.service.CandidatoService;
import br.unijorge.urnacidada.service.PersistenciaException;
import br.unijorge.urnacidada.service.RepositorioVotacao;
import br.unijorge.urnacidada.service.VotacaoService;
import br.unijorge.urnacidada.service.firestore.ClienteFirestore;
import br.unijorge.urnacidada.service.firestore.ConfiguracaoException;
import br.unijorge.urnacidada.service.firestore.ConfiguracaoFirebase;
import br.unijorge.urnacidada.service.firestore.RepositorioFirestore;
import br.unijorge.urnacidada.util.Json;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Testes sem dependências externas. Execute: java -cp target/classes:target/test-classes
 * br.unijorge.urnacidada.TestesUrna   (sai com código 1 se algum teste falhar).
 */
public final class TestesUrna {

    private static int total;
    private static int falhas;

    public static void main(String[] args) throws Exception {
        executar("Json: ida e volta com acentos e escapes", TestesUrna::jsonIdaEVolta);
        executar("Json: rejeita entrada inválida", TestesUrna::jsonInvalido);
        executar("Modelo: cargos e polimorfismo de candidatos", TestesUrna::modeloPolimorfismo);
        executar("Modelo: validações de encapsulamento", TestesUrna::modeloValidacoes);
        executar("Modelo: registro de cada tipo de voto", TestesUrna::modeloVotos);
        executar("Configuração: variáveis de ambiente", TestesUrna::configuracao);
        executar("Controller: fluxo completo com candidatos", TestesUrna::fluxoCompleto);
        executar("Controller: branco, nulo e correção", TestesUrna::brancoNuloCorrecao);
        executar("Controller: regras de entrada inválida", TestesUrna::entradasInvalidas);
        executar("Controller: falha de rede e nova tentativa", TestesUrna::falhaDeRede);
        executar("Firestore REST: seed, listagem paginada e votos", TestesUrna::firestoreRest);
        executar("Firestore REST: erro HTTP e servidor fora do ar", TestesUrna::firestoreErros);
        executar("Firestore REST: retry após confirmação perdida não duplica", TestesUrna::retryIdempotente);
        executar("Firestore REST: 409 genérico não confirma voto", TestesUrna::conflitoGenerico);
        executar("Catálogo: recuperação de carga parcial", TestesUrna::catalogoParcial);
        executar("Catálogo: rejeita candidato adulterado", TestesUrna::catalogoAdulterado);
        executar("Modelo: voto nulo exige número completo", TestesUrna::nuloInvalido);

        if (java.util.Arrays.asList(args).contains("--gui")) {
            executar("Swing: votação completa usando teclado real", TestesUrna::janelaReal);
        }
        System.out.println("\n" + (total - falhas) + "/" + total + " testes passaram.");
        if (falhas > 0) System.exit(1);
    }

    // ---------------- infraestrutura mínima ----------------

    private interface Teste { void rodar() throws Exception; }

    private static void executar(String nome, Teste t) {
        total++;
        try {
            t.rodar();
            System.out.println("[OK]    " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("[FALHA] " + nome + " -> " + e);
        }
    }

    private static void verdade(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void igual(Object esperado, Object atual, String msg) {
        if (!java.util.Objects.equals(esperado, atual)) {
            throw new AssertionError(msg + " (esperado=" + esperado + ", atual=" + atual + ")");
        }
    }

    private static void lanca(Class<? extends Throwable> tipo, Teste t, String msg) {
        try {
            t.rodar();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) return;
            throw new AssertionError(msg + ": lançou " + e);
        }
        throw new AssertionError(msg + ": nada foi lançado");
    }

    // ---------------- Json / modelo ----------------

    private static void jsonIdaEVolta() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nome", "Bia Dendê \"A\"\n\\x");
        m.put("n", 42L);
        m.put("ok", true);
        m.put("nada", null);
        m.put("lista", List.of(1L, "a"));
        Object lido = Json.ler(Json.escrever(m));
        igual(m, lido, "ida e volta");
        igual("é", Json.ler("\"\\u00e9\""), "escape unicode");
        igual(1.5, Json.ler("1.5"), "decimal");
    }

    private static void jsonInvalido() {
        for (String ruim : new String[]{"{", "{\"a\" 1}", "[1,", "\"abc", "nulo", "{} x", ""}) {
            lanca(IllegalArgumentException.class, () -> Json.ler(ruim), "JSON ruim: " + ruim);
        }
    }

    private static void modeloPolimorfismo() {
        verdade(Cargos.VEREADOR.getQuantidadeDigitos() == 5, "vereador tem 5 dígitos");
        verdade(Cargos.PREFEITO.getQuantidadeDigitos() == 2, "prefeito tem 2 dígitos");
        igual("PROPORCIONAL", Cargos.VEREADOR.getTipo(), "tipo vereador");
        igual("MAJORITARIO", Cargos.PREFEITO.getTipo(), "tipo prefeito");
        Partido p = new Partido("91", "PAC", "Partido Acarajé Cívico");
        Candidato v = Cargos.VEREADOR.criarCandidato("91123", "Ana", p, null);
        Candidato pr = Cargos.PREFEITO.criarCandidato("91", "Heitor", p, "Marta");
        verdade(v instanceof CandidatoProporcional, "fábrica do vereador");
        verdade(pr instanceof CandidatoMajoritario, "fábrica do prefeito");
        igual(2, v.getLinhasDetalhe().size(), "linhas do vereador");
        igual(3, pr.getLinhasDetalhe().size(), "linhas do prefeito (com vice)");
        igual("vereador_91123", v.getId(), "id do documento");
        igual("Marta", pr.paraRegistro().get("vice"), "vice persistido");
        igual(null, v.paraRegistro().get("vice"), "vereador sem vice");
        verdade(Cargos.sequencia().get(0).equals(Cargos.VEREADOR), "proporcional vem primeiro");
        igual(Cargos.PREFEITO, Cargos.porId("prefeito"), "porId");
    }

    private static void modeloValidacoes() {
        Partido p = new Partido("91", "PAC", "Partido");
        lanca(IllegalArgumentException.class, () -> Cargos.VEREADOR.criarCandidato("123", "X", p, null), "número curto");
        lanca(IllegalArgumentException.class, () -> Cargos.PREFEITO.criarCandidato("91", "X", p, " "), "vice obrigatório");
        lanca(IllegalArgumentException.class, () -> Cargos.PREFEITO.criarCandidato("91", " ", p, "V"), "nome obrigatório");
        lanca(IllegalArgumentException.class, () -> new Partido("9", "A", "B"), "partido com 1 dígito");
        lanca(IllegalArgumentException.class, () -> Cargos.porId("senador"), "cargo inexistente");
        lanca(IllegalArgumentException.class, () -> new VotoBranco(" ", Cargos.PREFEITO), "sessão obrigatória");
    }

    private static void modeloVotos() {
        Partido p = new Partido("91", "PAC", "Partido");
        Candidato c = Cargos.VEREADOR.criarCandidato("91123", "Ana", p, null);
        Voto vc = new br.unijorge.urnacidada.model.VotoCandidato("s1", c);
        Voto vb = new VotoBranco("s1", Cargos.PREFEITO);
        Voto vn = new br.unijorge.urnacidada.model.VotoNulo("s1", Cargos.PREFEITO, "99");
        igual("CANDIDATO", vc.paraRegistro().get("tipo"), "tipo candidato");
        igual("91123", vc.paraRegistro().get("numero"), "número do candidato");
        igual("Ana", vc.paraRegistro().get("candidato"), "nome do candidato");
        igual("BRANCO", vb.paraRegistro().get("tipo"), "tipo branco");
        igual(null, vb.paraRegistro().get("numero"), "branco sem número");
        igual("NULO", vn.paraRegistro().get("tipo"), "tipo nulo");
        igual("99", vn.paraRegistro().get("numero"), "número do nulo");
        igual("MAJORITARIO", vn.paraRegistro().get("tipoCargo"), "tipo do cargo");
        verdade(vn.paraRegistro().get("registradoEm") instanceof java.time.Instant, "carimbo de tempo");
    }

    private static void configuracao() throws Exception {
        Function<String, String> vazio = k -> null;
        lanca(ConfiguracaoException.class, () -> ConfiguracaoFirebase.deAmbiente(vazio), "sem projeto");
        lanca(ConfiguracaoException.class, () -> ConfiguracaoFirebase.deAmbiente(k -> k.equals("FIREBASE_PROJECT_ID") ? "p" : null), "sem chave");
        ConfiguracaoFirebase real = ConfiguracaoFirebase.deAmbiente(k -> switch (k) {
            case "FIREBASE_PROJECT_ID" -> "meu-projeto";
            case "FIREBASE_API_KEY" -> "chave";
            default -> null;
        });
        igual("https://firestore.googleapis.com/v1/projects/meu-projeto/databases/(default)/documents", real.urlBase(), "URL real");
        igual("chave", real.getApiKey(), "chave");
        ConfiguracaoFirebase emu = ConfiguracaoFirebase.deAmbiente(k -> switch (k) {
            case "FIREBASE_PROJECT_ID" -> "p";
            case "FIRESTORE_EMULATOR_HOST" -> "localhost:8080";
            default -> null;
        });
        igual("http://localhost:8080/v1/projects/p/databases/(default)/documents", emu.urlBase(), "URL emulador");
        igual(null, emu.getApiKey(), "emulador sem chave");
    }

    // ---------------- controller com repositório em memória (apenas dublê de teste) ----------------

    private static final class RepositorioMemoria implements RepositorioVotacao {
        final List<Candidato> candidatos = new ArrayList<>();
        final List<Voto> votos = new ArrayList<>();
        int falhasRestantes;

        @Override public List<Candidato> listarCandidatos() throws PersistenciaException {
            if (falhasRestantes > 0) { falhasRestantes--; throw new PersistenciaException("Sem conexão"); }
            return new ArrayList<>(candidatos);
        }
        @Override public boolean salvarCandidato(Candidato c) { return candidatos.add(c); }
        @Override public void registrarVoto(Voto v) throws PersistenciaException {
            if (falhasRestantes > 0) { falhasRestantes--; throw new PersistenciaException("Sem conexão"); }
            votos.add(v);
        }
    }

    private static UrnaController novoController(RepositorioMemoria repo) {
        UrnaController c = new UrnaController(new CandidatoService(repo), new VotacaoService(repo),
                Cargos.sequencia(), Runnable::run, Runnable::run);
        c.inicializar();
        return c;
    }

    private static void digitar(UrnaController c, String numero) {
        for (char ch : numero.toCharArray()) c.digitar(ch - '0');
    }

    private static void fluxoCompleto() {
        RepositorioMemoria repo = new RepositorioMemoria();
        UrnaController c = novoController(repo);
        igual(Fase.INICIO, c.getEstado().fase(), "urna pronta após carregar (semeia candidatos)");
        verdade(repo.candidatos.size() == 12, "dados fictícios criados na primeira execução");

        c.confirmar();
        EstadoTela e = c.getEstado();
        igual(Fase.VOTANDO, e.fase(), "votando");
        igual(Cargos.VEREADOR, e.cargo(), "primeiro cargo é o proporcional");
        digitar(c, "9112");
        igual(null, c.getEstado().candidato(), "número incompleto não identifica");
        digitar(c, "3");
        igual("Ana Cajueiro", c.getEstado().candidato().getNome(), "candidato identificado");
        c.confirmar();
        igual(1, repo.votos.size(), "primeiro voto gravado");
        igual(Cargos.PREFEITO, c.getEstado().cargo(), "avanço automático para o majoritário");
        digitar(c, "93");
        igual("Dandara Cambuci", ((CandidatoMajoritario) c.getEstado().candidato()).getVice(), "vice exibido");
        c.confirmar();
        igual(Fase.FIM, c.getEstado().fase(), "encerramento");
        igual(2, repo.votos.size(), "dois votos gravados");
        igual(repo.votos.get(0).getSessaoId(), repo.votos.get(1).getSessaoId(), "mesma sessão");
        igual(TipoVoto.CANDIDATO, repo.votos.get(1).getTipo(), "tipo do voto");

        c.confirmar();
        igual(Fase.INICIO, c.getEstado().fase(), "volta ao início para novo eleitor");
        c.confirmar();
        verdade(!repo.votos.get(0).getSessaoId().equals(c.getEstado().digitos()), "nova sessão");
        igual(1, c.getEstado().posicaoCargo(), "novo eleitor recomeça no primeiro cargo");
    }

    private static void brancoNuloCorrecao() {
        RepositorioMemoria repo = new RepositorioMemoria();
        UrnaController c = novoController(repo);
        c.confirmar();

        digitar(c, "91123");
        c.corrigir();
        igual("", c.getEstado().digitos(), "corrige apaga tudo");
        igual(null, c.getEstado().candidato(), "corrige limpa candidato");
        digitar(c, "99999");
        verdade(c.getEstado().numeroNulo(), "número inexistente é nulo");
        c.confirmar();
        igual(TipoVoto.NULO, repo.votos.get(0).getTipo(), "voto nulo gravado");
        igual("99999", repo.votos.get(0).paraRegistro().get("numero"), "número nulo gravado");

        c.votarEmBranco();
        verdade(c.getEstado().votoEmBranco(), "branco ativado");
        c.digitar(1);
        igual("", c.getEstado().digitos(), "não digita após branco");
        c.corrigir();
        verdade(!c.getEstado().votoEmBranco(), "corrige desfaz branco");
        c.votarEmBranco();
        c.confirmar();
        igual(TipoVoto.BRANCO, repo.votos.get(1).getTipo(), "voto em branco gravado");
        igual(Fase.FIM, c.getEstado().fase(), "fim após dois cargos");
    }

    private static void entradasInvalidas() {
        RepositorioMemoria repo = new RepositorioMemoria();
        UrnaController c = novoController(repo);
        c.digitar(1);
        igual("", c.getEstado().digitos(), "ignora dígito fora da votação");
        c.confirmar();
        digitar(c, "123456789");
        igual(5, c.getEstado().digitos().length(), "limita aos dígitos do cargo");
        c.corrigir();
        digitar(c, "12");
        c.confirmar();
        igual(0, repo.votos.size(), "não confirma número incompleto");
        verdade(c.getEstado().mensagem() != null, "avisa o eleitor");
        c.votarEmBranco();
        verdade(!c.getEstado().votoEmBranco(), "branco com dígitos digitados é recusado");
        c.digitar(-1);
        c.digitar(10);
        igual("12", c.getEstado().digitos(), "ignora dígito inválido");
    }

    private static void falhaDeRede() {
        RepositorioMemoria repo = new RepositorioMemoria();
        repo.falhasRestantes = 1;
        UrnaController c = novoController(repo);
        igual(Fase.ERRO, c.getEstado().fase(), "erro ao carregar");
        c.confirmar();
        igual(Fase.INICIO, c.getEstado().fase(), "repetição bem-sucedida");

        c.confirmar();
        digitar(c, "91123");
        repo.falhasRestantes = 1;
        c.confirmar();
        igual(Fase.ERRO, c.getEstado().fase(), "erro ao gravar voto");
        igual(0, repo.votos.size(), "nada gravado");
        c.confirmar();
        igual(1, repo.votos.size(), "voto gravado na nova tentativa");
        igual(Cargos.PREFEITO, c.getEstado().cargo(), "só avança após gravar");
    }

    // ---------------- Firestore REST contra servidor HTTP falso ----------------

    /** Imita o trecho da API REST do Firestore usado pela urna (list, create, 409, paginação). */
    private static final class FirestoreFalso {
        final HttpServer servidor;
        final Map<String, Map<String, Map<String, Object>>> colecoes = new LinkedHashMap<>();
        final List<String> urls = new ArrayList<>();
        int autoId;
        int statusForcado;

        FirestoreFalso() throws IOException {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            servidor.createContext("/", troca -> {
                String caminho = troca.getRequestURI().getPath();
                String query = troca.getRequestURI().getRawQuery() == null ? "" : troca.getRequestURI().getRawQuery();
                urls.add(troca.getRequestURI().toString());
                String colecao = caminho.substring(caminho.lastIndexOf('/') + 1);
                String resposta;
                int status = 200;
                if (statusForcado != 0) {
                    status = statusForcado;
                    resposta = "{\"error\":{\"code\":" + status + ",\"message\":\"Missing or insufficient permissions.\"}}";
                } else if (troca.getRequestMethod().equals("GET")) {
                    resposta = listar(colecao, query);
                } else {
                    String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    String id = parametro(query, "documentId");
                    if (id == null) id = "auto" + (++autoId);
                    var docs = colecoes.computeIfAbsent(colecao, k -> new LinkedHashMap<>());
                    if (docs.containsKey(id)) {
                        status = 409;
                        resposta = "{\"error\":{\"code\":409,\"status\":\"ALREADY_EXISTS\"}}";
                    } else {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fields = (Map<String, Object>) ((Map<?, ?>) Json.ler(corpo)).get("fields");
                        docs.put(id, fields);
                        resposta = "{}";
                    }
                }
                byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
                troca.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                troca.sendResponseHeaders(status, bytes.length);
                troca.getResponseBody().write(bytes);
                troca.close();
            });
            servidor.start();
        }

        String listar(String colecao, String query) {
            var docs = colecoes.getOrDefault(colecao, Map.of());
            if (docs.isEmpty()) return "{}";
            List<String> ids = new ArrayList<>(docs.keySet());
            int tam = 5;
            String token = parametro(query, "pageToken");
            int ini = token == null ? 0 : Integer.parseInt(token);
            int fim = Math.min(ids.size(), ini + tam);
            List<Object> lista = new ArrayList<>();
            for (String id : ids.subList(ini, fim)) {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("name", "projects/p/databases/(default)/documents/" + colecao + "/" + id);
                d.put("fields", docs.get(id));
                lista.add(d);
            }
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("documents", lista);
            if (fim < ids.size()) r.put("nextPageToken", String.valueOf(fim));
            return Json.escrever(r);
        }

        static String parametro(String query, String nome) {
            for (String par : query.split("&")) {
                String[] kv = par.split("=", 2);
                if (kv[0].equals(nome) && kv.length == 2) return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
            return null;
        }

        String host() { return "localhost:" + servidor.getAddress().getPort(); }
        void parar() { servidor.stop(0); }
    }

    private static RepositorioFirestore repositorioApontandoPara(String host) {
        ConfiguracaoFirebase cfg = new ConfiguracaoFirebase("p", null, host);
        return new RepositorioFirestore(new ClienteFirestore(cfg));
    }

    private static void firestoreRest() throws Exception {
        FirestoreFalso fake = new FirestoreFalso();
        try {
            RepositorioFirestore repo = repositorioApontandoPara(fake.host());
            igual(0, repo.listarCandidatos().size(), "coleção vazia");

            CandidatoService servico = new CandidatoService(repo);
            servico.inicializar();
            igual(12, fake.colecoes.get("candidatos").size(), "12 candidatos semeados (documentos)");
            verdade(fake.colecoes.get("candidatos").containsKey("vereador_91123"), "id do documento");
            igual(12, repo.listarCandidatos().size(), "listagem paginada (3 páginas)");
            verdade(servico.buscar(Cargos.PREFEITO, "94").isPresent(), "busca após carregar");
            verdade(servico.buscar(Cargos.PREFEITO, "00").isEmpty(), "busca de número inexistente");

            Candidato c = servico.buscar(Cargos.VEREADOR, "92111").orElseThrow();
            igual("Bia Dendê", c.getNome(), "acentos preservados na ida e volta pelo Firestore");
            igual(false, repo.salvarCandidato(c), "documento repetido retorna false (409), sem erro");
            igual(12, fake.colecoes.get("candidatos").size(), "não duplica");

            new CandidatoService(repo).inicializar();
            igual(12, fake.colecoes.get("candidatos").size(), "segunda inicialização não recria dados");

            UrnaController ctrl = new UrnaController(servico, new VotacaoService(repo),
                    Cargos.sequencia(), Runnable::run, Runnable::run);
            ctrl.inicializar();
            ctrl.confirmar();
            digitar(ctrl, "92111");
            ctrl.confirmar();
            ctrl.votarEmBranco();
            ctrl.confirmar();
            igual(Fase.FIM, ctrl.getEstado().fase(), "fluxo completo sobre o Firestore");

            var votos = new ArrayList<>(fake.colecoes.get("votos").values());
            igual(2, votos.size(), "dois votos gravados");
            @SuppressWarnings("unchecked")
            Map<String, Object> v1 = (Map<String, Object>) votos.get(0);
            igual("{\"stringValue\":\"CANDIDATO\"}", Json.escrever(v1.get("tipo")), "tipo tipado");
            igual("{\"stringValue\":\"Bia Dendê\"}", Json.escrever(v1.get("candidato")), "candidato no voto");
            verdade(Json.escrever(v1.get("registradoEm")).contains("timestampValue"), "timestamp tipado");
            @SuppressWarnings("unchecked")
            Map<String, Object> v2 = (Map<String, Object>) votos.get(1);
            verdade(Json.escrever(v2.get("numero")).contains("nullValue"), "branco grava número nulo");
            verdade(fake.urls.stream().noneMatch(u -> u.contains("key=")), "emulador não envia chave de API");
        } finally {
            fake.parar();
        }
    }

    private static void janelaReal() throws Exception {
        RepositorioMemoria repo = new RepositorioMemoria();
        UrnaController ctrl = novoController(repo);
        br.unijorge.urnacidada.view.JanelaUrna[] janela = new br.unijorge.urnacidada.view.JanelaUrna[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            janela[0] = new br.unijorge.urnacidada.view.JanelaUrna(ctrl);
            janela[0].setVisible(true);
            janela[0].toFront();
            janela[0].requestFocus();
        });
        try {
            java.awt.Robot robot = new java.awt.Robot();
            robot.setAutoDelay(90);
            robot.waitForIdle();
            java.awt.Point p = janela[0].getLocationOnScreen();
            robot.mouseMove(p.x + 80, p.y + 80);
            robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            for (int tecla : new int[] {10, 57, 50, 49, 49, 49, 10, 66, 10}) {
                robot.keyPress(tecla); robot.keyRelease(tecla);
            }
            robot.waitForIdle();
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                igual(Fase.FIM, ctrl.getEstado().fase(), "tela FIM");
                igual(2, repo.votos.size(), "dois votos no fluxo visual");
                igual(TipoVoto.CANDIDATO, repo.votos.get(0).getTipo(), "candidato");
                igual(TipoVoto.BRANCO, repo.votos.get(1).getTipo(), "branco");
            });
        } finally {
            javax.swing.SwingUtilities.invokeAndWait(() -> janela[0].dispose());
        }
    }

    private static void retryIdempotente() throws Exception {
        FirestoreFalso fake = new FirestoreFalso();
        try {
            RepositorioFirestore repo = repositorioApontandoPara(fake.host());
            String sessao = java.util.UUID.randomUUID().toString();
            Voto voto = new VotoBranco(sessao, Cargos.VEREADOR);
            repo.registrarVoto(voto);
            // Simula o servidor ter gravado, mas o cliente precisar repetir a confirmação.
            repo.registrarVoto(voto);
            igual(1, fake.colecoes.get("votos").size(), "retry não duplica");
            verdade(fake.colecoes.get("votos").containsKey(sessao + "_vereador"), "ID estável");
            repo.registrarVoto(new VotoBranco(sessao, Cargos.PREFEITO));
            igual(2, fake.colecoes.get("votos").size(), "cargos independentes");
        } finally { fake.parar(); }
    }

    private static void conflitoGenerico() throws Exception {
        FirestoreFalso fake = new FirestoreFalso();
        try {
            fake.statusForcado = 409;
            RepositorioFirestore repo = repositorioApontandoPara(fake.host());
            lanca(PersistenciaException.class, () -> repo.registrarVoto(new VotoBranco("s1", Cargos.PREFEITO)), "409 genérico");
        } finally { fake.parar(); }
    }

    private static void catalogoParcial() throws Exception {
        FirestoreFalso fake = new FirestoreFalso();
        try {
            RepositorioFirestore repo = repositorioApontandoPara(fake.host());
            repo.salvarCandidato(br.unijorge.urnacidada.service.CandidatosFicticios.lista().get(0));
            new CandidatoService(repo).inicializar();
            igual(12, repo.listarCandidatos().size(), "completa carga parcial");
        } finally { fake.parar(); }
    }

    private static void catalogoAdulterado() throws Exception {
        RepositorioMemoria repo = new RepositorioMemoria();
        repo.candidatos.add(Cargos.PREFEITO.criarCandidato("91", "Nome diferente", new Partido("91", "PAC", "Partido"), "Vice"));
        lanca(PersistenciaException.class, () -> new CandidatoService(repo).inicializar(), "rejeita catálogo divergente");
    }

    private static void nuloInvalido() {
        lanca(IllegalArgumentException.class, () -> new br.unijorge.urnacidada.model.VotoNulo("s1", Cargos.PREFEITO, "9"), "nulo incompleto");
        lanca(IllegalArgumentException.class, () -> new br.unijorge.urnacidada.model.VotoNulo("s1", Cargos.PREFEITO, "xx"), "nulo não numérico");
    }

    private static void firestoreErros() throws Exception {
        FirestoreFalso fake = new FirestoreFalso();
        RepositorioFirestore repo = repositorioApontandoPara(fake.host());
        fake.statusForcado = 403;
        try {
            repo.listarCandidatos();
            throw new AssertionError("deveria falhar com HTTP 403");
        } catch (PersistenciaException e) {
            verdade(e.getMessage().contains("403") && e.getMessage().contains("insufficient permissions"),
                    "mensagem inclui status e motivo: " + e.getMessage());
        }
        lanca(PersistenciaException.class,
                () -> repo.registrarVoto(new VotoBranco("s", Cargos.VEREADOR)), "gravação negada");
        String host = fake.host();
        fake.parar();
        lanca(PersistenciaException.class, () -> repositorioApontandoPara(host).listarCandidatos(),
                "servidor fora do ar");
    }
}
