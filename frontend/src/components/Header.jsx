import React from 'react';
import { ShieldCheck, Activity, Cpu, Database, Radio, RefreshCw } from 'lucide-react';

export default function Header({ isConnected, onRefresh, isRefreshing }) {
  return (
    <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur-md sticky top-0 z-40 px-6 py-4">
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        
        {/* Brand */}
        <div className="flex items-center space-x-3">
          <div className="p-2.5 bg-gradient-to-tr from-blue-600 to-indigo-600 rounded-xl shadow-lg shadow-blue-500/20 border border-blue-400/30">
            <ShieldCheck className="w-6 h-6 text-white" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
                FraudShield <span className="text-xs px-2 py-0.5 rounded-full font-mono font-medium bg-blue-500/20 text-blue-400 border border-blue-500/30">ENTERPRISE v2.1</span>
              </h1>
            </div>
            <p className="text-xs text-slate-400">
              Real-Time Financial Transaction Analysis & Autonomous Risk Scoring Engine
            </p>
          </div>
        </div>

        {/* Telemetry Chips & Controls */}
        <div className="flex items-center flex-wrap gap-2 text-xs">
          {/* Stream Status */}
          <div className="flex items-center space-x-2 bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700/60">
            <span className="relative flex h-2 w-2">
              <span className={`animate-ping absolute inline-flex h-full w-full rounded-full ${isConnected ? 'bg-emerald-400 opacity-75' : 'bg-amber-400 opacity-75'}`}></span>
              <span className={`relative inline-flex rounded-full h-2 w-2 ${isConnected ? 'bg-emerald-500' : 'bg-amber-500'}`}></span>
            </span>
            <span className="text-slate-300 font-mono">Stream: {isConnected ? 'Active (Kafka)' : 'Polling (REST)'}</span>
          </div>

          {/* Model Status */}
          <div className="hidden sm:flex items-center space-x-2 bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700/60 text-slate-300 font-mono">
            <Cpu className="w-3.5 h-3.5 text-blue-400" />
            <span>XGBoost + SHAP</span>
          </div>

          {/* State Store */}
          <div className="hidden md:flex items-center space-x-2 bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700/60 text-slate-300 font-mono">
            <Database className="w-3.5 h-3.5 text-indigo-400" />
            <span>Redis State & Postgres</span>
          </div>

          {/* Refresh Action */}
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            className="flex items-center space-x-1.5 bg-blue-600/20 hover:bg-blue-600/30 text-blue-400 px-3 py-1.5 rounded-lg border border-blue-500/30 transition-all cursor-pointer disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin' : ''}`} />
            <span>Sync</span>
          </button>
        </div>

      </div>
    </header>
  );
}
