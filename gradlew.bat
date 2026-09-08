@echo off
where gradle >nul 2>nul
if %ERRORLEVEL%==0 (
  gradle %*
  goto :eof
)
echo Gradle is not installed. Install Gradle or run the GitHub Actions workflow. 1>&2
exit /b 1
