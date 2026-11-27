# Arquitetura

## Camadas e pacotes (br.unijorge.urnacidada)

| Pacote | Papel |
|---|---|
| model | Entidades: cargos, candidatos, partido, votos |
| view | Swing: JanelaUrna, TelaUrna, TecladoUrna, CaixasNumero, FotoCandidato |
| controller | UrnaController, EstadoTela (record imutável), UrnaListener |
| service | CandidatoService, VotacaoService, RepositorioVotacao, dados fictícios |
| service.firestore | ClienteFirestore (REST), RepositorioFirestore, ConfiguracaoFirebase |
| util | Json (leitor/escritor mínimo) |

A view nunca fala com o banco; o controller não usa Swing e recebe executores por injeção. O acesso ao Firestore roda em segundo plano.

## Fluxo da urna

CARREGANDO → INICIO → VOTANDO (cargo 1) → SALVANDO → VOTANDO (cargo 2) → SALVANDO → FIM → INICIO.

Uma falha de rede leva a ERRO; CONFIRMA repete a operação que falhou. O avanço só ocorre após gravação.

## Modelagem Firestore

- candidatos/{cargo}_{numero}: cargo, numero, nome, partidoNumero, partidoSigla, partidoNome, vice.
- votos/{sessaoId}_{cargo}: sessaoId, cargo, tipoCargo, tipo (CANDIDATO, BRANCO ou NULO), numero, candidato, registradoEm.

O ID estável protege contra duplicação em retries de uma mesma sessão/cargo. Não autentica o eleitor e não impede a criação de novas sessões por um cliente externo. O uso é exclusivamente educativo.
