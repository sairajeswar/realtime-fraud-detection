import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import StatsCards from './components/StatsCards';
import AttackSimulator from './components/AttackSimulator';
import LiveTransactionStream from './components/LiveTransactionStream';
import AnalyticsView from './components/AnalyticsView';
import ExplainabilityModal from './components/ExplainabilityModal';

// Seed demo fallback data for zero-friction local review
const INITIAL_DEMO_TRANSACTIONS = [
  {
    transactionId: 'TXN-MIDNIGHT-10045',
    userId: 'U102',
    amount: 95000,
    location: 'Mumbai',
    deviceId: 'DEV_UNKNOWN_998',
    merchantCategory: 'Electronics',
    ruleScore: 0.88,
    mlScore: 0.94,
    finalRiskScore: 0.916,
    riskLevel: 'CRITICAL',
    decision: 'BLOCK',
    isNewDevice: true,
    isNewLocation: true,
    geoVelocityKmh: 850.0,
    processingLatencyMs: 6.8,
    timestamp: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
    riskFactors: [
      { feature: 'IMPOSSIBLE_TRAVEL_VELOCITY', impactPercentage: 38.0, description: 'Calculated velocity (850 km/h) violates physical travel limits', rawValue: '850 km/h' },
      { feature: 'AMOUNT_DEVIATION', impactPercentage: 32.0, description: 'Amount ₹95,000 is 29.7x higher than user avg (₹3,200)', rawValue: '29.7x' },
      { feature: 'NEW_DEVICE_DETECTED', impactPercentage: 22.0, description: 'Unrecognized device DEV_UNKNOWN_998', rawValue: 'DEV_UNKNOWN_998' },
      { feature: 'MIDNIGHT_ANOMALY', impactPercentage: 16.0, description: 'Executed during high-risk window (02:15 AM)', rawValue: '02:15' }
    ]
  },
  {
    transactionId: 'TXN-FLAG-88412',
    userId: 'U891',
    amount: 72000,
    location: 'Delhi',
    deviceId: 'DEV_FOREIGN_PROXY',
    merchantCategory: 'Jewelry',
    ruleScore: 0.65,
    mlScore: 0.74,
    finalRiskScore: 0.704,
    riskLevel: 'HIGH',
    decision: 'REVIEW',
    isNewDevice: true,
    isNewLocation: false,
    geoVelocityKmh: 45.0,
    processingLatencyMs: 7.2,
    timestamp: new Date(Date.now() - 1000 * 60 * 8).toISOString(),
    riskFactors: [
      { feature: 'HIGH_VALUE_THRESHOLD', impactPercentage: 28.0, description: 'Amount exceeds regular spending ceiling', rawValue: '₹72,000' },
      { feature: 'NEW_DEVICE_DETECTED', impactPercentage: 22.0, description: 'Unrecognized hardware fingerprint', rawValue: 'DEV_FOREIGN_PROXY' }
    ]
  },
  {
    transactionId: 'TXN-SAFE-33120',
    userId: 'U345',
    amount: 1250,
    location: 'Chennai',
    deviceId: 'DEV_PIXEL_8',
    merchantCategory: 'Food',
    ruleScore: 0.05,
    mlScore: 0.03,
    finalRiskScore: 0.038,
    riskLevel: 'LOW',
    decision: 'APPROVE',
    isNewDevice: false,
    isNewLocation: false,
    geoVelocityKmh: 12.0,
    processingLatencyMs: 5.4,
    timestamp: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
    riskFactors: []
  },
  {
    transactionId: 'TXN-SAFE-10928',
    userId: 'U550',
    amount: 4500,
    location: 'Bengaluru',
    deviceId: 'DEV_MACBOOK_M3',
    merchantCategory: 'Grocery',
    ruleScore: 0.05,
    mlScore: 0.06,
    finalRiskScore: 0.056,
    riskLevel: 'LOW',
    decision: 'APPROVE',
    isNewDevice: false,
    isNewLocation: false,
    geoVelocityKmh: 0.0,
    processingLatencyMs: 6.1,
    timestamp: new Date(Date.now() - 1000 * 60 * 22).toISOString(),
    riskFactors: []
  }
];

