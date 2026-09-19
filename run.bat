@echo off
setlocal
cd /d "%~dp0"
call mvnw.cmd clean javafx:run
if errorlevel 1 (
  echo.
  echo The application could not start. Make sure JDK 17 or newer is installed.
  pause
)
endlocal
