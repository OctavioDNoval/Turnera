import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { apiFetch } from '../api/client'

export default function Home() {
  const { user, logout } = useAuth()
  const [status, setStatus] = useState('checking')

  useEffect(() => {
    apiFetch('/api/health')
      .then(() => setStatus('ok'))
      .catch(() => setStatus('error'))
  }, [])

  return (
    <main className="app">
      <section className="card">
        <h1>Hola, {user?.name}</h1>
        <p>Gestioná tus turnos y tu disponibilidad.</p>
        <p className={`status status--${status}`}>
          <span className="detail">Backend:</span>{' '}
          {status === 'ok' ? 'conectado' : status === 'error' ? 'sin conexión' : 'verificando…'}
        </p>
        <button type="button" onClick={logout}>
          Cerrar sesión
        </button>
      </section>
    </main>
  )
}