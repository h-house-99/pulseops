type Monitor = {
    id: number
    name: string
    url: string
    status: 'UNKNOWN' | 'UP' | 'DOWN'
    lastStatusCode: number | null
    lastResponseTimeMs: number | null
    lastCheckedAt: string | null
    totalChecks: number
    uptimePercentage: number | null
    averageResponseTimeMs: number | null
    fastestResponseTimeMs: number | null
    slowestResponseTimeMs: number | null
    latestErrorMessage: string | null
    latestFailureReason: string | null
    lastFailureAt: string | null
}

type CheckResult = {
    id: number
    monitorId: number
    status: 'UP' | 'DOWN' | 'UNKNOWN'
    statusCode: number | null
    responseTimeMs: number | null
    checkedAt: string
    errorMessage: string | null
}

export type { Monitor, CheckResult }