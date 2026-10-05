@echo off
where node >nul 2>nul || (echo Bitte zuerst Node.js installieren: https://nodejs.org & pause & exit /b)
call npm install
call npm run build
explorer dist
pause
