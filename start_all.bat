@echo off
title FraudShield Platform Starter
color 0A

echo ==========================================================
echo          FRAUDSHIELD REAL-TIME PLATFORM STARTER
echo ==========================================================
echo.
echo Choose launch mode:
echo [1] Full Docker Compose Stack (Kafka, Redis, Postgres, Backend, ML, Frontend)
echo [2] Instant Zero-Dependency Demo Server (Pure Python Engine on :8080)
echo [3] Run Frontend Local Dev Server (Vite on :3000)
echo.
set /p choice="Enter choice [1, 2, or 3]: "

if "%choice%"=="1" (
    echo.
    echo Launching Docker Compose stack...
    docker compose up --build
    goto end
)

if "%choice%"=="2" (
    echo.
    echo Launching Unified Python Engine...
    python run_demo_server.py
    goto end
)

if "%choice%"=="3" (
    echo.
    echo Launching React Frontend...
    cd frontend
    call npm install
    call npm run dev
    goto end
)

:end
pause
