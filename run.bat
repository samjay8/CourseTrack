@echo off
rem Compile CourseTrack and start it.
rem   run.bat          console menu
rem   run.bat --gui    Swing window
cd /d "%~dp0"
if exist out rmdir /s /q out
javac -d out -sourcepath src src\coursetrack\Main.java
if errorlevel 1 exit /b 1
java -cp out coursetrack.Main %*
