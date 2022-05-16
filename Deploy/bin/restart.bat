@echo off

@echo 실행 프로그램들을 실행 합니다.

SET CLASSPATH=C:\xit\IntelliJ-Workspace\Deploy\bin

@START /d %CLASSPATH% /b shutdown.bat
@START /d %CLASSPATH% /b startup.bat