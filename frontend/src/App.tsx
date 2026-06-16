import './App.css'
import { useState, useEffect, useRef, useCallback } from 'react'
import MonitorForm from './components/MonitorForm'
import MonitorCard from './components/MonitorCard'
import type { Monitor, CheckResult } from './types'
import { canManageMonitors } from './config'
type ChartCacheKey = `${number}-${number}`

const BASE_URL = 'http://localhost:8080/api'
const MONITOR_REFRESH_INTERVAL_MS = 60_000
const SEVEN_DAY_CHART_TTL_MS = 60 * 60 * 1000 // 1 hour
const SEVEN_DAY_WINDOW_HOURS = 168

function getChartCacheKey(monitorId: number, windowHours: number): ChartCacheKey {
  return `${monitorId}-${windowHours}`
}

function shouldSkipChartRefetch(
  fetchedAtByKey: Record<ChartCacheKey, number>,
  monitorId: number,
  windowHours: number,
): boolean {
  if (windowHours !== SEVEN_DAY_WINDOW_HOURS) {
    return false
  }
  const cacheKey = getChartCacheKey(monitorId, windowHours)
  const fetchedAt = fetchedAtByKey[cacheKey]
  if (fetchedAt == null) {
    return false
  }
  return Date.now() - fetchedAt < SEVEN_DAY_CHART_TTL_MS
}

function removeMonitorChartCache<T>(cache: Record<ChartCacheKey, T>, monitorId: number) {
  const prefix = `${monitorId}-`
  return Object.fromEntries(
    Object.entries(cache).filter(([key]) => !key.startsWith(prefix)),
  ) as Record<ChartCacheKey, T>
}

