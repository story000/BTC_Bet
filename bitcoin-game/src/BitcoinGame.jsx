import React, { useState, useEffect, useRef } from 'react';
import { ArrowUp, ArrowDown, Wallet, Plus, Minus, RotateCcw, TrendingUp, TrendingDown, Zap, ShieldAlert, Bitcoin, ArrowLeft } from 'lucide-react';
import jackpotBackground from '../fig/jackpot_background.png';

// --- Custom Icons (Refined Axis-Style Candles) ---

const RiseCandles = ({ className }) => (
  <svg viewBox="0 0 40 40" className={className} fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    {/* L-Shape Axis */}
    <polyline points="4,6 4,34 36,34" className="opacity-80" />
    
    {/* 1. Candle (Low) */}
    <line x1="10" y1="22" x2="10" y2="30" strokeWidth="1.5" />
    <rect x="8" y="24" width="4" height="4" fill="currentColor" stroke="none" />
    
    {/* 2. Candle (Medium) */}
    <line x1="18" y1="14" x2="18" y2="26" strokeWidth="1.5" />
    <rect x="16" y="18" width="4" height="6" fill="currentColor" stroke="none" />
    
    {/* 3. Bar (Dip) */}
    <rect x="24" y="26" width="4" height="6" fill="currentColor" stroke="none" />
    
    {/* 4. Bar (Big Rise) */}
    <rect x="30" y="10" width="4" height="22" fill="currentColor" stroke="none" />
  </svg>
);

const FallCandles = ({ className }) => (
  <svg viewBox="0 0 40 40" className={className} fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    {/* L-Shape Axis */}
    <polyline points="4,6 4,34 36,34" className="opacity-80" />
    
    {/* 1. Candle (High) */}
    <line x1="10" y1="10" x2="10" y2="22" strokeWidth="1.5" />
    <rect x="8" y="12" width="4" height="8" fill="currentColor" stroke="none" />
    
    {/* 2. Candle (Lower) */}
    <line x1="18" y1="18" x2="18" y2="28" strokeWidth="1.5" />
    <rect x="16" y="20" width="4" height="6" fill="currentColor" stroke="none" />
    
    {/* 3. Bar (Small Rise) */}
    <rect x="24" y="24" width="4" height="8" fill="currentColor" stroke="none" />
    
    {/* 4. Bar (Big Drop / Low) */}
    <rect x="30" y="18" width="4" height="14" fill="currentColor" stroke="none" />
  </svg>
);

