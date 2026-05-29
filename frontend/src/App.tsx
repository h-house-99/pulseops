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
  const [loadingHistoryMonitorIds, setLoadingHistoryMonitorIds] = useState<number[]>([])
  const [historyErrorByMonitorId, setHistoryErrorByMonitorId] = useState<Record<number, string | null>>({})
  const [checkingMonitorIds, setCheckingMonitorIds] = useState<number[]>([])

  async function fetchMonitors() {
    const response = await fetch(`${BASE_URL}/monitors`)

    if (!response.ok) {
      throw new Error('Could not load monitors.')
    }

    const data: Monitor[] = await response.json()

    return data
  }

  async function fetchRecentChecks(monitorId: number) {
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
      setCheckingMonitorIds((currentValues) => [...currentValues, monitorId])
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
      if (expandedMonitorIds.includes(monitorId)) {
        const checkData = await fetchRecentChecks(monitorId)
        setChecksByMonitorId((currentChecks) => ({ ...currentChecks, [monitorId]: checkData }))
      } else {
        setChecksByMonitorId((currentChecks) => {
          const nextChecks = { ...currentChecks }
          delete nextChecks[monitorId]
          return nextChecks
        })
      }
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
    } finally {
      setCheckingMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
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

  async function handleToggleCheckHistory(monitorId: number) {
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
      setLoadingHistoryMonitorIds((currentIds) => [...currentIds, monitorId])
      setHistoryErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: null }))

      const data: CheckResult[] = await fetchRecentChecks(monitorId)

      setChecksByMonitorId(currentChecks => ({ ...currentChecks, [monitorId]: data }))
    } catch (error) {
      setHistoryErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
    } finally {
      setLoadingHistoryMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
    }
  }

  async function handleDeleteMonitor(monitorId: number) {
    try {
      setErrorMessage(null)
      const shouldDelete = window.confirm(`Delete this monitor? This will also delete its check history.`)
      if (!shouldDelete) {
        return
      }
      const response = await fetch(`${BASE_URL}/monitors/${monitorId}`, {
        method: 'DELETE',
      })
      if (!(response.status === 204)) {
        throw new Error(`Could not delete monitor ${monitorId}.`)
      }
      const data = await fetchMonitors()
      setMonitors(data)
      setExpandedMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
      setChecksByMonitorId(currentChecks => {
        const nextChecks = { ...currentChecks }
        delete nextChecks[monitorId]
        return nextChecks
      })
      setHistoryErrorByMonitorId(currentErrors => {
        const nextErrors = { ...currentErrors }
        delete nextErrors[monitorId]
        return nextErrors
      })
      setLoadingHistoryMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
      setCheckingMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
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
          <p>No monitors yet. Add one above.</p>
        )}

        {monitors.map((monitor) => (
          <MonitorCard
            key={monitor.id}
            monitor={monitor}
            onCheckNow={handleCheckNow}
            isExpanded={expandedMonitorIds.includes(monitor.id)}
            checks={checksByMonitorId[monitor.id] ?? []}
            isHistoryLoading={loadingHistoryMonitorIds.includes(monitor.id)}
            isChecking={checkingMonitorIds.includes(monitor.id)}
            historyErrorMessage={historyErrorByMonitorId[monitor.id] ?? null}
            onToggleCheckHistory={() => handleToggleCheckHistory(monitor.id)}
            onDeleteMonitor={() => handleDeleteMonitor(monitor.id)}
          />
        ))}

      </section>

    </main>
  )
}

export default App
