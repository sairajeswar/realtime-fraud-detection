import os
import json
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score,
    f1_score, roc_auc_score, average_precision_score
)
import xgboost as xgb
import joblib

MODEL_DIR = os.path.dirname(os.path.abspath(__file__))
ARTIFACTS_DIR = os.path.join(MODEL_DIR, "artifacts")
os.makedirs(ARTIFACTS_DIR, exist_ok=True)

CATEGORIES = ["Grocery", "Utility", "Food", "Entertainment", "Electronics", "Travel", "Jewelry", "Crypto"]
CATEGORY_MAP = {cat: i for i, cat in enumerate(CATEGORIES)}

def generate_synthetic_data(n_samples: int = 25000, random_state: int = 42):
    """
    Generates realistic credit/debit transaction records with typical
    financial distributions and authentic fraud patterns.
    """
    np.random.seed(random_state)
    
    # 1. Base transaction amounts (Log-normal distribution simulating real transactions)
    base_amounts = np.exp(np.random.normal(7.5, 1.2, n_samples))
    user_avg = np.exp(np.random.normal(7.5, 1.0, n_samples))
    user_std = user_avg * np.random.uniform(0.2, 0.6, n_samples)
    
    # Amount z-score / ratio
    amount_ratio = base_amounts / np.maximum(user_avg, 50.0)
    
    # 2. Categorical / Temporal features
    merchant_cat_idx = np.random.choice(len(CATEGORIES), n_samples, p=[0.25, 0.20, 0.20, 0.12, 0.10, 0.07, 0.04, 0.02])
    hours = np.random.choice(24, n_samples)
    
    # 3. Anomaly flags
    is_new_device = np.random.choice([0, 1], n_samples, p=[0.88, 0.12])
    is_new_location = np.random.choice([0, 1], n_samples, p=[0.90, 0.10])
    
    # 4. Behavioral & velocity features
    txns_5m = np.random.poisson(0.4, n_samples)
    txns_24h = np.random.poisson(2.5, n_samples)
    geo_speed = np.random.exponential(25.0, n_samples) # km/h
    account_age_days = np.random.randint(10, 1500, n_samples)
    failed_txns = np.random.choice([0, 1, 2, 3], n_samples, p=[0.92, 0.05, 0.02, 0.01])

    # Construct Ground Truth Fraud Probability using non-linear risk weights
    fraud_logits = (
        -4.5 # Prior low base fraud rate (~2%)
        + 1.8 * (amount_ratio > 4.0)
        + 2.2 * (amount_ratio > 10.0)
        + 1.5 * is_new_device
        + 1.3 * is_new_location
        + 2.6 * (is_new_device * is_new_location) # High correlation synergy
        + 2.8 * (geo_speed > 600.0) # Impossible travel
        + 1.4 * ((hours >= 0) & (hours <= 4)) # Midnight anomaly
        + 1.9 * (txns_5m >= 4) # Velocity burst
        + 1.6 * (failed_txns >= 2) # Brute force card testing
        + 1.2 * np.isin(merchant_cat_idx, [6, 7]) # Jewelry or Crypto
        - 0.5 * (account_age_days > 365) # Mature account safety
    )
    
    fraud_probs = 1.0 / (1.0 + np.exp(-fraud_logits))
    labels = (np.random.rand(n_samples) < fraud_probs).astype(int)

    df = pd.DataFrame({
        "amount": base_amounts,
        "user_avg_amount": user_avg,
        "amount_ratio": amount_ratio,
        "merchant_cat_idx": merchant_cat_idx,
        "hour_of_day": hours,
        "is_new_device": is_new_device,
        "is_new_location": is_new_location,
        "txns_last_5min": txns_5m,
        "txns_last_24h": txns_24h,
        "geo_velocity_kmh": geo_speed,
        "account_age_days": account_age_days,
        "prev_failed_txns": failed_txns,
        "is_fraud": labels
    })
    
    return df