const BitcoinGame = ({ onBack }) => {
  // --- Game State ---
  const [balance, setBalance] = useState(12345);
  const [isAndroid, setIsAndroid] = useState(false);
  const [betAmount, setBetAmount] = useState(1000);
  const [prediction, setPrediction] = useState(null); // 'RISE' | 'FALL'
  const [gameState, setGameState] = useState('IDLE'); // 'IDLE' | 'PLAYING' | 'RESULT'
  const [timeLeft, setTimeLeft] = useState(10);
  const [result, setResult] = useState(null); // { win: boolean, amount: number, finalPrice: number }
  
  // --- Price Simulation State ---
  const [currentPrice, setCurrentPrice] = useState(64230.50);
  const [startPrice, setStartPrice] = useState(null); 
  const [priceHistory, setPriceHistory] = useState([]);
  
  // --- Refs ---
  const timerRef = useRef(null);
  const priceUpdateRef = useRef(null);
  const currentPriceRef = useRef(64230.50); 
  const gameSessionRef = useRef({ startPrice: 0, prediction: null, betAmount: 0 });

  // --- Constants ---
  const GAME_DURATION = 20;
  const MAX_HISTORY = 100; // Increased for 20 second game 
  const CHART_SENSITIVITY = 1000; // 1% price move => 1000 SVG units before clamping
  const hasActiveSession = startPrice !== null;
  const isIdle = gameState === 'IDLE';
  const priceDelta = hasActiveSession ? currentPrice - startPrice : 0;
  const priceDeltaPercent = hasActiveSession && startPrice !== 0 ? (priceDelta / startPrice) * 100 : 0;
  const priceDeltaLabel = hasActiveSession
    ? `${priceDelta >= 0 ? '+' : ''}${priceDelta.toFixed(2)} (${priceDeltaPercent.toFixed(2)}%)`
    : '';
  const priceDeltaChipClasses = hasActiveSession
    ? priceDelta >= 0
      ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-200'
      : 'bg-rose-500/15 border-rose-500/40 text-rose-200'
    : 'bg-white/5 border-white/10 text-white/60';

  // --- Android Bridge Setup ---
  useEffect(() => {
    // Detect if running in Android WebView
    const isAndroidWebView = typeof window.AndroidBridge !== 'undefined';
    setIsAndroid(isAndroidWebView);

    // Expose functions to Android
    window.updateBalance = (newBalance) => {
      console.log('Balance updated from Android:', newBalance);
      setBalance(newBalance);
    };

    window.updatePrice = (newPrice) => {
      console.log('Price updated from Android:', newPrice);
      currentPriceRef.current = newPrice;
      setCurrentPrice(newPrice);
    };

    // If in Android, log that we're waiting for real prices
    if (isAndroidWebView) {
      console.log('Android Bridge initialized - waiting for real prices from server');
      // Notify Android that we're ready
      if (window.AndroidBridge && window.AndroidBridge.log) {
        window.AndroidBridge.log('React game loaded and ready');
      }
    } else {
      console.log('Browser mode - will use simulated prices');
    }
  }, []);

  // --- Price Simulation Engine ---
  useEffect(() => {
    // Only run simulation if NOT in Android WebView
    // Android will push real prices via window.updatePrice
    if (isAndroid) {
      console.log('Running in Android WebView - using real prices from server');
      return;
    }

    console.log('Running in browser - using simulated prices');
    priceUpdateRef.current = setInterval(() => {
      const change = (Math.random() - 0.5) * 150;
      const newPrice = currentPriceRef.current + change;
      currentPriceRef.current = newPrice;
      setCurrentPrice(newPrice);
    }, 100);

    return () => clearInterval(priceUpdateRef.current);
  }, [isAndroid]);

  // --- History Management ---
  useEffect(() => {
    // Only add to history when game is playing
    if (gameState === 'PLAYING') {
      setPriceHistory(prev => {
        const newHistory = [...prev, currentPrice];
        if (newHistory.length > MAX_HISTORY) return newHistory.slice(1);
        return newHistory;
      });
    }
  }, [currentPrice, gameState]);

  // --- Core Game Logic ---
  const handleStartGame = () => {
    if (!prediction) return;
    if (betAmount > balance) return;

    const entryPrice = currentPriceRef.current;
    gameSessionRef.current = {
      startPrice: entryPrice,
      prediction: prediction,
      betAmount: betAmount
    };

    setStartPrice(entryPrice);
    // Clear price history and start fresh with the entry price
    setPriceHistory([entryPrice]);
    setGameState('PLAYING');
    setTimeLeft(GAME_DURATION);
    setResult(null);

    if (timerRef.current) clearInterval(timerRef.current);

    timerRef.current = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          finishGame();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const finishGame = () => {
    clearInterval(timerRef.current);
    const session = gameSessionRef.current;
    const finalPrice = currentPriceRef.current;

    const didRise = finalPrice > session.startPrice;
    const didFall = finalPrice < session.startPrice;
    let isWin = false;

    if (session.prediction === 'RISE' && didRise) isWin = true;
    if (session.prediction === 'FALL' && didFall) isWin = true;

    const newBalance = isWin ? balance + session.betAmount : balance - session.betAmount;
    setBalance(newBalance);

    const gameResult = {
      win: isWin,
      amount: session.betAmount,
      finalPrice: finalPrice,
      startPrice: session.startPrice
    };

    setResult(gameResult);
    setGameState('RESULT');

    // Notify Android
    if (window.AndroidBridge) {
      window.AndroidBridge.onGameFinished(
        isWin,
        session.betAmount,
        newBalance,
        finalPrice,
        session.startPrice
      );
    }
  };

  const resetGame = () => {
    setGameState('IDLE');
    setTimeLeft(GAME_DURATION);
    setResult(null);
    setStartPrice(null);
    setPriceHistory([]); // Clear price history for next game
  };

  const adjustBet = (amount) => {
    setBetAmount(prev => {
      const next = prev + amount;
      return Math.max(100, Math.min(next, balance));
    });
  };

  // --- Visual Helpers ---
  const getPolylinePoints = () => {
    if (priceHistory.length < 2 || !startPrice) return "";

    return priceHistory.map((price, index) => {
      const x = (index / (priceHistory.length - 1)) * 300;

      // Calculate Y based on percentage change from start price
      // startPrice = center line (Y = 50)
      // +1% change = CHART_SENSITIVITY units upward (clamped later)
      // -1% change = CHART_SENSITIVITY units downward
      const percentChange = ((price - startPrice) / startPrice) * 100;
      const yOffset = percentChange * CHART_SENSITIVITY;
      const y = 50 - yOffset; // Invert because SVG Y increases downward

      // Clamp Y to visible range [10, 90]
      const clampedY = Math.max(10, Math.min(90, y));

      return `${x},${clampedY}`;
    }).join(" ");
  };

  const isWinning = () => {
    if (!startPrice) return true; 
    if (prediction === 'RISE') return currentPrice >= startPrice;
    if (prediction === 'FALL') return currentPrice < startPrice;
    return false;
  };

  return (
    <div
      className="min-h-screen text-white font-sans flex items-center justify-center p-0 sm:p-4 overflow-hidden relative"
      style={{
        backgroundImage: `url(${jackpotBackground})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundAttachment: 'fixed'
      }}
    >
      {onBack && (
        <button
          className="absolute top-4 left-4 z-20 flex items-center gap-2 px-3 py-2 rounded-xl bg-black/60 border border-white/10 text-white hover:bg-black/70 transition"
          onClick={onBack}
        >
          <ArrowLeft className="w-4 h-4" />
          Home
        </button>
      )}
      
      {/* --- Background Atmosphere --- */}
      <div className={`absolute inset-0 transition-colors duration-700 pointer-events-none
        ${prediction === 'RISE' ? 'bg-emerald-900/15' : ''}
        ${prediction === 'FALL' ? 'bg-rose-900/15' : ''}
        ${!prediction ? 'bg-black/70' : ''}
      `} />
      
      <div className={`absolute top-[-20%] left-1/2 -translate-x-1/2 w-[600px] h-[600px] rounded-full blur-[150px] transition-all duration-1000 opacity-40
         ${prediction === 'RISE' ? 'bg-emerald-500' : prediction === 'FALL' ? 'bg-rose-600' : 'bg-indigo-900'}
      `} />

      {/* Main Game Interface */}
      <div className="relative z-10 w-full max-w-[390px] h-[844px] max-h-screen bg-black/70 backdrop-blur-xl sm:rounded-[40px] shadow-2xl border border-white/10 flex flex-col overflow-hidden">
        
        {/* Header */}
        <div className="pt-10 pb-4 flex flex-col items-center z-20 bg-gradient-to-b from-[#0c0e14] to-transparent">
          <div className="flex items-center gap-2 text-amber-300 text-4xl font-black tracking-tighter drop-shadow-[0_0_15px_rgba(251,191,36,0.4)]">
            <Wallet className="w-7 h-7 fill-current" />
            <span className="tabular-nums">{Math.floor(balance).toLocaleString()}</span>
          </div>
          <span className="text-amber-300/50 text-[10px] font-bold tracking-[0.3em] uppercase mt-1">Current Points</span>
        </div>

        {/* Middle: Betting & Graph */}
        <div className="flex-1 flex flex-col px-6 py-2 relative">

          {/* Info & Instruction Cards */}
          {isIdle ? (
            <div className="space-y-3 mb-6">
              <div className="bg-white/5 border border-white/10 rounded-2xl p-4 shadow-inner backdrop-blur-md">
                <div className="text-[10px] font-bold uppercase tracking-[0.3em] text-white/40">Live BTC Feed</div>
                <div className="flex items-end justify-between mt-3">
                <div>
                  <div className="flex items-center gap-2 text-3xl font-mono font-black tracking-tight text-white">
                    <Bitcoin className="w-7 h-7 text-amber-300" />
                    <span>${currentPrice.toFixed(2)}</span>
                  </div>
                </div>
                {hasActiveSession && (
                  <div className={`px-3 py-1 rounded-full border text-xs font-black uppercase tracking-[0.2em] ${priceDeltaChipClasses}`}>
                    {priceDeltaLabel}
                  </div>
                )}
              </div>
            </div>

            <div className="bg-gradient-to-r from-emerald-600/30 via-yellow-400/10 to-orange-500/20 border border-emerald-400/30 rounded-2xl p-4 shadow-lg">
              <div className="flex items-center gap-2 text-emerald-300 text-sm font-black uppercase tracking-[0.3em]">
                <Zap size={16} /> 20s Sudden Death
              </div>
              <div className="flex flex-wrap gap-2 mt-3 text-[11px] font-semibold uppercase tracking-[0.2em] text-white/80">
                  <span className="px-3 py-1 rounded-full bg-amber-400/10 border border-amber-300/40 text-amber-200">1. Dial your bet</span>
                  <span className="px-3 py-1 rounded-full bg-amber-400/10 border border-amber-300/40 text-amber-200">2. Choose RISE / FALL</span>
                  <span className="px-3 py-1 rounded-full bg-amber-400/10 border border-amber-300/40 text-amber-200">3. Smash START</span>
                </div>
              </div>
            </div>
          ) : (
            <div className="mb-6">
              <div className="bg-black/40 border border-white/10 rounded-2xl p-3 flex items-center justify-between backdrop-blur-md">
                <div>
                  <div className="text-[10px] uppercase tracking-[0.4em] text-white/40">Live BTC</div>
                  <div className="flex items-center gap-2 text-xl font-mono font-black text-white">
                    <Bitcoin className="w-5 h-5 text-amber-300" />
                    <span>${currentPrice.toFixed(2)}</span>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-[10px] uppercase tracking-[0.4em] text-white/40">Entry</div>
                  <div className="text-lg font-mono text-white/80">{startPrice?.toFixed(2) ?? '--'}</div>
                </div>
                {hasActiveSession && (
                  <div className={`px-3 py-1 rounded-full border text-[11px] font-black uppercase tracking-[0.2em] ${priceDeltaChipClasses}`}>
                    {priceDeltaLabel}
                  </div>
                )}
              </div>
              <div className="text-center text-[10px] uppercase tracking-[0.3em] text-white/50 mt-3">
                Bets locked. 20s sudden death in progress.
              </div>
            </div>
          )}

          {/* 1. Bet Control (Idle Only) */}
          {isIdle && (
            <>
              <div className="flex items-center justify-between text-[10px] uppercase tracking-[0.3em] text-white/40 px-2 mb-2">
                <span>Set Your Wager</span>
                <span>Min 100 · Max Balance</span>
              </div>
              <div className="bg-black/40 rounded-full p-1 border border-white/10 flex items-center justify-between mb-3 shadow-inner relative z-30 backdrop-blur-sm">
                <button 
                  onClick={() => adjustBet(-100)}
                  disabled={gameState !== 'IDLE'}
                  className="w-10 h-10 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/70 disabled:opacity-30 transition-all active:scale-90"
                >
                  <Minus size={18} />
                </button>
                <div className="flex flex-col items-center">
                  <span className="text-[10px] uppercase tracking-[0.4em] text-white/40">Wager</span>
                  <div className="text-2xl font-black font-mono tracking-widest text-white">{betAmount}</div>
                  <span className="text-[10px] text-white/50">points</span>
                </div>
                <div className="flex gap-1">
                   <button 
                    onClick={() => adjustBet(100)}
                    disabled={gameState !== 'IDLE'}
                    className="w-10 h-10 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center text-white/70 disabled:opacity-30 transition-all active:scale-90"
                  >
                    <Plus size={18} />
                  </button>
                  <button 
                    onClick={() => setBetAmount(balance)}
                    disabled={gameState !== 'IDLE'}
                    className="px-4 h-10 rounded-full bg-white/10 hover:bg-white/20 text-xs font-bold text-white/90 transition-all border border-white/5"
                  >
                    MAX
                  </button>
                </div>
              </div>
              <div className="text-center text-[11px] uppercase tracking-[0.2em] text-white/50 mb-6">
                Tap START to lock entry after picking a direction
              </div>
            </>
          )}

          {/* 2. The BIG Buttons */}
          {gameState === 'IDLE' && (
            <div className="flex items-center justify-center gap-6 my-auto animate-in fade-in zoom-in duration-500">
              {/* RISE Button */}
              <button
                onClick={() => setPrediction('RISE')}
                className={`group relative w-36 h-36 rounded-full border-[6px] flex flex-col items-center justify-center transition-all duration-300 active:scale-95
                  ${prediction === 'RISE' 
                    ? 'border-emerald-400 bg-emerald-500/10 shadow-[0_0_50px_rgba(52,211,153,0.6)] scale-110 z-10' 
                    : 'border-emerald-500/30 bg-black/20 hover:border-emerald-500/60 grayscale hover:grayscale-0'
                  }
                  ${prediction === 'FALL' ? 'opacity-40 scale-90 blur-sm' : 'opacity-100'}
                `}
              >
                <ArrowUp className={`w-10 h-10 transition-transform group-hover:-translate-y-1 ${prediction === 'RISE' ? 'text-emerald-400' : 'text-emerald-500/50'}`} strokeWidth={4} />
                
                {/* New Axis-Style Candle Chart */}
                <RiseCandles className={`w-12 h-12 my-1 transition-colors ${prediction === 'RISE' ? 'text-emerald-400' : 'text-emerald-500/40'}`} />
                
                <span className={`text-lg font-black tracking-widest ${prediction === 'RISE' ? 'text-emerald-400 drop-shadow-[0_0_5px_rgba(52,211,153,1)]' : 'text-emerald-500/50'}`}>RISE</span>
              </button>

              {/* FALL Button */}
              <button
                onClick={() => setPrediction('FALL')}
                className={`group relative w-36 h-36 rounded-full border-[6px] flex flex-col items-center justify-center transition-all duration-300 active:scale-95
                  ${prediction === 'FALL' 
                    ? 'border-rose-500 bg-rose-500/10 shadow-[0_0_50px_rgba(244,63,94,0.6)] scale-110 z-10' 
                    : 'border-rose-500/30 bg-black/20 hover:border-rose-500/60 grayscale hover:grayscale-0'
                  }
                  ${prediction === 'RISE' ? 'opacity-40 scale-90 blur-sm' : 'opacity-100'}
                `}
              >
                <ArrowDown className={`w-10 h-10 transition-transform group-hover:translate-y-1 ${prediction === 'FALL' ? 'text-rose-500' : 'text-rose-500/50'}`} strokeWidth={4} />
                
                {/* New Axis-Style Candle Chart */}
                <FallCandles className={`w-12 h-12 my-1 transition-colors ${prediction === 'FALL' ? 'text-rose-500' : 'text-rose-500/40'}`} />

                <span className={`text-lg font-black tracking-widest ${prediction === 'FALL' ? 'text-rose-500 drop-shadow-[0_0_5px_rgba(244,63,94,1)]' : 'text-rose-500/50'}`}>FALL</span>
              </button>
            </div>
          )}

          {/* 3. Game Graph */}
          {(gameState === 'PLAYING' || gameState === 'RESULT') && (
             <div className="absolute inset-0 top-20 flex items-center justify-center z-0">
                <div className={`absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-[120px] font-black font-mono leading-none select-none tracking-tighter opacity-20 scale-150 pointer-events-none
                    ${timeLeft <= 3 ? 'text-red-500 animate-ping' : 'text-white'}
                `}>
                    {timeLeft}
                </div>

                <svg className="w-full h-64 overflow-visible drop-shadow-[0_0_10px_rgba(255,255,255,0.3)]" viewBox="0 0 300 100" preserveAspectRatio="none">
                  <defs>
                     <linearGradient id="lineGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={isWinning() ? "#34d399" : "#f43f5e"} stopOpacity="1" />
                        <stop offset="100%" stopColor={isWinning() ? "#065f46" : "#881337"} stopOpacity="0.1" />
                     </linearGradient>
                  </defs>
                  <polyline 
                    points={getPolylinePoints()} 
                    fill="none" 
                    stroke={isWinning() ? "#34d399" : "#f43f5e"} 
                    strokeWidth="4" 
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="transition-colors duration-200"
                    filter="drop-shadow(0 0 8px currentColor)"
                  />
                  {startPrice && (
                    <>
                      {/* Starting price line (center) */}
                      <line
                        x1="0" y1="50" x2="300" y2="50"
                        stroke="rgba(255,255,255,0.5)"
                        strokeWidth="2"
                        strokeDasharray="8 4"
                      />
                      {/* Reference lines for ±0.5% */}
                      <line
                        x1="0" y1="45" x2="300" y2="45"
                        stroke="rgba(52,211,153,0.2)"
                        strokeWidth="1"
                        strokeDasharray="2 2"
                      />
                      <line
                        x1="0" y1="55" x2="300" y2="55"
                        stroke="rgba(244,63,94,0.2)"
                        strokeWidth="1"
                        strokeDasharray="2 2"
                      />
                    </>
                  )}
                </svg>
                
                <div className="absolute inset-0 flex flex-col items-center justify-center z-10">
                    <div className={`text-8xl font-mono font-bold tracking-tighter drop-shadow-2xl transition-all duration-200
                        ${timeLeft <= 3 ? 'text-rose-500 scale-110 animate-pulse' : 'text-white'}
                    `}>
                        00:{timeLeft.toString().padStart(2, '0')}
                    </div>
                    <div className={`mt-4 px-4 py-1 rounded-full border backdrop-blur-md text-sm font-bold uppercase tracking-widest shadow-lg flex items-center gap-2
                        ${isWinning() 
                            ? 'bg-emerald-500/20 border-emerald-400/50 text-emerald-400 shadow-[0_0_20px_rgba(16,185,129,0.3)]' 
                            : 'bg-rose-500/20 border-rose-400/50 text-rose-400 shadow-[0_0_20px_rgba(244,63,94,0.3)]'}
                    `}>
                        {isWinning() ? <TrendingUp size={16} /> : <TrendingDown size={16} />}
                        {isWinning() ? 'WINNING' : 'LOSING'}
                    </div>
                </div>
             </div>
          )}
        </div>

        {/* Bottom: Action Button Area */}
        <div className="p-6 pb-10 z-30 mt-auto">
          
          {gameState === 'RESULT' ? (
             <div className="absolute inset-0 z-50 flex flex-col items-center justify-center bg-black/80 backdrop-blur-sm animate-in zoom-in duration-300">
                <div className={`text-6xl font-black uppercase tracking-tighter italic transform -skew-x-12 mb-4 drop-shadow-[0_0_30px_rgba(255,255,255,0.5)]
                    ${result?.win 
                        ? 'text-transparent bg-clip-text bg-gradient-to-b from-emerald-300 to-emerald-600 animate-bounce' 
                        : 'text-transparent bg-clip-text bg-gradient-to-b from-rose-300 to-rose-600'}
                `}>
                    {result?.win ? 'VICTORY!' : 'DEFEAT'}
                </div>
                <div className={`text-2xl font-bold mb-8 px-6 py-2 rounded-xl border bg-black/50
                     ${result?.win ? 'text-emerald-400 border-emerald-500/50' : 'text-rose-400 border-rose-500/50'}
                `}>
                    {result?.win ? '+' : '-'}{result?.amount} POINTS
                </div>
                <button 
                  onClick={resetGame}
                  className="bg-white text-black px-12 py-4 rounded-full font-black uppercase tracking-widest hover:scale-105 transition-transform flex items-center gap-2 shadow-[0_0_40px_rgba(255,255,255,0.4)]"
                >
                    <RotateCcw size={20} /> Play Again
                </button>
             </div>
          ) : (
            <button 
              onClick={handleStartGame}
              disabled={!prediction || gameState === 'PLAYING'}
              className={`w-full h-20 rounded-2xl font-black uppercase tracking-[0.2em] text-xl transition-all duration-300 relative overflow-hidden group
                ${gameState === 'PLAYING' ? 'opacity-0 pointer-events-none' : 'opacity-100'}
                ${!prediction 
                  ? 'bg-white/5 text-white/20 cursor-not-allowed' 
                  : 'bg-gradient-to-r from-blue-600 to-cyan-400 text-white shadow-[0_0_30px_rgba(6,182,212,0.4)] hover:shadow-[0_0_50px_rgba(6,182,212,0.6)] hover:scale-[1.02] active:scale-[0.98]'
                }`}
            >
               <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent -translate-x-full group-hover:animate-[shimmer_1s_infinite]" />
               <span className="relative z-10 flex items-center justify-center gap-3">
                  {prediction ? 'START GAME' : 'SELECT PREDICTION'}
                  {prediction && <Zap className="fill-current" />}
               </span>
            </button>
          )}

          {gameState !== 'RESULT' && (
             <div className="flex justify-between items-center mt-6 text-[10px] font-mono text-slate-500 uppercase tracking-wider">
                <div className="flex items-center gap-2">
                    <div className={`w-2 h-2 rounded-full ${gameState === 'PLAYING' ? 'bg-emerald-500 animate-ping' : 'bg-slate-700'}`} />
                    Live Bitcoin
                </div>
                <div className={`${currentPrice > currentPriceRef.current - 100 ? 'text-emerald-400' : 'text-rose-400'}`}>
                    ${currentPrice.toFixed(2)}
                </div>
             </div>
          )}
        </div>

      </div>
      <style>{`
        @keyframes shimmer {
          100% { transform: translateX(100%); }
        }
      `}</style>
    </div>
  );
};

export default BitcoinGame;
