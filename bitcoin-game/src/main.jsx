import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { PrivyProvider } from '@privy-io/react-auth';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <PrivyProvider
      appId={import.meta.env.VITE_PRIVY_APP_ID || 'YOUR_PRIVY_APP_ID'}
      config={{
        loginMethods: ['email', 'wallet'],
        embeddedWallets: { createOnLogin: true },
        appearance: {
          theme: 'dark',
        },
      }}
    >
      <App />
    </PrivyProvider>
  </React.StrictMode>
);
