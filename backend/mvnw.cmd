@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------
@echo off
title %0
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

if not "%JAVA_HOME%" == "" goto OkJHome
for /d %%d in ("C:\Program Files\Java\jdk-*") do set "JAVA_HOME=%%d"
if not "%JAVA_HOME%" == "" goto OkJHome
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do set "JAVA_HOME=%%d"
if not "%JAVA_HOME%" == "" goto OkJHome
echo Error: JAVA_HOME not found. Set JAVA_HOME to your JDK directory.
exit /B 1

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init
echo Error: JAVA_HOME is invalid: %JAVA_HOME%
exit /B 1

:init
set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

:run
"%JAVA_HOME%\bin\java.exe" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 exit /B 1
exit /B 0
