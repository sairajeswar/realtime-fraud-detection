"""
Standalone Unified Demo Server for FraudShield Platform.
Runs a zero-dependency full API & Rule Engine server on port 8080.
Useful for quick testing without Docker/Kafka/Maven prerequisites!
"""

import sys
import json
import time
import math
import uuid
from datetime import datetime, timedelta
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs

# In-Memory State
DB_TRANSACTIONS = [
    {
        "transactionId": "TXN-MIDNIGHT-10045",
        "userId": "U102",
        "amount": 95000.0,
        "location": "Mumbai",
        "deviceId": "DEV_UNKNOWN_998",
        "merchantCategory": "Electronics",
        "ruleScore": 0.88,
        "mlScore": 0.94,
        "finalRiskScore": 0.916,
        "riskLevel": "CRITICAL",
        "decision": "BLOCK",
        "isNewDevice": True,
        "isNewLocation": True,
        "geoVelocityKmh": 850.0,
        "processingLatencyMs": 6.8,
        "timestamp": (datetime.now() - timedelta(minutes=2)).isoformat(),
        "riskFactors": [
            {"feature": "IMPOSSIBLE_TRAVEL_VELOCITY", "impactPercentage": 38.0, "description": "Calculated velocity (850 km/h) violates physical travel limits", "rawValue": "850 km/h"},
            {"feature": "AMOUNT_DEVIATION", "impactPercentage": 32.0, "description": "Amount ₹95,000 is 29.7x higher than user avg (₹3,200)", "rawValue": "29.7x"},
            {"feature": "NEW_DEVICE_DETECTED", "impactPercentage": 22.0, "description": "Unrecognized device DEV_UNKNOWN_998", "rawValue": "DEV_UNKNOWN_998"},
            {"feature": "MIDNIGHT_ANOMALY", "impactPercentage": 16.0, "description": "Executed during high-risk window (02:15 AM)", "rawValue": "02:15"}
        ]
    },
    {
        "transactionId": "TXN-FLAG-88412",
        "userId": "U891",
        "amount": 72000.0,
        "location": "Delhi",
        "deviceId": "DEV_FOREIGN_PROXY",
        "merchantCategory": "Jewelry",
        "ruleScore": 0.65,
        "mlScore": 0.74,
        "finalRiskScore": 0.704,
        "riskLevel": "HIGH",
        "decision": "REVIEW",
        "isNewDevice": True,
        "isNewLocation": False,
        "geoVelocityKmh": 45.0,
        "processingLatencyMs": 7.2,
        "timestamp": (datetime.now() - timedelta(minutes=8)).isoformat(),
        "riskFactors": [
            {"feature": "HIGH_VALUE_THRESHOLD", "impactPercentage": 28.0, "description": "Amount exceeds regular spending ceiling", "rawValue": "₹72,000"},
            {"feature": "NEW_DEVICE_DETECTED", "impactPercentage": 22.0, "description": "Unrecognized hardware fingerprint", "rawValue": "DEV_FOREIGN_PROXY"}
        ]
    },
    {
        "transactionId": "TXN-SAFE-33120",
        "userId": "U345",
        "amount": 1250.0,
        "location": "Chennai",
        "deviceId": "DEV_PIXEL_8",
        "merchantCategory": "Food",
        "ruleScore": 0.05,
        "mlScore": 0.03,
        "finalRiskScore": 0.038,
        "riskLevel": "LOW",
        "decision": "APPROVE",
        "isNewDevice": False,
        "isNewLocation": False,
        "geoVelocityKmh": 12.0,
        "processingLatencyMs": 5.4,
        "timestamp": (datetime.now() - timedelta(minutes=15)).isoformat(),
        "riskFactors": []
    }
]

USER_PROFILES = {
    "U102": {"avg": 3200.0, "primaryDevice": "DEV_APPLE_12", "primaryLoc": "Hyderabad", "lat": 17.3850, "lon": 78.4867, "lastTime": datetime.now() - timedelta(hours=3)},
    "U891": {"avg": 2100.0, "primaryDevice": "DEV_SAMSUNG_S23", "primaryLoc": "Delhi", "lat": 28.6139, "lon": 77.2090, "lastTime": datetime.now() - timedelta(hours=2)},
    "U345": {"avg": 1500.0, "primaryDevice": "DEV_PIXEL_8", "primaryLoc": "Chennai", "lat": 13.0827, "lon": 80.2707, "lastTime": datetime.now() - timedelta(hours=4)},
}

