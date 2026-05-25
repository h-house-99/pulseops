type Monitor = {
    id: number
    name: string
    url: string
    status: 'UNKNOWN' | 'UP' | 'DOWN'
    lastStatusCode: number | null
    lastResponseTimeMs: number | null
    lastCheckedAt: string | null
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