from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class TransactionFeatureInput(BaseModel):
    transaction_id: str = Field(..., example="TXN10045")
    user_id: str = Field(..., example="U102")
    amount: float = Field(..., example=95000.0)
    location: str = Field(..., example="Mumbai")
    latitude: float = Field(19.0760, example=19.0760)
    longitude: float = Field(72.8777, example=72.8777)
    device_id: str = Field(..., example="DEV998")
    merchant_category: str = Field("Electronics", example="Electronics")
    timestamp: Optional[str] = Field(None, example="2026-10-06T02:15:00")
    
    # Behavioral and historical context features
    user_avg_amount: float = Field(3200.0, example=3200.0)
    user_std_dev_amount: float = Field(1500.0, example=1500.0)
    is_new_device: bool = Field(True, example=True)
    is_new_location: bool = Field(True, example=True)
    geo_velocity_kmh: float = Field(0.0, example=850.0)
    hour_of_day: int = Field(2, example=2)
    txns_last_5min: int = Field(1, example=4)
    txns_last_24h: int = Field(3, example=6)
    account_age_days: int = Field(180, example=180)
    prev_failed_txns: int = Field(0, example=2)

class FactorImpact(BaseModel):
    feature: str
    impact_percentage: float
    description: str
    raw_value: Any

class PredictionResponse(BaseModel):
    transaction_id: str
    fraud_probability: float
    ml_risk_level: str
    top_risk_factors: List[FactorImpact]
    model_version: str
    inference_latency_ms: float

class BatchPredictionRequest(BaseModel):
    transactions: List[TransactionFeatureInput]

class BatchPredictionResponse(BaseModel):
    results: List[PredictionResponse]

class ModelMetricsResponse(BaseModel):
    model_name: str
    accuracy: float
    precision: float
    recall: float
    f1_score: float
    roc_auc: float
    pr_auc: float
    total_training_samples: int
    fraud_rate: float
