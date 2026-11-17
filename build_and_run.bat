@echo off
title Build and Run - Online Complaint and Helpdesk System
echo [1/2] Compiling Java Sources...
if not exist "bin" mkdir bin
dir /s /b src\*.java > sources.txt
javac -cp "lib\*;src" -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Application GUI...
java --enable-native-access=ALL-UNNAMED -cp "lib\*;bin" com.helpdesk.Main
pause
