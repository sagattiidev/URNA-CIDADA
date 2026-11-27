@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>&1 || (echo Instale o JDK 17 ou superior. & pause & exit /b 1)
powershell -NoProfile -Command "$ErrorActionPreference='Stop'; New-Item -ItemType Directory -Force target/classes | Out-Null; $fontes=(Get-ChildItem src/main -Recurse -Filter *.java).FullName; javac --release 17 -encoding UTF-8 -d target/classes $fontes; exit $LASTEXITCODE"
if errorlevel 1 (pause & exit /b 1)
java -cp target/classes br.unijorge.urnacidada.Main %*
if errorlevel 1 pause
