import type { CheckResult } from '../types'
import { useState } from 'react'

type MonitorLatencyChartProps = {
    checks: CheckResult[]
    timeWindowHours: number
    chartFetchedAt: number
    isLoading: boolean
}

type ChartPoint = {
    x: number
    y: number
    checkResult: CheckResult
}

function formatCheckedAt(checkedAt: string) {
    return new Date(checkedAt).toLocaleString([], {
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
    })
}

function MonitorLatencyChart({ checks, timeWindowHours, chartFetchedAt, isLoading }: MonitorLatencyChartProps) {
    const [activeChartPoint, setActiveChartPoint] = useState<ChartPoint | null>(null)

    if (isLoading) {
        return (
            <div className="monitor-latency-chart">
                <div className="monitor-latency-chart-state">Loading chart...</div>
            </div>
        )
    }
    if (checks.length === 0) {
        return (
            <div className="monitor-latency-chart">
                <div className="monitor-latency-chart-state">No checks in this window yet.</div>
            </div>
        )
    }
    const width = 640
    const height = 160
    const padding = {
        top: 16,
        right: 16,
        bottom: 16,
        left: 48,
    }
    const chartWidth = width - padding.left - padding.right
    const chartHeight = height - padding.top - padding.bottom

    const responseTimes = checks
        .map((check) => check.responseTimeMs)
        .filter((responseTime) => responseTime !== null)

    const sortedResponseTimes = responseTimes.sort((a, b) => a - b)
    const percentile95Index = Math.floor((sortedResponseTimes.length - 1) * 0.95)
    const visual95thPercentileResponseTime = Math.max(sortedResponseTimes[percentile95Index] ?? 1, 1)
    function getYForResponseTime(responseTime: number) {
        return padding.top + chartHeight - (responseTime / visual95thPercentileResponseTime) * chartHeight
    }
    const yAxisTicks = Array.from(new Set([0, Math.round(visual95thPercentileResponseTime / 2), visual95thPercentileResponseTime]))


    const endTime = chartFetchedAt
    const startTime = endTime - timeWindowHours * 60 * 60 * 1000
    const timeWindowMs = endTime - startTime

    const points: ChartPoint[] = checks.map((check) => {
        const checkedAt = new Date(check.checkedAt).getTime()
        const x = padding.left + ((checkedAt - startTime) / timeWindowMs) * chartWidth
        const responseTime = check.responseTimeMs ?? 0
        const cappedResponseTime = Math.min(responseTime, visual95thPercentileResponseTime)
        const y = padding.top + chartHeight - (cappedResponseTime / visual95thPercentileResponseTime) * chartHeight
        return { x, y, checkResult: check }
    })

    const linePoints = points.map((point) => `${point.x},${point.y}`).join(' ')

    return (
        <div className="monitor-latency-chart">
            <div className="monitor-latency-chart-container">
                <svg viewBox="0 0 640 160" preserveAspectRatio="none" role="img" aria-label="Latency trend over the selected time window">
                    <rect
                        x="0"
                        y="0"
                        width="640"
                        height="160"
                        rx="8"
                        fill="#212223"
                        stroke="#3c3d3d"
                    />
                    {yAxisTicks.map((tick) => {
                        const y = getYForResponseTime(tick)

                        return (
                            <g key={tick}>
                                <line
                                    x1={padding.left}
                                    x2={width - padding.right}
                                    y1={y}
                                    y2={y}
                                    stroke="#3c3d3d"
                                    strokeWidth="1"
                                    strokeDasharray={tick === 0 ? undefined : '4 4'}
                                />
                                <text
                                    x={padding.left - 8}
                                    y={y}
                                    fill="#7a8293"
                                    fontSize="8"
                                    textAnchor="end"
                                    dominantBaseline="middle"
                                >
                                    {tick}ms
                                </text>
                            </g>
                        )
                    })}
                    <polyline
                        points={linePoints}
                        fill="none"
                        stroke="#3673f8"
                        strokeWidth="1.5"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                    {points.map((point, index) => (
                        <g key={checks[index].id}>
                            <circle
                                cx={point.x}
                                cy={point.y}
                                r="2"
                                fill={point.checkResult.status === 'UP' ? '#85bb98' : '#fca5a5'}
                            />
                            <circle
                                className="monitor-latency-chart-hit-target"
                                cx={point.x}
                                cy={point.y}
                                r="8"
                                fill="transparent"
                                tabIndex={0}
                                aria-label={`${point.checkResult.status} check at ${formatCheckedAt(point.checkResult.checkedAt)}`}
                                onMouseEnter={() => setActiveChartPoint(point)}
                                onMouseLeave={() => setActiveChartPoint(null)}
                                onFocus={() => setActiveChartPoint(point)}
                                onBlur={() => setActiveChartPoint(null)}
                            />
                        </g>

                    ))}
                </svg>
                {activeChartPoint && (
                    <div
                        className="monitor-latency-chart-tooltip"
                        style={{
                            left: `${(activeChartPoint.x / width) * 100}%`,
                            top: `${(activeChartPoint.y / height) * 100}%`,
                        }}
                    >
                        <strong>{activeChartPoint.checkResult.responseTimeMs ?? '-'}ms</strong>
                        <span>{activeChartPoint.checkResult.status}</span>
                        <span>Code {activeChartPoint.checkResult.statusCode ?? '-'}</span>
                        <span>{formatCheckedAt(activeChartPoint.checkResult.checkedAt)}</span>
                    </div>
                )}
            </div>
            <div className="monitor-latency-chart-axis">
                <span>{timeWindowHours}h ago</span>
                <span>Now</span>
            </div>
        </div>
    )
}

export default MonitorLatencyChart