import React, { useState } from 'react';
import { Zap, Play, AlertOctagon, Plane, Bot, ShieldCheck, Send } from 'lucide-react';

export default function AttackSimulator({ onTransactionCreated }) {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('presets'); // 'presets' or 'custom'

  // Custom Form State
  const [formData, setFormData] = useState({
    userId: 'U102',
    amount: '85000',
    location: 'Mumbai',
    deviceId: 'DEV_UNKNOWN_99',
    merchantCategory: 'Electronics',
    timestamp: '02:30'
  });

  const handleSimulateEndpoint = async (endpoint) => {
    setLoading(true);
    try {
      const res = await fetch(`/api/simulate/${endpoint}`, { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        if (onTransactionCreated) {
          onTransactionCreated(Array.isArray(data) ? data[data.length - 1] : data);
        }
      }
    } catch (err) {
      console.error("Simulation failed:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCustomSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {
        userId: formData.userId,
        amount: parseFloat(formData.amount),
        location: formData.location,
        deviceId: formData.deviceId,
        merchantCategory: formData.merchantCategory,
        timestamp: formData.timestamp
      };

      const res = await fetch('/api/transactions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        const result = await res.json();
        if (onTransactionCreated) {
          onTransactionCreated(result);
        }
      }
    } catch (err) {
      console.error("Custom transaction submission failed:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 backdrop-blur-md">
      <div className="flex items-center justify-between pb-4 border-b border-slate-800/80 mb-4">
        <div className="flex items-center space-x-2">
          <div className="p-1.5 bg-amber-500/20 text-amber-400 rounded-lg">
            <Zap className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">
              Live Attack & Traffic Simulator
            </h3>
            <p className="text-xs text-slate-400">
              Inject real-world attack vectors to test Kafka stream & XGBoost inference live
            </p>
          </div>
        </div>

        {/* Tab switcher */}
        <div className="flex bg-slate-800/80 p-0.5 rounded-lg text-xs font-medium">
          <button
            onClick={() => setActiveTab('presets')}
            className={`px-3 py-1 rounded-md transition-all cursor-pointer ${activeTab === 'presets' ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-400 hover:text-white'}`}
          >
            Attack Presets
          </button>
          <button
            onClick={() => setActiveTab('custom')}
            className={`px-3 py-1 rounded-md transition-all cursor-pointer ${activeTab === 'custom' ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-400 hover:text-white'}`}
          >
            Custom Builder
          </button>
        </div>
      </div>

      {activeTab === 'presets' ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          
          {/* Preset 1: Normal */}
          <button
            disabled={loading}
            onClick={() => handleSimulateEndpoint('normal')}
            className="p-3.5 bg-slate-800/40 hover:bg-slate-800 border border-slate-700/60 hover:border-emerald-500/40 rounded-xl text-left transition-all group cursor-pointer disabled:opacity-50"
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-emerald-400 flex items-center gap-1.5">
                <ShieldCheck className="w-3.5 h-3.5" /> Normal Payment
              </span>
              <Play className="w-3 h-3 text-slate-500 group-hover:text-emerald-400 transition-colors" />
            </div>
            <div className="text-base font-bold text-white font-mono">₹450</div>
            <div className="text-[11px] text-slate-400 mt-1">
              Hyderabad • Grocery • Usual Device
            </div>
            <div className="mt-2 text-[10px] text-emerald-400/80 font-mono">Expected: ALLOW (2%)</div>
          </button>

          {/* Preset 2: Midnight High-Value */}
          <button
            disabled={loading}
            onClick={() => handleSimulateEndpoint('midnight-attack')}
            className="p-3.5 bg-slate-800/40 hover:bg-slate-800 border border-slate-700/60 hover:border-rose-500/40 rounded-xl text-left transition-all group cursor-pointer disabled:opacity-50"
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-rose-400 flex items-center gap-1.5">
                <AlertOctagon className="w-3.5 h-3.5" /> Midnight Transfer
              </span>
              <Play className="w-3 h-3 text-slate-500 group-hover:text-rose-400 transition-colors" />
            </div>
            <div className="text-base font-bold text-white font-mono">₹95,000</div>
            <div className="text-[11px] text-slate-400 mt-1">
              Mumbai • Electronics • 02:15 AM
            </div>
            <div className="mt-2 text-[10px] text-rose-400/80 font-mono">Expected: BLOCK (94%)</div>
          </button>

          {/* Preset 3: Impossible Travel */}
          <button
            disabled={loading}
            onClick={() => handleSimulateEndpoint('impossible-travel')}
            className="p-3.5 bg-slate-800/40 hover:bg-slate-800 border border-slate-700/60 hover:border-amber-500/40 rounded-xl text-left transition-all group cursor-pointer disabled:opacity-50"
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-amber-400 flex items-center gap-1.5">
                <Plane className="w-3.5 h-3.5" /> Impossible Travel
              </span>
              <Play className="w-3 h-3 text-slate-500 group-hover:text-amber-400 transition-colors" />
            </div>
            <div className="text-base font-bold text-white font-mono">₹88,000</div>
            <div className="text-[11px] text-slate-400 mt-1">
              Delhi → London in 15 mins
            </div>
            <div className="mt-2 text-[10px] text-amber-400/80 font-mono">Expected: BLOCK (96%)</div>
          </button>

          {/* Preset 4: Card Testing Burst */}
          <button
            disabled={loading}
            onClick={() => handleSimulateEndpoint('card-testing-burst')}
            className="p-3.5 bg-slate-800/40 hover:bg-slate-800 border border-slate-700/60 hover:border-cyan-500/40 rounded-xl text-left transition-all group cursor-pointer disabled:opacity-50"
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-cyan-400 flex items-center gap-1.5">
                <Bot className="w-3.5 h-3.5" /> Botnet Velocity
              </span>
              <Play className="w-3 h-3 text-slate-500 group-hover:text-cyan-400 transition-colors" />
            </div>
            <div className="text-base font-bold text-white font-mono">5x Rapid Swipes</div>
            <div className="text-[11px] text-slate-400 mt-1">
              Burst velocity &lt; 30s • Crypto
            </div>
            <div className="mt-2 text-[10px] text-cyan-400/80 font-mono">Expected: BLOCK (91%)</div>
          </button>

        </div>
      ) : (
        <form onSubmit={handleCustomSubmit} className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 text-xs">
          <div>
            <label className="text-slate-400 block mb-1">User ID</label>
            <input
              type="text"
              value={formData.userId}
              onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
              className="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white font-mono focus:border-blue-500 outline-none"
              placeholder="U102"
              required
            />
          </div>

          <div>
            <label className="text-slate-400 block mb-1">Amount (₹)</label>
            <input
              type="number"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              className="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white font-mono focus:border-blue-500 outline-none"
              placeholder="85000"
              required
            />
          </div>

          <div>
            <label className="text-slate-400 block mb-1">Location</label>
            <input
              type="text"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              className="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white focus:border-blue-500 outline-none"
              placeholder="Mumbai"
              required
            />
          </div>

          <div>
            <label className="text-slate-400 block mb-1">Device ID</label>
            <input
              type="text"
              value={formData.deviceId}
              onChange={(e) => setFormData({ ...formData, deviceId: e.target.value })}
              className="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white font-mono focus:border-blue-500 outline-none"
              placeholder="DEV_XYZ"
              required
            />
          </div>

          <div>
            <label className="text-slate-400 block mb-1">Category</label>
            <select
              value={formData.merchantCategory}
              onChange={(e) => setFormData({ ...formData, merchantCategory: e.target.value })}
              className="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-2.5 py-1.5 text-white focus:border-blue-500 outline-none"
            >
              <option value="Grocery">Grocery</option>
              <option value="Food">Food</option>
              <option value="Utility">Utility</option>
              <option value="Electronics">Electronics</option>
              <option value="Travel">Travel</option>
              <option value="Jewelry">Jewelry</option>
              <option value="Crypto">Crypto</option>
            </select>
          </div>

          <div className="flex items-end">
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-1.5 px-3 rounded-lg flex items-center justify-center gap-1.5 transition-all cursor-pointer disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Evaluate</span>
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
