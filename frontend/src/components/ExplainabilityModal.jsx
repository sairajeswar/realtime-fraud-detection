import React from 'react';
import { X, ShieldAlert, ShieldCheck, AlertTriangle, Cpu, Layers, MapPin, Smartphone, Clock, ArrowRight } from 'lucide-react';

export default function ExplainabilityModal({ txn, onClose }) {
  if (!txn) return null;

  let factors = [];
  try {
    if (txn.riskFactors && Array.isArray(txn.riskFactors)) {
      factors = txn.riskFactors;
    } else if (txn.explanationJson) {
      factors = JSON.parse(txn.explanationJson);
    }
  } catch (e) {
    factors = [];
  }

  const scorePct = Math.round((txn.finalRiskScore || 0) * 100);
  const rulePct = Math.round((txn.ruleScore || 0) * 100);
  const mlPct = Math.round((txn.mlScore || 0) * 100);

  const getDecisionBadge = (decision) => {
    switch (decision) {
      case 'BLOCK':
        return <span className="px-3 py-1 bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-full font-bold flex items-center gap-1.5"><ShieldAlert className="w-4 h-4" /> BLOCK</span>;
      case 'REVIEW':
        return <span className="px-3 py-1 bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-full font-bold flex items-center gap-1.5"><AlertTriangle className="w-4 h-4" /> MANUAL REVIEW</span>;
      default:
        return <span className="px-3 py-1 bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-full font-bold flex items-center gap-1.5"><ShieldCheck className="w-4 h-4" /> APPROVED</span>;
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-[#0D1322] border border-slate-700/80 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
        
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-slate-800">
          <div>
            <div className="flex items-center gap-3">
              <h2 className="text-lg font-bold text-white font-mono">
                {txn.transactionId}
              </h2>
              {getDecisionBadge(txn.decision)}
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Autonomous Risk Score Attribution & Explainable AI (TreeSHAP) Breakdown
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">

          {/* Key Metrics Header */}
          <div className="grid grid-cols-3 gap-3 p-4 bg-slate-900/80 rounded-xl border border-slate-800 text-center font-mono">
            <div className="p-2 bg-slate-800/40 rounded-lg">
              <div className="text-xs text-slate-400">Rule Engine Score</div>
              <div className="text-xl font-bold text-amber-400 mt-1">{rulePct}%</div>
              <div className="text-[10px] text-slate-500 mt-0.5">Weight 40%</div>
            </div>
            <div className="p-2 bg-slate-800/40 rounded-lg">
              <div className="text-xs text-slate-400">XGBoost ML Score</div>
              <div className="text-xl font-bold text-blue-400 mt-1">{mlPct}%</div>
              <div className="text-[10px] text-slate-500 mt-0.5">Weight 60%</div>
            </div>
            <div className="p-2 bg-slate-800/40 rounded-lg border border-slate-700">
              <div className="text-xs text-slate-400">Composite Risk</div>
              <div className={`text-xl font-bold mt-1 ${scorePct >= 70 ? 'text-rose-400' : scorePct >= 30 ? 'text-amber-400' : 'text-emerald-400'}`}>
                {scorePct}%
              </div>
              <div className="text-[10px] text-slate-400 mt-0.5">{txn.riskLevel || 'TIER'}</div>
            </div>
          </div>

          {/* Transaction Metadata Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs bg-slate-950/60 p-4 rounded-xl border border-slate-800/60">
            <div>
              <span className="text-slate-500 block mb-1">User ID</span>
              <span className="font-mono font-semibold text-slate-200">{txn.userId}</span>
            </div>
            <div>
              <span className="text-slate-500 block mb-1">Amount</span>
              <span className="font-mono font-semibold text-white">₹{Number(txn.amount).toLocaleString()}</span>
            </div>
            <div>
              <span className="text-slate-500 block mb-1">Category</span>
              <span className="font-semibold text-slate-200">{txn.merchantCategory}</span>
            </div>
            <div>
              <span className="text-slate-500 block mb-1">Location</span>
              <span className="font-semibold text-slate-200">{txn.location}</span>
            </div>
            <div>
              <span className="text-slate-500 block mb-1">Device</span>
              <span className="font-mono text-slate-300">{txn.deviceId}</span>
            </div>
            <div>
              <span className="text-slate-500 block mb-1">New Device</span>
              <span className={txn.isNewDevice ? 'text-rose-400 font-semibold' : 'text-slate-400'}>
                {txn.isNewDevice ? 'YES (Mismatch)' : 'No (Trusted)'}
              </span>
            </div>
            <div>
              <span className="text-slate-500 block mb-1">New Location</span>
              <span className={txn.isNewLocation ? 'text-rose-400 font-semibold' : 'text-slate-400'}>
                {txn.isNewLocation ? 'YES (Anomaly)' : 'No (Usual)'}
              </span>
            </div>
            <div>
              <span className="text-slate-500 block mb-1">Speed Check</span>
              <span className={txn.geoVelocityKmh > 500 ? 'text-rose-400 font-semibold' : 'text-slate-400'}>
                {txn.geoVelocityKmh ? `${Math.round(txn.geoVelocityKmh)} km/h` : '0 km/h'}
              </span>
            </div>
          </div>

          {/* Explainable AI (Why?) Breakdown */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
                <Cpu className="w-4 h-4 text-blue-400" />
                Why was this decision taken? (Top Feature Attribution)
              </h3>
              <span className="text-[11px] text-slate-400 font-mono">TreeSHAP Values</span>
            </div>

            {factors.length === 0 ? (
              <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-center text-xs text-emerald-400">
                ✅ No suspicious anomalies detected. Transaction fits within user's standard behavioral profile.
              </div>
            ) : (
              <div className="space-y-3">
                {factors.map((factor, index) => {
                  const pct = Math.min(100, Math.max(5, factor.impactPercentage || 20));
                  return (
                    <div key={index} className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl">
                      <div className="flex items-center justify-between text-xs mb-1.5">
                        <span className="font-semibold text-slate-200">
                          {factor.description || factor.feature}
                        </span>
                        <span className="font-mono font-bold text-rose-400">
                          +{factor.impactPercentage}%
                        </span>
                      </div>
                      <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                        <div
                          className="bg-gradient-to-r from-amber-500 to-rose-500 h-1.5 rounded-full transition-all duration-500"
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/80 flex items-center justify-between text-xs text-slate-400">
          <span>Processing latency: <strong className="text-white font-mono">{txn.processingLatencyMs || 8.5} ms</strong></span>
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg transition-colors cursor-pointer"
          >
            Close
          </button>
        </div>

      </div>
    </div>
  );
}
