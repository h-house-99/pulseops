import type { CheckResult, Monitor } from '../types'

type MonitorCardProps = {
    monitor: Monitor
    onCheckNow: (monitorId: number) => void
    isExpanded: boolean
    checks: CheckResult[]
    isChecksLoading: boolean
    checksErrorMessage: string | null
    onToggleChecks: () => void
}

function MonitorCard({
    monitor,
    onCheckNow,
    isExpanded,
    checks,
    isChecksLoading,
    checksErrorMessage,
    onToggleChecks,
}: MonitorCardProps) {
    return (
        <div className="monitor-card">
            <div className="monitor-card-main">
                <div>
                    <h3>{monitor.name}</h3>
                    <p>{monitor.url}</p>
                </div>
                <div className="monitor-meta">
                    <button className="icon-button" aria-label={`Check ${monitor.name} now`} onClick={() => onCheckNow(monitor.id)}>↻</button>
                    <span className={`status-pill status-${monitor.status.toLowerCase()}`}>{monitor.status}</span>
                    <span>{monitor.lastResponseTimeMs === null ? '-' : `${monitor.lastResponseTimeMs}ms`}</span>
                    <button
                        className="icon-button"
                        aria-label={`${isExpanded ? 'Hide' : 'Show'} ${monitor.name} check history`}
                        onClick={onToggleChecks}
                    >
                        {isExpanded ? '⌃' : '⌄'}
                    </button>
                </div>
            </div>
            {isExpanded && (
                <div className="check-history">
                    <h4>Recent checks</h4>

                    {isChecksLoading && <p>Loading checks...</p>}

                    {checksErrorMessage && <p className="error-message">{checksErrorMessage}</p>}

                    {!isChecksLoading && !checksErrorMessage && checks.length === 0 && (
                        <p>No checks recorded yet.</p>
                    )}

                    {!isChecksLoading && !checksErrorMessage && checks.length > 0 && (
                        <ul>
                            {checks.map((check) => (
                                <li key={check.id}>
                                    <span className={`status-pill status-${check.status.toLowerCase()}`}>
                                        {check.status}
                                    </span>
                                    <span>{check.statusCode ?? '-'}</span>
                                    <span>{check.responseTimeMs === null ? '-' : `${check.responseTimeMs}ms`}</span>
                                    <span>{new Date(check.checkedAt).toLocaleString()}</span>
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