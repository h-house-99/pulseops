import type { CheckResult, Monitor } from '../types'

type MonitorCardProps = {
  monitor: Monitor
  onCheckNow: (monitorId: number) => void
  isExpanded: boolean
  checks: CheckResult[]
  isHistoryLoading: boolean
  isChecking: boolean
  historyErrorMessage: string | null
  onToggleCheckHistory: () => void
  onDeleteMonitor: () => void
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
  checks,
  isHistoryLoading,
  isChecking,
  historyErrorMessage,
  onToggleCheckHistory,
  onDeleteMonitor,
}: MonitorCardProps) {
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
            aria-label={`${isExpanded ? 'Hide' : 'Show'} ${monitor.name} check history`}
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

      <div className="monitor-summary">
          <span>Uptime {monitor.uptimePercentage === null ? '-' : `${monitor.uptimePercentage}%`}</span>
          <span>Checks {monitor.totalChecks}</span>
          <span>Avg {monitor.averageResponseTimeMs === null ? '-' : `${monitor.averageResponseTimeMs}ms`}</span>
          <span>Fast {monitor.fastestResponseTimeMs === null ? '-' : `${monitor.fastestResponseTimeMs}ms`}</span>
          <span>Slow {monitor.slowestResponseTimeMs === null ? '-' : `${monitor.slowestResponseTimeMs}ms`}</span>
      </div>

      {monitor.lastFailureAt && (
        <p className="monitor-summary-last-failure">
          Last failure {formatCheckedAt(monitor.lastFailureAt)}
        </p>
      )}

      {monitor.latestErrorMessage && (
        <p className="monitor-summary-latest-error">
          {monitor.latestErrorMessage}
        </p>
      )}

      {isExpanded && (
        <div className="check-history">
          <h4>Recent checks</h4>
          <div className="check-history-header">
            <span>Status</span>
            <span>Code</span>
            <span>Latency</span>
            <span>Checked</span>
          </div>

          {isHistoryLoading && <p>Loading checks...</p>}

          {historyErrorMessage && <p className="error-message">{historyErrorMessage}</p>}

          {!isHistoryLoading && !historyErrorMessage && checks.length === 0 && (
            <p>No checks recorded yet.</p>
          )}

          {!isHistoryLoading && !historyErrorMessage && checks.length > 0 && (
            <ul>
              {checks.map((check) => (
                <li key={check.id}>
                  <span className={`status-pill status-${check.status.toLowerCase()}`}>
                    {check.status}
                  </span>
                  <span>{check.statusCode ?? '-'}</span>
                  <span>{check.responseTimeMs === null ? '-' : `${check.responseTimeMs}ms`}</span>
                  <span>{formatCheckedAt(check.checkedAt)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}

export default MonitorCard
