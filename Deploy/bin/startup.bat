@echo off

rem Started...
rem java -jar ..\webapp\ci-1.0.0-SNAPSHOT.war

rem Started background...
@START /b shutdown.bat
@START /b .\Java\jdk1.8.0_121\bin\java "-Dspring.profiles.active=prod" -jar ..\webapp\ci-1.0.0-SNAPSHOT.war