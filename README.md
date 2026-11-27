# Urna Cidadã — simulador educativo de urna eletrônica

Projeto acadêmico independente de extensão da UNIJORGE — Ciência da Computação.
Simula votação em dois cargos fictícios: vereador (5 dígitos) e prefeito (2 dígitos), usando Java Swing e Firebase Cloud Firestore. A atividade promove formação cidadã, pensamento crítico e diálogo sobre desinformação (ODS 4 e 16). Não representa uma urna oficial, nem serve para eleições reais.

## Funcionalidades

- Candidatos e partidos fictícios (91–94); confirmação, correção, votos brancos e nulos.
- Avanço automático entre cargos e tela FIM.
- Suporte ao teclado físico e mouse.
- Cadastro inicial de 12 candidatos, recuperação de carga parcial e validação do catálogo.
- Registro REST com ID estável da sessão e cargo, evitando duplicação no retry.
- Acesso à rede em segundo plano e repetição após falha.

## Executar

Requisitos: JDK 17+ e, se usar Maven, Maven 3.9+.

    mvn package
    java -jar target/urna-cidada.jar

No Windows, EXECUTAR_URNA.cmd solicita as configurações de ambiente e usa o JAR gerado. No Linux/macOS, scripts/executar.sh compila, testa e abre a urna sem Maven.

## Configuração do Firebase (somente banco de demonstração)

1. Crie um projeto de demonstração no Console do Firebase (https://console.firebase.google.com), habilite Firestore em modo nativo e registre um app Web.
2. Configure as variáveis FIREBASE_PROJECT_ID e FIREBASE_API_KEY na sessão local. Nunca versione credenciais ou contas de serviço.
3. Revise e teste firestore.rules no emulador antes de publicar em um banco de demonstração. O cadastro inicial precisa de criação temporária de candidatos; depois da carga, desabilite create em candidatos. Não abra o acesso de leitura dos votos no cliente.
4. Inicie a urna e confira se os 12 candidatos fictícios foram cadastrados.
5. Vote com dados fictícios e verifique a coleção votos no console.

Emulador opcional: defina FIRESTORE_EMULATOR_HOST=localhost:8080 e FIREBASE_PROJECT_ID com um ID exclusivo de demonstração. A chave Web não é necessária no emulador.

## Testes

    mvn test

Sem Maven:

    mkdir -p target/classes target/test-classes
    javac --release 17 -encoding UTF-8 -d target/classes $(find src/main -name '*.java')
    javac --release 17 -encoding UTF-8 -cp target/classes -d target/test-classes $(find src/test -name '*.java')
    java -cp target/classes:target/test-classes br.unijorge.urnacidada.TestesUrna

Use --gui no último comando para incluir o teste Swing com display disponível.

O pacote revisado foi compilado com JDK 21 em modo Java 17: 17 testes automatizados e um teste Swing com teclado real aprovados. A camada REST foi testada contra um servidor simulado, não um Firebase real. Consulte AUDITORIA.md.

## Arquitetura e POO

- model: entidades de domínio e classes abstratas Cargo, Candidato e Voto, com herança e polimorfismo.
- controller: máquina de estados da votação, independente do Swing.
- service: regras de negócio e interface RepositorioVotacao.
- service.firestore: comunicação REST e configuração.
- view: janela, teclado e display.
- util: JSON sem dependência externa.

## Limitações e segurança

Não coleta nome ou CPF do eleitor, mas os registros contêm UUID de sessão e horário. Não há garantia de anonimato, autenticação, nem controle de uma votação por pessoa. O sistema não deve receber dados reais. Qualquer cliente com acesso ao banco de demonstração pode tentar criar sessões fictícias. A apuração é feita no console por operador autorizado, não no aplicativo; não habilite leitura pública dos votos. As regras de segurança ainda não foram validadas em projeto Firebase ou emulador. O conteúdo educativo e a oficina são entregas acadêmicas separadas.

O repositório privado contém código-fonte e testes. Binários de build, configurações particulares e credenciais não são versionados.
