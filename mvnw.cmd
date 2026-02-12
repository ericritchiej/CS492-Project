@echo off
@REM Maven Wrapper startup script for Windows

@setlocal

set ERROR_CODE=0

@REM Set base directory (remove trailing backslash from %~dp0)
set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

@REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto checkMavenWrapper

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. >&2
goto error

:findJavaFromJavaHome
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if exist "%JAVA_EXE%" goto checkMavenWrapper

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
goto error

:checkMavenWrapper
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if exist "%WRAPPER_JAR%" goto runWrapper

@REM Download maven-wrapper.jar if it does not exist
echo Downloading Maven Wrapper...
for /F "usebackq tokens=1,2 delims==" %%A in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") do (
    if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
)

if not "%WRAPPER_URL%"=="" (
    powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
    if %ERRORLEVEL% neq 0 goto error
)

:runWrapper
"%JAVA_EXE%" %MAVEN_OPTS% -cp "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
cmd /C exit /B %ERROR_CODE%
