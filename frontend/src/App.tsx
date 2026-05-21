import './App.css'
import { useState, useEffect } from 'react'

type Monitor = {
  id: number
  name: string
  url: string
  status: 'UNKNOWN' | 'UP' | 'DOWN'
  lastStatusCode: number | null
  lastResponseTimeMs: number | null
  lastCheckedAt: string | null
}

function App() {
  const [monitors, setMonitors] = useState<Monitor[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  async function fetchMonitors() {
    const response = await fetch('http://localhost:8080/api/monitors')

    if (!response.ok) {
      throw new Error('Could not load monitors.')
    }

    return await response.json()
  }

  async function handleCheckNow(monitorId: number) {
    try {
      setErrorMessage(null)
      const response = await fetch(`http://localhost:8080/api/monitors/${monitorId}/check-now`, 
        {
          method: 'POST',
        }
      )
      if (!response.ok) {
        throw new Error('Could not check monitor.')
      }
      const data = await fetchMonitors()
      setMonitors(data)
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
    }
  }

  useEffect(() => {
    async function loadInitialMonitors() {
      try{
        const data = await fetchMonitors()
        setMonitors(data)
        setErrorMessage(null)
      } catch (error) {
        setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
      } finally {
        setIsLoading(false)
      }
    }
    loadInitialMonitors()
  }, [])

  return (
    <main className="app-shell">
      <p className="eyebrow">PulseOps</p>
      <h1>API monitoring dashboard</h1>
      <p className="intro">
        Track API health, latency, and recent check results from one focused dashboard.
      </p>
      <section className="monitor-section">
        <h2>Monitors</h2>

        {isLoading && <p>Loading monitors...</p>}

        {errorMessage && <p>{errorMessage}</p>} 

        {!isLoading && !errorMessage && monitors.length === 0 && (
          <p>No monitors yet. Add one from the public API list next.</p>
        )}

        {monitors.map((monitor) => (
          <div className="monitor-card" key={monitor.id}>
            <div>
              <h3>{monitor.name}</h3>
              <p>{monitor.url}</p>
            </div>

            <div className="monitor-meta">
              <button className="icon-button" aria-label={`Check ${monitor.name} now`} onClick={() => handleCheckNow(monitor.id)}>↻</button>
              <span className={`status-pill status-${monitor.status.toLowerCase()}`}>{monitor.status}</span>
              <span>{monitor.lastResponseTimeMs === null ? '-' : `${monitor.lastResponseTimeMs}ms`}</span>
            </div>
          </div>
        ))}

      </section>

    </main>
  )
}

export default App
