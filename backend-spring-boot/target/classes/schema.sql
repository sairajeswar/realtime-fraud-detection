-- PostgreSQL Initialization Schema for Real-Time Fraud Detection Platform

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id VARCHAR(64) PRIMARY KEY,
    avg_amount NUMERIC(12, 2) DEFAULT 2500.00,
    std_dev_amount NUMERIC(12, 2) DEFAULT 1200.00,
    primary_device_id VARCHAR(64),
    primary_location VARCHAR(128),
    primary_latitude NUMERIC(9, 6),
    primary_longitude NUMERIC(9, 6),
    account_age_days INT DEFAULT 180,
    total_transactions INT DEFAULT 0,
    last_txn_timestamp TIMESTAMP,
    last_txn_latitude NUMERIC(9, 6),
    last_txn_longitude NUMERIC(9, 6),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(64) UNIQUE NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    location VARCHAR(128) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    device_id VARCHAR(64) NOT NULL,
    merchant_category VARCHAR(64) NOT NULL,
    is_new_device BOOLEAN DEFAULT FALSE,
    is_new_location BOOLEAN DEFAULT FALSE,
    geo_velocity_kmh NUMERIC(8, 2) DEFAULT 0.0,
    
    rule_score NUMERIC(5, 4) NOT NULL,
    ml_score NUMERIC(5, 4) NOT NULL,
    final_risk_score NUMERIC(5, 4) NOT NULL,
    
    risk_level VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    decision VARCHAR(20) NOT NULL,   -- APPROVE, REVIEW, BLOCK
    
    explanation_json TEXT,
    processing_latency_ms NUMERIC(6, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_decision ON transactions(decision);
CREATE INDEX IF NOT EXISTS idx_transactions_created ON transactions(created_at DESC);

-- Seed initial user profiles for testing
INSERT INTO user_profiles (user_id, avg_amount, std_dev_amount, primary_device_id, primary_location, primary_latitude, primary_longitude, account_age_days)
VALUES 
('U102', 3200.00, 1500.00, 'DEV_APPLE_12', 'Hyderabad', 17.3850, 78.4867, 340),
('U891', 2100.00, 800.00, 'DEV_SAMSUNG_S23', 'Delhi', 28.6139, 77.2090, 410),
('U345', 1500.00, 600.00, 'DEV_PIXEL_8', 'Chennai', 13.0827, 80.2707, 190),
('U550', 5400.00, 2200.00, 'DEV_MACBOOK_M3', 'Bengaluru', 12.9716, 77.5946, 620)
ON CONFLICT (user_id) DO NOTHING;
