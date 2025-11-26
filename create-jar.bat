@echo off
echo ========================================
echo   Creation du JAR du Framework Sprint
echo ========================================
echo.

echo Nettoyage et compilation...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   JAR cree avec succes !
    echo ========================================
    echo.
    echo Le JAR se trouve dans : target\sprint_framework-1.0-SNAPSHOT.jar
    echo.
    echo Copie automatique du JAR dans ..\sprint_test\lib\
    copy /Y target\sprint_framework-1.0-SNAPSHOT.jar ..\sprint_test\lib\
    echo.
    echo Pour l'utiliser dans un autre projet :
    echo   1. Copiez le JAR dans le dossier lib/ de votre projet
    echo   2. Ajoutez-le comme dependance dans votre pom.xml
    echo   3. Consultez GUIDE_JAR.md pour plus de details
    echo.
) else (
    echo.
    echo ========================================
    echo   ERREUR lors de la creation du JAR
    echo ========================================
    echo.
)

pause

