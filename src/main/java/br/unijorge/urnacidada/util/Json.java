package br.unijorge.urnacidada.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Leitor/escritor JSON mínimo, suficiente para a API REST do Firestore (evita dependências). */
public final class Json {

    private Json() { }

    public static String escrever(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(valor, sb);
        return sb.toString();
    }

    private static void escrever(Object v, StringBuilder sb) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String s) {
            texto(s, sb);
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Map<?, ?> m) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                texto(String.valueOf(e.getKey()), sb);
                sb.append(':');
                escrever(e.getValue(), sb);
            }
            sb.append('}');
        } else if (v instanceof Iterable<?> it) {
            sb.append('[');
            boolean primeiro = true;
            for (Object o : it) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escrever(o, sb);
            }
            sb.append(']');
        } else {
            throw new IllegalArgumentException("Tipo não suportado em JSON: " + v.getClass());
        }
    }

    private static void texto(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    /** Retorna Map, List, String, Long, Double, Boolean ou null. */
    public static Object ler(String json) {
        Leitor l = new Leitor(json);
        Object v = l.valor();
        l.pularEspacos();
        if (l.pos != json.length()) throw l.erro("Conteúdo após o fim do JSON");
        return v;
    }

    private static final class Leitor {
        private final String s;
        private int pos;

        Leitor(String s) { this.s = s; }

        IllegalArgumentException erro(String msg) {
            return new IllegalArgumentException("JSON inválido na posição " + pos + ": " + msg);
        }

        void pularEspacos() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        Object valor() {
            pularEspacos();
            if (pos >= s.length()) throw erro("fim inesperado");
            char c = s.charAt(pos);
            if (c == '{') return objeto();
            if (c == '[') return lista();
            if (c == '"') return texto();
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (s.startsWith("null", pos)) { pos += 4; return null; }
            return numero();
        }

        Map<String, Object> objeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            pos++;
            pularEspacos();
            if (pos >= s.length()) throw erro("fim inesperado");
            if (s.charAt(pos) == '}') { pos++; return m; }
            while (true) {
                pularEspacos();
                if (pos >= s.length() || s.charAt(pos) != '"') throw erro("chave esperada");
                String chave = texto();
                pularEspacos();
                if (pos >= s.length() || s.charAt(pos) != ':') throw erro("':' esperado");
                pos++;
                m.put(chave, valor());
                pularEspacos();
                if (pos >= s.length()) throw erro("fim inesperado");
                char c = s.charAt(pos++);
                if (c == '}') return m;
                if (c != ',') throw erro("',' ou '}' esperado");
            }
        }

        List<Object> lista() {
            List<Object> l = new ArrayList<>();
            pos++;
            pularEspacos();
            if (pos >= s.length()) throw erro("fim inesperado");
            if (s.charAt(pos) == ']') { pos++; return l; }
            while (true) {
                l.add(valor());
                pularEspacos();
                if (pos >= s.length()) throw erro("fim inesperado");
                char c = s.charAt(pos++);
                if (c == ']') return l;
                if (c != ',') throw erro("',' ou ']' esperado");
            }
        }

        String texto() {
            StringBuilder sb = new StringBuilder();
            pos++;
            while (true) {
                if (pos >= s.length()) throw erro("texto não terminado");
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                if (pos >= s.length()) throw erro("escape incompleto");
                char e = s.charAt(pos++);
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > s.length()) throw erro("escape \\u incompleto");
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> throw erro("escape desconhecido");
                }
            }
        }

        Object numero() {
            int ini = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            String t = s.substring(ini, pos);
            if (t.isEmpty()) throw erro("valor inesperado");
            try {
                return t.matches("[+-]?\\d+") ? (Object) Long.valueOf(t) : (Object) Double.valueOf(t);
            } catch (NumberFormatException ex) {
                throw erro("número inválido");
            }
        }
    }
}
