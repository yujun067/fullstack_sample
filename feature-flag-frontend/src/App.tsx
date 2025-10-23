import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useParams } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './store';
import FlagListPage from './pages/FlagListPage';
import CreateFlagPage from './pages/CreateFlagPage';
import EditFlagPage from './pages/EditFlagPage';
import { GlobalErrorNotification } from './components/feature';

// URL parameter extractor for edit page
const EditFlagPageWrapper: React.FC = () => {
  const { name } = useParams<{ name: string }>();
  return name ? <EditFlagPage flagName={decodeURIComponent(name)} /> : <Navigate to="/flags" replace />;
};

function App() {
  return (
    <Provider store={store}>
      <Router>
        <div className="App">
          <Routes>
            {/* Default route - redirect to flags */}
            <Route path="/" element={<Navigate to="/flags" replace />} />
            
            {/* Feature flags routes */}
            <Route path="/flags" element={<FlagListPage />} />
            <Route path="/flags/create" element={<CreateFlagPage />} />
            <Route path="/flags/:name/edit" element={<EditFlagPageWrapper />} />
            
            {/* Catch all route - redirect to flags */}
            <Route path="*" element={<Navigate to="/flags" replace />} />
          </Routes>
          
          {/* Global Error Notification */}
          <GlobalErrorNotification />
        </div>
      </Router>
    </Provider>
  );
}

export default App;
