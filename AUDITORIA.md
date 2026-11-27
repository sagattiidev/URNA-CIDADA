# Auditoria inicial — Urna Cidadã

Projeto acadêmico independente. Simulador educativo em Java 17+ / Swing, com persistência REST no Cloud Firestore. Não é uma urna oficial e não deve ser utilizado em eleições reais.

## Verificação em 27/11/2026

- Código original recebido em ZIP: compilação com JDK 21 e `--release 17`; 12/12 testes aprovados.
- Versão corrigida no pacote de entrega: 17/17 testes automatizados e 1 teste adicional de janela Swing (Xvfb + Robot), total 18/18.
- Correções: identificador estável do voto por sessão e cargo; tratamento exclusivo de ALREADY_EXISTS; recuperação de catálogo parcial; rejeição de catálogo divergente; validação de votos nulos; regras Firestore mais restritivas.
- REST testado contra servidor HTTP simulado, não Firebase real.
- Regras Firestore não executadas em emulador ou projeto real; Maven não disponível no ambiente. Não há autenticação dos eleitores, apuração na interface ou material educativo integrado. Não usar dados reais.

O código-fonte revisado está no ZIP de entrega. Este arquivo registra a auditoria; o envio integral dos arquivos será feito pelo script de publicação incluído no ZIP, usando a sessão GitHub do proprietário. Nenhum arquivo do AKASHI foi modificado.
