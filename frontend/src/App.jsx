import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [status, setStatus] = useState('checking')

  useEffect(() => {
    fetch('/api/health')
      .then((res) => {
        if (!res.ok) throw new Error('Bad response')
        return res.json()
      })
      .then(() => setStatus('ok'))
      .catch(() => setStatus('error'))
  }, [])

  return (
    <main className="app">
      <h1>Turnera</h1>
      <p>Sistema de turnos para clases de guitarra.</p>
      <p className={`status status--${status}`}>
        Backend:{' '}
        {status === 'ok' ? 'conectado' : status === 'error' ? 'sin conexión' : 'verificando…'}
      </p>
    </main>
  )
}

export default App