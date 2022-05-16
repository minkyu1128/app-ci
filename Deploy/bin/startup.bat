@echo off

rem Started...
rem java -jar ..\webapp\ci-1.0.0.war

rem Started background...
@START /b shutdown.bat
@START /b ..\java\corretto-1.8.0_332\bin\java_app-ci  "-Dspring.profiles.active=prod" -jar ..\webapp\ci-1.0.0.war