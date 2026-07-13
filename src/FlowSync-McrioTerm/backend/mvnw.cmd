@echo off
setlocal

set "BASEDIR=%~dp0"
if "%BASEDIR%"=="" set "BASEDIR=%CD%\"

set "CLASSWORLDS_JAR=%BASEDIR%.mvn\wrapper\maven-wrapper.jar"

if not exist "%CLASSWORLDS_JAR%" (
    echo Maven Wrapper JAR not found: %CLASSWORLDS_JAR%
    exit /b 1
)

if not "%JAVA_HOME%"=="" (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

"%JAVA_EXE%" -Xmx1024m -classpath "%CLASSWORLDS_JAR%" "-Dmaven.home=%BASEDIR%" "-Dmaven.multiModuleProjectDirectory=%BASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
