import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './app/app';
import './styles.css';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { neonTheme } from './theme';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

const queryClient = new QueryClient();

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <ThemeProvider theme={neonTheme('blue')}> {/* или 'green', 'purple' */}
            <CssBaseline />
            <App />
        </ThemeProvider>
    </React.StrictMode>,
);

root.render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>
);
