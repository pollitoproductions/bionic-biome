@echo off
cd /d "%~dp0"
FOR /F "tokens=*" %%i IN ('java -version 2>&1') DO SET JAVA_VERSION=%%i
echo %JAVA_VERSION%
echo Using gradle 7.6.1
java -XX:+IgnoreUnrecognizedVMOptions --add-modules jdk.base -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain %*
