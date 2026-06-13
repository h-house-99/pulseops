import type { CheckResult, Monitor } from '../types'
import MonitorLatencyChart from './MonitorLatencyChart'

type MonitorCardProps = {
  monitor: Monitor
  onCheckNow: (monitorId: number) => void
  isExpanded: boolean
  isChecking: boolean
  chartChecks: CheckResult[]
  isChartLoading: boolean
  chartErrorMessage: string | null
  chartWindowHours: number
  onChartWindowHoursChange: (hours: number) => void
  chartFetchedAt: number
  onToggleCheckHistory: () => void
  onDeleteMonitor: () => void
}

type CheckStats = {
  totalChecks: number
  uptimePercentage: number
  averageResponseTimeMs: number | null
  fastestResponseTimeMs: number | null
  slowestResponseTimeMs: number | null
}

function getCheckStats(checks: CheckResult[]): CheckStats {
  const responseTimes = checks
    .map((check) => check.responseTimeMs)
    .filter((responseTime): responseTime is number => responseTime !== null)

  const upChecks = checks.filter((check) => check.status === 'UP').length

  return {
    totalChecks: checks.length,
    uptimePercentage: checks.length === 0 ? 0 : Math.round((upChecks / checks.length) * 100),
    averageResponseTimeMs:
      responseTimes.length === 0
        ? null
        : Math.round(responseTimes.reduce((sum, time) => sum + time, 0) / responseTimes.length),
    fastestResponseTimeMs: responseTimes.length === 0 ? null : Math.min(...responseTimes),
    slowestResponseTimeMs: responseTimes.length === 0 ? null : Math.max(...responseTimes),
  }
}

function formatCheckedAt(checkedAt: string) {
  return new Date(checkedAt).toLocaleString([], {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

function MonitorCard({
  monitor,
  onCheckNow,
  isExpanded,
  isChecking,
  chartChecks,
  isChartLoading,
  chartErrorMessage,
  chartWindowHours,
  onChartWindowHoursChange,
  chartFetchedAt,
  onToggleCheckHistory,
  onDeleteMonitor,
}: MonitorCardProps) {
  const checkStats = getCheckStats(chartChecks)

  return (
    <div className="monitor-card">
      <div className="monitor-card-main">
        <div>
          <h3>{monitor.name}</h3>
          <p>{monitor.url}</p>
        </div>

        <div className="monitor-meta">
          <button
            className="icon-button"
            aria-label={`Check ${monitor.name} now`}
            onClick={() => onCheckNow(monitor.id)}
            disabled={isChecking}
          >
            {isChecking ? '...' : '↻'}
          </button>
          <span className={`status-pill status-${monitor.status.toLowerCase()}`}>
            {monitor.status}
          </span>
          <span>{monitor.lastResponseTimeMs === null ? '-' : `${monitor.lastResponseTimeMs}ms`}</span>
          <button
            className="icon-button"
            aria-label={`${isExpanded ? 'Hide' : 'Show'} ${monitor.name} details`}
            onClick={onToggleCheckHistory}
          >
            {isExpanded ? '⌃' : '⌄'}
          </button>
          <button
            className="icon-button danger-button"
            aria-label={`Delete ${monitor.name}`}
            onClick={onDeleteMonitor}
          >
            ×
          </button>
        </div>
      </div>

      {isExpanded && (
        <>

          {chartErrorMessage && <p className="error-message">{chartErrorMessage}</p>}

          <div className="monitor-chart-window-control" aria-label="Chart window hours">
            {[1, 8, 24, 168].map((hours) => (
              <button
                key={hours}
                type="button"
                className={chartWindowHours === hours ? 'active' : ''}
                onClick={() => onChartWindowHoursChange(hours)}
              >
                {hours === 168 ? '7d' : `${hours}h`}
              </button>
            ))}
          </div>

          {!chartErrorMessage && (
            <MonitorLatencyChart checks={chartChecks} timeWindowHours={chartWindowHours} chartFetchedAt={chartFetchedAt} isLoading={isChartLoading} />
          )}

          <div className="monitor-summary">
            <span>Uptime: {checkStats.uptimePercentage}%</span>
            <span>Checks: {checkStats.totalChecks}</span>
            <span>Avg: {checkStats.averageResponseTimeMs === null ? '-' : `${checkStats.averageResponseTimeMs}ms`}</span>
            <span>Fast: {checkStats.fastestResponseTimeMs === null ? '-' : `${checkStats.fastestResponseTimeMs}ms`}</span>
            <span>Slow: {checkStats.slowestResponseTimeMs === null ? '-' : `${checkStats.slowestResponseTimeMs}ms`}</span>
          </div>

          {monitor.lastFailureAt && (
            <p className="monitor-summary-last-failure">
              <strong>Last failure:</strong> {formatCheckedAt(monitor.lastFailureAt)}
            </p>
          )}

          {monitor.latestFailureReason && (
            <p className="monitor-summary-latest-error">
              <strong>Failure reason:</strong> {monitor.latestFailureReason}
            </p>
          )}
        </>
      )}
    </div>
  )
}

export default MonitorCard
