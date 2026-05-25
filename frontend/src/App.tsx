import './App.css'
import { useState, useEffect } from 'react'
import MonitorForm from './components/MonitorForm'
import MonitorCard from './components/MonitorCard'
import type { Monitor, CheckResult } from './types'

const BASE_URL = 'http://localhost:8080/api'

function App() {
  const [monitors, setMonitors] = useState<Monitor[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [newMonitorName, setNewMonitorName] = useState('')
  const [newMonitorUrl, setNewMonitorUrl] = useState('')
  const [expandedMonitorIds, setExpandedMonitorIds] = useState<number[]>([])
  const [checksByMonitorId, setChecksByMonitorId] = useState<Record<number, CheckResult[]>>({})
  const [loadingCheckMonitorIds, setLoadingCheckMonitorIds] = useState<number[]>([])
  const [checksErrorByMonitorId, setChecksErrorByMonitorId] = useState<Record<number, string | null>>({})

  async function fetchMonitors() {
    const response = await fetch(`${BASE_URL}/monitors`)

    if (!response.ok) {
      throw new Error('Could not load monitors.')
    }

    const data: Monitor[] = await response.json()

    return data
  }

  async function fetchCheckResults(monitorId: number) {
    const response = await fetch(`${BASE_URL}/monitors/${monitorId}/checks/recent`)
    if (!response.ok) {
      throw new Error('Could not load check results.')
    }
    const data: CheckResult[] = await response.json()
    return data
  }

  async function handleCheckNow(monitorId: number) {
    try {
      setErrorMessage(null)
      const response = await fetch(`${BASE_URL}/monitors/${monitorId}/check-now`,
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

  async function handleCreateMonitor(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault()

    try {
      setErrorMessage(null)
      const response = await fetch(`${BASE_URL}/monitors`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name: newMonitorName, url: newMonitorUrl }),
      })
      if (!response.ok) {
        throw new Error('Could not create monitor.')
      }

      const data = await fetchMonitors()
      setMonitors(data)
      setNewMonitorName('')
      setNewMonitorUrl('')
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
    }
  }

  async function handleToggleChecks(monitorId: number) {
    const isExpanded = expandedMonitorIds.includes(monitorId)

    if (isExpanded) {
      setExpandedMonitorIds((currentIds) => currentIds.filter(id => id !== monitorId))
      return
    }

    setExpandedMonitorIds((currentIds) => [...currentIds, monitorId])

    if (checksByMonitorId[monitorId]) {
      return
    }

    try {
      setLoadingCheckMonitorIds((currentIds) => [...currentIds, monitorId])
      setChecksErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: null }))

      const data: CheckResult[] = await fetchCheckResults(monitorId)

      setChecksByMonitorId(currentChecks => ({ ...currentChecks, [monitorId]: data }))
    } catch (error) {
      setChecksErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
    } finally {
      setLoadingCheckMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
    }
  }



  useEffect(() => {
    async function loadInitialMonitors() {
      try {
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

        <MonitorForm
          name={newMonitorName}
          url={newMonitorUrl}
          onNameChange={setNewMonitorName}
          onUrlChange={setNewMonitorUrl}
          onSubmit={handleCreateMonitor}
        />

        {isLoading && <p>Loading monitors...</p>}

        {errorMessage && <p className="error-message">{errorMessage}</p>}

        {!isLoading && !errorMessage && monitors.length === 0 && (
          <p>No monitors yet. Add one from the public API list next.</p>
        )}

        {monitors.map((monitor) => (
          <MonitorCard
            key={monitor.id}
            monitor={monitor}
            onCheckNow={handleCheckNow}
            isExpanded={expandedMonitorIds.includes(monitor.id)}
            checks={checksByMonitorId[monitor.id] ?? []}
            isChecksLoading={loadingCheckMonitorIds.includes(monitor.id)}
            checksErrorMessage={checksErrorByMonitorId[monitor.id] ?? null}
            onToggleChecks={() => handleToggleChecks(monitor.id)}
          />
        ))}

      </section>

    </main>
  )
}

export default App
