@echo off

rem Started...
rem java -jar ..\webapp\ci-1.0.0-SNAPSHOT.war

rem Started background...
@START /b shutdown.bat
@START /b C:\xit\spring-tool-suite-4-4.11.0.RELEASE-e4.20.0-win32.win32.x86_64.self-extracting\env-setting\Java\jdk1.8.0_121\bin\java  "-Dspring.profiles.active=prod" -jar ..\webapp\ci-1.0.0-SNAPSHOT.war