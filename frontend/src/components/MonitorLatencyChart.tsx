import type { CheckResult } from '../types'

type MonitorLatencyChartProps = {
    checks: CheckResult[]
}


function MonitorLatencyChart({ checks }: MonitorLatencyChartProps) {
    if (checks.length === 0) {
        return <p>No chart data yet.</p>
    }
    const width = 640
    const height = 160
    const padding = 16
    const chartWidth = width - padding * 2
    const chartHeight = height - padding * 2

    const responseTimes = checks
        .map((check) => check.responseTimeMs)
        .filter((responseTime) => responseTime !== null)

    const sortedResponseTimes = responseTimes.sort((a, b) => a - b)
    const percentile95Index = Math.floor((sortedResponseTimes.length - 1) * 0.95)
    const visual95thPercentileResponseTime = Math.max(sortedResponseTimes[percentile95Index] ?? 1, 1)

    const timeWindowHours = 24
    const endTime = new Date(checks[checks.length - 1].checkedAt).getTime()
    const startTime = endTime - timeWindowHours * 60 * 60 * 1000
    const timeWindowMs = endTime - startTime

    const points = checks.map((check) => {
        const checkedAt = new Date(check.checkedAt).getTime()
        const x = padding + ((checkedAt - startTime) / timeWindowMs) * chartWidth
        const responseTime = check.responseTimeMs ?? 0
        const cappedResponseTime = Math.min(responseTime, visual95thPercentileResponseTime)
        const y = padding + chartHeight - (cappedResponseTime / visual95thPercentileResponseTime) * chartHeight
        return { x, y, status: check.status }
    })

    const linePoints = points.map((point) => `${point.x},${point.y}`).join(' ')

    return (
        <div className="monitor-latency-chart">
            <svg viewBox="0 0 640 160" role="img" aria-label="Latency trend over the selected time window">
                <rect
                    x="0"
                    y="0"
                    width="640"
                    height="160"
                    rx="8"
                    fill="#212223"
                    stroke="#3c3d3d"
                />
                <polyline
                    points={linePoints}
                    fill="none"
                    stroke="#3673f8"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />
                {points.map((point, index) => (
                    <circle
                        key={checks[index].id}
                        cx={point.x}
                        cy={point.y}
                        r="2"
                        fill={point.status === 'UP' ? '#85bb98' : '#fca5a5'}
                    />
                ))}
            </svg>
            <div className="monitor-latency-chart-axis">
                <span>24 hours ago</span>
                <span>Latest check</span>
            </div>
        </div>
    )
}

export default MonitorLatencyChart