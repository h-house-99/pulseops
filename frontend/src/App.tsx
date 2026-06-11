import './App.css'
import { useState, useEffect, useRef, useCallback } from 'react'
import MonitorForm from './components/MonitorForm'
import MonitorCard from './components/MonitorCard'
import type { Monitor, CheckResult } from './types'

const BASE_URL = 'http://localhost:8080/api'
const MONITOR_REFRESH_INTERVAL_MS = 60_000

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
  const [cachedChartChecksByMonitorId, setCachedChartChecksByMonitorId] = useState<Record<number, CheckResult[]>>({})
  const [loadingChartMonitorIds, setLoadingChartMonitorIds] = useState<number[]>([])
  const [chartErrorByMonitorId, setChartErrorByMonitorId] = useState<Record<number, string | null>>({})
  const [chartWindowHours, setChartWindowHours] = useState(24)
  const [cachedChartFetchedAtByMonitorId, setCachedChartFetchedAtByMonitorId] = useState<Record<number, number>>({})
  const [cachedChartWindowHoursByMonitorId, setCachedChartWindowHoursByMonitorId] = useState<Record<number, number>>({})

  const expandedMonitorIdsRef = useRef<number[]>([])
  const chartWindowHoursRef = useRef(24)

  const fetchMonitors = useCallback(async () => {
    const response = await fetch(`${BASE_URL}/monitors`)

    if (!response.ok) {
      throw new Error('Could not load monitors.')
    }

    const data: Monitor[] = await response.json()

    return data
  }, [])

  const fetchRecentChecks = useCallback(async (monitorId: number) => {
    const response = await fetch(`${BASE_URL}/monitors/${monitorId}/checks/recent`)
    if (!response.ok) {
      throw new Error('Could not load check results.')
    }
    const data: CheckResult[] = await response.json()
    return data
  }, [])

  const fetchChartChecks = useCallback(async (monitorId: number, windowHours: number) => {
    const response = await fetch(`${BASE_URL}/monitors/${monitorId}/checks?hours=${windowHours}`)
    if (!response.ok) {
      throw new Error('Could not load check results.')
    }
    const data: CheckResult[] = await response.json()
    return data
  }, [])

  const refreshExpandedChartHistory = useCallback(async (monitorIds: number[], windowHours: number) => {
    if (monitorIds.length === 0) {
      return
    }

    const nextCachedChartChecksByMonitorId: Record<number, CheckResult[]> = {}
    const nextCachedChartFetchedAtByMonitorId: Record<number, number> = {}
    const nextCachedChartWindowHoursByMonitorId: Record<number, number> = {}

    const fetchedAt = Date.now()
    for (const monitorId of monitorIds) {
      try {
        const checks = await fetchChartChecks(monitorId, windowHours)
        nextCachedChartChecksByMonitorId[monitorId] = checks
        nextCachedChartFetchedAtByMonitorId[monitorId] = fetchedAt
        nextCachedChartWindowHoursByMonitorId[monitorId] = windowHours
        setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: null }))
      } catch (error) {
        setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
      }
    }
    setCachedChartChecksByMonitorId((currentChecks) => ({ ...currentChecks, ...nextCachedChartChecksByMonitorId }))
    setCachedChartFetchedAtByMonitorId((currentValues) => ({ ...currentValues, ...nextCachedChartFetchedAtByMonitorId }))
    setCachedChartWindowHoursByMonitorId((currentValues) => ({ ...currentValues, ...nextCachedChartWindowHoursByMonitorId }))
  }, [fetchChartChecks])

  const refreshExpandedCheckHistory = useCallback(async (monitorIds: number[]) => {
    if (monitorIds.length === 0) {
      return
    }

    const nextChecksByMonitorId: Record<number, CheckResult[]> = {}

    for (const monitorId of monitorIds) {
      try {
        const checks = await fetchRecentChecks(monitorId)
        nextChecksByMonitorId[monitorId] = checks
        setHistoryErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: null }))
      } catch (error) {
        setHistoryErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
      }
    }
    setChecksByMonitorId((currentChecks) => ({ ...currentChecks, ...nextChecksByMonitorId }))
  }, [fetchRecentChecks])

  const refreshMonitorDashboard = useCallback(async () => {
    try {
      const data = await fetchMonitors()
      setMonitors(data)
      setErrorMessage(null)
      await refreshExpandedCheckHistory(expandedMonitorIdsRef.current)
      await refreshExpandedChartHistory(expandedMonitorIdsRef.current, chartWindowHoursRef.current)
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
    }
  }, [fetchMonitors, refreshExpandedCheckHistory, refreshExpandedChartHistory])

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
        try {
          const checkData = await fetchRecentChecks(monitorId)
          setChecksByMonitorId((currentChecks) => ({ ...currentChecks, [monitorId]: checkData }))
          setHistoryErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: null }))
        } catch (error) {
          setHistoryErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
        }

        try {
          const fetchedAt = Date.now()
          const chartData = await fetchChartChecks(monitorId, chartWindowHours)
          setCachedChartChecksByMonitorId((currentChecks) => ({ ...currentChecks, [monitorId]: chartData }))
          setCachedChartFetchedAtByMonitorId((currentValues) => ({ ...currentValues, [monitorId]: fetchedAt }))
          setCachedChartWindowHoursByMonitorId((currentValues) => ({ ...currentValues, [monitorId]: chartWindowHours }))
          setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: null }))
        } catch (error) {
          setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
        }
      } else {
        setChecksByMonitorId((currentChecks) => {
          const nextChecks = { ...currentChecks }
          delete nextChecks[monitorId]
          return nextChecks
        })
        setCachedChartChecksByMonitorId((currentChecks) => {
          const nextChecks = { ...currentChecks }
          delete nextChecks[monitorId]
          return nextChecks
        })
        setCachedChartFetchedAtByMonitorId((currentValues) => {
          const nextValues = { ...currentValues }
          delete nextValues[monitorId]
          return nextValues
        })
        setCachedChartWindowHoursByMonitorId((currentValues) => {
          const nextValues = { ...currentValues }
          delete nextValues[monitorId]
          return nextValues
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

    const hasRecentChecks = checksByMonitorId[monitorId]
    const hasChartChecks = cachedChartChecksByMonitorId[monitorId]
    const hasChartChecksForSelectedWindow = hasChartChecks && cachedChartWindowHoursByMonitorId[monitorId] === chartWindowHours

    if (hasRecentChecks && hasChartChecksForSelectedWindow) {
      return
    }

    try {
      if (!hasChartChecksForSelectedWindow) {
        setLoadingChartMonitorIds((currentIds) => [...currentIds, monitorId])
        setChartErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: null }))
        const fetchedAt = Date.now()
        const chartData = await fetchChartChecks(monitorId, chartWindowHours)
        setCachedChartChecksByMonitorId(currentChecks => ({ ...currentChecks, [monitorId]: chartData }))
        setCachedChartFetchedAtByMonitorId(currentValues => ({ ...currentValues, [monitorId]: fetchedAt }))
        setCachedChartWindowHoursByMonitorId(currentValues => ({ ...currentValues, [monitorId]: chartWindowHours }))
      }
    } catch (error) {
      setChartErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
    } finally {
      setLoadingChartMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
    }

    try {
      if (!hasRecentChecks) {
        setLoadingHistoryMonitorIds((currentIds) => [...currentIds, monitorId])
        setHistoryErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: null }))
        const data: CheckResult[] = await fetchRecentChecks(monitorId)
        setChecksByMonitorId(currentChecks => ({ ...currentChecks, [monitorId]: data }))
      }
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
      setCachedChartChecksByMonitorId(currentChecks => {
        const nextChecks = { ...currentChecks }
        delete nextChecks[monitorId]
        return nextChecks
      })
      setChartErrorByMonitorId(currentErrors => {
        const nextErrors = { ...currentErrors }
        delete nextErrors[monitorId]
        return nextErrors
      })
      setLoadingChartMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
      setCachedChartFetchedAtByMonitorId(currentValues => {
        const nextValues = { ...currentValues }
        delete nextValues[monitorId]
        return nextValues
      })
      setCachedChartWindowHoursByMonitorId(currentValues => {
        const nextValues = { ...currentValues }
        delete nextValues[monitorId]
        return nextValues
      })
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
    }
  }

  useEffect(() => {
    async function loadInitialMonitors() {
      try {
        await refreshMonitorDashboard()
      } finally {
        setIsLoading(false)
      }
    }
    loadInitialMonitors()

    const intervalId = window.setInterval(() => refreshMonitorDashboard(), MONITOR_REFRESH_INTERVAL_MS)
    return () => window.clearInterval(intervalId)
  }, [refreshMonitorDashboard])

  useEffect(() => {
    expandedMonitorIdsRef.current = expandedMonitorIds
  }, [expandedMonitorIds])
  useEffect(() => {
    chartWindowHoursRef.current = chartWindowHours
    refreshExpandedChartHistory(expandedMonitorIdsRef.current, chartWindowHoursRef.current)
  }, [chartWindowHours, refreshExpandedChartHistory])

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

        {monitors.map((monitor) => {
          const isExpanded = expandedMonitorIds.includes(monitor.id)
          const chartErrorMessage = chartErrorByMonitorId[monitor.id] ?? null
          const hasChartChecksForSelectedWindow = cachedChartWindowHoursByMonitorId[monitor.id] === chartWindowHours
          const chartChecks = hasChartChecksForSelectedWindow ? cachedChartChecksByMonitorId[monitor.id] ?? [] : []
          const latestChartCheck = chartChecks.at(-1)
          const chartFetchedAt = cachedChartFetchedAtByMonitorId[monitor.id]
            ?? (latestChartCheck ? new Date(latestChartCheck.checkedAt).getTime() : 0)
          const isChartDataPending = isExpanded && !hasChartChecksForSelectedWindow && !chartErrorMessage

          return (
            <MonitorCard
              key={monitor.id}
              monitor={monitor}
              onCheckNow={handleCheckNow}
              isExpanded={isExpanded}
              checks={checksByMonitorId[monitor.id] ?? []}
              isHistoryLoading={loadingHistoryMonitorIds.includes(monitor.id)}
              isChecking={checkingMonitorIds.includes(monitor.id)}
              historyErrorMessage={historyErrorByMonitorId[monitor.id] ?? null}
              chartChecks={chartChecks}
              isChartLoading={loadingChartMonitorIds.includes(monitor.id) || isChartDataPending}
              chartErrorMessage={chartErrorMessage}
              chartWindowHours={chartWindowHours}
              onChartWindowHoursChange={setChartWindowHours}
              chartFetchedAt={chartFetchedAt}
              onToggleCheckHistory={() => handleToggleCheckHistory(monitor.id)}
              onDeleteMonitor={() => handleDeleteMonitor(monitor.id)}
            />
          )
        })}

      </section>

    </main>
  )
}

export default App
