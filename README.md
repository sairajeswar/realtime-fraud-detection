# 🛡️ FraudShield: Real-Time Financial Fraud Detection Platform

[![Architecture](https://img.shields.io/badge/Architecture-Polyglot%20Microservices-blue.svg)](#)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203-green.svg)](#)
[![ML](https://img.shields.io/badge/ML%20Engine-FastAPI%20%2B%20XGBoost%20%2B%20SHAP-orange.svg)](#)
[![Streaming](https://img.shields.io/badge/Event%20Stream-Apache%20Kafka-red.svg)](#)
[![Cache](https://img.shields.io/badge/State%20Store-Redis%207-darkred.svg)](#)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%2016-blue.svg)](#)
[![Frontend](https://img.shields.io/badge/Dashboard-React%20%2B%20Vite%20%2B%20Tailwind-cyan.svg)](#)

An enterprise-grade, millisecond-latency financial transaction fraud detection platform that combines **deterministic business rule engines**, **gradient-boosted machine learning (XGBoost)**, **Explainable AI (TreeSHAP)**, and **Apache Kafka event streaming**.

---

## ⚡ Quick Start Options

### Option 1: Full Docker Compose (Production Stack)
Starts Kafka, Zookeeper, Redis, PostgreSQL, Spring Boot Backend, Python ML Service, and React Frontend in one command:
```bash
docker compose up --build
```
* **React Dashboard:** `http://localhost:3000`
* **Spring Boot API:** `http://localhost:8080`
* **FastAPI ML Service:** `http://localhost:8000`

---

### Option 2: Zero-Dependency Instant Demo (Python Standalone)
Want to inspect and demo the entire system immediately without installing Docker or Maven?
```bash
python run_demo_server.py
```
This runs the full Rule Engine, synthetic XGBoost model approximation, and REST endpoints on `http://localhost:8080`.

To start the React frontend simultaneously:
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:3000` in your browser.

---

## 🏛️ System Architecture

```
                      [ Client Transaction POST /api/transactions ]
                                           │
                                           ▼
                      ┌─────────────────────────────────────────┐
                      │        Spring Boot API Gateway          │
                      │     (Validation & Kafka Producer)       │
                      └────────────────────┬────────────────────┘
                                           │
                                           ▼
                      ┌─────────────────────────────────────────┐
                      │           Apache Kafka Broker           │
                      │        Topic: transactions.inbound      │
                      └────────────────────┬────────────────────┘
                                           │
                                           ▼
                      ┌─────────────────────────────────────────┐
                      │        Fraud Detection Orchestrator     │
                      └─────────────┬──────────────────┬────────┘
                                    │                  │
                ┌───────────────────┘                  └───────────────────┐
                ▼                                                          ▼
  ┌───────────────────────────┐                              ┌───────────────────────────┐
  │     Rule Engine (40%)     │                              │      ML Engine (60%)      │
  │ - State lookup in Redis   │                              │     (Python FastAPI)      │
  │ - Amount Z-Score Ratio    │                              │ - XGBoost Inference       │
  │ - Haversine Geo-Velocity  │                              │ - TreeSHAP Feature XAI    │
  │ - Sliding Window Rate     │                              └─────────────┬─────────────┘
  └─────────────┬─────────────┘                                            │
                │                                                          │
                └─────────────────────┬────────────────────────────────────┘
                                      │
                                      ▼
                      ┌─────────────────────────────────────────┐
                      │         Risk Aggregator Engine          │
                      │   Risk = (Rule * 0.40) + (ML * 0.60)    │
                      └───────────────────┬─────────────────────┘
                                          │
                     ┌────────────────────┼────────────────────┐
                     ▼                    ▼                    ▼
                [ APPROVE ]          [ REVIEW ]           [  BLOCK  ]
               (Risk < 0.30)       (0.30 - 0.70)        (Risk > 0.70)
                     │                    │                    │
                     └────────────────────┼────────────────────┘
                                          ▼
                      ┌─────────────────────────────────────────┐
                      │               Persistence               │
                      ├───────────────────┬─────────────────────┤
                      │  PostgreSQL (JPA) │  WebSockets (STOMP) │
                      │   (Audit Trail)   │    (/topic/feed)    │
                      └───────────────────┴──────────┬──────────┘
                                                     ▼
                                      ┌─────────────────────────────┐
                                      │     React Vite Dashboard    │
                                      │ - Live Transaction Feed     │
                                      │ - TreeSHAP Factor Cards     │
                                      │ - Attack Simulator Triggers │
                                      └─────────────────────────────┘
```

---

## 🔬 Core Fraud Detection Rules & Engines

### 1. Deterministic Business Rules
* **Rule 1 (High Amount):** Any transaction $> ₹1,00,000$ incurs an instant risk penalty.
* **Rule 2 (Device Fingerprint):** Mismatch between incoming device ID and user's registered device.
* **Rule 3 (Location Zone):** Deviation from user's primary geographical footprint.
* **Rule 4 (Temporal Midnight Anomaly):** Activity between 12:00 AM and 05:00 AM flagged with elevated risk weights.
* **Rule 5 (Spending Multiplier):** Ratio of current amount vs user's historical rolling mean ($> 4.0\times$ baseline).

### 2. Advanced Physical & Graph Rules
* **Haversine Geo-Velocity Engine:** Calculates physical velocity ($v = \frac{\Delta \text{distance}}{\Delta t}$). If calculated velocity $> 600\text{ km/h}$, flags impossible travel anomaly.
* **Sliding Window Burst Rate:** Redis atomic sorted sets detect automated botnet card testing ($> 4$ transactions in under 5 minutes).

### 3. Machine Learning Microservice (FastAPI + XGBoost + SHAP)
* **XGBoost Classifier:** Trained on 25,000 imbalanced financial transactions using synthetic minority generation and scale-positive weighting.
* **TreeSHAP Explainability:** Produces local feature contribution values for every prediction (e.g., `+38% Impossible Travel, +32% Amount Deviation`).

---

## 📡 API Specification

### Ingest Transaction
```http
POST /api/transactions
Content-Type: application/json

{
  "userId": "U102",
  "amount": 95000,
  "location": "Mumbai",
  "latitude": 19.0760,
  "longitude": 72.8777,
  "deviceId": "DEV_UNKNOWN_998",
  "merchantCategory": "Electronics",
  "timestamp": "02:15"
}
```

#### Response
```json
{
  "transactionId": "TXN-874BD29A",
  "userId": "U102",
  "amount": 95000.0,
  "ruleScore": 0.88,
  "mlScore": 0.94,
  "finalRiskScore": 0.916,
  "riskLevel": "CRITICAL",
  "decision": "BLOCK",
  "isNewDevice": true,
  "isNewLocation": true,
  "geoVelocityKmh": 850.0,
  "riskFactors": [
    {
      "feature": "IMPOSSIBLE_TRAVEL_VELOCITY",
      "impactPercentage": 38.0,
      "description": "Calculated velocity (850 km/h) violates physical travel limits",
      "rawValue": "850 km/h"
    },
    {
      "feature": "AMOUNT_DEVIATION",
      "impactPercentage": 32.0,
      "description": "Amount ₹95,000 is 29.7x higher than user avg (₹3,200)",
      "rawValue": "29.7x"
    }
  ],
  "processingLatencyMs": 6.8
}
```

### Attack Simulator Endpoints
* `POST /api/simulate/normal`: Injects legitimate ₹450 grocery payment.
* `POST /api/simulate/midnight-attack`: Injects high-risk ₹95,000 midnight payment.
* `POST /api/simulate/impossible-travel`: Injects sequential Delhi $\to$ London transactions 15 minutes apart.
* `POST /api/simulate/card-testing-burst`: Injects 5 rapid-fire card testing swipes.

---

## 📁 Repository Structure

```
realtime-fraud-detection/
├── docker-compose.yml              # Complete 7-service orchestration
├── run_demo_server.py              # Instant standalone test server
├── start_all.bat                   # 1-click Windows starter script
├── README.md                       # System manual & specs
├── PROJECT_REPORT_VIVA_GUIDE.md    # B.Tech viva exam defense guide
│
├── backend-spring-boot/            # Java Spring Boot 3 Engine
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/fraudshield/
│       ├── FraudDetectionApplication.java
│       ├── config/                 # Kafka, Redis, WebSocket, CORS
│       ├── controller/             # Transactions, Dashboard, Simulators
│       ├── model/                  # JPA Entities & DTOs
│       ├── repository/             # Spring Data JPA interfaces
│       └── service/                # RuleEngine, MlClient, RedisState, Orchestrator
│
├── ml-service/                     # Python FastAPI & ML microservice
│   ├── Dockerfile
│   ├── requirements.txt
│   └── app/
│       ├── main.py                 # REST endpoints
│       ├── model_engine.py         # XGBoost inference & TreeSHAP
│       ├── schemas.py              # Pydantic models
│       └── train_model.py          # Synthetic dataset generator & trainer
│
└── frontend/                       # React.js Vite Dashboard
    ├── package.json
    ├── vite.config.js
    ├── Dockerfile
    └── src/
        ├── App.jsx                 # Dashboard root
        ├── components/
        │   ├── Header.jsx          # Live telemetry chips
        │   ├── StatsCards.jsx      # KPI counter grid
        │   ├── AttackSimulator.jsx # 1-click attack injections
        │   ├── LiveTransactionStream.jsx # Real-time event log
        │   ├── ExplainabilityModal.jsx  # TreeSHAP factor cards
        │   └── AnalyticsView.jsx   # Charts & leaderboard
        └── index.css
```