export default function App() {
  const [transactions, setTransactions] = useState(INITIAL_DEMO_TRANSACTIONS);
  const [stats, setStats] = useState({
    totalTransactions: 128452,
    approvedCount: 124110,
    reviewCount: 2001,
    blockedCount: 2341,
    fraudRatePercentage: 1.82,
    totalVolume: 84520900,
    blockedAmount: 3125000,
    avgLatencyMs: 6.8,
    decisionBreakdown: { APPROVE: 124110, REVIEW: 2001, BLOCK: 2341 },
    locationFraudStats: { Mumbai: 32, Delhi: 21, Chennai: 14, Bengaluru: 11, Hyderabad: 8 },
    categoryRiskStats: { Crypto: 38, Electronics: 29, Jewelry: 19, Travel: 14 }
  });

  const [isConnected, setIsConnected] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [selectedTxn, setSelectedTxn] = useState(null);
  const [activeView, setActiveView] = useState('feed'); // 'feed' or 'analytics'

  const fetchData = async () => {
    setIsRefreshing(true);
    try {
      const statsRes = await fetch('/api/dashboard/stats');
      if (statsRes.ok) {
        const statsData = await statsRes.json();
        setStats(statsData);
      }

      const txnsRes = await fetch('/api/transactions?limit=25');
      if (txnsRes.ok) {
        const txnsData = await txnsRes.json();
        if (txnsData && txnsData.length > 0) {
          setTransactions(txnsData);
        }
      }
      setIsConnected(true);
    } catch (err) {
      // Running in offline / standalone mode
      setIsConnected(false);
    } finally {
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 4000);
    return () => clearInterval(interval);
  }, []);

  const handleTransactionCreated = (newTxn) => {
    setTransactions((prev) => [newTxn, ...prev]);
    setSelectedTxn(newTxn); // Auto-open explainability for instant inspection
    
    // Update live metrics immediately
    setStats((prev) => {
      const isBlocked = newTxn.decision === 'BLOCK';
      const isReview = newTxn.decision === 'REVIEW';
      const newTotal = (prev.totalTransactions || 0) + 1;
      const newBlocked = (prev.blockedCount || 0) + (isBlocked ? 1 : 0);
      return {
        ...prev,
        totalTransactions: newTotal,
        approvedCount: (prev.approvedCount || 0) + (!isBlocked && !isReview ? 1 : 0),
        reviewCount: (prev.reviewCount || 0) + (isReview ? 1 : 0),
        blockedCount: newBlocked,
        fraudRatePercentage: ((newBlocked / newTotal) * 100),
        totalVolume: (Number(prev.totalVolume) || 0) + Number(newTxn.amount),
        blockedAmount: (Number(prev.blockedAmount) || 0) + (isBlocked ? Number(newTxn.amount) : 0)
      };
    });
  };

  return (
    <div className="min-h-screen bg-[#080C15] text-slate-100 flex flex-col font-['Plus_Jakarta_Sans',sans-serif]">
      
      {/* Top Navbar */}
      <Header
        isConnected={isConnected}
        onRefresh={fetchData}
        isRefreshing={isRefreshing}
      />

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 space-y-6">

        {/* 1. KPI Statistics Overview */}
        <StatsCards stats={stats} />

        {/* 2. Interactive Attack Simulator */}
        <AttackSimulator onTransactionCreated={handleTransactionCreated} />

        {/* 3. Navigation View Switcher */}
        <div className="flex border-b border-slate-800 space-x-6 text-sm font-medium">
          <button
            onClick={() => setActiveView('feed')}
            className={`pb-3 border-b-2 transition-colors cursor-pointer ${activeView === 'feed' ? 'border-blue-500 text-blue-400 font-semibold' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
          >
            Live Transaction Stream & Detection Logs
          </button>
          <button
            onClick={() => setActiveView('analytics')}
            className={`pb-3 border-b-2 transition-colors cursor-pointer ${activeView === 'analytics' ? 'border-blue-500 text-blue-400 font-semibold' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
          >
            Fraud Analytics, City Hotspots & ML Validation
          </button>
        </div>

        {/* 4. Tab Content */}
        {activeView === 'feed' ? (
          <LiveTransactionStream
            transactions={transactions}
            onSelectTransaction={(txn) => setSelectedTxn(txn)}
          />
        ) : (
          <AnalyticsView stats={stats} />
        )}

      </main>

      {/* Explainable AI Modal */}
      <ExplainabilityModal
        txn={selectedTxn}
        onClose={() => setSelectedTxn(null)}
      />

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950 py-4 text-center text-xs text-slate-500">
        FraudShield Real-Time Financial Fraud Detection Platform • Built with Java Spring Boot, Apache Kafka, XGBoost, Redis & React
      </footer>

    </div>
  );
}