def haversine(lat1, lon1, lat2, lon2):
    r = 6371.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp = math.radians(lat2 - lat1)
    dl = math.radians(lon2 - lon1)
    a = math.sin(dp / 2)**2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2)**2
    return r * (2 * math.atan2(math.sqrt(a), math.sqrt(1 - a)))

def evaluate_transaction(data):
    start = time.perf_counter()
    user_id = data.get("userId", "U102")
    amount = float(data.get("amount", 1000.0))
    location = data.get("location", "Mumbai")
    device_id = data.get("deviceId", "DEV_UNKNOWN")
    category = data.get("merchantCategory", "Grocery")

    profile = USER_PROFILES.get(user_id, {
        "avg": 2500.0, "primaryDevice": "DEV_APPLE_12", "primaryLoc": "Hyderabad", "lat": 17.3850, "lon": 78.4867, "lastTime": datetime.now() - timedelta(hours=4)
    })

    is_new_device = device_id != profile["primaryDevice"]
    is_new_loc = location != profile["primaryLoc"]
    
    # Geo-velocity
    lat = data.get("latitude", 19.0760)
    lon = data.get("longitude", 72.8777)
    dist = haversine(profile["lat"], profile["lon"], lat, lon)
    speed = dist / 0.25 if dist > 50.0 else 0.0

    # Rule Engine Accumulator
    rule_score = 0.04
    factors = []

    if amount > 100000.0:
        rule_score += 0.28
        factors.append({"feature": "HIGH_AMOUNT", "impactPercentage": 28.0, "description": f"Amount ₹{amount:,.0f} exceeds ₹1,00,000 threshold", "rawValue": amount})
    
    ratio = amount / max(profile["avg"], 50.0)
    if ratio > 4.0:
        pct = min(35.0, round(ratio * 3.2, 1))
        rule_score += 0.32
        factors.append({"feature": "AMOUNT_DEVIATION", "impactPercentage": pct, "description": f"Amount is {ratio:.1f}x higher than average", "rawValue": f"{ratio:.1f}x"})

    if is_new_device:
        rule_score += 0.22
        factors.append({"feature": "NEW_DEVICE", "impactPercentage": 22.0, "description": f"Unrecognized hardware fingerprint ({device_id})", "rawValue": device_id})

    if is_new_loc:
        rule_score += 0.18
        factors.append({"feature": "NEW_LOCATION", "impactPercentage": 18.0, "description": f"Origin zone ({location}) deviates from baseline", "rawValue": location})

    if speed > 600.0:
        rule_score += 0.40
        factors.append({"feature": "IMPOSSIBLE_TRAVEL", "impactPercentage": 40.0, "description": f"Travel velocity ({speed:.0f} km/h) violates physical speed limits", "rawValue": f"{speed:.0f} km/h"})

    rule_score = min(1.0, rule_score)

    # Simulated XGBoost model prediction
    ml_score = 1.0 / (1.0 + math.exp(-( (rule_score - 0.45) * 8.0 )))
    ml_score = round(ml_score, 4)

    # Aggregator (40% rules, 60% ML)
    final_risk = round((rule_score * 0.40) + (ml_score * 0.60), 4)

    if final_risk < 0.30:
        risk_level = "LOW"
        decision = "APPROVE"
    elif final_risk < 0.70:
        risk_level = "MEDIUM"
        decision = "REVIEW"
    elif final_risk < 0.90:
        risk_level = "HIGH"
        decision = "BLOCK"
    else:
        risk_level = "CRITICAL"
        decision = "BLOCK"

    latency_ms = round((time.perf_counter() - start) * 1000 + 4.5, 2)

    result = {
        "transactionId": data.get("transactionId", f"TXN-{uuid.uuid4().hex[:8].upper()}"),
        "userId": user_id,
        "amount": amount,
        "location": location,
        "deviceId": device_id,
        "merchantCategory": category,
        "ruleScore": round(rule_score, 4),
        "mlScore": ml_score,
        "finalRiskScore": final_risk,
        "riskLevel": risk_level,
        "decision": decision,
        "isNewDevice": is_new_device,
        "isNewLocation": is_new_loc,
        "geoVelocityKmh": round(speed, 1),
        "riskFactors": factors,
        "processingLatencyMs": latency_ms,
        "timestamp": datetime.now().isoformat()
    }

    DB_TRANSACTIONS.insert(0, result)
    return result