def train_and_evaluate():
    print(" [ML Training] Generating 25,000 synthetic financial transactions...")
    df = generate_synthetic_data(25000)
    
    feature_cols = [
        "amount", "user_avg_amount", "amount_ratio", "merchant_cat_idx",
        "hour_of_day", "is_new_device", "is_new_location", "txns_last_5min",
        "txns_last_24h", "geo_velocity_kmh", "account_age_days", "prev_failed_txns"
    ]
    
    X = df[feature_cols]
    y = df["is_fraud"]
    fraud_rate = float(y.mean())
    print(f" [ML Training] Dataset created. Fraud rate: {fraud_rate*100:.2f}%")

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    # Calculate class weight balance for XGBoost
    scale_pos = (len(y_train) - sum(y_train)) / max(1, sum(y_train))
    
    # 1. Train Primary XGBoost Model
    print(" [ML Training] Training XGBoost Classifier...")
    xgb_model = xgb.XGBClassifier(
        n_estimators=160,
        max_depth=5,
        learning_rate=0.08,
        scale_pos_weight=scale_pos,
        subsample=0.85,
        colsample_bytree=0.85,
        random_state=42,
        eval_metric="logloss"
    )
    xgb_model.fit(X_train, y_train)

    # 2. Train Benchmark Random Forest Model
    print(" [ML Training] Training Benchmark Random Forest Classifier...")
    rf_model = RandomForestClassifier(
        n_estimators=100,
        max_depth=7,
        class_weight="balanced",
        random_state=42,
        n_jobs=-1
    )
    rf_model.fit(X_train, y_train)

    # Evaluate Models
    def get_metrics(model, name):
        probs = model.predict_proba(X_test)[:, 1]
        preds = (probs >= 0.5).astype(int)
        return {
            "model_name": name,
            "accuracy": round(float(accuracy_score(y_test, preds)), 4),
            "precision": round(float(precision_score(y_test, preds, zero_division=0)), 4),
            "recall": round(float(recall_score(y_test, preds)), 4),
            "f1_score": round(float(f1_score(y_test, preds)), 4),
            "roc_auc": round(float(roc_auc_score(y_test, probs)), 4),
            "pr_auc": round(float(average_precision_score(y_test, probs)), 4),
            "total_test_samples": len(y_test)
        }

    xgb_metrics = get_metrics(xgb_model, "XGBoost")
    rf_metrics = get_metrics(rf_model, "Random Forest")

    print(f" -> XGBoost  ROC-AUC: {xgb_metrics['roc_auc']}, PR-AUC: {xgb_metrics['pr_auc']}, F1: {xgb_metrics['f1_score']}")
    print(f" -> RanForest ROC-AUC: {rf_metrics['roc_auc']}, PR-AUC: {rf_metrics['pr_auc']}, F1: {rf_metrics['f1_score']}")

    # Save Model Artifacts
    artifacts = {
        "model": xgb_model,
        "benchmark_rf": rf_model,
        "feature_cols": feature_cols,
        "category_map": CATEGORY_MAP,
        "feature_importances": dict(zip(feature_cols, [float(x) for x in xgb_model.feature_importances_]))
    }
    
    model_path = os.path.join(ARTIFACTS_DIR, "fraud_model.joblib")
    joblib.dump(artifacts, model_path)
    
    summary = {
        "primary_model": xgb_metrics,
        "benchmark_model": rf_metrics,
        "feature_columns": feature_cols,
        "total_training_samples": len(df),
        "fraud_rate": fraud_rate
    }
    
    metrics_path = os.path.join(ARTIFACTS_DIR, "metrics.json")
    with open(metrics_path, "w") as f:
        json.dump(summary, f, indent=2)

    print(f" [ML Training] Artifacts successfully serialized to {model_path} and {metrics_path}")
    return summary

if __name__ == "__main__":
    train_and_evaluate()
