@echo off
set PROJECT_ROOT=%~dp0..
set JAVA_HOME=D:\Java\jdk-17.0.19+10
set PATH=%JAVA_HOME%\bin;%PATH%
set MAVEN_OPTS=-Dmaven.repo.local=%PROJECT_ROOT%\.m2\repository
"%PROJECT_ROOT%\..\.tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
