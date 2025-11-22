import React, { useState } from 'react';
import { Gamepad2, LayoutDashboard } from 'lucide-react';
import BitcoinGame from './BitcoinGame.jsx';
import jackpotBackground from '../fig/jackpot_background.png';

const BACKEND_DASHBOARD = 'https://btc-bet-backend-1012075997843.us-central1.run.app/dashboard';

const HomeCard = ({ onPlay }) => (
  <div
    className="min-h-screen flex items-center justify-center bg-black text-white px-4"
    style={{
      backgroundImage: `url(${jackpotBackground})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundAttachment: 'fixed'
    }}
  >
    <div className="absolute inset-0 bg-black/70" aria-hidden="true" />
    <div className="relative w-full max-w-lg rounded-2xl border border-white/10 bg-white/10 backdrop-blur p-8 shadow-2xl">
      <div className="flex items-center gap-3 mb-2">
        <Gamepad2 className="w-6 h-6 text-emerald-300" />
        <p className="text-sm uppercase tracking-[0.3em] text-emerald-200">BTC Bet</p>
      </div>
      <h1 className="text-3xl font-bold leading-tight">Jackpot Lounge</h1>
      <p className="text-white/80 mt-2">
        20s prediction game with live BTC feed. Pick rise or fall, place your bet, and see if you win.
      </p>
      <div className="mt-8 space-y-3">
        <button
          className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-white font-semibold transition transform hover:translate-y-[-1px]"
          onClick={onPlay}
        >
          <Gamepad2 className="w-5 h-5" />
          Play Prediction Game
        </button>
        <a
          className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl border border-white/20 bg-white/10 hover:bg-white/15 text-white font-semibold transition"
          href={BACKEND_DASHBOARD}
          target="_blank"
          rel="noreferrer"
        >
          <LayoutDashboard className="w-5 h-5" />
          Ops Dashboard
        </a>
      </div>
    </div>
  </div>
);

const App = () => {
  const [view, setView] = useState('home'); // 'home' | 'game'

  if (view === 'game') {
    return <BitcoinGame onBack={() => setView('home')} />;
  }
  return <HomeCard onPlay={() => setView('game')} />;
};

export default App;
