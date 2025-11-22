import React, { useState } from 'react';
import { Gamepad2, Coins, Sparkles, LogIn, Crown } from 'lucide-react';
import BitcoinGame from './BitcoinGame.jsx';
import jackpotBackground from '../fig/jackpot_background.png';

const CashRain = () => {
  const bills = Array.from({ length: 16 });
  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      {bills.map((_, idx) => (
        <span
          key={idx}
          className="absolute text-3xl text-amber-200/80 animate-cash-fall select-none"
          style={{
            left: `${(idx * 7) % 100}%`,
            animationDelay: `${idx * 0.2}s`,
          }}
        >
          💵
        </span>
      ))}
    </div>
  );
};

const HomeCard = ({ onPlay, onLogin, isLoading }) => (
  <div
    className="min-h-screen flex items-center justify-center bg-black text-white px-4 relative overflow-hidden"
    style={{
      backgroundImage: `linear-gradient(135deg, rgba(0,0,0,0.9), rgba(14,14,14,0.6)), url(${jackpotBackground})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }}
  >
    <div className="absolute inset-0 bg-gradient-to-b from-black/70 via-black/40 to-black/85" />
    <CashRain />
    <div className="relative w-full max-w-3xl rounded-3xl border border-amber-400/30 bg-black/60 backdrop-blur p-10 shadow-[0_10px_60px_rgba(0,0,0,0.45)]">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/15 text-amber-200 text-xs font-semibold tracking-[0.2em] uppercase">
            <Sparkles className="w-4 h-4" /> High Roller Mode
          </div>
          <h1 className="text-4xl md:text-5xl font-black tracking-tight mt-3">
            Spin Up Your BTC Fortune
          </h1>
          <p className="text-white/70 mt-3 max-w-xl">
            20秒内预测涨跌，赢取筹码。实时对接后端行情，下注、开局、见证结果。
          </p>
          <div className="flex flex-wrap gap-3 mt-6">
            <button
              className="inline-flex items-center gap-2 px-5 py-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-black font-bold shadow-[0_10px_30px_rgba(234,179,8,0.35)] transition"
              onClick={onPlay}
            >
              <Gamepad2 className="w-5 h-5" />
              Enter Game
            </button>
            <button
              onClick={onLogin}
              disabled={isLoading}
              className="inline-flex items-center gap-2 px-5 py-3 rounded-xl bg-white text-gray-900 font-semibold shadow-[0_10px_25px_rgba(255,255,255,0.25)] hover:shadow-[0_10px_35px_rgba(255,255,255,0.35)] transition disabled:opacity-70"
            >
              <LogIn className="w-5 h-5" />
              {isLoading ? 'Signing in...' : 'Sign in with Google'}
            </button>
            <div className="inline-flex items-center gap-2 px-4 py-3 rounded-xl border border-white/10 bg-white/5 text-white/80">
              <Crown className="w-4 h-4 text-amber-300" />
              Live BTC Feed
            </div>
          </div>
        </div>
        <div className="w-full md:w-[320px] bg-gradient-to-br from-amber-500/20 to-emerald-500/15 border border-white/10 rounded-2xl p-5 shadow-inner relative overflow-hidden">
          <div className="absolute -left-8 -top-8 w-32 h-32 bg-amber-500/20 blur-3xl" />
          <div className="absolute -right-4 bottom-0 w-24 h-24 bg-emerald-500/20 blur-3xl" />
          <div className="relative flex items-center gap-3">
            <div className="p-3 rounded-xl bg-black/50 border border-amber-400/30">
              <Coins className="w-6 h-6 text-amber-300" />
            </div>
            <div>
              <p className="text-sm text-white/60">Prize Pool</p>
              <p className="text-2xl font-black text-amber-300">₿ 0.125</p>
            </div>
          </div>
          <div className="mt-6 space-y-3 text-sm">
            <div className="flex items-center justify-between">
              <span className="text-white/60">Win Rate (last 10)</span>
              <span className="text-emerald-300 font-bold">72%</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-white/60">Fastest Round</span>
              <span className="text-amber-300 font-bold">20s</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-white/60">Top Streak</span>
              <span className="text-emerald-300 font-bold">x5</span>
            </div>
          </div>
          <div className="mt-6 text-xs text-white/50">
            实时行情来自后端 `/api/price`，下注前请确认网络通畅。
          </div>
        </div>
      </div>
    </div>
  </div>
);

const App = () => {
  const [view, setView] = useState('home'); // 'home' | 'game'
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = () => {
    if (isLoading) return;
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      setView('game');
    }, 1200);
  };

  if (view === 'game') {
    return <BitcoinGame onBack={() => setView('home')} />;
  }
  return <HomeCard onPlay={() => setView('game')} onLogin={handleLogin} isLoading={isLoading} />;
};

export default App;
