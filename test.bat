@echo off
rem Compile the program and the JUnit 5 tests, then run every test.
rem Exits with a non-zero status if any test fails.
cd /d "%~dp0"
set JUNIT=lib\junit-platform-console-standalone-1.10.2.jar
if exist out rmdir /s /q out
javac -d out -sourcepath src src\coursetrack\Main.java
if errorlevel 1 exit /b 1
javac -d out\test -cp "out;%JUNIT%" test\coursetrack\model\*.java test\coursetrack\service\*.java test\coursetrack\storage\*.java test\coursetrack\ui\*.java
if errorlevel 1 exit /b 1
java -jar "%JUNIT%" execute --class-path "out;out\test" --scan-class-path out\test --details=tree --disable-banner %*