function App() {
  const [monitors, setMonitors] = useState<Monitor[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [newMonitorName, setNewMonitorName] = useState('')
  const [newMonitorUrl, setNewMonitorUrl] = useState('')
  const [expandedMonitorIds, setExpandedMonitorIds] = useState<number[]>([])
  const [checkingMonitorIds, setCheckingMonitorIds] = useState<number[]>([])
  const [loadingChartMonitorIds, setLoadingChartMonitorIds] = useState<number[]>([])
  const [chartErrorByMonitorId, setChartErrorByMonitorId] = useState<Record<number, string | null>>({})
  const [chartWindowHours, setChartWindowHours] = useState(24)

  const [cachedChartChecksByKey, setCachedChartChecksByKey] = useState<Record<ChartCacheKey, CheckResult[]>>({})
  const [cachedChartFetchedAtByKey, setCachedChartFetchedAtByKey] = useState<Record<ChartCacheKey, number>>({})

  const expandedMonitorIdsRef = useRef<number[]>([])
  const chartWindowHoursRef = useRef(24)
  const cachedChartFetchedAtByKeyRef = useRef<Record<ChartCacheKey, number>>({})
  useEffect(() => {
    cachedChartFetchedAtByKeyRef.current = cachedChartFetchedAtByKey
  }, [cachedChartFetchedAtByKey])

  const fetchMonitors = useCallback(async () => {
    const response = await fetch(`${BASE_URL}/monitors`)

    if (!response.ok) {
      throw new Error('Could not load monitors.')
    }

    const data: Monitor[] = await response.json()

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

    const nextChecks: Record<ChartCacheKey, CheckResult[]> = {}
    const nextFetchedAt: Record<ChartCacheKey, number> = {}

    const fetchedAt = Date.now()
    for (const monitorId of monitorIds) {
      if (shouldSkipChartRefetch(cachedChartFetchedAtByKeyRef.current, monitorId, windowHours)) {
        continue
      }
      try {
        const checks = await fetchChartChecks(monitorId, windowHours)
        const cacheKey = getChartCacheKey(monitorId, windowHours)
        nextChecks[cacheKey] = checks
        nextFetchedAt[cacheKey] = fetchedAt
        setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: null }))
      } catch (error) {
        setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
      }
    }
    if (Object.keys(nextChecks).length > 0) {
      setCachedChartChecksByKey((currentChecks) => ({ ...currentChecks, ...nextChecks }))
      setCachedChartFetchedAtByKey((currentFetchedAt) => ({ ...currentFetchedAt, ...nextFetchedAt }))
    }
  }, [fetchChartChecks])

  const refreshMonitorDashboard = useCallback(async () => {
    try {
      const data = await fetchMonitors()
      setMonitors(data)
      setErrorMessage(null)
      await refreshExpandedChartHistory(expandedMonitorIdsRef.current, chartWindowHoursRef.current)
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Something went wrong.')
    }
  }, [fetchMonitors, refreshExpandedChartHistory])

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
      const cacheKey = getChartCacheKey(monitorId, chartWindowHours)
      if (expandedMonitorIds.includes(monitorId)) {

        try {
          const fetchedAt = Date.now()
          const chartData = await fetchChartChecks(monitorId, chartWindowHours)
          setCachedChartChecksByKey((currentChecks) => ({ ...currentChecks, [cacheKey]: chartData }))
          setCachedChartFetchedAtByKey((currentFetchedAt) => ({ ...currentFetchedAt, [cacheKey]: fetchedAt }))
          setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: null }))
        } catch (error) {
          setChartErrorByMonitorId((currentErrors) => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
        }
      } else {
        setCachedChartChecksByKey((currentChecks) => removeMonitorChartCache(currentChecks, monitorId))
        setCachedChartFetchedAtByKey((currentFetchedAt) => removeMonitorChartCache(currentFetchedAt, monitorId))
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

    const key = getChartCacheKey(monitorId, chartWindowHours)
    const hasChartChecks = cachedChartChecksByKey[key] !== undefined
    const isCacheValid = shouldSkipChartRefetch(cachedChartFetchedAtByKeyRef.current, monitorId, chartWindowHours)

    if (isCacheValid && hasChartChecks) {
      return
    }

    try {
      if (!isCacheValid || !hasChartChecks) {
        setLoadingChartMonitorIds((currentIds) => [...currentIds, monitorId])
        setChartErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: null }))
        const fetchedAt = Date.now()
        const chartData = await fetchChartChecks(monitorId, chartWindowHours)
        setCachedChartChecksByKey((currentChecks) => ({ ...currentChecks, [key]: chartData }))
        setCachedChartFetchedAtByKey((currentFetchedAt) => ({ ...currentFetchedAt, [key]: fetchedAt }))
      }
    } catch (error) {
      setChartErrorByMonitorId(currentErrors => ({ ...currentErrors, [monitorId]: error instanceof Error ? error.message : 'Something went wrong.' }))
    } finally {
      setLoadingChartMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
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
      setCheckingMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
      setChartErrorByMonitorId(currentErrors => {
        const nextErrors = { ...currentErrors }
        delete nextErrors[monitorId]
        return nextErrors
      })
      setLoadingChartMonitorIds(currentIds => currentIds.filter(id => id !== monitorId))
      setCachedChartChecksByKey((currentChecks) => removeMonitorChartCache(currentChecks, monitorId))
      setCachedChartFetchedAtByKey((currentFetchedAt) => removeMonitorChartCache(currentFetchedAt, monitorId))
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
      <div className="app-header">
        <div className="app-header-top">
          <p className="eyebrow">PulseOps</p>
          <span className={`role-tag ${canManageMonitors ? 'role-tag-admin' : 'role-tag-viewer'}`}>
            {canManageMonitors ? 'Admin' : 'Viewer'}
          </span>
        </div>
        <h1>Monitor API health in real time</h1>
        <p className="intro">
          Live health for curated public APIs.
        </p>
      </div>
      <section className="monitor-section">
        <h2>Monitors</h2>

        {canManageMonitors && <MonitorForm
          name={newMonitorName}
          url={newMonitorUrl}
          onNameChange={setNewMonitorName}
          onUrlChange={setNewMonitorUrl}
          onSubmit={handleCreateMonitor}
        />}

        {isLoading && <p>Loading monitors...</p>}

        {errorMessage && <p className="error-message">{errorMessage}</p>}

        {canManageMonitors && !isLoading && !errorMessage && monitors.length === 0 && (
          <p>No monitors yet. Add one above.</p>
        )}

        <div className="monitor-list">
          {monitors.map((monitor) => {
            const isExpanded = expandedMonitorIds.includes(monitor.id)
            const chartErrorMessage = chartErrorByMonitorId[monitor.id] ?? null
            const chartKey = getChartCacheKey(monitor.id, chartWindowHours)
            const hasChartData = chartKey in cachedChartChecksByKey
            const chartChecks = hasChartData ? cachedChartChecksByKey[chartKey] ?? [] : []
            const chartFetchedAt = cachedChartFetchedAtByKey[chartKey]
              ?? (chartChecks.at(-1) ? new Date(chartChecks.at(-1)!.checkedAt).getTime() : Date.now())
            const isChartDataPending = isExpanded && !hasChartData && !chartErrorMessage

            return (
              <MonitorCard
                key={monitor.id}
                monitor={monitor}
                onCheckNow={handleCheckNow}
                isExpanded={isExpanded}
                isChecking={checkingMonitorIds.includes(monitor.id)}
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
        </div>

      </section>

    </main>
  )
}

export default App
