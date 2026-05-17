@echo off
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
javac -encoding UTF-8 -d out -sourcepath src src/Main.java
if errorlevel 1 (
    echo Build failed.
    pause
    exit /b 1
)
java -cp out Main
