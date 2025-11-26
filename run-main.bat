@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   BUILD + RUN Main (annotation demo)
echo ========================================
echo.

REM Go to repo root
cd /d "%~dp0"

REM 1) Build framework JAR
echo [1/4] Building framework JAR...
call mvn -q -f "C:\Users\Mario\Documents\S5\FRAMEWORK\TP\framework_mario\sprint_framework" clean package
if %ERRORLEVEL% neq 0 (
    echo ERREUR: Echec build framework
    exit /b 1
)

set "FRAMEWORK_JAR=C:\Users\Mario\Documents\S5\FRAMEWORK\TP\framework_mario\sprint_framework\target\sprint_framework-1.0-SNAPSHOT.jar"
if not exist "%FRAMEWORK_JAR%" (
    echo ERREUR: JAR introuvable: %FRAMEWORK_JAR%
    exit /b 1
)
echo ✓ Framework JAR pret: %FRAMEWORK_JAR%

REM 2) Ensure test lib contains the JAR (for IDE/packaging convenience)
echo [2/4] Copy framework JAR into test WEB-INF/lib (optional)...
if not exist "teste\src\main\webapp\WEB-INF\lib" mkdir "teste\src\main\webapp\WEB-INF\lib"
copy /Y "%FRAMEWORK_JAR%" "teste\src\main\webapp\WEB-INF\lib\framework-java-1.0.0.jar" >nul
echo ✓ JAR copie dans teste\src\main\webapp\WEB-INF\lib

REM 3) Compile test sources against the JAR
echo [3/4] Compiling test sources (Main/Teste) against the framework JAR...
set "OUT_DIR=teste\target\classes"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

set "SRC_MAIN1=teste\src\main\java\mg\teste\Main.java"
set "SRC_MAIN2=teste\src\main\java\mg\teste\Teste.java"

javac -cp "%FRAMEWORK_JAR%" -d "%OUT_DIR%" "%SRC_MAIN1%" "%SRC_MAIN2%"
if %ERRORLEVEL% neq 0 (
    echo ERREUR: Echec compilation des sources du module test
    exit /b 1
)
echo ✓ Compilation OK: %OUT_DIR%

REM 4) Run Main to print annotation values
echo [4/4] Running mg.teste.Main ...
echo ----------------------------------------
java -cp "%OUT_DIR%;%FRAMEWORK_JAR%" mg.teste.Main
set RUN_RC=%ERRORLEVEL%
echo ----------------------------------------

if %RUN_RC% neq 0 (
  echo ERREUR: L'execution de Main a echoue (code %RUN_RC%)
  exit /b %RUN_RC%
)

echo ✓ Terminé
pause
