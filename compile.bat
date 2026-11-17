@echo off
title Compile - Online Complaint and Helpdesk System
echo Compiling Java Sources into bin directory...
if not exist "bin" mkdir bin
dir /s /b src\*.java > sources.txt
javac -cp "lib\*;src" -d bin @sources.txt
del sources.txt
echo Compilation complete!
pause
