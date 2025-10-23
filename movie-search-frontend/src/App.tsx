import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './store';
import { ThemeProvider } from './contexts';
import { GlobalErrorNotification } from './components/ui';
import SearchPage from './pages/SearchPage';
import MovieDetailsPage from './pages/MovieDetailsPage';
import './App.css';

const App: React.FC = () => {
  return (
    <Provider store={store}>
      <ThemeProvider>
        <Router>
          <div className="App">
            <GlobalErrorNotification />
            <Routes>
              <Route path="/" element={<SearchPage />} />
              <Route path="/movie/:imdbId" element={<MovieDetailsPage />} />
            </Routes>
          </div>
        </Router>
      </ThemeProvider>
    </Provider>
  );
};

export default App;
