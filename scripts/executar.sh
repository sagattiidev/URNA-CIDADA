#!/usr/bin/env sh
# Alternativa ao Maven: compila com o JDK, roda os testes e abre a urna.
# Requer JDK 17+ e as variáveis FIREBASE_PROJECT_ID / FIREBASE_API_KEY definidas.
set -e
cd "$(dirname "$0")/.."
rm -rf target/classes target/test-classes
mkdir -p target/classes target/test-classes
javac --release 17 -encoding UTF-8 -d target/classes $(find src/main -name '*.java')
javac --release 17 -encoding UTF-8 -cp target/classes -d target/test-classes $(find src/test -name '*.java')
java -cp target/classes:target/test-classes br.unijorge.urnacidada.TestesUrna
exec java -cp target/classes br.unijorge.urnacidada.Main
