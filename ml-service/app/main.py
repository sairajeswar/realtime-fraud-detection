from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from typing import Dict, Any

from .schemas import (
    TransactionFeatureInput,
    PredictionResponse,
    BatchPredictionRequest,
    BatchPredictionResponse,
    ModelMetricsResponse
)
from .model_engine import engine
from .train_model import train_and_evaluate

app = FastAPI(
    title="Real-Time Fraud Detection ML Microservice",
    description="High-throughput XGBoost & Explainable AI inference microservice for real-time banking transactions.",
    version="2.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health_check() -> Dict[str, str]:
    return {
        "status": "healthy",
        "service": "FraudShield-ML-Service",
        "engine": "XGBoost",
        "features_loaded": str(len(engine.feature_cols))
    }

@app.post("/predict", response_model=PredictionResponse)
def predict_fraud(txn: TransactionFeatureInput) -> PredictionResponse:
    try:
        return engine.predict(txn)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference error: {str(e)}")

@app.post("/predict/batch", response_model=BatchPredictionResponse)
def predict_batch(batch: BatchPredictionRequest) -> BatchPredictionResponse:
    try:
        results = [engine.predict(txn) for txn in batch.transactions]
        return BatchPredictionResponse(results=results)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Batch error: {str(e)}")

@app.get("/metrics")
def get_model_metrics() -> Dict[str, Any]:
    return engine.metrics

@app.post("/retrain")
def retrain_model(background_tasks: BackgroundTasks) -> Dict[str, str]:
    background_tasks.add_task(train_and_evaluate)
    return {"message": "Retraining job dispatched in background."}
