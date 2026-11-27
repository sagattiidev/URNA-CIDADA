@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>&1 || (echo Instale Java 17 ou superior. & pause & exit /b 1)
if not defined FIREBASE_PROJECT_ID set /p FIREBASE_PROJECT_ID=ID do projeto Firebase: 
if not defined FIRESTORE_EMULATOR_HOST if not defined FIREBASE_API_KEY set /p FIREBASE_API_KEY=Chave Web do Firebase: 
if not exist target\urna-cidada.jar (
  where mvn >nul 2>&1 || (echo Compile com Maven ou use o pacote de entrega com o JAR. & pause & exit /b 1)
  call mvn package || (pause & exit /b 1)
)
java -jar target\urna-cidada.jar
if errorlevel 1 pause
