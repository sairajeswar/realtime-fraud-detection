import React from 'react';
import { ShieldCheck, ShieldAlert, AlertTriangle, Eye, ArrowUpRight, Smartphone, MapPin, Clock } from 'lucide-react';

export default function LiveTransactionStream({ transactions, onSelectTransaction }) {

  const getDecisionBadge = (decision) => {
    switch (decision) {
      case 'BLOCK':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-500/20 text-rose-400 border border-rose-500/30">
            <span className="h-1.5 w-1.5 rounded-full bg-rose-500 animate-pulse"></span>
            BLOCK
          </span>
        );
      case 'REVIEW':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30">
            <span className="h-1.5 w-1.5 rounded-full bg-amber-500"></span>
            REVIEW
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-500"></span>
            ALLOW
          </span>
        );
    }
  };

  const getScoreColor = (score) => {
    const s = (score || 0);
    if (s >= 0.70) return 'text-rose-400';
    if (s >= 0.30) return 'text-amber-400';
    return 'text-emerald-400';
  };

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden backdrop-blur-md">
      
      {/* Table Header */}
      <div className="p-4 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-500"></span>
          </span>
          <h3 className="text-sm font-bold text-white uppercase tracking-wider">
            Real-Time Transaction Pipeline Feed
          </h3>
        </div>
        <span className="text-xs text-slate-400 font-mono">
          Showing {transactions.length} recent events
        </span>
      </div>

      {/* Transactions Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-950/60 text-slate-400 border-b border-slate-800 font-mono">
            <tr>
              <th className="py-3 px-4">TXN ID / TIME</th>
              <th className="py-3 px-4">USER</th>
              <th className="py-3 px-4">AMOUNT</th>
              <th className="py-3 px-4">CONTEXT</th>
              <th className="py-3 px-4">CATEGORY</th>
              <th className="py-3 px-4 text-center">RISK PROBABILITY</th>
              <th className="py-3 px-4 text-center">DECISION</th>
              <th className="py-3 px-4 text-right">EXPLAIN</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {transactions.length === 0 ? (
              <tr>
                <td colSpan="8" className="py-8 text-center text-slate-500">
                  No transactions recorded yet. Use the simulator above to inject transactions.
                </td>
              </tr>
            ) : (
              transactions.map((txn, index) => {
                const riskPct = Math.round((txn.finalRiskScore || 0) * 100);
                return (
                  <tr
                    key={txn.transactionId || index}
                    className="hover:bg-slate-800/40 transition-colors group cursor-pointer"
                    onClick={() => onSelectTransaction(txn)}
                  >
                    {/* TXN ID */}
                    <td className="py-3 px-4 font-mono">
                      <div className="font-semibold text-slate-200 group-hover:text-blue-400 transition-colors">
                        {txn.transactionId}
                      </div>
                      <div className="text-[10px] text-slate-500">
                        {txn.timestamp ? new Date(txn.timestamp).toLocaleTimeString() : 'Just now'}
                      </div>
                    </td>

                    {/* User */}
                    <td className="py-3 px-4 font-mono font-medium text-slate-300">
                      {txn.userId}
                    </td>

                    {/* Amount */}
                    <td className="py-3 px-4 font-mono font-bold text-white">
                      ₹{Number(txn.amount).toLocaleString()}
                    </td>

                    {/* Context / Flags */}
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-1.5 text-slate-300">
                        <MapPin className="w-3 h-3 text-slate-500" />
                        <span>{txn.location}</span>
                        {txn.isNewLocation && (
                          <span className="text-[9px] px-1 py-0.2 bg-rose-500/20 text-rose-300 rounded font-semibold">NEW</span>
                        )}
                      </div>
                      <div className="flex items-center gap-1.5 text-slate-400 text-[10px] mt-0.5">
                        <Smartphone className="w-3 h-3 text-slate-500" />
                        <span>{txn.deviceId}</span>
                        {txn.isNewDevice && (
                          <span className="text-[9px] px-1 py-0.2 bg-rose-500/20 text-rose-300 rounded font-semibold">NEW</span>
                        )}
                      </div>
                    </td>

                    {/* Category */}
                    <td className="py-3 px-4 text-slate-300">
                      <span className="px-2 py-0.5 bg-slate-800 rounded text-[11px] text-slate-300">
                        {txn.merchantCategory}
                      </span>
                    </td>

                    {/* Risk Score */}
                    <td className="py-3 px-4 text-center">
                      <div className={`font-mono font-bold text-sm ${getScoreColor(txn.finalRiskScore)}`}>
                        {riskPct}%
                      </div>
                      <div className="w-16 bg-slate-800 h-1 rounded-full mx-auto mt-1 overflow-hidden">
                        <div
                          className={`h-full ${riskPct >= 70 ? 'bg-rose-500' : riskPct >= 30 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                          style={{ width: `${riskPct}%` }}
                        />
                      </div>
                    </td>

                    {/* Decision */}
                    <td className="py-3 px-4 text-center">
                      {getDecisionBadge(txn.decision)}
                    </td>

                    {/* Action */}
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onSelectTransaction(txn);
                        }}
                        className="p-1.5 bg-slate-800/80 hover:bg-blue-600/30 text-slate-400 hover:text-blue-300 rounded-lg transition-colors cursor-pointer inline-flex items-center gap-1 text-[11px]"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span className="hidden sm:inline">Why?</span>
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

    </div>
  );
}
