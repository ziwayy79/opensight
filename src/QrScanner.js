import { useEffect, useRef, useState } from 'react';
import { Html5Qrcode } from 'html5-qrcode';

export default function QrScanner({ onScan, onError, onClose, isActive }) {
  const qrRef = useRef(null);
  const html5QrCodeRef = useRef(null);
  const [cameraError, setCameraError] = useState('');

  useEffect(() => {
    setCameraError('');
    if (!isActive) {
      if (html5QrCodeRef.current) {
        try {
          if (typeof html5QrCodeRef.current.stop === 'function') {
            html5QrCodeRef.current.stop().catch(() => {});
          }
          if (typeof html5QrCodeRef.current.clear === 'function') {
            html5QrCodeRef.current.clear();
          }
        } catch {}
        html5QrCodeRef.current = null;
      }
      return;
    }
    if (!qrRef.current) return;
    // Prevent double camera views: clean up any previous instance before starting
    if (html5QrCodeRef.current) {
      try {
        if (typeof html5QrCodeRef.current.stop === 'function') {
          html5QrCodeRef.current.stop().catch(() => {});
        }
        if (typeof html5QrCodeRef.current.clear === 'function') {
          html5QrCodeRef.current.clear();
        }
      } catch {}
      html5QrCodeRef.current = null;
    }
    const config = {
      fps: 10,
      qrbox: { width: 250, height: 250 },
      aspectRatio: 1,
      disableFlip: false,
      experimentalFeatures: { useBarCodeDetectorIfSupported: true },
    };
    const html5QrCode = new Html5Qrcode(qrRef.current.id);
    html5QrCodeRef.current = html5QrCode;
    html5QrCode
      .start(
        { facingMode: 'user' },
        config,
        (decodedText) => {
          onScan(decodedText);
          html5QrCode.stop().catch(() => {});
        },
        (err) => {}
      )
      .catch((err) => {
        setCameraError('No camera found or accessible. Please check your device and browser permissions.');
        onError && onError(err);
      });
    return () => {
      if (html5QrCodeRef.current) {
        try {
          if (typeof html5QrCodeRef.current.stop === 'function') {
            html5QrCodeRef.current.stop().catch(() => {});
          }
          if (typeof html5QrCodeRef.current.clear === 'function') {
            html5QrCodeRef.current.clear();
          }
        } catch {}
        html5QrCodeRef.current = null;
      }
    };
  }, [isActive, onScan, onError]);

  return (
    <div style={{ textAlign: 'center' }}>
      <div id="qr-reader" ref={qrRef} style={{ width: 280, margin: '0 auto' }} />
      {cameraError && (
        <div style={{ color: 'red', marginTop: 12 }}>{cameraError}</div>
      )}
    </div>
  );
}