class FraudHandler(BaseHTTPRequestHandler):
    def _send_cors(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")

    def do_OPTIONS(self):
        self.send_response(200)
        self._send_cors()
        self.end_headers()

    def do_GET(self):
        url = urlparse(self.path)
        path = url.path

        if path == "/api/dashboard/stats":
            total = len(DB_TRANSACTIONS)
            approved = sum(1 for t in DB_TRANSACTIONS if t["decision"] == "APPROVE")
            review = sum(1 for t in DB_TRANSACTIONS if t["decision"] == "REVIEW")
            blocked = sum(1 for t in DB_TRANSACTIONS if t["decision"] == "BLOCK")
            total_vol = sum(t["amount"] for t in DB_TRANSACTIONS)
            blocked_vol = sum(t["amount"] for t in DB_TRANSACTIONS if t["decision"] == "BLOCK")

            res = {
                "totalTransactions": total + 128450,
                "approvedCount": approved + 124110,
                "reviewCount": review + 2000,
                "blockedCount": blocked + 2340,
                "fraudRatePercentage": round((blocked / max(1, total)) * 100, 2),
                "totalVolume": round(total_vol + 84500000, 2),
                "blockedAmount": round(blocked_vol + 3120000, 2),
                "avgLatencyMs": 6.8,
                "decisionBreakdown": {"APPROVE": approved, "REVIEW": review, "BLOCK": blocked},
                "locationFraudStats": {"Mumbai": 32, "Delhi": 21, "Chennai": 14, "Bengaluru": 11, "Hyderabad": 8},
                "categoryRiskStats": {"Crypto": 38, "Electronics": 29, "Jewelry": 19, "Travel": 14}
            }
            self._send_json(res)

        elif path == "/api/transactions":
            self._send_json(DB_TRANSACTIONS[:50])

        elif path == "/health":
            self._send_json({"status": "healthy", "service": "FraudShield-Unified-Server"})

        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        url = urlparse(self.path)
        path = url.path
        length = int(self.headers.get("Content-Length", 0))
        body = json.loads(self.rfile.read(length).decode("utf-8")) if length > 0 else {}

        if path == "/api/transactions":
            result = evaluate_transaction(body)
            self._send_json(result)

        elif path == "/api/simulate/normal":
            result = evaluate_transaction({
                "userId": "U102", "amount": 450, "location": "Hyderabad",
                "deviceId": "DEV_APPLE_12", "merchantCategory": "Grocery", "latitude": 17.3850, "longitude": 78.4867
            })
            self._send_json(result)

        elif path == "/api/simulate/midnight-attack":
            result = evaluate_transaction({
                "userId": "U102", "amount": 95000, "location": "Mumbai",
                "deviceId": "DEV_UNKNOWN_998", "merchantCategory": "Electronics", "latitude": 19.0760, "longitude": 72.8777
            })
            self._send_json(result)

        elif path == "/api/simulate/impossible-travel":
            r1 = evaluate_transaction({"userId": "U891", "amount": 1200, "location": "Delhi", "deviceId": "DEV_SAMSUNG_S23", "merchantCategory": "Food"})
            r2 = evaluate_transaction({"userId": "U891", "amount": 88000, "location": "London", "deviceId": "DEV_PROXY", "merchantCategory": "Jewelry", "latitude": 51.5, "longitude": -0.12})
            self._send_json([r1, r2])

        elif path == "/api/simulate/card-testing-burst":
            results = []
            for i in range(5):
                results.append(evaluate_transaction({
                    "userId": "U345", "amount": 99 * (i + 1), "location": "VPN-Proxy",
                    "deviceId": f"BOT_{i}", "merchantCategory": "Crypto"
                }))
            self._send_json(results)

        else:
            self.send_response(404)
            self.end_headers()

    def _send_json(self, data):
        self.send_response(200)
        self._send_cors()
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(data).encode("utf-8"))

def run():
    port = 8080
    server = HTTPServer(("0.0.0.0", port), FraudHandler)
    print(f"\n==========================================================")
    print(f" 🛡️  FraudShield Real-Time Fraud Detection Engine Started")
    print(f" 🌐  Listening on: http://localhost:{port}")
    print(f" 🚀  Ready for API requests, attack simulation, and frontend")
    print(f"==========================================================\n")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server.")

if __name__ == "__main__":
    run()
