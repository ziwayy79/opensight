import { useState } from 'react';
import QrScanner from './QrScanner';
import './App.css';

function App() {
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [summary, setSummary] = useState('');
  const [keyActions, setKeyActions] = useState([]);
  const [submitted, setSubmitted] = useState(false);
  const [savedLinks, setSavedLinks] = useState([]);
  const [showScanner, setShowScanner] = useState(false);
  // Handle QR scan result
  const handleQrScan = (scannedText) => {
    // Validate if scannedText is a URL
    try {
      const urlObj = new URL(scannedText);
      setUrl(urlObj.href);
      setShowScanner(false);
      setTimeout(() => {
        document.getElementById('url-form')?.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
      }, 100);
    } catch {
      setError('Scanned QR is not a valid URL.');
      setShowScanner(false);
    }
  };

  const handleQrError = (err) => {
    setError('QR Scanner error: ' + (err?.message || err));
    setShowScanner(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Reset state
    setError('');
    setSummary('');
    setKeyActions([]);
    setLoading(true);
    setSubmitted(true);

    // Validate URL
    if (!url.trim()) {
      setError('PLEASE ENTER A VALID URL');
      setLoading(false);
      return;
    }

    try {
      // Call backend API
      const apiUrl = process.env.REACT_APP_API_URL || 'http://localhost:5000';
      const response = await fetch(`${apiUrl}/api/summarize`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ url: url.trim() }),
      });

      if (!response.ok) {
        throw new Error(`Error: ${response.statusText}`);
      }

      const data = await response.json();
      setSummary(data.summary || '');
      setKeyActions(data.keyActions || []);
    } catch (err) {
      setError(`Failed to fetch summary: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setUrl('');
    setSummary('');
    setKeyActions([]);
    setError('');
    setSubmitted(false);
  };

  // Save a link to the Saved Links section
  const handleSaveLink = (label, url) => {
    if (!url || savedLinks.some(link => link.url === url)) return;
    setSavedLinks(prev => [...prev, { label, url }]);
  };


  return (
    <div className="App">
      <div className="container">
        <header className="header">
          <h1>OPENSIGHT</h1>
          <p className="subtitle"></p>
        </header>

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: 16 }}>
          <button
            className="btn btn-secondary"
            style={{ marginBottom: 10, fontSize: 16, padding: '10px 18px' }}
            onClick={() => setShowScanner((s) => !s)}
            aria-pressed={showScanner}
          >
            {showScanner ? 'Stop QR Scanner' : 'Scan QR Code'}
          </button>
        </div>

        {showScanner && (
          <div style={{ marginBottom: 20 }}>
            <QrScanner
              isActive={showScanner}
              onScan={handleQrScan}
              onError={handleQrError}
              onClose={() => setShowScanner(false)}
            />
          </div>
        )}

        <form className="form" id="url-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="url-input">Insert Website URL</label>
            <input
              id="url-input"
              type="url"
              placeholder="https://example.com"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              className="url-input"
              aria-label="Website URL input"
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Analyzing...' : 'Show Insights'}
          </button>
        </form>

        {error && (
          <div className="alert alert-error" role="alert">
            {error}
          </div>  
        )}

        {submitted && !loading && summary && (
          <div className="results">
            <section className="result-section">
              <h2>Summary</h2>
              <p className="summary-text">{summary}</p>
            </section>
            

            {keyActions.length > 0 && (
              <section className="result-section">
                <h2>Key Actions</h2>
                <ul className="actions-list">
                  {keyActions.map((action, index) => {
                    let label, url;
                    if (typeof action === 'object' && action.label && action.url) {
                      label = action.label;
                      url = action.url;
                    } else {
                      const urlMatch = /https?:\/\/[\w\-._~:/?#[\]@!$&'()*+,;=%]+/i.exec(action);
                      label = typeof action === 'string' ? action : '';
                      url = urlMatch ? urlMatch[0] : null;
                    }
                    return (
                      <li key={index} className="action-item">
                        <button
                          className="btn btn-primary key-action-btn"
                          style={{
                            marginBottom: 8,
                            fontSize: 16,
                            padding: '10px 18px',
                            cursor: url ? 'pointer' : 'default',
                            background: url ? '#b39ddb' : undefined,
                            color: url ? '#fff' : undefined,
                            border: url ? '3px solid #6c3483' : undefined,
                          }}
                          onClick={() => url && window.open(url, '_blank')}
                        >
                          {label}
                        </button>
                        {url && (
                          <button
                            className="btn btn-secondary"
                            style={{ marginLeft: 8, fontSize: 14, padding: '8px 14px' }}
                            onClick={() => handleSaveLink(label, url)}
                          >
                            Save Link
                          </button>
                        )}
                      </li>
                    );
                  })}
                </ul>
              </section>
            )}
          </div>
        )}

        {loading && (
          <div className="loading" role="status" aria-live="polite">
            <p>⏳ Analyzing website...</p>
          </div>
        )}
      </div>
      <footer style={{ marginTop: 40 }}>
        <section className="saved-links-section">
        <h2 style={{ fontSize: 30, color: '#000', marginBottom: 6, textAlign: 'center', fontWeight: 'bold'}}>SAVED LINKS</h2>          
        <div style={{ width: 700,maxHeight: 180, overflowY: 'auto', border: '3px solid #000', borderRadius: 12, padding: 8, background: '#fff', minHeight: 60, margin: '0 auto', textAlign: 'center', alignItems: 'center', display: 'flex'}}>
            {savedLinks.length === 0 ? (
              <div style={{ color: '#00000', fontSize: 18 }}>no links saved yet</div>
            ) : (
              <ul style={{ margin: 0, padding: 0, listStyle: 'none' }}>
                {savedLinks.map((link, idx) => (
                  <li key={idx} style={{ marginBottom: 8 }}>
                    <a
                      href={link.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{ color: '#0056b3', textDecoration: 'underline', fontWeight: 600 }}
                    >
                      {link.label}
                    </a>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      </footer>
    </div>
  );
}

export default App;
