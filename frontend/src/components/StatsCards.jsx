import React from 'react';
import { Activity, ShieldAlert, ShieldCheck, Clock, IndianRupee, AlertCircle } from 'lucide-react';

export default function StatsCards({ stats }) {
  const cards = [
    {
      title: 'Total Transactions',
      value: (stats.totalTransactions || 0).toLocaleString(),
      subtext: `${stats.approvedCount || 0} approved, ${stats.reviewCount || 0} flagged`,
      icon: Activity,
      color: 'text-blue-400',
      bg: 'bg-blue-500/10 border-blue-500/20'
    },
    {
      title: 'Fraud Cases Blocked',
      value: (stats.blockedCount || 0).toLocaleString(),
      subtext: `Total blocked: ₹${(stats.blockedAmount || 0).toLocaleString()}`,
      icon: ShieldAlert,
      color: 'text-rose-400',
      bg: 'bg-rose-500/10 border-rose-500/20'
    },
    {
      title: 'Fraud Rate',
      value: `${(stats.fraudRatePercentage || 0).toFixed(2)}%`,
      subtext: 'Target industry standard < 2.0%',
      icon: AlertCircle,
      color: stats.fraudRatePercentage > 5 ? 'text-rose-400' : 'text-amber-400',
      bg: stats.fraudRatePercentage > 5 ? 'bg-rose-500/10 border-rose-500/20' : 'bg-amber-500/10 border-amber-500/20'
    },
    {
      title: 'Processed Volume',
      value: `₹${(stats.totalVolume || 0).toLocaleString()}`,
      subtext: 'Real-time financial flow',
      icon: IndianRupee,
      color: 'text-emerald-400',
      bg: 'bg-emerald-500/10 border-emerald-500/20'
    },
    {
      title: 'Avg Engine Latency',
      value: `${stats.avgLatencyMs || 7.2} ms`,
      subtext: 'Sub-second real-time SLA',
      icon: Clock,
      color: 'text-cyan-400',
      bg: 'bg-cyan-500/10 border-cyan-500/20'
    }
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className={`p-4 rounded-xl border backdrop-blur-md bg-slate-900/40 transition-all hover:translate-y-[-2px] ${card.bg}`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-medium text-slate-400 tracking-wider uppercase">{card.title}</span>
              <div className={`p-1.5 rounded-lg bg-slate-800/80 ${card.color}`}>
                <Icon className="w-4 h-4" />
              </div>
            </div>
            <div className="text-2xl font-bold font-mono tracking-tight text-white mb-1">
              {card.value}
            </div>
            <div className="text-xs text-slate-400 truncate">
              {card.subtext}
            </div>
          </div>
        );
      })}
    </div>
  );
}
